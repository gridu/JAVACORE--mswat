package threadpool.models;

class Worker extends Thread {
    private Job assignedJob;
    public final Object jobLock = new Object();

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                synchronized (jobLock) {
                    if (assignedJob == null) {
                        jobLock.wait();
                    }
                    else {
                        assignedJob.getRunnable().run();
                        assignedJob = null;
                        jobLock.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public void assignJob(Job job) {
        synchronized (jobLock) {
            assignedJob = job;
            jobLock.notifyAll();
        }
    }

    public void shutdown() {
        this.interrupt();
    }
}
