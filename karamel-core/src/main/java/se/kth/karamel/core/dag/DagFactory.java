package se.kth.karamel.core.dag;

import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.clusterdef.Recipe;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.cookbookmeta.KaramelFileDeps;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
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
  Map<Recipe, List<Task>> recipeToTasksMap = new HashMap<>();

  // Useful for local dependencies
  Map<Recipe, List<Node>> recipeToNodesMap = new HashMap<>();
  Map<Node, Map<Recipe, Task>> nodeToRecipeMap = new HashMap<>();

  public Dag buildDag(Cluster cluster, Settings settings, DataBagsFactory dbFactory) throws IOException {
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

    return dag;
  }

  private void addNodeSetupTasks(Dag dag, Cluster cluster, Settings settings) {
    cluster.getGroups()
        .stream().flatMap(g -> g.getProvider().getNodes().stream())
        .map(node -> new NodeSetupTask(taskIdProgress++, node, settings))
        .forEach(dag::addTask);
  }

  private void addFetchCookbookTasks(Dag dag, Cluster cluster, Settings settings) throws IOException {
    Path localCookbookPath = Paths.get(CookbookCache.getInstance().getLocalCookbookPath());
    cluster.getGroups()
        .stream().flatMap(g -> g.getProvider().getNodes().stream())
        .map(node -> new FetchCookbooksTask(taskIdProgress++, node, settings, localCookbookPath))
        .forEach(dag::addTask);
  }

  private void addRecipeTasks(Dag dag, Cluster cluster, Settings settings,
                              DataBagsFactory dbFactory) throws IOException {
    for (Group group : cluster.getGroups()) {
      addGroupRecipes(dag, group, settings, dbFactory);
    }
  }

  private void addGroupRecipes(Dag dag, Group group, Settings settings, DataBagsFactory dbFactory) {
    addInstallRecipes(dag, group, settings, dbFactory);

    for (Node node : group.getProvider().getNodes()) {
      for (Recipe recipe : group.getRecipes()) {
        Task runRecipeTask = new RunRecipeTask(taskIdProgress++, node, group, recipe, settings, dbFactory);

        addToTaskCache(recipe, node, runRecipeTask);

        dag.addTask(runRecipeTask);
      }
    }
  }

  private void addInstallRecipes(Dag dag, Group group, Settings settings, DataBagsFactory dbFactory) {
    Set<String> uniqueCookbookNames = group.getRecipes().stream().map(Recipe::getCookbook)
        .map(KaramelizedCookbook::getCookbookName)
        .collect(Collectors.toSet());

    for (Node node : group.getProvider().getNodes()) {
      for (String cookbookName : uniqueCookbookNames) {
        Recipe installRecipe = new Recipe(cookbookName + Constants.COOKBOOK_DELIMITER + Constants.INSTALL_RECIPE);
        Task runRecipeTask = new RunRecipeTask(taskIdProgress++, node, group, installRecipe, settings, dbFactory);

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

    // TODO(Fabio): add local dependencies to setup tasks
    for (KaramelizedCookbook kCookbook : kCookbookSet) {
      addKCookbookDependencies(kCookbook.getKaramelFile().getDependencies());
    }
  }

  private void addKCookbookDependencies(List<KaramelFileDeps> dependencies) {
    for (KaramelFileDeps dependency : dependencies) {
      Recipe recipe = new Recipe(dependency.getRecipe());
      addLocalDependencies(recipe, dependency.getLocal());
      addGlobalDependencies(recipe, dependency.getGlobal());
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
          targetRecipeTask.getDependsOn().add(srcRecipeTask);
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

      // Add all the srcRecipeTasks to each targetRecipeTasks
      targetRecipeTasks.forEach(t -> t.getDependsOn().addAll(srcRecipeTasks));
    }
  }
}
