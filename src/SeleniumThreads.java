import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class SeleniumThreads {

    public static class SeleniumDriverByThread {
        private static ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<WebDriver>();

        public void set(WebDriver webDriver) {
            driverThreadLocal.set(webDriver);
        }
        public WebDriver get() {
            return driverThreadLocal.get();
        }
        public void stopDriver() {
            get().quit();
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

            this.builder.set(new FirefoxDriver());
            this.builder.open("http://www.google.com");
            WebElement element = this.builder.get().findElement(By.name("q"));
            element.sendKeys("Cheese!");
            element.submit();

            try {
                Thread.sleep((int) (Math.random() * 10000 + 10000));
            } catch(InterruptedException ex) {};

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