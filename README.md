## Simplified selenium API for scala

### Maven

#### scala 2.13
```xml
<dependency>
  <groupId>me.xethh.libs</groupId>
  <artifactId>simplified-selenium-api-4-scala</artifactId>
  <version>1.0.0</version>
</dependency>
```
#### scala 2.12
```xml
<dependency>
  <groupId>me.xethh.libs</groupId>
  <artifactId>simplified-selenium-api-4-scala_12</artifactId>
  <version>1.0.0</version>
</dependency>
```

---------------------------
### Example

#### Package to be import
```scala
import me.xethh.lib.simplfiedSeleniumApi4Scala.Selenium4s
```

#### Code Init
```scala
    Selenium4s.setChromeDriveLocation("/path/to/chromedriver.exe")
    
    //Import implicit conversion coding
    import Selenium4s.impl._
    
    //Set web driver and web driver wait to be implicit object
    implicit val driver: ChromeDriver = Selenium4s.chromeDriver()
    implicit val waitFor: WebDriverWait = driver.waitFor(10)    
```

#### Coding

-----------------
##### Create By object
```scala
        val by = "input#pwd".css()
        val by2 = "[xpath]".xpath()
```
Code in original selenium java api 
```java
        By by = By.cssSelector("input#pwd");
        By by2 = By.xpath("[xpath]");
```
-----------------
##### Obtain input web element and send text to input
Load css selector of "input#pwd" element and send text to the input
```scala
        "input#pwd".waitForSingle.get.sendKeys("password")
```

Code in original selenium java api 
```java
        By by = By.cssSelector("input#pwd");
        WebElement element = wait.until(ExpectedConditions.numberOfElementsToBe(by, 1)).get(0);
        element.sendKeys("password");
```
-----------------
##### Scroll to element and click
```scala
        "input#pwd".waitForSingle.get.let{it=>
          it.scrollToMe
          driver.actions.moveToElement(it).click(it).build().perform();
        }
```
Code in original selenium java api 
```java
        By by = By.cssSelector("input#pwd");
        WebElement element = wait.until(ExpectedConditions.numberOfElementsToBe(by, 1)).get(0);
        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView()",element)
        element.sendKeys("password");
        new Actions(driver)..moveToElement(element).click(element).build().perform()
```

