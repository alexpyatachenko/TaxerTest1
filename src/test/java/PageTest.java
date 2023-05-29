import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.File;
import java.time.Duration;
import java.util.List;

public class PageTest {
    public static void DropFile(File filePath, WebElement target, int offsetX, int offsetY) {
        if(!filePath.exists())
            throw new WebDriverException("File not found: " + filePath.toString());

        WebDriver driver = ((RemoteWebElement)target).getWrappedDriver();
        JavascriptExecutor jse = (JavascriptExecutor)driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        String JS_DROP_FILE =
                "var target = arguments[0]," +
                        "    offsetX = arguments[1]," +
                        "    offsetY = arguments[2]," +
                        "    document = target.ownerDocument || document," +
                        "    window = document.defaultView || window;" +
                        "" +
                        "var input = document.createElement('INPUT');" +
                        "input.type = 'file';" +
                        "input.style.display = 'none';" +
                        "input.onchange = function () {" +
                        "  var rect = target.getBoundingClientRect()," +
                        "      x = rect.left + (offsetX || (rect.width >> 1))," +
                        "      y = rect.top + (offsetY || (rect.height >> 1))," +
                        "      dataTransfer = { files: this.files };" +
                        "" +
                        "  ['dragenter', 'dragover', 'drop'].forEach(function (name) {" +
                        "    var evt = document.createEvent('MouseEvent');" +
                        "    evt.initMouseEvent(name, !0, !0, window, 0, 0, 0, x, y, !1, !1, !1, !1, 0, null);" +
                        "    evt.dataTransfer = dataTransfer;" +
                        "    target.dispatchEvent(evt);" +
                        "  });" +
                        "" +
                        "  setTimeout(function () { document.body.removeChild(input); }, 25);" +
                        "};" +
                        "document.body.appendChild(input);" +
                        "return input;";

        WebElement input =  (WebElement)jse.executeScript(JS_DROP_FILE, target, offsetX, offsetY);
        input.sendKeys(filePath.getAbsoluteFile().toString());
        wait.until(ExpectedConditions.stalenessOf(input));
    }
    @Test
    public void certificateTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);
        SoftAssert softAssert = new SoftAssert();

        String pathFile = "C:\\Users\\Александр\\Desktop\\Серты\\cert.cer";

        driver.get("https://js-55fbfg.stackblitz.io/");

        WebElement runProjectButton = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text() = 'Run this project']")));
        runProjectButton.click();

        WebElement addButton = new WebDriverWait(driver, Duration.ofSeconds(2))
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@class = 'btn btn-primary']")));
        addButton.click();

        WebElement droparea = new WebDriverWait(driver, Duration.ofSeconds(1))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//dropbox")));

        Point location = droparea.getLocation();

        DropFile(new File(pathFile), droparea, location.getX(), location.getY());

        String subjectCN = driver.findElement(By.xpath("//th[text() = 'SubjectCN:']/following-sibling::td")).getText();
        String issuerCN = driver.findElement(By.xpath("//th[text() = 'IssuerCN:']/following-sibling::td")).getText();
        String validFrom = driver.findElement(By.xpath("//th[text() = 'ValidFrom:']/following-sibling::td")).getText();
        String validTill = driver.findElement(By.xpath("//th[text() = 'ValidTill:']/following-sibling::td")).getText();

        driver.get("https://lapo.it/asn1js/");

        WebElement uploadButton = new WebDriverWait(driver, Duration.ofSeconds(2))
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id = 'file']")));
        uploadButton.sendKeys(pathFile);

        String actualIssuerName = driver.findElement(By.xpath(
                "//span[text() = 'issuer']/../..//span[text() = 'commonName']/../../.././following-sibling::div//span[@class = 'preview']")).getText();
        String actualSubjectCN = driver.findElement(By.xpath(
                "//span[text() = 'subject']/../..//span[text() = 'commonName']/../../.././following-sibling::div//span[@class = 'preview']")).getText();

        List <WebElement> actualDates = driver.findElements(By.xpath("//div[text() = 'GeneralizedTime']/span[@class = 'preview']"));

        String actualValidFrom = actualDates.get(0).getText();
        String actualValidTill = actualDates.get(1).getText();

        softAssert.assertEquals(subjectCN, actualSubjectCN);
        softAssert.assertEquals(issuerCN, actualIssuerName);
        softAssert.assertEquals(validFrom, actualValidFrom);
        softAssert.assertEquals(validTill, actualValidTill);

        softAssert.assertAll();

        driver.close();
        driver.quit();
    }
}
