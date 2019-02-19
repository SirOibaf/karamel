package se.kth.karamel.backend.running.model.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import se.kth.karamel.backend.converter.ChefJsonGenerator;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.clusterdef.Recipe;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.launcher.amazon.InstanceType;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.clusterdef.Ec2;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.DagConstructionException;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.cookbookmeta.KaramelFileYamlDeps;
import se.kth.karamel.common.util.Confs;

/**
 *
 * @author kamal
 */
public class DagBuilder {

  private static final Logger logger = Logger.getLogger(DagBuilder.class);

  /**
   * 1. Machine-level tasks such as: - AptGetEssential - PrepareStorage - InstallBerkshelf - MakeSoloRb 2.Cookbook-level
   * tasks such as: - CloneAndVendorCookbook - RunRecipeTask for purge
   *
   * @param cluster
   * @param clusterEntity
   * @param clusterStats
   * @param submitter
   * @param chefJsons
   * @return
   * @throws KaramelException
   */
  public static Dag getPurgingDag(Cluster cluster, ClusterRuntime clusterEntity, ClusterStats clusterStats,
                                  TaskSubmitter submitter, Map<String, JsonObject> chefJsons) throws KaramelException {
    Dag dag = new Dag();
    Map<String, RunRecipeTask> allRecipeTasks = new HashMap<>();
    machineLevelTasks(cluster, clusterEntity, clusterStats, submitter, dag);
    cookbookLevelPurgingTasks(cluster, clusterEntity, clusterStats, chefJsons, submitter, allRecipeTasks, dag);
    return dag;
  }

  /**
   * 1. Machine-level tasks such as: - AptGetEssential - PrepareStorage - InstallBerkshelf - MakeSoloRb 2.Cookbook-level
   * tasks such as: - CloneAndVendorCookbook - RunRecipeTask for Install 3.Recipe-level tasks such as: - RunRecipe tasks
   * for all recipes except install 4.Applies dependencies that are defined in the Karamelfile
   *
   * @param cluster
   * @param clusterEntity
   * @param clusterStats
   * @param submitter
   * @param chefJsons
   * @return
   * @throws KaramelException
   */
  public static Dag getInstallationDag(Cluster cluster, ClusterRuntime clusterEntity, ClusterStats clusterStats,
                                       TaskSubmitter submitter, Map<String, JsonObject> chefJsons)
      throws KaramelException {
    Dag dag = new Dag();
    Map<String, RunRecipeTask> allRecipeTasks = new HashMap<>();
    machineLevelTasks(cluster, clusterEntity, clusterStats, submitter, dag);
    cookbookLevelInstallationTasks(cluster, clusterEntity, clusterStats, chefJsons, submitter, allRecipeTasks, dag);
    Map<String, Map<String, Task>> rlts = recipeLevelTasks(cluster, clusterEntity, clusterStats, chefJsons, submitter,
        allRecipeTasks, dag);
    updateKaramelDependencies(allRecipeTasks, dag, rlts);
    return dag;
  }

  private static boolean updateKaramelDependencies(Map<String, RunRecipeTask> allRecipeTasks, Dag dag,
      Map<String, Map<String, Task>> rlts) throws KaramelException {
    boolean newDepFound = false;
    HashSet<String> cbids = new HashSet<>();
    for (RunRecipeTask task : allRecipeTasks.values()) {
      cbids.add(task.getCookbookId());
    }
    CookbookCache cache = CookbookCache.getInstance();
    for (RunRecipeTask task : allRecipeTasks.values()) {
      String tid = task.uniqueId();
      KaramelizedCookbook kcb = cache.get(task.getCookbookId());
      KaramelFileYamlDeps dependency = kcb.getKaramelFile().getDependency(task.getRecipeCanonicalName());
      if (dependency != null) {
        for (String depRec : dependency.getLocal()) {
          String depId = RunRecipeTask.makeUniqueId(task.getMachineId(), depRec);
          newDepFound |= dag.addDependency(depId, tid);
        }

        for (String depRec : dependency.getGlobal()) {
          Map<String, Task> rlt2 = rlts.get(depRec);
          if (rlt2 != null) {
            for (Map.Entry<String, Task> entry : rlt2.entrySet()) {
              Task t7 = entry.getValue();
              newDepFound |= dag.addDependency(t7.uniqueId(), tid);
            }
          }
        }
      }
    }
    return newDepFound;
  }

