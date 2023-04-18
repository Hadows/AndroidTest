import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.kubernetes.client.Copy;
import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.exception.CopyNotSupportedException;
import org.apache.commons.cli.ParseException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

//curl -X DELETE http://posteitaliane.192.168.186.209.nip.io/wd/hub/session/14fa535e10b5c02b00116ef2766ae29b

/* 
 * General flow: 
 *  1. start test with remote web driver and get sessionID
 *  2. thru sessionID you can get the ip node where the test case is currently executed
 *  3. With node IP using kubernetes api you can get the browser node pod name
 *  4. With pod name we know that the sidecar container named "node-chrome-video" can record videos running the script /opt/bin/video.sh
 *  5. Using kubernetes api run an exec (eq. kubectl exec command) for running video.sh script, it must be executed in a thread since it is a background registration
 *  6. continue.. your test
 *  7. Stop video using kubernetes api run an exec (eq. kubectl exec command) for kill the ffmpeg process , this will trigger the video saving performed by video.sh script
 *  8. After stop using kubernetes api we can copy locally the video results
 * 
 * 
 *  node-chrome-video is a sidecar that usually start the video recording at startup and save the video when container stop
 *  to skip this capability just override the entrypoint as follow:
 * 
 *      
 *  - name: {{ .Values.chrome.video.name }}
 *      image: {{ .Values.chrome.video.image }}
 *      command: [ "/bin/bash", "-c", "--" ]
 *      args: [ "while true; do sleep 30; done;" ]       
*/


public class VideoRecorderBrowser {

    static RemoteWebDriver driver;
    static private String namespace;
    static private String browserPodName;


  /*
     Get Pod name using the ip returned by remotewebdriver session
  */  
  private static String getPodByIP(String podIP) throws IOException{ 
    
        // Create an ApiClient instance
        try {
          ApiClient apiClient = Config.defaultClient();
          Configuration.setDefaultApiClient(apiClient);

          // Create a CoreV1Api instance
          CoreV1Api coreV1Api = new CoreV1Api(apiClient);

          // Get the Pod by IP address
          V1Pod pod = coreV1Api.listPodForAllNamespaces(false, null, null, null, null, null, null, null, null,false)
                  .getItems().stream()
                  .filter(p -> p.getStatus().getPodIP().equals(podIP))
                  .findFirst()
                  .orElse(null);

          // Check if the Pod was found
          if (pod != null) {
              String podName = pod.getMetadata().getName();              
              browserPodName=podName;
              namespace=pod.getMetadata().getNamespace();
              System.out.println("Pod name for IP address " + podIP + ": " + podName + "in namespace:" + namespace);
              return podName;
          } else {
              System.out.println("Pod not found for IP address " + podIP);
              return null;
          }
      } catch (ApiException e) {
          e.printStackTrace();
          return null;
      }    
  }


  /*
     Get node IP where the automation will be executed
  */  
  private static String getIPFromSession(String seleniumHost, String sessionId ) throws IOException {

    URL url = new URL(seleniumHost + "/" + "graphql");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");

    String query = "{ \"query\": \"{ session (id: \\\""+sessionId+"\\\") { uri, nodeId, nodeUri, sessionDurationMillis, slot { id, stereotype, lastStarted } } } \"}";
    
    OutputStream os = conn.getOutputStream();
    os.write(query.getBytes());
    os.flush();
    
    BufferedReader br = new BufferedReader(new InputStreamReader(
      (conn.getInputStream())));
    
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      sb.append(line);
      sb.append(System.getProperty("line.separator"));
    }
    
    String result = sb.toString();
    
    System.out.println("API Response .... \n" + result);
    
    conn.disconnect();

    String jsonString = sb.toString(); 
    Gson gson = new Gson();
    JsonObject jsonObject = gson.fromJson(jsonString , JsonObject.class);
    String values=jsonObject.get("data").getAsJsonObject().get("session").getAsJsonObject().get("uri").getAsString();    
    System.out.println("URI .... \n" + values);
    conn.disconnect();
    return values;
  }

  /*
     copy video from remote container locally
  */ 
  public static void getVideo(String namespace , String podName , String containerName ) throws IOException, ApiException, InterruptedException {
    ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);

    Copy copy = new Copy();

    try {
      copy.copyDirectoryFromPod(namespace, podName, containerName, "/videos", Paths.get("results"));
    } catch (CopyNotSupportedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("Done!");
  }


  /*
     Generic method for running a command inside a container in a pod
  */   
  public static void kubeExecCommand(String namespace , String pod, String container, String[] command)
      throws IOException,  InterruptedException, ParseException, ApiException {

    ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);
    Exec exec = new Exec();
    final Process proc = exec.exec(namespace, pod, command, container , true, true);
    Thread in = new Thread(new Runnable() {
      public void run() {
          try {
              ByteStreams.copy(System.in, proc.getOutputStream());
          } catch (IOException ex) {
              ex.printStackTrace();
          }
      }
    });
    in.start();

    Thread out = new Thread(new Runnable() {
        public void run() {
            try {
                ByteStreams.copy(proc.getInputStream(), System.out);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    });
    out.start();
    proc.waitFor();

    // wait for any last output; no need to wait for input thread
    out.join();
        
    proc.destroy();
  }


  /*
     Start recording in a thread in background: we are using the videos.sh scripts implemented by selenium video node support.
  */ 
 public static void startRecording(String seleniumGrid, String sessionID) throws URISyntaxException, IOException{

  URI uri = new URI(getIPFromSession(seleniumGrid , sessionID));    
  getPodByIP(uri.getHost());

  Thread register =
  new Thread(
      new Runnable() {
        public void run() {
          try {
            try {
              kubeExecCommand(namespace,browserPodName, "node-video", new String[]
              {"sh", "-c", "/opt/bin/video.sh", "&"});
            } catch (InterruptedException e) {
              e.printStackTrace();
            } catch (ParseException e) {
              e.printStackTrace();
            } catch (ApiException e) {
              e.printStackTrace();
            }
       
          } catch (IOException ex) {
            ex.printStackTrace();
          }
        }
      });
  register.start();
 }


  /*
     Stop recording killing: just kill the ffmpeg process the video will be automatically saved  
  */ 
public static void stopRecording() throws IOException, InterruptedException, ParseException, ApiException {
  kubeExecCommand(namespace,browserPodName, "node-video", new String[]
    {"sh", "-c", "pkill ffmpeg"});
  System.out.println("Video Stopped.... Getting videos...");
  getVideo(namespace, browserPodName, "node-video" );
  System.out.println("Video Saved");  
}

public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException, ParseException, ApiException {
    ChromeOptions dc = new ChromeOptions();
    dc.setCapability("platformName", "linux");
    dc.setCapability("se:recordVideo", "true");
    dc.setCapability("se:screenResolution", "1920x1080");

    // New Remote Web driver
    RemoteWebDriver driver = new RemoteWebDriver(new URL("http://posteitaliane.selenium.grid"),dc);
    //try to navigate
    driver.get("http://www.google.com");

    //start recording using a specific session id
    startRecording("http://posteitaliane.selenium.grid", driver.getSessionId().toString());

    //sleep just for generating a couple of second video
    Thread.sleep(5000);
       
    System.out.println(driver.getTitle());

    // Stop recording and copying locally the video results.
    stopRecording();    

    // quit the driver
    driver.quit();

    //force all threads to be terminated  
    System.exit(0);
     }
}