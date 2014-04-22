import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.HashMap;

public class SeleniumThreads2 {

    public static class SeleniumDriverByThread {
        final static String defaultUUID = "DEFAULT";
        private static ThreadLocal<String> activeUUID = new ThreadLocal<String>();
        private static ThreadLocal<HashMap<String, WebDriver>> driversMap = new ThreadLocal<HashMap<String, WebDriver>>() {
            @Override
            protected HashMap<String, WebDriver> initialValue() {
                return new HashMap<String, WebDriver>();
            }
        };

        public void set(WebDriver webDriver, String uuid) {
            HashMap driversThreadLocal = driversMap.get();
            driversThreadLocal.put(uuid, webDriver);
            driversMap.set(driversThreadLocal);
            setActive(uuid);
        }

        public void set(WebDriver webDriver) {
            set(webDriver, defaultUUID);
        }

        public void setActive(String uuid) {
            activeUUID.set(uuid);
        }

        public void setActive() {
            setActive(defaultUUID);
        }

        public WebDriver get() {
            return get(activeUUID.get());
        }

        private WebDriver get(String uuid) {
            return driversMap.get().get(uuid);
        }

        public void stopDriver() {
            get().quit();
/*            HashMap driversThreadLocal = driversMap.get();
            driversThreadLocal.remove(activeUUID);
            driversMap.set(driversThreadLocal);*/
        }

        public void open(String url) {
            get().get(url);
        }
    }

    public static class SomeBuilderThread extends Thread {
        private SeleniumDriverByThread builder;

        public SomeBuilderThread(SeleniumDriverByThread builder) {
            this.builder = builder;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + "-" + Thread.currentThread().getId());

            this.builder.set(new FirefoxDriver(), "FF");
            this.builder.open("http://www.google.com");
            WebElement element = this.builder.get().findElement(By.name("q"));
            element.sendKeys("Cheese!");
            element.submit();

            this.builder.set(new ChromeDriver(), "CH");
            this.builder.open("http://www.google.com");
            this.builder.stopDriver();

            this.builder.setActive("FF");
            this.builder.stopDriver();

        }
    }

    public static void main(String[] args) {
        SeleniumDriverByThread builder = new SeleniumDriverByThread();

        Thread thread1 = new SomeBuilderThread(builder);
        Thread thread2 = new SomeBuilderThread(builder);

        try {
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();
          }

        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}