package com.org.practicum.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

public class FireFoxTest {

	static WebDriver driver;

	  static String appPath;

	  @BeforeClass
	  public static void setUpOnce() throws Exception {
		System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
		driver = new ChromeDriver();
	    //driver = new FirefoxDriver(new FirefoxProfile());
	    appPath = "http://localhost:9090/WebxenBNY/app";
	  }

	  @Before
	  public void setUp() throws Exception {
	  }

	  @After
	  public void tearDown() throws Exception {
	  }

	  @AfterClass
	  public static void tearDownOnce() throws Exception {
	    driver.quit();
	  }

	  @Test
	  public void testProductSearchFormDisplayUseCase1() {

	    driver.get(appPath + "/home.html");
	    WebElement sideForm = driver.findElement(By.id("sideForm"));
		//WebElement name = sideForm.findElement(By.id("elementType"));
		Select dropdown = new Select(driver.findElement(By.id("elementType")));
		dropdown.selectByVisibleText("Involved Party");
		
		WebElement elementName = sideForm.findElement(By.id("elementName"));
		elementName.sendKeys("IPID Name 1");
		
		WebElement dateBox = driver.findElement(By.xpath("//form//input[@name='date']"));
		dateBox.sendKeys("12112014");
		
		WebElement submitButton = sideForm.findElement(By.id("leftFormSubmit"));
		submitButton.click();

	  }
	  
	  
	  @Test
	  public void testProductSearchFormDisplayUseCase2() {

	    driver.get(appPath + "/home.html");
	    WebElement sideForm = driver.findElement(By.id("sideForm"));
		//WebElement name = sideForm.findElement(By.id("elementType"));
		Select dropdown = new Select(driver.findElement(By.id("elementType")));
		dropdown.selectByVisibleText("Involved Party");
		
		WebElement elementName = sideForm.findElement(By.id("elementName"));
		elementName.sendKeys("IPID Name 1");
		
		WebElement dateBox = driver.findElement(By.xpath("//form//input[@name='date']"));
		dateBox.sendKeys("12112014");
		
		WebElement minExposure = sideForm.findElement(By.id("minExposure"));
		minExposure.sendKeys("5000000000");
		
		WebElement submitButton = sideForm.findElement(By.id("leftFormSubmit"));
		submitButton.click();

	  }

	  
	  @Test
	  public void testProductSearchFormDisplayUseCase3() {

	    driver.get(appPath + "/home.html");
	    WebElement sideForm = driver.findElement(By.id("sideForm"));
		//WebElement name = sideForm.findElement(By.id("elementType"));
		Select dropdown = new Select(driver.findElement(By.id("elementType")));
		dropdown.selectByVisibleText("Legal Entity");
		
		Select viewType = new Select(driver.findElement(By.id("elementType")));
		viewType.selectByVisibleText("Legal Entity");
		
		Select hierType = new Select(driver.findElement(By.id("hierarchyType")));
		hierType.selectByVisibleText("Risk");
		
		WebElement dateBox = driver.findElement(By.xpath("//form//input[@name='date']"));
		dateBox.sendKeys("12112014");
		
		WebElement minExposure = sideForm.findElement(By.id("minExposure"));
		minExposure.sendKeys("1");
		
		WebElement submitButton = sideForm.findElement(By.id("leftFormSubmit"));
		submitButton.click();

	  }
}
