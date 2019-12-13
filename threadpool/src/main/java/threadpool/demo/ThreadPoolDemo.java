package threadpool.demo;

import threadpool.models.ThreadPool;

import java.time.Duration;
import java.time.Instant;


public class ThreadPoolDemo {

    public static void main(String[] args) {
        var start = Instant.now();

        var pool = new ThreadPool(2);
        pool.submitTask(() -> System.out.println(String.format("task 1 %s", Duration.between(start, Instant.now()).toMillis())), 400);
        pool.submitTask(() -> System.out.println(String.format("task 2 %s", Duration.between(start, Instant.now()).toMillis())), 1200);
        pool.submitTask(() -> System.out.println(String.format("task 3 %s", Duration.between(start, Instant.now()).toMillis())), 800);
        pool.submitTask(() -> System.out.println(String.format("task 4 %s", Duration.between(start, Instant.now()).toMillis())), 100);
        pool.submitTask(() -> System.out.println(String.format("task 5 %s", Duration.between(start, Instant.now()).toMillis())), 500);

        pool.startProcessing();

        var nextStart = Instant.now();


        pool.submitTask(() -> System.out.println(String.format("task 6 %s", Duration.between(nextStart, Instant.now()).toMillis())), 50);
        pool.submitTask(() -> System.out.println(String.format("task 7 %s", Duration.between(nextStart, Instant.now()).toMillis())), 1500);

        pool.waitForJobs();

        pool.endProcessing();

    }
}
