import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

public class PosteTest {
    public static void main(String[] args) throws MalformedURLException {
        // Impostare le capacità desiderate per l'emulatore o il dispositivo fisico
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("androidInstallTimeout", 200000);
        capabilities.setCapability("uiautomator2ServerInstallTimeout", 100000);
        capabilities.setCapability(MobileCapabilityType.APP, "http://192.168.48.57/in/PostepayV4_11.280.2-COLL.apk");

        URL url = new URL("http://127.0.0.1:4444");
        AndroidDriver driver = new AndroidDriver(url,capabilities);
        // Chiudere il driver
        //driver.quit();
    }
}