  /**
   * Creates all recipe tasks for cluster and groups them by recipe-name. In other words, by having a recipe-name such
   * as hadoop::dn you fetch all the tasks in the cluster that are running hadoop::dn. recipeName -> taskid(recipe +
   * machineid) -> task
   *
   * @param cluster
   * @param clusterEntity
   * @param clusterStats
   * @param chefJsons
   * @param submitter
   * @param allRecipeTasks
   * @param dag
   * @return
   * @throws KaramelException
   */
  public static Map<String, Map<String, Task>> recipeLevelTasks(Cluster cluster, ClusterRuntime clusterEntity,
                                                                ClusterStats clusterStats,
                                                                Map<String, JsonObject> chefJsons,
                                                                TaskSubmitter submitter,
                                                                Map<String, RunRecipeTask> allRecipeTasks, Dag dag)
      throws KaramelException {
    Map<String, Map<String, Task>> map = new HashMap<>();
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      Group jg = UserClusterDataExtractor.findGroup(cluster, ge.getName());
      for (MachineRuntime me : ge.getMachines()) {
        for (Recipe rec : jg.getRecipes()) {
          JsonObject json1 = chefJsons.get(me.getId() + rec.getCanonicalName());
          addRecipeTaskForMachineIntoRecipesMap(rec.getCanonicalName(), me, clusterStats, map, json1, submitter,
              rec.getCookbook().getCookbookName(), allRecipeTasks, dag, cluster.getCookbooks());
        }
      }
    }
    return map;
  }

  /*
   * Makes sure recipe-task for machine exists both in the DAG and in the grouping map of recipes
   */
  private static RunRecipeTask addRecipeTaskForMachineIntoRecipesMap(String recipeName, MachineRuntime machine,
      ClusterStats clusterStats, Map<String, Map<String, Task>> map, JsonObject chefJson, TaskSubmitter submitter,
      String cookbookId, Map<String, RunRecipeTask> allRecipeTasks, Dag dag,
      Map<String, Cookbook> rootCookbooks) throws DagConstructionException {

    RunRecipeTask t1 = makeRecipeTaskIfNotExist(recipeName, machine, clusterStats, chefJson, submitter, cookbookId,
        allRecipeTasks, dag, rootCookbooks);

    Map<String, Task> map1 = map.get(recipeName);
    if (map1 == null) {
      map1 = new HashMap<>();
      map.put(recipeName, map1);
    }
    map1.put(t1.uniqueId(), t1);
    return t1;
  }

  /*
   * Finds recipe task for machine if it has been already created otherwise makes a new one and adds it into the DAG
   */
  private static RunRecipeTask makeRecipeTaskIfNotExist(String recipeName, MachineRuntime machine,
      ClusterStats clusterStats, JsonObject chefJson,
      TaskSubmitter submitter, String cookbookId, Map<String, RunRecipeTask> allRecipeTasks,
      Dag dag, Map<String, Cookbook> rootCookbooks) throws DagConstructionException {

    String recId = RunRecipeTask.makeUniqueId(machine.getId(), recipeName);
    RunRecipeTask runRecipeTask = allRecipeTasks.get(recId);
    if (!allRecipeTasks.containsKey(recId)) {
      ChefJsonGenerator.addRunListForRecipe(chefJson, recipeName);
      GsonBuilder builder = new GsonBuilder();
      builder.disableHtmlEscaping();
      Gson gson = builder.setPrettyPrinting().create();
      String jsonString = gson.toJson(chefJson);
      runRecipeTask
          = new RunRecipeTask(machine, clusterStats, recipeName, jsonString,
              submitter, cookbookId, rootCookbooks);
      dag.addTask(runRecipeTask);
    }
    allRecipeTasks.put(recId, runRecipeTask);
    return runRecipeTask;
  }

  /**
   * machine -> taskid -> task
   *
   * @param cluster
   * @param clusterEntity
   * @param clusterStats
   * @param chefJsons
   * @param submitter
   * @param allRecipeTasks
   * @param dag
   * @param rootCookbooks
   * @return
   * @throws KaramelException
   */
  public static Map<String, Map<String, Task>> cookbookLevelPurgingTasks(Cluster cluster,
                                                                         ClusterRuntime clusterEntity,
                                                                         ClusterStats clusterStats,
                                                                         Map<String, JsonObject> chefJsons,
                                                                         TaskSubmitter submitter,
                                                                         Map<String, RunRecipeTask> allRecipeTasks,
                                                                         Dag dag)
      throws KaramelException {

    Map<String, Map<String, Task>> map = new HashMap<>();
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      Group jg = UserClusterDataExtractor.findGroup(cluster, ge.getName());
      for (MachineRuntime me : ge.getMachines()) {
        Map<String, Task> map1 = new HashMap<>();

        for (Map.Entry<String, Cookbook> cb : cluster.getCookbooks().entrySet()) {
          VendorCookbookTask t1 = new VendorCookbookTask(me, clusterStats, submitter,
              Settings.REMOTE_COOKBOOKS_PATH(me.getSshUser()), cb.getKey(), cb.getValue());

          dag.addTask(t1);
          map1.put(t1.uniqueId(), t1);
        }

        for (KaramelizedCookbook kbc : jg.getCookbooks()) {
          String recipeName = kbc.getCookbookName() + Settings.COOKBOOK_DELIMITER + Settings.PURGE_RECIPE;
          JsonObject json = chefJsons.get(me.getId() + recipeName);
          RunRecipeTask t2 = makeRecipeTaskIfNotExist(recipeName, me, clusterStats, json, submitter,
              kbc.getCookbookName(), allRecipeTasks, dag, cluster.getCookbooks());
          map1.put(t2.uniqueId(), t2);
        }
        logger.debug(String.format("Cookbook-level tasks for the machine '%s' in the group '%s' are: %s",
            me.getPublicIp(), ge.getName(), map1.keySet()));
        if (map.get(me.getId()) != null) {
          map.get(me.getId()).putAll(map1);
        } else {
          map.put(me.getId(), map1);
        }
      }
    }
    return map;
  }

  /**
   * machine -> taskid -> task
   *
   * @param cluster
   * @param clusterEntity
   * @param clusterStats
   * @param chefJsons
   * @param submitter
   * @param allRecipeTasks
   * @param dag
   * @param rootCookbooks
   * @return
   * @throws KaramelException
   */
  static Map<String, Map<String, Task>> cookbookLevelInstallationTasks(Cluster cluster,
                                                                       ClusterRuntime clusterEntity,
                                                                       ClusterStats clusterStats,
                                                                       Map<String, JsonObject> chefJsons,
                                                                       TaskSubmitter submitter,
                                                                       Map<String, RunRecipeTask> allRecipeTasks,
                                                                       Dag dag)
      throws KaramelException {
    Map<String, Map<String, Task>> map = new HashMap<>();
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      Group jg = UserClusterDataExtractor.findGroup(cluster, ge.getName());
      for (MachineRuntime me : ge.getMachines()) {
        Map<String, Task> map1 = new HashMap<>();

        for (Map.Entry<String, Cookbook> cb : cluster.getCookbooks().entrySet()) {
          VendorCookbookTask t1 = new VendorCookbookTask(me, clusterStats, submitter,
              Settings.REMOTE_COOKBOOKS_PATH(me.getSshUser()), cb.getKey(), cb.getValue());
          dag.addTask(t1);
          map1.put(t1.uniqueId(), t1);
        }

        for (KaramelizedCookbook kcb : jg.getCookbooks()) {
          String recipeName = kcb.getCookbookName() + Settings.COOKBOOK_DELIMITER + Settings.INSTALL_RECIPE;
          JsonObject json = chefJsons.get(me.getId() + recipeName);
          RunRecipeTask t2 = makeRecipeTaskIfNotExist(recipeName, me, clusterStats,
              json, submitter, kcb.getCookbookName(), allRecipeTasks, dag, cluster.getCookbooks());
          map1.put(t2.uniqueId(), t2);
        }
        logger.debug(String.format("Cookbook-level tasks for the machine '%s' in the group '%s' are: %s",
            me.getPublicIp(), ge.getName(), map1.keySet()));
        if (map.get(me.getId()) != null) {
          map.get(me.getId()).putAll(map1);
        } else {
          map.put(me.getId(), map1);
        }
      }
    }
    return map;
  }

  /**
   * Tasks that are machine specific, specifically those that are run in the very start preparation phase. For example:
   * - AptGetEssential - PrepareStorage - InstallBerkshelf - MakeSoloRb
   *
   * @param cluster
   * @param clusterEntity
   * @param clusterStats
   * @param submitter
   * @param dag
   * @throws KaramelException
   */
  public static void machineLevelTasks(Cluster cluster, ClusterRuntime clusterEntity, ClusterStats clusterStats,
                                       TaskSubmitter submitter, Dag dag) throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    String prepStoragesConf = confs.getProperty(Settings.PREPARE_STORAGES_KEY);
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      for (MachineRuntime me : ge.getMachines()) {

        String vendorPath = UserClusterDataExtractor.makeVendorPath(me.getSshUser(), cluster.getCookbooks());
        FindOsTypeTask findOs = new FindOsTypeTask(me, clusterStats, submitter);
        dag.addTask(findOs);

        Provider provider = UserClusterDataExtractor.getGroupProvider(cluster, ge.getName());
        boolean storagePreparation = (prepStoragesConf != null && prepStoragesConf.equalsIgnoreCase("true")
            && (provider instanceof Ec2));
        if (storagePreparation) {
          String model = ((Ec2) provider).getType();
          InstanceType instanceType = InstanceType.valueByModel(model);
          PrepareStoragesTask st
              = new PrepareStoragesTask(me, clusterStats, submitter, instanceType.getStorageDevices());
          dag.addTask(st);
        }

        AptGetEssentialsTask t1 = new AptGetEssentialsTask(me, clusterStats, submitter, storagePreparation);
        InstallChefdkTask t2 = new InstallChefdkTask(me, clusterStats, submitter);
        MakeSoloRbTask t3 = new MakeSoloRbTask(me, vendorPath, clusterStats, submitter);
        dag.addTask(t1);
        dag.addTask(t2);
        dag.addTask(t3);
      }
    }
  }

}
