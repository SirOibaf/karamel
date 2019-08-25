package se.kth.karamel.core.dag;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.clusterdef.NoOp;
import se.kth.karamel.common.clusterdef.Recipe;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.cookbookmeta.KaramelFile;
import se.kth.karamel.common.cookbookmeta.KaramelFileDeps;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.cookbookmeta.MetadataRb;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Constants;
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
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static se.kth.karamel.core.execution.TaskStatus.FAILED;
import static se.kth.karamel.core.execution.TaskStatus.SCHEDULED;
import static se.kth.karamel.core.execution.TaskStatus.SKIPPED;
import static se.kth.karamel.core.execution.TaskStatus.SUCCESS;
import static se.kth.karamel.core.execution.TaskStatus.WAITING;

public class TestDagFactory {

  private Cluster baseCluster = null;
  private ClusterContext clusterContext = null;
  private DagFactory dagFactory = null;
  private DataBagsFactory dataBagsFactory = null;
  private KaramelizedCookbook karamelizedCookbook = null;

  private final static String TEST_INSTALL = "test::install";
  private final static String TEST_DEFAULT = "test::default";

  @Before
  public void setup() throws KaramelException, IOException {
    baseCluster = new Cluster();

    Map<String, String> cookbookRecipes = new HashMap<>();
    cookbookRecipes.put(TEST_INSTALL, "Install recipe");
    cookbookRecipes.put(TEST_DEFAULT, "Default recipe");

    MetadataRb testMetadataRb = new MetadataRb();
    testMetadataRb.setName("test");
    testMetadataRb.setRecipes(cookbookRecipes);

    karamelizedCookbook =
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

    ArrayList<Group> groups = new ArrayList<>(Arrays.asList(firstGroup));
    cluster.setGroups(groups);

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
    if (runTask.getRecipe().getCanonicalName().contains(Constants.INSTALL_RECIPE)) {
      assertEquals(1, runTask.getDependsOn().stream()
          .filter(t -> t instanceof FetchCookbooksTask && t.getNode().equals(runTask.getNode()))
          .count()
      );
    } else {
      // Check that it has a dependency to the install recipe
      assertEquals(1, runTask.getDependsOn().stream()
          .filter(t -> ((RunRecipeTask)t).getRecipe().getCanonicalName().contains(Constants.INSTALL_RECIPE))
          .count()
        );
    }
  }

