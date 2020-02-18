package se.kth.karamel.client.api;

import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.node.Node;
import se.kth.karamel.core.ClusterService;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Ec2Credentials;
import se.kth.karamel.common.util.SSHKeyPair;
import se.kth.karamel.core.execution.RunRecipeTask;
import se.kth.karamel.core.execution.Task;
import se.kth.karamel.core.node.NodeImpl;
import se.kth.karamel.core.provisioner.jcloud.amazon.Ec2Context;
import se.kth.karamel.core.util.SSHUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * Implementation of the Karamel Api for UI
 */
public class KaramelApiImpl implements KaramelApi {

  private static final Logger logger = Logger.getLogger(KaramelApiImpl.class);

  private ClusterService clusterService = ClusterService.getInstance();
  private SSHUtil sshUtil = new SSHUtil();

  @Override
  public void loadClusterDefinition(String clusterDefinition) throws KaramelException {
    Yaml yaml = new Yaml(new Constructor(Cluster.class));
    try {
      Cluster cluster = (Cluster) yaml.load(clusterDefinition);
      clusterService.setCurrentCluster(cluster);
    } catch (YAMLException e) {
      throw new KaramelException("Could not parse the cluster definition");
    }
  }

  @Override
  public Cluster getCluster() throws KaramelException {
    return clusterService.getCurrentCluster();
  }

  @Override
  public List<SSHKeyPair> getAvailableSSHKeys() throws KaramelException {
    try {
      return sshUtil.getAvailableKeys();
    } catch (IOException e) {
      throw new KaramelException("Could not find available SSH private keys");
    }
  }

  @Override
  public void registerSshKeys(SSHKeyPair keyPair) throws KaramelException {
    clusterService.registerSSHKeyPair(keyPair);
  }

  @Override
  public void registerSudoPassword(String password) {
    clusterService.registerSudoAccountPassword(password);
  }

  @Override
  public void setEc2CredentialsIfValid(Ec2Credentials credentials) throws KaramelException {
    clusterService.registerEc2Context(new Ec2Context(credentials));
  }

  @Override
  public void startCluster() throws KaramelException {
    try {
      clusterService.startCluster();
    } catch (IOException e) {
      throw new KaramelException(e);
    }
  }

  @Override
  public void terminateCluster() throws KaramelException {
    clusterService.terminateCluster();
  }

  @Override
  public void pause(Integer taskId, Integer nodeId, String groupStr) throws KaramelException {
    if (taskId != null) {
      // Doesn't matter which implementation as it's not used in the comparison
      Task task = new RunRecipeTask(taskId);
      clusterService.pauseTask(task);
    } else if (nodeId != null) {
      Node node = new NodeImpl(nodeId);
      clusterService.pauseNode(node);
    } else if (!Strings.isNullOrEmpty(groupStr)) {
      Group group = new Group(groupStr);
      clusterService.pauseGroup(group);
    } else {
      clusterService.pauseDag();
    }
  }

  @Override
  public void resume(Integer taskId, Integer nodeId, String groupStr) throws KaramelException {
    if (taskId != null) {
      // Doesn't matter which implementation as it's not used in the comparison
      Task task = new RunRecipeTask(taskId);
      clusterService.resumeTask(task);
    } else if (nodeId != null) {
      Node node = new NodeImpl(nodeId);
      clusterService.resumeNode(node);
    } else if (!Strings.isNullOrEmpty(groupStr)) {
      Group group = new Group(groupStr);
      clusterService.resumeGroup(group);
    } else {
      clusterService.resumeDag();
    }
  }

  @Override
  public void skip(Integer taskId, Integer nodeId, String groupStr) throws KaramelException {
    if (taskId != null) {
      // Doesn't matter which implementation as it's not used in the comparison
      Task task = new RunRecipeTask(taskId);
      clusterService.skipTask(task);
    } else if (nodeId != null) {
      Node node = new NodeImpl(nodeId);
      clusterService.skipNode(node);
    } else if (!Strings.isNullOrEmpty(groupStr)) {
      Group group = new Group(groupStr);
      clusterService.skipGroup(group);
    }
  }

  @Override
  public void retry(Integer taskId, Integer nodeId, String groupStr) throws KaramelException {
    if (taskId != null) {
      // Doesn't matter which implementation as it's not used in the comparison
      Task task = new RunRecipeTask(taskId);
      clusterService.retryTask(task);
    } else if (nodeId != null) {
      Node node = new NodeImpl(nodeId);
      clusterService.retryNode(node);
    } else if (!Strings.isNullOrEmpty(groupStr)) {
      Group group = new Group(groupStr);
      clusterService.retryGroup(group);
    }
  }

  @Override
  public Map<Node, List<Task>> getClusterDeploymentStatus() {
    return clusterService.getClusterDeploymentStatus();
  }

  @Override
  public List<Task> getNodeDeploymentStatus(Node node) {
    return null;
  }

  @Override
  public String getLogs(int taskId) throws KaramelException {
    Task task = clusterService.getTask(taskId);
    if (task.getTaskOutputReader() == null) {
      throw new KaramelException("No logs available for task with id: " + taskId);
    }
    return task.getTaskOutputReader().toString();
  }
}
