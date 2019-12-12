package threadpool.models;

import java.time.Instant;

class Job {
    private Runnable runnable;
    private long delay;
    private final Instant timeOfAssignment = Instant.now();

    public Job(Runnable job, long delay) {
        this.runnable = job;
        this.delay = delay;
    }

    public long getDelay() {
        return delay;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public Instant getTimeOfAssignment() {
        return timeOfAssignment;
    }
}
