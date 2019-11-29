package se.kth.karamel.core.execution;

import org.junit.Before;
import org.junit.Test;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.cookbookmeta.KaramelFile;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.cookbookmeta.MetadataRb;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.dag.Dag;
import se.kth.karamel.core.node.NodeNoop;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestTaskConsumer {

  private KaramelizedCookbook karamelizedCookbook = null;
  private Settings settings;

  private final static String TEST_INSTALL = "test::install";
  private final static String TEST_DEFAULT = "test::default";

  @Before
  public void setup() throws KaramelException, IOException {
    Map<String, String> cookbookRecipes = new HashMap<>();
    cookbookRecipes.put(TEST_INSTALL, "Install recipe");
    cookbookRecipes.put(TEST_DEFAULT, "Default recipe");

    MetadataRb testMetadataRb = new MetadataRb();
    testMetadataRb.setName("test");
    testMetadataRb.setRecipes(cookbookRecipes);

    karamelizedCookbook =
        new KaramelizedCookbook(testMetadataRb, new KaramelFile());
    CookbookCache.getInstance().addToCache("test", karamelizedCookbook);

    settings = new Settings();
  }

  private Task createTask() {
    NoopTask task = mock(NoopTask.class);

    when(task.getTaskStatus()).thenCallRealMethod();
    doCallRealMethod().when(task).setTaskStatus(anyObject());
    task.setTaskStatus(TaskStatus.WAITING);

    when(task.getNode()).thenReturn(new NodeNoop(0));

    return task;
  }

  @Test
  public void testSingleConsumer() throws KaramelException, InterruptedException, IOException {
    Task task = createTask();

    Dag dag = new Dag();
    dag.getTaskList().add(task);

    settings.setInt(Settings.SettingsKeys.EXECUTION_THREADS, 1);

    ExecutionEngine executionEngine = new ExecutionEngine(settings);
    executionEngine.execute(dag, 1);
    Thread.sleep(10000);
    verify(task, times(1)).execute();
    assertEquals(TaskStatus.SUCCESS, task.getTaskStatus());
  }

  @Test
  public void testMultipleConsumer() throws KaramelException, InterruptedException, IOException {
    Task t1 = createTask();
    Task t2 = createTask();

    Dag dag = new Dag();
    dag.getTaskList().add(t1);
    dag.getTaskList().add(t2);

    settings.setInt(Settings.SettingsKeys.EXECUTION_THREADS, 2);

    ExecutionEngine executionEngine = new ExecutionEngine(settings);
    executionEngine.execute(dag, 1);
    Thread.sleep(10000);
    assertEquals(TaskStatus.SUCCESS, t1.getTaskStatus());
    assertEquals(TaskStatus.SUCCESS, t2.getTaskStatus());
    verify(t1, times(1)).execute();
    verify(t2, times(1)).execute();
  }

  @Test
  public void testExecutionExceptionHandling() throws KaramelException, InterruptedException, IOException {
    Task task = createTask();
    doThrow(ExecutionException.class).when(task).execute();

    Dag dag = new Dag();
    dag.getTaskList().add(task);

    ExecutionEngine executionEngine = new ExecutionEngine(settings);
    executionEngine.execute(dag, 1);

    Thread.sleep(10000);
    assertEquals(TaskStatus.FAILED, task.getTaskStatus());
    verify(task, times(1)).execute();
  }

  @Test
  public void testIOExceptionHandling() throws KaramelException, InterruptedException, IOException {
    Task task = createTask();
    doThrow(IOException.class).when(task).execute();

    Dag dag = new Dag();
    dag.getTaskList().add(task);

    ExecutionEngine executionEngine = new ExecutionEngine(settings);
    executionEngine.execute(dag, 1);

    Thread.sleep(10000);
    assertEquals(TaskStatus.FAILED, task.getTaskStatus());
    verify(task, times(1)).execute();
  }

  @Test
  public void testPauseExecution() throws KaramelException, InterruptedException, IOException {
    Task task = createTask();

    Dag dag = new Dag();
    dag.getTaskList().add(task);

    settings.setInt(Settings.SettingsKeys.EXECUTION_THREADS, 1);
    ExecutionEngine executionEngine = new ExecutionEngine(settings);
    executionEngine.execute(dag, 1);

    executionEngine.pause();

    // At most once before the suspend in case the thread
    // is already blocking on the tasksQueue.take() operation
    verify(task, atMost(1)).execute();

    // Add another task to the list
    Task t1 = createTask();
    dag.getTaskList().add(t1);

    executionEngine.resume();
    Thread.sleep(10000);

    // Both tasks should have been executed one time successfully
    verify(t1, times(1)).execute();
    verify(task, times(1)).execute();
    assertEquals(TaskStatus.SUCCESS, task.getTaskStatus());
    assertEquals(TaskStatus.SUCCESS, t1.getTaskStatus());
  }

  @Test
  public void testTerminateExecution() throws KaramelException, InterruptedException, IOException {
    Task task = createTask();

    Dag dag = new Dag();
    dag.getTaskList().add(task);

    settings.setInt(Settings.SettingsKeys.EXECUTION_THREADS, 1);
    ExecutionEngine executionEngine = new ExecutionEngine(settings);
    executionEngine.execute(dag, 1);

    executionEngine.pause();

    // At most once before the suspend in case the thread
    // is already blocking on the tasksQueue.take() operation
    verify(task, atMost(1)).execute();

    // Add another task to the list
    Task t1 = createTask();
    dag.getTaskList().add(t1);

    executionEngine.terminate();
    Thread.sleep(10000);

    // Task 1 should have not been executed. Task could have been executed at most once.
    verify(t1, times(0)).execute();
    verify(task, atMost(1)).execute();
    assertEquals(TaskStatus.WAITING, t1.getTaskStatus());
  }

  @Test
  public void testRetry() throws KaramelException, InterruptedException, IOException {
    Task task = createTask();
    doThrow(ExecutionException.class).when(task).execute();

    Dag dag = new Dag();
    dag.getTaskList().add(task);

    settings.setInt(Settings.SettingsKeys.EXECUTION_THREADS, 1);
    ExecutionEngine executionEngine = new ExecutionEngine(settings);
    executionEngine.execute(dag, 1);
    Thread.sleep(10000);

    assertEquals(TaskStatus.FAILED, task.getTaskStatus());
    assertTrue(executionEngine.getNodeBusy()[0].get());
    executionEngine.retryTask(task);

    Thread.sleep(10000);

    // Verify the task has been re-executed
    verify(task, times(2)).execute();
  }

  @Test
  public void testSkipTask() throws KaramelException, InterruptedException, IOException {
    Task task = createTask();
    doThrow(ExecutionException.class).when(task).execute();

    Dag dag = new Dag();
    dag.getTaskList().add(task);

    settings.setInt(Settings.SettingsKeys.EXECUTION_THREADS, 1);
    ExecutionEngine executionEngine = new ExecutionEngine(settings);
    executionEngine.execute(dag, 1);
    Thread.sleep(10000);

    assertEquals(TaskStatus.FAILED, task.getTaskStatus());
    assertTrue(executionEngine.getNodeBusy()[0].get());

    executionEngine.skipTask(task);

    Thread.sleep(10000);

    // Verify that the task has not been re-executed and that the machine is free
    verify(task, times(1)).execute();
    assertEquals(TaskStatus.SKIPPED, task.getTaskStatus());
    assertFalse(executionEngine.getNodeBusy()[0].get());
  }
}
