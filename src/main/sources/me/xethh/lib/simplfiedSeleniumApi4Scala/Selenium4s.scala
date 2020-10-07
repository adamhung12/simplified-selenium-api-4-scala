package me.xethh.lib.simplfiedSeleniumApi4Scala

import java.io.File

import me.xethh.utils.functionalPacks.Scope
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.{By, JavascriptExecutor, WebDriver, WebElement}

import scala.jdk.CollectionConverters._
//import scala.collection.JavaConverters._


object Selenium4s {

  trait Scoped

  class StringExtension(byStr: String) extends Scoped {
    def css(): By = By.cssSelector(byStr)
    def xpath(): By = By.xpath(byStr)
  }

  class ActionsExtension(actions: Actions) extends Scoped {
  }

  class DriverExtension(webDriver: WebDriver) extends Scoped{
    def actions(implicit webDriverWait: WebDriverWait): Actions = {
      new Actions(webDriver)
    }

    def waitFor(time:Long): WebDriverWait ={
      new WebDriverWait(webDriver, time)
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

  class WebDriverWaitExtension(webDriverWait: WebDriverWait) extends Scoped{
  }

  object impl {
    implicit def toFunctionScope[T <: Scoped](t : T): Scope[T] = Scope.of(t)
    implicit def toFunctionScope(t : WebElement): Scope[WebElement] = Scope.of(t)
    implicit def toFunctionScope(t : WebDriver): Scope[WebDriver] = Scope.of(t)

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
    if (!location.isFile()) {
      throw new RuntimeException(s"Chrome driver[${location.toString}] not a file")
    }
    System.setProperty("webdriver.chrome.driver", location.toString)
  }

  def setChromeDriveLocation(location: String): Unit = setChromeDriveLocation(new File(location))

  def chromeDriver(): ChromeDriver = {
    val driver = new ChromeDriver()
    driver.manage().getCookies.asScala.foreach(it => println(it))
    driver
  }
}
