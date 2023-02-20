import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
public class Test {
    public static void main(String[] args) throws MalformedURLException {
        // Impostare le capacità desiderate per l'emulatore o il dispositivo fisico
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("browserName", "chrome");
        capabilities.setCapability("se:recordVideo", "true");
        capabilities.setCapability("se:screenResolution", "1920x1080");


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
