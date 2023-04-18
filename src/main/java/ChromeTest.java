import com.google.common.annotations.VisibleForTesting;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

public class ChromeTest {
    public static void main(String[] args) throws MalformedURLException {
        ChromeOptions capabilities = new ChromeOptions();
        capabilities.setCapability("platformName", "Linux");

        // Creare un'istanza di WebDriver per Android
        WebDriver driver = new RemoteWebDriver(
                new URL("http://posteitaliane.selenium.grid"), capabilities);

        int i = 0;
        while (i < 50){
            driver.get("https://www.google.com/");
            i++;
        }
        driver.quit();
    }
}
