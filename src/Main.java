import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String args[]) throws InterruptedException {

        CacheChooser cacheChooser = new CacheChooser();
        cacheChooser.setSize(6);
        Runnable taskString = () -> {
            cacheChooser.addCache("muffin");
        };
        taskString.run();
        Runnable taskInt = () -> {
            cacheChooser.addCache(35);
        };
        taskInt.run();
        Runnable taskNull = () -> {
            cacheChooser.addCache(null);
        };
        taskNull.run();
        Thread[] threads = {new Thread(taskString), new Thread(taskString), new Thread(taskString),
                new Thread(taskInt), new Thread(taskInt), new Thread(taskInt),
                new Thread(taskNull), new Thread(taskNull), new Thread(taskNull)};
        ExecutorService executor = Executors.newWorkStealingPool();
        for (Thread t : threads) {
            t.join();
            executor.submit(t);
            //t.start();
            TimeUnit.SECONDS.sleep(2);

        }

        System.out.println(cacheChooser.toString());
    }

}
