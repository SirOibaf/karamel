package se.kth.karamel.core.dag;

import org.junit.Before;
import org.junit.Test;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.clusterdef.NoOp;
import se.kth.karamel.common.clusterdef.Recipe;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.cookbookmeta.KaramelFile;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.cookbookmeta.MetadataRb;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.ClusterContext;
import se.kth.karamel.core.chef.DataBagsFactory;
import se.kth.karamel.core.execution.FetchCookbooksTask;
import se.kth.karamel.core.execution.NodeSetupTask;
import se.kth.karamel.core.execution.RunRecipeTask;
import se.kth.karamel.core.execution.Task;
import se.kth.karamel.core.provisioner.jcloud.baremetal.NoopProvisioner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static se.kth.karamel.core.execution.TaskStatus.WAITING;

public class TestDagFactory {

  private Cluster baseCluster = null;
  private ClusterContext clusterContext = null;
  private DagFactory dagFactory = null;
  private DataBagsFactory dataBagsFactory = null;

  public final static String TEST_INSTALL = "test::install";
  public final static String TEST_DEFAULT = "test::default";

  @Before
  public void setup() throws KaramelException, IOException {
    baseCluster = new Cluster();

    Map<String, String> cookbookRecipes = new HashMap<>();
    cookbookRecipes.put(TEST_INSTALL, "Install recipe");
    cookbookRecipes.put(TEST_DEFAULT, "Default recipe");

    MetadataRb testMetadataRb = new MetadataRb();
    testMetadataRb.setName("test");
    testMetadataRb.setRecipes(cookbookRecipes);

    KaramelizedCookbook karamelizedCookbook =
        new KaramelizedCookbook(testMetadataRb, new KaramelFile());
    CookbookCache.getInstance().addToCache("test", karamelizedCookbook);

    baseCluster = buildCluster(karamelizedCookbook);
    clusterContext = new ClusterContext(baseCluster);
    dataBagsFactory = new DataBagsFactory(baseCluster);
    dagFactory = new DagFactory();
  }

  private Cluster buildCluster(KaramelizedCookbook karamelizedCookbook) {
    Cluster cluster = new Cluster();

    NoOp noopGlobal = new NoOp();
    noopGlobal.setUsername("Username");
    cluster.setNoop(noopGlobal);

    Group firstGroup = new Group();
    NoOp noopGroup = new NoOp();
    noopGroup.setIps(Arrays.asList("127.0.0.1", "127.0.0.2", "127.0.0.3"));

    List<Recipe> recipesList = new ArrayList<>();
    recipesList.add(new Recipe(karamelizedCookbook, "test"));

    firstGroup.setNoop(noopGroup);
    firstGroup.setRecipes(recipesList);

    cluster.setGroups(Arrays.asList(firstGroup));

    return cluster;
  }

  private void provisionNoopNodes() throws KaramelException {
    NoopProvisioner noopProvisioner = new NoopProvisioner();
    int numNodes = 0;
    for (Group group : clusterContext.getCluster().getGroups()) {
      numNodes += noopProvisioner.provisionGroup(clusterContext,
          clusterContext.getCluster(), group, numNodes);
    }
  }

  @Test
  public void testSingleCookbookDAG() throws IOException, KaramelException {
    provisionNoopNodes();
    Dag dag = dagFactory.buildDag(baseCluster, new Settings(), dataBagsFactory);
    assertNotNull(dag);
    // 3 nodes, 2 recipes (default + install), 1 setup task, 1 fetch cookbook
    assertEquals(12, dag.getTaskList().size());
    // check all tasks are in WAITING state
    for (Task t : dag.getTaskList()) {
      assertEquals(WAITING, t.getTaskStatus());
    }
    // make sure that dependencies are set correctly
    for (Task t : dag.getTaskList()) {
      if (t instanceof NodeSetupTask) {
        assertEquals(0, t.getDependsOn().size());
      } else if (t instanceof FetchCookbooksTask) {
        assertFetchDependencies((FetchCookbooksTask) t);
      } else {
        assertRunRecipeTaskDependencies((RunRecipeTask) t);
      }
    }
  }

  private void assertFetchDependencies(FetchCookbooksTask t) {
    // 1 dependency on the same node
    assertEquals(1, t.getDependsOn().size());
    assertEquals(t.getNode(), t.getDependsOn().get(0).getNode());
    assertTrue(t.getDependsOn().get(0) instanceof NodeSetupTask);
  }

  private void assertRunRecipeTaskDependencies(RunRecipeTask runTask) {
    // Check that install recipes depend on FetchCookbooksTask
    if (runTask.getRecipe().getCanonicalName().equals(TEST_INSTALL)) {
      assertTrue(runTask.getDependsOn().stream()
          .anyMatch(t -> t instanceof FetchCookbooksTask && t.getNode().equals(runTask.getNode())));
    } else {
      // Check that it has a dependency to the install recipe
      assertTrue(runTask.getDependsOn().stream()
          .anyMatch(t -> ((RunRecipeTask)t).getRecipe().equals(new Recipe(TEST_INSTALL))));
    }
  }

  @Test
  public void testMultipleCookbooksDAG() {
    throw new RuntimeException("Still have to implement this");
  }

  @Test
  public void testMultipleGroup() {
    throw new RuntimeException("Still have to implement this");
  }

  @Test
  public void testLocalDependencies() {
    throw new RuntimeException("Still have to implement this");
  }

  @Test
  public void testGlobalDependencies() {
    throw new RuntimeException("Still have to implement this");
  }

  @Test
  public void testTransientDependencies() {
    throw new RuntimeException("Still have to implement this");
  }

  @Test
  public void testCycles() {
    throw new RuntimeException("Still have to implement this");
  }
}
