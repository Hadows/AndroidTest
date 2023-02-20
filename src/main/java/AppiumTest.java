import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import java.net.MalformedURLException;
import java.net.URL;

import io.appium.java_client.remote.MobileCapabilityType;

public class AppiumTest {
    public static void main(String[] args) throws MalformedURLException {
        // Impostare le capacità desiderate per l'emulatore o il dispositivo fisico
        DesiredCapabilities dc = new DesiredCapabilities();

        dc.setCapability(MobileCapabilityType.AUTOMATION_NAME, "Appium");
        dc.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
        dc.setCapability(MobileCapabilityType.PLATFORM_VERSION, "8.1");
        dc.setCapability(MobileCapabilityType.DEVICE_NAME, "Android Emulator");
        dc.setCapability("androidInstallTimeout", 120000);
        dc.setCapability(MobileCapabilityType.APP, "./sample_apk_debug.apk");

        URL url = new URL("http://127.0.0.1:4444");
        AndroidDriver driver = new AndroidDriver(url,dc);
    }
}
