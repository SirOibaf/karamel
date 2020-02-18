package se.kth.karamel.core.dag;

import lombok.Getter;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.clusterdef.Recipe;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.cookbookmeta.KaramelFileDeps;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.node.Node;
import se.kth.karamel.common.util.Constants;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.chef.DataBagsFactory;
import se.kth.karamel.core.execution.FetchCookbooksTask;
import se.kth.karamel.core.execution.NodeSetupTask;
import se.kth.karamel.core.execution.RunRecipeTask;
import se.kth.karamel.core.execution.Task;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// TODO(Fabio): Add transitive dependencies
public class DagFactory {

  private int taskIdProgress = 0;

  // HashMap for quick task access. Used during the building of the dependency graph.
  // Useful for global dependencies
  private Map<Recipe, List<Task>> recipeToTasksMap = new HashMap<>();

  // Useful for local dependencies
  private Map<Recipe, List<Node>> recipeToNodesMap = new HashMap<>();

  @Getter
  private Map<Node, Map<Recipe, Task>> nodeToRecipeMap = new HashMap<>();

  private static final String NODE_SETUP_RECIPE_NAME = "node::setup";
  private static final String FETCH_COOKBOOKS_RECIPE_NAME = "fetch::cookbooks";

  public Dag buildDag(Cluster cluster, Settings settings, DataBagsFactory dbFactory)
    throws IOException, KaramelException {
    Dag dag = new Dag();

    // 1. Add machine setup tasks. They will be responsible of installing Chefdk,
    // downloading the vendored cookbooks and generating the solo.rb script.
    addNodeSetupTasks(dag, cluster, settings);

    // 2. Add fetch cookbook tasks. They will be responsible of fetching the cookbooks
    addFetchCookbookTasks(dag, cluster, settings);

    // 3. Add recipes tasks
    addRecipeTasks(dag, cluster, settings, dbFactory);

    // 4. Add dependencies between tasks and recipes
    addDependencies(cluster);

    // 5. Check if the dag has circular dependencies
    if (dag.hasCycle()) {
      // TODO(Fabio): Return the cycle to the user
      throw new KaramelException("The DAG has circular dependencies, please fix them");
    }

    return dag;
  }

  private void addNodeSetupTasks(Dag dag, Cluster cluster, Settings settings) {
    cluster.getGroups()
      .stream().flatMap(g -> g.getProvider().getNodes().stream())
      .map(node -> new NodeSetupTask(taskIdProgress++, node, settings))
      .forEach(task -> {
          dag.addTask(task);
          addToTaskCache(new Recipe(NODE_SETUP_RECIPE_NAME), task.getNode(), task);
        });
  }

  private void addFetchCookbookTasks(Dag dag, Cluster cluster, Settings settings)
    throws KaramelException, IOException {
    Path localCookbookPath = Paths.get(CookbookCache.getInstance().getLocalCookbookPath());
    cluster.getGroups()
      .stream().flatMap(g -> g.getProvider().getNodes().stream())
      .map(node -> new FetchCookbooksTask(taskIdProgress++, node, settings, localCookbookPath))
      .forEach(task -> {
          task.getDependsOn().add(nodeToRecipeMap.get(task.getNode()).get(new Recipe(NODE_SETUP_RECIPE_NAME)));
          dag.addTask(task);
          addToTaskCache(new Recipe(FETCH_COOKBOOKS_RECIPE_NAME), task.getNode(), task);
        });
  }

  private void addRecipeTasks(Dag dag, Cluster cluster, Settings settings,
                              DataBagsFactory dbFactory) throws IOException {
    for (Group group : cluster.getGroups()) {
      addGroupRecipes(dag, cluster, group, settings, dbFactory);
    }
  }

  private void addGroupRecipes(Dag dag, Cluster cluster, Group group, Settings settings, DataBagsFactory dbFactory) {
    addInstallRecipes(dag, cluster, group, settings, dbFactory);

    for (Node node : group.getProvider().getNodes()) {
      for (Recipe recipe : group.getRecipes()) {
        Task runRecipeTask = new RunRecipeTask(taskIdProgress++, node, cluster, group, recipe, settings, dbFactory);

        addToTaskCache(recipe, node, runRecipeTask);

        dag.addTask(runRecipeTask);
      }
    }
  }

  private void addInstallRecipes(Dag dag, Cluster cluster, Group group, Settings settings, DataBagsFactory dbFactory) {
    Set<String> uniqueCookbookNames = group.getRecipes().stream().map(Recipe::getCookbook)
      .map(KaramelizedCookbook::getCookbookName)
      .collect(Collectors.toSet());

    for (Node node : group.getProvider().getNodes()) {
      for (String cookbookName : uniqueCookbookNames) {
        Recipe installRecipe = new Recipe(cookbookName + Constants.COOKBOOK_DELIMITER + Constants.INSTALL_RECIPE);
        Task runRecipeTask = new RunRecipeTask(taskIdProgress++, node, cluster,
          group, installRecipe, settings, dbFactory);
        // Add dependencies to fetch cookbooks recipe
        runRecipeTask.getDependsOn().add(nodeToRecipeMap.get(node).get(new Recipe(FETCH_COOKBOOKS_RECIPE_NAME)));
        addToTaskCache(installRecipe, node, runRecipeTask);

        dag.addTask(runRecipeTask);
      }
    }
  }

