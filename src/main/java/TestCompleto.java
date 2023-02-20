import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
public class TestCompleto {
    public static void main(String[] args) throws MalformedURLException {
        // Impostare le capacità desiderate per l'emulatore o il dispositivo fisico
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability(MobileCapabilityType.APP, "https://github.com/Hadows/TestGit/releases/download/0.1/sample_apk_debug.apk");

        URL url = new URL("http://127.0.0.1:4444");
        AndroidDriver driver = new AndroidDriver(url,capabilities);



        // Chiudere il driver
        driver.quit();
    }
}