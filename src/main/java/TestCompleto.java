import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;

public class TestCompleto {
    public static void main(String[] args) throws MalformedURLException {
        // Impostare le capacità desiderate per l'emulatore o il dispositivo fisico
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("browserName", "chrome");
        capabilities.setCapability("automationName", "UIAutomator2");
        //capabilities.setCapability("se:recordVideo", "true");
        //capabilities.setCapability("se:screenResolution", "1920x1080");

        URL url = new URL("http://localhost:4444/");
        AndroidDriver driver = new AndroidDriver(url,capabilities);

        int i = 0;
        while (i < 25){
            driver.get("https://www.google.com/");
            i++;
        }
        driver.quit();
    }
}
