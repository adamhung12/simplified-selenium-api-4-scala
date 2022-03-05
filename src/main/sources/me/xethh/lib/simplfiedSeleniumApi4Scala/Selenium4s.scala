package me.xethh.lib.simplfiedSeleniumApi4Scala

import me.xethh.utils.functionalPacks.Scope
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxOptions}
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.remote._
import org.openqa.selenium.remote.codec.w3c.{W3CHttpCommandCodec, W3CHttpResponseCodec}
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.{By, JavascriptExecutor, WebDriver, WebElement}

import java.io.File
import java.net.URL
import java.time.Duration
import java.util
import java.util.function.Supplier
import java.util.regex.Pattern
import scala.jdk.CollectionConverters._
import scala.language.implicitConversions
import scala.util.{Random, Try}


object Selenium4s {

  trait Scoped

  class StringExtension(byStr: String) extends Scoped {
    def css(): By = By.cssSelector(byStr)

    def xpath(): By = By.xpath(byStr)
  }

  class ActionsExtension(actions: Actions) extends Scoped {
  }

  class DriverExtension(webDriver: WebDriver) extends Scoped {
    def actions(implicit webDriverWait: WebDriverWait): Actions = {
      new Actions(webDriver)
    }

    def waitFor(time: Long): WebDriverWait = {
      val duration = Duration.ofMillis(time)
      new WebDriverWait(webDriver, duration)
    }

    // Scroll to the bottom of page
    def scrollToBottom(): Unit = {
      webDriver.asInstanceOf[JavascriptExecutor].executeScript("window.scrollTo(0, document.body.scrollHeight)")
    }

    def exponentialBackoff(): Supplier[Long] = {
      new Supplier[Long] {
        var num = 0
        override def get(): Long = {
          num +=1
          num match {
            case 1 => 1000
            case x if x >= 10 => 100 * 1000 + Random.nextLong(500)
            case x if x > 1 => x * x * 1000 + Random.nextLong(500)
          }
        }
      }
    }

    def waitForUrlAs(url: String): WebDriver = {
      var notChanged = true
      val backoff = exponentialBackoff()
      while (notChanged) {
        val curUrl = this.webDriver.getCurrentUrl
        println(s"Url: [${url}] vs [$curUrl]")
        val pattern = Pattern.compile(url)
        if (pattern.matcher(curUrl).matches()) {
          notChanged = false
          println("Matched")
        }

        var next = backoff.get()
        println(s"Sleep for ${next}")
        Thread.sleep(next)
      }

      webDriver
    }

  }

  class ElementExtension(webElement: WebElement) extends Scoped{

    def scrollToMe(implicit webDriver: WebDriver): WebElement ={
      webDriver.asInstanceOf[JavascriptExecutor].executeScript("arguments[0].scrollIntoView()",webElement)
      webElement
    }

    def find(by: By): Option[WebElement] = {
      try {
        Some(webElement.findElement(by))
      }
      catch {
        case _: Throwable => None
      }
    }

    def findAll(by: By): Option[Array[WebElement]] = {
      try {
        Some(webElement.findElements(by).asScala.toArray)
      }
      catch {
        case _: Throwable => None
      }
    }
  }

  class ByExtension(by: By) extends Scoped{

    def waitForSingle(implicit webDriverWait: WebDriverWait): Option[WebElement] = {
      try {
        Some(webDriverWait.until(ExpectedConditions.numberOfElementsToBe(by, 1)).asScala.head)
      }
      catch {
        case _: Throwable => None
      }
    }

    def waitUntilCountLessThan(num: Int)(implicit webDriverWait: WebDriverWait): Array[WebElement] = {
      webDriverWait.until(ExpectedConditions.numberOfElementsToBeLessThan(by, num)).asScala.toArray
    }

    def waitUntilCountLargerThan(num: Int)(implicit webDriverWait: WebDriverWait): Array[WebElement] = {
      webDriverWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(by, num)).asScala.toArray
    }

    def waitUntilCountTo(num: Int)(implicit webDriverWait: WebDriverWait): Array[WebElement] = {
      webDriverWait.until(ExpectedConditions.numberOfElementsToBe(by, num)).asScala.toArray
    }

