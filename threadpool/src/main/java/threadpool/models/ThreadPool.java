package threadpool.models;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Comparator;


public class ThreadPool {
    private final ArrayDeque<Job> taskQueue = new ArrayDeque<>();
    private final ArrayDeque<Worker> workers;
    private final Scheduler scheduler = new Scheduler();
    private final Object poolStatusLock = new Object();
    private boolean isPoolDoneWithProcessing = false;

    public ThreadPool(int numOfWorkers) {
        workers = new ArrayDeque<>();
        for (int i = numOfWorkers; i > 0; i--) {
            workers.add(new Worker());
        }
    }

    public void submitTask(Runnable job, long delay) {
        synchronized (taskQueue) {
            taskQueue.add(new Job(job, delay));
            taskQueue.notifyAll();
        }
    }

    /**
     * Starts scheduling of tasks present in queue.
     * New tasks can be submitted to queue even if processing already started.
     */
    public void startProcessing() {
        scheduler.start();
        workers.forEach(Worker::start);
    }

    /**
     * Shutdowns scheduler and all workers in a pool - tasks that are already running will be interrupted.
     */
    public void endProcessing() {
        scheduler.shutdown();
        workers.forEach(Worker::shutdown);
    }

    /**
     * Waits for all scheduled jobs to finish.
     */
    public void waitForJobs() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (poolStatusLock) {
                if (isPoolDoneWithProcessing) return;
                else {
                    try {
                        poolStatusLock.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }
    }

    class Scheduler extends Thread {
        private static final long MAX_TIME_IN_MS_TO_WAIT_FOR_QUEUE_NOTIFICATION = 10;

        private boolean isWorkerFree(Worker worker) {
            var workerState = worker.getState();
            return workerState.equals(State.WAITING) || workerState.equals(State.NEW);
        }

        private Job getJobClosestToItsRunTime() {
            return taskQueue
                    .stream()
                    .min(Comparator.comparing(j -> j.getDelay() - Duration.between(j.getTimeOfAssignment(), Instant.now()).toMillis()))
                    .get();
        }

        private void updatePoolStatus(boolean isDoneWithJobs) {
            synchronized (poolStatusLock) {
                isPoolDoneWithProcessing = isDoneWithJobs;
                poolStatusLock.notifyAll();
            }
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (taskQueue) {
                    try {
                        if (taskQueue.isEmpty()) {
                            if (workers.stream().allMatch(this::isWorkerFree)) {
                                updatePoolStatus(true);
                            }
                            taskQueue.wait(MAX_TIME_IN_MS_TO_WAIT_FOR_QUEUE_NOTIFICATION);
                        }
                        else {
                            updatePoolStatus(false);
                            var nextJobToRun = getJobClosestToItsRunTime();
                            var maxTimeForSleep = nextJobToRun.getDelay() - Duration.between(nextJobToRun.getTimeOfAssignment(), Instant.now()).toMillis();
                            if (maxTimeForSleep > 0) {
                                taskQueue.wait(maxTimeForSleep);
                            }
                            else {
                                var freeWorker = workers.stream().filter(this::isWorkerFree).findFirst().orElse(null);
                                if (freeWorker != null) {
                                    var jobToSchedule = getJobClosestToItsRunTime();
                                    taskQueue.remove(jobToSchedule);
                                    freeWorker.assignJob(jobToSchedule);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }

        void shutdown() {
            this.interrupt();
        }
    }
}
