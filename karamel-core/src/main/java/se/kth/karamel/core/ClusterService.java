package se.kth.karamel.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.node.Node;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.execution.Task;
import se.kth.karamel.core.provisioner.jcloud.amazon.Ec2Context;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.util.SSHKeyPair;

/**
 * Keeps repository of running clusters with a unique name for each. Privacy sensitive data such as credentials is
 * stored inside a context.
 */
// TODO(Fabio) this class is total BS. We run 1 cluster at the time
// First you learn to walk then you run.
public class ClusterService {

  private final Logger LOGGER = Logger.getLogger(ClusterService.class);

  private ClusterContext clusterContext = null;
  private DeploymentManager deploymentManager = null;

  private static ClusterService instance = null;
  public synchronized static ClusterService getInstance() {
    if (instance == null) {
      instance = new ClusterService();
    }
    return instance;
  }

  public synchronized void setCurrentCluster(Cluster currentCluster) {
    clusterContext = new ClusterContext(currentCluster);
  }

  public synchronized Cluster getCurrentCluster() throws KaramelException{
    if (clusterContext == null) {
      throw new KaramelException("Please upload a cluster definition first");
    }
    return clusterContext.getCluster();
  }

  public synchronized void registerSudoAccountPassword(String password) {
    clusterContext.setSudoPassword(password);
  }

  public synchronized void registerEc2Context(Ec2Context ec2Context) throws KaramelException {
    clusterContext.setEc2Context(ec2Context);
  }

  public synchronized void registerSSHKeyPair(SSHKeyPair sshKeyPair) throws KaramelException {
    File privKey = new File(sshKeyPair.getPrivateKeyPath());
    if (!privKey.exists()) {
      throw new KaramelException("Could not find private key: " + sshKeyPair.getPrivateKeyPath());
    }

    clusterContext.setSSHKeyPair(sshKeyPair);
  }

  public synchronized void startCluster() throws KaramelException, IOException {
    if (deploymentManager != null) {
      throw new KaramelException("Karamel is already deploying the cluster");
    }

    deploymentManager = new DeploymentManager(new Settings());
    deploymentManager.deploy(clusterContext);
    LOGGER.log(Level.INFO, "Deployment started");
  }

  public synchronized void pauseDag() throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.pauseDag();
    LOGGER.log(Level.INFO, "Deployment paused");
  }

  // TODO(Fabio): validate if task/node/group is available
  public synchronized void pauseTask(Task task) throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.pause(task);
    LOGGER.log(Level.INFO, String.format("NodeId %s Deployment paused", task.getTaskId()));
  }

  public synchronized void pauseNode(Node node) throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.pause(node);
    LOGGER.log(Level.INFO, String.format("NodeId %s Deployment paused", node.getNodeId()));
  }

  public synchronized void pauseGroup(Group group) throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.pause(group);
    LOGGER.log(Level.INFO, String.format("Group %s Deployment paused", group.getName()));
  }

  public synchronized void resumeDag() throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.resumeDag();
    LOGGER.log(Level.INFO, "Deployment resumed");
  }

  public synchronized void resumeTask(Task task) throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.resume(task);
    LOGGER.log(Level.INFO, String.format("NodeId %s Deployment resumed", task.getTaskId()));
  }

  public synchronized void resumeNode(Node node) throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.resume(node);
    LOGGER.log(Level.INFO, String.format("NodeId %s Deployment resumed", node.getNodeId()));
  }

  public synchronized void resumeGroup(Group group) throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.resume(group);
    LOGGER.log(Level.INFO, String.format("Group %s Deployment resumed", group.getName()));
  }

  public synchronized void terminateCluster() throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.terminateDag();
    LOGGER.log(Level.INFO, "Deployment terminated");
  }

  public synchronized void skipTask(Task task) throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.skip(task);
    LOGGER.log(Level.INFO, String.format("NodeId %s Deployment skipped", task.getTaskId()));
  }

  public synchronized void skipNode(Node node) throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.skip(node);
    LOGGER.log(Level.INFO, String.format("NodeId %s Deployment skipped", node.getNodeId()));
  }

  public synchronized void skipGroup(Group group) throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.skip(group);
    LOGGER.log(Level.INFO, String.format("Group %s Deployment skipped", group.getName()));
  }

  public synchronized void retryTask(Task task) throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.retry(task);
    LOGGER.log(Level.INFO, String.format("NodeId %s Deployment retried", task.getTaskId()));
  }

  public synchronized void retryNode(Node node) throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.retry(node);
    LOGGER.log(Level.INFO, String.format("NodeId %s Deployment retried", node.getNodeId()));
  }

  public synchronized void retryGroup(Group group) throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.retry(group);
    LOGGER.log(Level.INFO, String.format("Group %s Deployment retried", group.getName()));
  }

  public Map<Node, List<Task>> getClusterDeploymentStatus() {
    return deploymentManager.getDeploymentStatus();
  }

  public List<Task> getNodeDeploymentStatus(Node node) {
    return deploymentManager.getNodeDeployment(node);
  }

  public Task getTask(int taskId) throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    return deploymentManager.getDag().getTaskList().stream()
      .filter(task -> task.getTaskId() == taskId)
      .findFirst()
      .orElseThrow(() -> new KaramelException("Cannot find task with id: " + taskId));
  }
}
