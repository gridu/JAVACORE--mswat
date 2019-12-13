package threadpool.models;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;


public class ThreadPoolTest {
    private class FakeJob {
        void testCall(String s) {
        }
    }

    private final static long SINGLE_TEST_TIMEOUT_IN_MILLISECONDS = 1000;
    private final static long IRRELEVANT_TASK_DELAY = 5;

    @Test(timeout = SINGLE_TEST_TIMEOUT_IN_MILLISECONDS)
    public void shouldProcessAllJobs() {
        FakeJob fakeJob = mock(FakeJob.class);
        ThreadPool threadPool = new ThreadPool(2);
        ArrayList<String> tasksNames = new ArrayList<>(List.of("A", "B", "C"));
        tasksNames.forEach(name -> threadPool.submitTask(() -> fakeJob.testCall(name), IRRELEVANT_TASK_DELAY));

        threadPool.startProcessing();
        threadPool.waitForJobs();
        threadPool.endProcessing();

        tasksNames.forEach(name -> Mockito.verify(fakeJob).testCall(name));
    }

    @Test(timeout = SINGLE_TEST_TIMEOUT_IN_MILLISECONDS)
    public void shouldProcessDelayedJobInOrderWhenNotOverloaded() {
        FakeJob fakeJob = mock(FakeJob.class);
        ThreadPool threadPool = new ThreadPool(2);
        threadPool.submitTask(() -> fakeJob.testCall("A"), 20);
        threadPool.submitTask(() -> fakeJob.testCall("B"), 5);

        threadPool.startProcessing();
        threadPool.waitForJobs();
        threadPool.endProcessing();

        InOrder order = Mockito.inOrder(fakeJob);
        order.verify(fakeJob).testCall("B");
        order.verify(fakeJob).testCall("A");
    }

    @Test(timeout = SINGLE_TEST_TIMEOUT_IN_MILLISECONDS)
    public void shouldNotFailWhenStartedWithEmptyJobQueue() {
        ThreadPool threadPool = new ThreadPool(2);

        threadPool.startProcessing();
        threadPool.endProcessing();
    }

    @Test(timeout = SINGLE_TEST_TIMEOUT_IN_MILLISECONDS)
    public void shouldProcessJobsScheduledDuringProcessing() {
        FakeJob fakeJob = mock(FakeJob.class);
        ThreadPool threadPool = new ThreadPool(2);
        ArrayList<String> tasksNames = new ArrayList<>(List.of("A", "B", "C"));
        tasksNames.forEach(name -> threadPool.submitTask(() -> fakeJob.testCall(name), IRRELEVANT_TASK_DELAY));

        threadPool.startProcessing();
        threadPool.waitForJobs();
        threadPool.endProcessing();

        tasksNames.forEach(name -> Mockito.verify(fakeJob).testCall(name));
    }

}