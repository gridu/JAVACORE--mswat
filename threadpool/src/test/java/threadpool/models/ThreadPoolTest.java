package threadpool.models;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class ThreadPoolTest {
    class FakeJob {
        void testCall(String s) {}
    }

    private final static long IRRELEVANT_TASK_DELAY = 0;

    @Mock
    FakeJob fakeJob;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test(timeout = 100)
    public void shouldProcessAllJobs() {
        ThreadPool threadPool = new ThreadPool(2);
        ArrayList<String> tasksNames = new ArrayList<>(List.of("A", "B", "C"));
        tasksNames.forEach(name -> threadPool.submitTask(() -> fakeJob.testCall(name), IRRELEVANT_TASK_DELAY));

        threadPool.startProcessing();
        try {
            sleep(50);
        } catch (InterruptedException e) {
            Assert.fail();
        }
        threadPool.endProcessing();

        tasksNames.forEach(name -> Mockito.verify(fakeJob).testCall(name));
    }

    @Test(timeout = 100)
    public void shouldProcessDelayedJobInOrderWhenNotOverloaded() {
        ThreadPool threadPool = new ThreadPool(2);
        threadPool.submitTask(() -> fakeJob.testCall("A"), 20);
        threadPool.submitTask(() -> fakeJob.testCall("B"), 5);

        threadPool.startProcessing();
        try {
            sleep(50);
        } catch (InterruptedException e) {
            Assert.fail();
        }
        threadPool.endProcessing();

        InOrder order = Mockito.inOrder(fakeJob);
        order.verify(fakeJob).testCall("B");
        order.verify(fakeJob).testCall("A");
    }

    @Test(timeout = 100)
    public void shouldNotFailWhenStartedWithEmptyJobQueue() {
        ThreadPool threadPool = new ThreadPool(2);

        threadPool.startProcessing();
        threadPool.endProcessing();
    }

    @Test(timeout = 100)
    public void shouldProcessJobsScheduledDuringProcessing() {
        ThreadPool threadPool = new ThreadPool(2);
        ArrayList<String> tasksNames = new ArrayList<>(List.of("A", "B", "C"));

        threadPool.startProcessing();
        tasksNames.forEach(name -> threadPool.submitTask(() -> fakeJob.testCall(name), IRRELEVANT_TASK_DELAY));

        try {
            sleep(50);
        } catch (InterruptedException e) {
            Assert.fail();
        }
        threadPool.endProcessing();

        tasksNames.forEach(name -> Mockito.verify(fakeJob).testCall(name));
    }

}