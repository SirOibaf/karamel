package se.kth.karamel.core;

import lombok.Getter;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.node.Node;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.chef.DataBagsFactory;
import se.kth.karamel.core.dag.Dag;
import se.kth.karamel.core.dag.DagFactory;
import se.kth.karamel.core.execution.ExecutionEngine;
import se.kth.karamel.core.execution.Task;
import se.kth.karamel.core.execution.TaskStatus;
import se.kth.karamel.core.provisioner.Provisioner;
import se.kth.karamel.core.provisioner.ProvisionerFactory;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeploymentManager {

  private Settings settings;

  @Getter
  private Dag dag;
  @Getter
  private ExecutionEngine executionEngine;

  private DagFactory dagFactory = null;

  public DeploymentManager(Settings settings) {
    this.settings = settings;
    this.executionEngine = new ExecutionEngine(settings);
  }

  /**
   * This function is the entrypoint for a cluster deployment
   * There are several steps:
   *  - Provision the HW
   *  - Build the DAG
   *  - Executing the DAG
   * @throws KaramelException
   */
  // TODO(Fabio): this should probably be a separate thread.
  public void deploy(ClusterContext clusterContext) throws KaramelException {

    // Provision HW
    Provisioner provisioner = ProvisionerFactory.getProvisioner(settings);
    int numNodes = 0;
    for (Group group : clusterContext.getCluster().getGroups()) {
      numNodes += provisioner.provisionGroup(clusterContext, clusterContext.getCluster(), group, numNodes);
    }

    // Build the DAG
    dagFactory = new DagFactory();
    try {
      dag = dagFactory.buildDag(clusterContext.getCluster(), settings,
          new DataBagsFactory(clusterContext.getCluster()));
    } catch (IOException e) {
      throw new KaramelException("Could not build the dag", e);
    }

    // Execute the DAG
    executionEngine.execute(dag, numNodes);
  }

  // TODO(Fabio): change the pause dag with the same concept of the below
  public void pauseDag() {
    executionEngine.pause();
  }

  public void pause(Task task) {
    setTasksStatus(filterTask(task), TaskStatus.PAUSED);
  }

  public void pause(Node node) {
    setTasksStatus(filterNode(node), TaskStatus.PAUSED);
  }

  public void pause(Group group) {
    setTasksStatus(filterGroup(group), TaskStatus.PAUSED);
  }

  public void resume(Task task) {
    setTasksStatus(filterTask(task), TaskStatus.WAITING);
  }

  public void resume(Node node) {
    setTasksStatus(filterNode(node), TaskStatus.WAITING);
  }

  public void resume(Group group) {
    setTasksStatus(filterGroup(group), TaskStatus.WAITING);
  }

  // TODO(Fabio): here we should make sure we unlock the thread
  public void skip(Task task) {
    setTasksStatus(filterTask(task), TaskStatus.SKIPPED);
  }

  public void skip(Node node) {
    setTasksStatus(filterNode(node), TaskStatus.SKIPPED);
  }

  public void skip(Group group) {
    setTasksStatus(filterGroup(group), TaskStatus.SKIPPED);
  }

  // TODO(Fabio): here we should make sure we unlock the thread
  public void retry(Task task) {
    setTasksStatus(filterTask(task), TaskStatus.WAITING);
  }

  public void retry(Node node) {
    setTasksStatus(filterNode(node), TaskStatus.WAITING);
  }

  public void retry(Group group) {
    setTasksStatus(filterGroup(group), TaskStatus.WAITING);
  }

  public Stream<Task> filterTask(Task task) {
    return dag.getTaskList().stream().filter(t -> t.equals(task));
  }

  public Stream<Task> filterNode(Node node) {
    return dagFactory.getNodeToRecipeMap().get(node).values().stream()
      .filter(task -> task.getTaskStatus() == TaskStatus.WAITING);
  }

  public Stream<Task> filterGroup(Group group) {
    return dagFactory.getNodeToRecipeMap().entrySet().stream()
      .filter(e -> e.getKey().getGroup().equals(group))
      .flatMap(e -> e.getValue().values().stream())
      .filter(task -> task.getTaskStatus() == TaskStatus.WAITING);
  }

  private void setTasksStatus(Stream<Task> tasks, TaskStatus taskStatus) {
    tasks.forEach(task -> task.setTaskStatus(taskStatus));
  }

  public void resumeDag() {
    executionEngine.resume();
  }

  public void terminateDag() {
    executionEngine.terminate();
  }

  public Map<Node, List<Task>> getDeploymentStatus() {
    return dagFactory.getNodeToRecipeMap().entrySet().stream()
      .map(e -> new AbstractMap.SimpleEntry<Node, List<Task>>(e.getKey(), new ArrayList<>(e.getValue().values())))
      .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
  }

  public List<Task> getNodeDeployment(Node node) {
    return new ArrayList<>(dagFactory.getNodeToRecipeMap().get(node).values());
  }
}
