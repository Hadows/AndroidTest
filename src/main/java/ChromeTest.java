import com.google.common.annotations.VisibleForTesting;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

public class ChromeTest {


    public ChromeTest(){

    }
    public void test() throws MalformedURLException {
        ChromeOptions capabilities = new ChromeOptions();
        capabilities.setCapability("platformName", "Linux");

        // Creare un'istanza di WebDriver per Android
        WebDriver driver = new RemoteWebDriver(
                new URL("http://127.0.0.1:4444"), capabilities);

        int i = 0;
        while (i < 100){
            driver.get("https://www.google.com/");
            i++;
        }
        driver.quit();
    }
}