  @Test
  public void testMultipleGroup() throws KaramelException, IOException {
    // Add the second group
    Group secondGroup = new Group();
    NoOp noopGroup = new NoOp();
    noopGroup.setIps(Arrays.asList("127.0.0.4", "127.0.0.5", "127.0.0.6"));

    List<Recipe> recipesList = new ArrayList<>();
    recipesList.add(new Recipe(karamelizedCookbook, "test"));

    secondGroup.setNoop(noopGroup);
    secondGroup.setRecipes(recipesList);

    this.clusterContext.getCluster().getGroups().add(secondGroup);
    provisionNoopNodes();
    Dag dag = dagFactory.buildDag(baseCluster, new Settings(), dataBagsFactory);

    assertNotNull(dag);
    // 6 nodes, 2 recipes (default + install), 1 setup task, 1 fetch cookbook
    assertEquals(24, dag.getTaskList().size());
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

  @Test
  public void testMultipleCookbooksDAG() throws KaramelException, IOException {
    Map<String, String> cookbookRecipes = new HashMap<>();
    cookbookRecipes.put("test2::install", "Install recipe");
    cookbookRecipes.put("test2::default", "Default recipe");

    MetadataRb testMetadataRb = new MetadataRb();
    testMetadataRb.setName("test2");
    testMetadataRb.setRecipes(cookbookRecipes);

    KaramelizedCookbook karamelizedCookbook =
        new KaramelizedCookbook(testMetadataRb, new KaramelFile());
    CookbookCache.getInstance().addToCache("test2", karamelizedCookbook);

    // Add a second recipe to the group
    clusterContext.getCluster().getGroups().get(0).getRecipes().add(new Recipe(karamelizedCookbook, "test2"));

    provisionNoopNodes();
    Dag dag = dagFactory.buildDag(baseCluster, new Settings(), dataBagsFactory);

    assertNotNull(dag);
    // 3 nodes, 4 recipes (default + install for both cookbooks), 1 setup task, 1 fetch cookbook
    assertEquals(18, dag.getTaskList().size());
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

  @Test
  public void testLocalDependencies() throws KaramelException, IOException {
    Map<String, String> cookbookRecipes = new HashMap<>();
    cookbookRecipes.put("test2::install", "Install recipe");
    cookbookRecipes.put("test2::default", "Default recipe");
    cookbookRecipes.put("test2::another", "Another recipe");

    MetadataRb testMetadataRb = new MetadataRb();
    testMetadataRb.setName("test2");
    testMetadataRb.setRecipes(cookbookRecipes);

    // "Default recipe" depends on locally "Another recipe"
    List<String> localDependencies = new ArrayList<>();
    localDependencies.add("test2::another");
    List<String> globalDependencies = new ArrayList<>();
    KaramelFileDeps dependency =
      new KaramelFileDeps("test2::default", localDependencies, globalDependencies);

    KaramelFile karamelFile = new KaramelFile();
    karamelFile.setDependencies(Arrays.asList(dependency));

    KaramelizedCookbook karamelizedCookbook =
        new KaramelizedCookbook(testMetadataRb, karamelFile);
    CookbookCache.getInstance().addToCache("test2", karamelizedCookbook);

    // Overwrite recipe list
    List<Recipe> recipesList = new ArrayList<>();
    recipesList.add(new Recipe(karamelizedCookbook, "test2::default"));
    recipesList.add(new Recipe(karamelizedCookbook, "test2::another"));
    clusterContext.getCluster().getGroups().get(0).setRecipes(recipesList);

    provisionNoopNodes();
    Dag dag = dagFactory.buildDag(baseCluster, new Settings(), dataBagsFactory);

    assertNotNull(dag);

    for (Task t : dag.getTaskList()) {
      if (t instanceof RunRecipeTask) {
        RunRecipeTask runRecipeTask = (RunRecipeTask) t;
        if (runRecipeTask.getRecipe().getCanonicalName().contains("another")) {
          // Validate that the another recipe has a single dependency to the install
          assertEquals(1, runRecipeTask.getDependsOn().size());
        } else if (runRecipeTask.getRecipe().getCanonicalName().contains("default")) {
          // Each default recipe should depends on the install recipe and on the another recipe
          assertEquals(2, runRecipeTask.getDependsOn().size());
        }
      }
    }
  }

  @Test
  public void testGlobalDependencies() throws KaramelException, IOException {
    Map<String, String> cookbookRecipes = new HashMap<>();
    cookbookRecipes.put("test2::install", "Install recipe");
    cookbookRecipes.put("test2::default", "Default recipe");
    cookbookRecipes.put("test2::another", "Another recipe");

    MetadataRb testMetadataRb = new MetadataRb();
    testMetadataRb.setName("test2");
    testMetadataRb.setRecipes(cookbookRecipes);

    // "Default recipe" depends on locally "Another recipe"
    List<String> localDependencies = new ArrayList<>();
    List<String> globalDependencies = new ArrayList<>();
    globalDependencies.add("test2::another");
    KaramelFileDeps dependency =
      new KaramelFileDeps("test2::default", localDependencies, globalDependencies);

    KaramelFile karamelFile = new KaramelFile();
    karamelFile.setDependencies(Arrays.asList(dependency));

    KaramelizedCookbook karamelizedCookbook =
      new KaramelizedCookbook(testMetadataRb, karamelFile);
    CookbookCache.getInstance().addToCache("test2", karamelizedCookbook);

    // Overwrite recipe list
    List<Recipe> recipesList = new ArrayList<>();
    recipesList.add(new Recipe(karamelizedCookbook, "test2::default"));
    recipesList.add(new Recipe(karamelizedCookbook, "test2::another"));
    clusterContext.getCluster().getGroups().get(0).setRecipes(recipesList);

    provisionNoopNodes();
    Dag dag = dagFactory.buildDag(baseCluster, new Settings(), dataBagsFactory);

    assertNotNull(dag);

    for (Task t : dag.getTaskList()) {
      if (t instanceof RunRecipeTask) {
        RunRecipeTask runRecipeTask = (RunRecipeTask) t;
        if (runRecipeTask.getRecipe().getCanonicalName().contains("another")) {
          // Validate that the another recipe has a single dependency to the install
          assertEquals(1, runRecipeTask.getDependsOn().size());
          assertTrue(runRecipeTask.getDependsOn().get(0) instanceof RunRecipeTask);
        } else if (runRecipeTask.getRecipe().getCanonicalName().contains("default")) {
          // Each default recipe should depends on the install recipe
          // and on all the another recipes running on the cluster (3 nodes)
          assertEquals(4, runRecipeTask.getDependsOn().size());
          for (Task dep : runRecipeTask.getDependsOn()) {
            assertTrue(dep instanceof RunRecipeTask);
          }
        }
      }
    }
  }

  @Test
  @Ignore
  public void testTransientDependencies() {
    throw new RuntimeException("Still have to implement this");
  }

  @Test
  public void testGetSchedulableTasks() throws KaramelException, IOException {
    provisionNoopNodes();
    Dag dag = dagFactory.buildDag(baseCluster, new Settings(), dataBagsFactory);
    Set<Task> schedulableTaskSet = dag.getSchedulableTasks();
    // First should be the NodeSetup tasks
    assertEquals(3, schedulableTaskSet.size());
    for (Task t : schedulableTaskSet) {
      assertEquals(0, t.getDependsOn().size());
      assertTrue(t instanceof NodeSetupTask);

      // Set the status to success for the next iteration
      t.setTaskStatus(SCHEDULED);
    }

    Set<Task> newSchedulableTaskSet = dag.getSchedulableTasks();
    // Should be empty, all tasks are scheduled
    assertEquals(0, newSchedulableTaskSet.size());

    schedulableTaskSet.forEach(t -> t.setTaskStatus(SUCCESS));

    // Get a new batch of schedulable tasks, this time should be the FetchCookbooks
    schedulableTaskSet = dag.getSchedulableTasks();
    assertEquals(3, schedulableTaskSet.size());
    for (Task t : schedulableTaskSet) {
      assertTrue(t.getDependsOn().stream().noneMatch(task -> (task.getTaskStatus() != SUCCESS)));
      t.setTaskStatus(SKIPPED);
    }

    // Check that also skipped recipes are considered "done"
    schedulableTaskSet = dag.getSchedulableTasks();
    assertEquals(3, schedulableTaskSet.size());
    for (Task t : schedulableTaskSet) {
      assertTrue(t.getDependsOn().stream().noneMatch(task -> (task.getTaskStatus() != SKIPPED)));
      t.setTaskStatus(FAILED);
    }

    newSchedulableTaskSet = dag.getSchedulableTasks();
    // Should be empty until the failed are resolved
    assertEquals(0, newSchedulableTaskSet.size());

    schedulableTaskSet.forEach(t -> t.setTaskStatus(SUCCESS));

    newSchedulableTaskSet = dag.getSchedulableTasks();
    assertEquals(3, newSchedulableTaskSet.size());
  }

  @Test
  public void testCyclesGlobal() throws KaramelException, IOException{
    Map<String, String> cookbookRecipes = new HashMap<>();
    cookbookRecipes.put("test2::install", "Install recipe");
    cookbookRecipes.put("test2::default", "Default recipe");
    cookbookRecipes.put("test2::another", "Another recipe");
    cookbookRecipes.put("test2::third", "Third recipe");

    MetadataRb testMetadataRb = new MetadataRb();
    testMetadataRb.setName("test2");
    testMetadataRb.setRecipes(cookbookRecipes);

    List<KaramelFileDeps> dependencies = new ArrayList<>();

    List<String> localDependencies = new ArrayList<>();
    List<String> globalDependencies = new ArrayList<>();
    globalDependencies.add("test2::another");
    dependencies.add(
      new KaramelFileDeps("test2::default", localDependencies, globalDependencies));

    localDependencies = new ArrayList<>();
    globalDependencies = new ArrayList<>();
    globalDependencies.add("test2::third");
    dependencies.add(
      new KaramelFileDeps("test2::another", localDependencies, globalDependencies));

    localDependencies = new ArrayList<>();
    globalDependencies = new ArrayList<>();
    globalDependencies.add("test2::default");
    dependencies.add(
      new KaramelFileDeps("test2::third", localDependencies, globalDependencies));

    KaramelFile karamelFile = new KaramelFile();
    karamelFile.setDependencies(dependencies);

    KaramelizedCookbook karamelizedCookbook =
      new KaramelizedCookbook(testMetadataRb, karamelFile);
    CookbookCache.getInstance().addToCache("test2", karamelizedCookbook);

    provisionNoopNodes();

    List<Recipe> recipesList = new ArrayList<>();
    recipesList.add(new Recipe(karamelizedCookbook, "test2::default"));
    recipesList.add(new Recipe(karamelizedCookbook, "test2::another"));
    recipesList.add(new Recipe(karamelizedCookbook, "test2::third"));
    clusterContext.getCluster().getGroups().get(0).setRecipes(recipesList);

    Dag dag = dagFactory.buildDag(baseCluster, new Settings(), dataBagsFactory);
    assertTrue(dag.hasCycle());
  }

  @Test
  public void testCyclesLocal() throws KaramelException, IOException{
    Map<String, String> cookbookRecipes = new HashMap<>();
    cookbookRecipes.put("test2::install", "Install recipe");
    cookbookRecipes.put("test2::default", "Default recipe");
    cookbookRecipes.put("test2::another", "Another recipe");
    cookbookRecipes.put("test2::third", "Third recipe");

    MetadataRb testMetadataRb = new MetadataRb();
    testMetadataRb.setName("test2");
    testMetadataRb.setRecipes(cookbookRecipes);

    List<KaramelFileDeps> dependencies = new ArrayList<>();

    List<String> localDependencies = new ArrayList<>();
    localDependencies.add("test2::another");
    List<String> globalDependencies = new ArrayList<>();
    dependencies.add(
      new KaramelFileDeps("test2::default", localDependencies, globalDependencies));

    localDependencies = new ArrayList<>();
    localDependencies.add("test2::third");
    globalDependencies = new ArrayList<>();
    dependencies.add(
      new KaramelFileDeps("test2::another", localDependencies, globalDependencies));

    localDependencies = new ArrayList<>();
    localDependencies.add("test2::default");
    globalDependencies = new ArrayList<>();
    dependencies.add(
      new KaramelFileDeps("test2::third", localDependencies, globalDependencies));

    KaramelFile karamelFile = new KaramelFile();
    karamelFile.setDependencies(dependencies);

    KaramelizedCookbook karamelizedCookbook =
      new KaramelizedCookbook(testMetadataRb, karamelFile);
    CookbookCache.getInstance().addToCache("test2", karamelizedCookbook);

    provisionNoopNodes();

    List<Recipe> recipesList = new ArrayList<>();
    recipesList.add(new Recipe(karamelizedCookbook, "test2::default"));
    recipesList.add(new Recipe(karamelizedCookbook, "test2::another"));
    recipesList.add(new Recipe(karamelizedCookbook, "test2::third"));
    clusterContext.getCluster().getGroups().get(0).setRecipes(recipesList);

    Dag dag = dagFactory.buildDag(baseCluster, new Settings(), dataBagsFactory);
    assertTrue(dag.hasCycle());
  }

   @Test
  public void testCyclesMixed() throws KaramelException, IOException{
    Map<String, String> cookbookRecipes = new HashMap<>();
    cookbookRecipes.put("test2::install", "Install recipe");
    cookbookRecipes.put("test2::default", "Default recipe");
    cookbookRecipes.put("test2::another", "Another recipe");
    cookbookRecipes.put("test2::third", "Third recipe");

    MetadataRb testMetadataRb = new MetadataRb();
    testMetadataRb.setName("test2");
    testMetadataRb.setRecipes(cookbookRecipes);

    List<KaramelFileDeps> dependencies = new ArrayList<>();

    List<String> localDependencies = new ArrayList<>();
    List<String> globalDependencies = new ArrayList<>();
    globalDependencies.add("test2::another");
    dependencies.add(
      new KaramelFileDeps("test2::default", localDependencies, globalDependencies));

    localDependencies = new ArrayList<>();
    localDependencies.add("test2::third");
    globalDependencies = new ArrayList<>();
    dependencies.add(
      new KaramelFileDeps("test2::another", localDependencies, globalDependencies));

    localDependencies = new ArrayList<>();
    localDependencies.add("test2::default");
    globalDependencies = new ArrayList<>();
    globalDependencies.add("test2::default");
    dependencies.add(
      new KaramelFileDeps("test2::third", localDependencies, globalDependencies));

    KaramelFile karamelFile = new KaramelFile();
    karamelFile.setDependencies(dependencies);

    KaramelizedCookbook karamelizedCookbook =
      new KaramelizedCookbook(testMetadataRb, karamelFile);
    CookbookCache.getInstance().addToCache("test2", karamelizedCookbook);

    provisionNoopNodes();

    List<Recipe> recipesList = new ArrayList<>();
    recipesList.add(new Recipe(karamelizedCookbook, "test2::default"));
    recipesList.add(new Recipe(karamelizedCookbook, "test2::another"));
    recipesList.add(new Recipe(karamelizedCookbook, "test2::third"));
    clusterContext.getCluster().getGroups().get(0).setRecipes(recipesList);

    Dag dag = dagFactory.buildDag(baseCluster, new Settings(), dataBagsFactory);
    assertTrue(dag.hasCycle());
  }


  @Test
  public void testNoCycles() throws KaramelException, IOException{
    Map<String, String> cookbookRecipes = new HashMap<>();
    cookbookRecipes.put("test2::install", "Install recipe");
    cookbookRecipes.put("test2::default", "Default recipe");
    cookbookRecipes.put("test2::another", "Another recipe");
    cookbookRecipes.put("test2::third", "Third recipe");

    MetadataRb testMetadataRb = new MetadataRb();
    testMetadataRb.setName("test2");
    testMetadataRb.setRecipes(cookbookRecipes);

    List<KaramelFileDeps> dependencies = new ArrayList<>();

    List<String> localDependencies = new ArrayList<>();
    List<String> globalDependencies = new ArrayList<>();
    globalDependencies.add("test2::another");
    dependencies.add(
      new KaramelFileDeps("test2::default", localDependencies, globalDependencies));

    localDependencies = new ArrayList<>();
    localDependencies.add("test2::third");
    globalDependencies = new ArrayList<>();
    dependencies.add(
      new KaramelFileDeps("test2::another", localDependencies, globalDependencies));

    KaramelFile karamelFile = new KaramelFile();
    karamelFile.setDependencies(dependencies);

    KaramelizedCookbook karamelizedCookbook =
      new KaramelizedCookbook(testMetadataRb, karamelFile);
    CookbookCache.getInstance().addToCache("test2", karamelizedCookbook);

    provisionNoopNodes();

    List<Recipe> recipesList = new ArrayList<>();
    recipesList.add(new Recipe(karamelizedCookbook, "test2::default"));
    recipesList.add(new Recipe(karamelizedCookbook, "test2::another"));
    recipesList.add(new Recipe(karamelizedCookbook, "test2::third"));
    clusterContext.getCluster().getGroups().get(0).setRecipes(recipesList);

    Dag dag = dagFactory.buildDag(baseCluster, new Settings(), dataBagsFactory);
    assertFalse(dag.hasCycle());
  }
}
