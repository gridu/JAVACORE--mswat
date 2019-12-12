package threadpool.models;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Comparator;


public class ThreadPool {
    private final ArrayDeque<Job> taskQueue = new ArrayDeque<>();
    private final ArrayDeque<Worker> workers;
    private final Scheduler scheduler = new Scheduler();

    public ThreadPool(int numOfWorkers) {
        workers = new ArrayDeque<>();
        for (int i=numOfWorkers; i > 0; i--) {
            workers.add(new Worker());
        }
    }

    public void submitTask(Runnable job, long delay) {
        synchronized (taskQueue){
            taskQueue.add(new Job(job, delay));
            taskQueue.notifyAll();
        }
    }

    public void startProcessing() {
        scheduler.start();
        workers.forEach(Worker::start);
    }

    public void endProcessing() {
        scheduler.shutdown();
        workers.forEach(Worker::shutdown);
    }

    class Scheduler extends Thread {

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

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (taskQueue) {
                    try {
                        if (taskQueue.isEmpty()) {
                            taskQueue.wait();
                        }
                        else {
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

        public void shutdown() {
            this.interrupt();
        }
    }

}
