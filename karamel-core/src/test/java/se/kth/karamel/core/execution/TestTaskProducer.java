package se.kth.karamel.core.execution;

import org.junit.Before;
import org.junit.Test;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.cookbookmeta.KaramelFile;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.cookbookmeta.MetadataRb;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.dag.Dag;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class TestTaskProducer {

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
    settings.setInt(Settings.SettingsKeys.EXECUTION_THREADS, 0);
  }

  @Test(timeout=6000)
  public void testProduceTask() throws InterruptedException, KaramelException, IOException {
    Cluster cluster = TestCommon.buildCluster(karamelizedCookbook, Arrays.asList("127.0.0.1"));
    Dag dag = TestCommon.buildDag(cluster, settings);
    ExecutionEngine executionEngine = new ExecutionEngine(settings);
    executionEngine.execute(dag, 1);

    // Assert that the thread is actually active
    assertTrue(executionEngine.getProducerThread().isAlive());
    Thread.sleep(5000);
    // Assert that there is a single task in the task queue
    assertEquals(1, executionEngine.getTasksQueue().size());
    // Assert that the single node is marked as busy
    assertTrue(executionEngine.getNodeBusy()[0].get());
  }

  @Test
  public void testConcurrentMachines() throws KaramelException, InterruptedException, IOException {
    Cluster cluster = TestCommon.buildCluster(karamelizedCookbook, Arrays.asList("127.0.0.1", "127.0.0.2"));
    Dag dag = TestCommon.buildDag(cluster, settings);
    ExecutionEngine executionEngine = new ExecutionEngine(settings);
    executionEngine.execute(dag, 2);

    // Assert that the thread is actually active
    assertTrue(executionEngine.getProducerThread().isAlive());
    Thread.sleep(5000);
    // Assert that there is a single task in the task queue
    assertEquals(2, executionEngine.getTasksQueue().size());
    // Assert that the single node is marked as busy
    assertTrue(executionEngine.getNodeBusy()[0].get());
    assertTrue(executionEngine.getNodeBusy()[1].get());
  }

  @Test(timeout=11000)
  public void testTerminateProducer() throws KaramelException, InterruptedException, IOException {
    Cluster cluster = TestCommon.buildCluster(karamelizedCookbook, Arrays.asList("127.0.0.1"));
    Dag dag = TestCommon.buildDag(cluster, settings);
    ExecutionEngine executionEngine = new ExecutionEngine(settings);
    executionEngine.execute(dag, 1);

    // Assert that the thread is actually active
    assertTrue(executionEngine.getProducerThread().isAlive());
    Thread.sleep(5000);
    // Assert that there is a single task in the task queue
    assertEquals(1, executionEngine.getTasksQueue().size());
    // Assert that the single node is marked as busy
    assertTrue(executionEngine.getNodeBusy()[0].get());

    // Terminate execution engine
    executionEngine.terminate();
    Thread.sleep(2000);
    executionEngine.getNodeBusy()[0].set(false);
    Task t = executionEngine.getTasksQueue().poll();
    t.setTaskStatus(TaskStatus.SUCCESS);
    Thread.sleep(2000);

    assertFalse(executionEngine.getProducerThread().isInterrupted());
    // Assert that there is a single task in the task queue
    assertEquals(0, executionEngine.getTasksQueue().size());
    // Assert that the single node is marked as busy
    assertFalse(executionEngine.getNodeBusy()[0].get());
  }
}