  private void addToTaskCache(Recipe recipe, Node node, Task task) {
    if (recipeToTasksMap.containsKey(recipe)) {
      recipeToTasksMap.get(recipe).add(task);
    } else {
      List<Task> taskList = new ArrayList<>();
      taskList.add(task);
      recipeToTasksMap.put(recipe, taskList);
    }

    if (recipeToNodesMap.containsKey(recipe)) {
      recipeToNodesMap.get(recipe).add(node);
    } else {
      List<Node> nodeList = new ArrayList<>();
      nodeList.add(node);
      recipeToNodesMap.put(recipe, nodeList);
    }

    if (nodeToRecipeMap.containsKey(node)) {
      nodeToRecipeMap.get(node).put(recipe, task);
    } else {
      Map<Recipe, Task> recipeToTaskMap = new HashMap<>();
      recipeToTaskMap.put(recipe, task);
      nodeToRecipeMap.put(node, recipeToTaskMap);
    }
  }

  private void addDependencies(Cluster cluster) {
    // Collect all the dependencies
    Set<KaramelizedCookbook> kCookbookSet = cluster.getGroups().stream()
      .flatMap(g -> g.getKaramelizedCookbooks().stream())
      .collect(Collectors.toSet());

    // Add install recipe dependencies. If a recipe runs on a node, it has to run after the install recipe
    // from the same cookbook. This is to maintain compatibility with the old Karamel
    // Users can specify more dependencies with install recipes (eg. of other cookbooks) or dependencies
    // between install recipe themselves. These are added in the for loop below
    recipeToTasksMap.entrySet().stream()
      .filter(e ->
        // Test only the first, all the the entries in this map are of the same type.
        e.getValue().get(0) instanceof RunRecipeTask &&
          !e.getKey().getCanonicalName().contains(Constants.INSTALL_RECIPE))
      .flatMap(e -> e.getValue().stream())
      .forEach(task -> {
          RunRecipeTask runTask = (RunRecipeTask) task;
          String installRecipeName = runTask.getRecipe().getCookbook().getCookbookName()
            + Constants.COOKBOOK_DELIMITER
            + Constants.INSTALL_RECIPE;
          task.getDependsOn().add(nodeToRecipeMap.get(task.getNode()).get(new Recipe(installRecipeName)));
        });

    for (KaramelizedCookbook kCookbook : kCookbookSet) {
      addKCookbookDependencies(kCookbook.getKaramelFile().getDependencies());
    }
  }

  private void addKCookbookDependencies(List<KaramelFileDeps> dependencies) {
    for (KaramelFileDeps dependency : dependencies) {
      Recipe recipe = new Recipe(dependency.getRecipe());

      // Karamelfile can contain dependency for a recipe not listed in the cluster definition.
      if (recipeToTasksMap.containsKey(recipe)) {
        if (dependency.getLocal() != null) {
          addLocalDependencies(recipe, dependency.getLocal());
        }
        if (dependency.getGlobal() != null) {
          addGlobalDependencies(recipe, dependency.getGlobal());
        }
      }
    }
  }

  private void addLocalDependencies(Recipe srcRecipe, List<String> localDependencies) {
    // Fetch the list of nodes on which this recipe runs
    List<Node> srcRecipeNodes = recipeToNodesMap.get(srcRecipe);

    // For each targetRecipe check if it runs on the srcRecipe nodes.
    // If it does, add the task dependency
    for (String targetRecipeStr : localDependencies) {
      Recipe targetRecipe = new Recipe(targetRecipeStr);

      for (Node node : srcRecipeNodes) {
        Task targetRecipeTask = nodeToRecipeMap.get(node).get(targetRecipe);

        if (targetRecipeTask != null) {
          // Fetch the local task executing the srcRecipe
          Task srcRecipeTask = nodeToRecipeMap.get(node).get(srcRecipe);
          srcRecipeTask.getDependsOn().add(targetRecipeTask);
        }
      }
    }
  }

  private void addGlobalDependencies(Recipe srcRecipe, List<String> globalDependencies) {
    // Fetch the source recipe tasks
    List<Task> srcRecipeTasks = recipeToTasksMap.get(srcRecipe);

    for (String targetRecipeStr : globalDependencies) {
      Recipe targetRecipe = new Recipe(targetRecipeStr);
      // Fetch the target recipe tasks
      List<Task> targetRecipeTasks = recipeToTasksMap.get(targetRecipe);

      if (targetRecipeTasks != null) {
        // Add all the srcRecipeTasks to each targetRecipeTasks
        srcRecipeTasks.forEach(t -> t.getDependsOn().addAll(targetRecipeTasks));
      }
    }
  }
}