    def waitForAll(implicit webDriverWait: WebDriverWait): Array[WebElement] = {
      webDriverWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(by, 0)).asScala.toArray
    }
  }

  class WebDriverWaitExtension(webDriverWait: WebDriverWait) extends Scoped {
  }

  class ChromeOptionExtension(chromeOptions: ChromeOptions) extends Scoped {
    def defaultHeadlessMode(userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.99 Safari/537.36"): ChromeOptions = {
      chromeOptions
        .addArguments("--headless")
        .addArguments("--no-sandbox")
        .addArguments("window-size=1920,1080")
        .addArguments("--disable-dev-shm-usage")
        .addArguments(s"user-agent=${userAgent}")
    }
  }

  object impl {
    implicit def toFunctionScope[T <: Scoped](t: T): Scope[T] = Scope.of(t)

    implicit def toFunctionScope(t: WebElement): Scope[WebElement] = Scope.of(t)

    implicit def toFunctionScope(t: WebDriver): Scope[WebDriver] = Scope.of(t)

    implicit def toStringExtension(str: String): StringExtension = new StringExtension(str)

    implicit def toByExtension(by: By): ByExtension = new ByExtension(by)

    implicit def toBy(css: String): ByExtension = toByExtension(By.cssSelector(css))

    implicit def toDriverExtension(webDriver: WebDriver): DriverExtension = new DriverExtension(webDriver)

    implicit def toElementExtension(element: WebElement): ElementExtension = new ElementExtension(element)

    implicit def toActionsExtension(actions: Actions): ActionsExtension = new ActionsExtension(actions)
  }

  def setChromeDriveLocation(location: File): Unit = {
    if (!location.exists()) {
      throw new RuntimeException(s"Chrome driver not exists in path[${location.toString}]")
    }
    if (!location.isFile) {
      throw new RuntimeException(s"Chrome driver[${location.toString}] not a file")
    }
    System.setProperty("webdriver.chrome.driver", location.toString)
  }

  def setChromeDriveLocation(location: String): Unit = setChromeDriveLocation(new File(location))

  def firefoxDriver(): FirefoxDriver = {
    val driver = new FirefoxDriver()
    driver.manage().getCookies.asScala.foreach(it => println(it))
    driver
  }

  def firefoxDriverWithOption(optionSetup: FirefoxOptions => Unit = _ => {}): FirefoxDriver = {
    val opt = new FirefoxOptions()
    optionSetup(opt)
    val driver = new FirefoxDriver(opt)
    driver.manage().getCookies.asScala.foreach(it => println(it))
    driver
  }

  def chromeDriverWithOption(optionSetup: ChromeOptions => Unit = _ => {}): ChromeDriver = {
    val opt = new ChromeOptions()
    optionSetup(opt)
    val driver = new ChromeDriver(opt)
    driver.manage().getCookies.asScala.foreach(it => println(it))
    driver
  }

  type DriverURL = String
  type SessionId = String


  def remoteDriver(url: DriverURL, browserType: Browser, sessionId: Option[SessionId] = None, desiredCapabilitiesOperation: DesiredCapabilities => Unit = _ => {}): RemoteWebDriver = {
    val cap = new DesiredCapabilities()
    cap.setBrowserName(browserType.browserName())
    desiredCapabilitiesOperation(cap)
    val driver = sessionId match {
      case Some(sessionId) =>
        val driver = new RemoteWebDriver(createDriverFromSession(sessionId, new URL(url)), cap);
        driver.manage().getCookies.asScala.foreach(it => println(it))
        driver
      case None =>
        val driver = new RemoteWebDriver(new URL(url), cap);
        driver.manage().getCookies.asScala.foreach(it => println(it))
        driver
    }
    driver
  }

  def createDriverFromSession(sessionId: String, url: URL): HttpCommandExecutor = {
    val executor = new HttpCommandExecutor(url) {
      override def execute(command: Command): Response = {
        var response: Response = null
        if ("newSession".equals(command.getName)) {
          response = new Response()
          response.setSessionId(sessionId)
          response.setStatus(0)
          response.setValue(new util.HashMap())

          Try {
            val codec = this.getClass.getSuperclass.getDeclaredField("commandCodec")
            codec.setAccessible(true)
            codec.set(this, new W3CHttpCommandCodec())

            val responseCodec = this.getClass.getSuperclass.getDeclaredField("responseCodec")
            responseCodec.setAccessible(true)
            responseCodec.set(this, new W3CHttpResponseCodec())
          }
            .get
          response
        } else {
          response = super.execute(command)
          response
        }
      }
    }
    executor
  }
}
