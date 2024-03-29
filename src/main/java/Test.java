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


        // Creare un'istanza di WebDriver per Android
        WebDriver driver = new RemoteWebDriver(
                new URL("http://posteitaliane.selenium.grid"), capabilities);


        int i = 0;
        while (i < 25){
            driver.get("https://www.google.com/");
            i++;
        }
        driver.quit();

    }
}
