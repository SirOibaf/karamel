package se.kth.karamel.client.api;

import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.node.Node;
import se.kth.karamel.common.util.Ec2Credentials;
import se.kth.karamel.common.util.SSHKeyPair;
import se.kth.karamel.core.execution.Task;

import java.util.List;
import java.util.Map;


/**
 * The main API of Karamel-Core for Karamel clients
 *
 */
public interface KaramelApi {

  /**
   * Load a cluster definition in YAML format, fetches and validates the cookbooks
   *
   * @param clusterDefinition
   * @throws KaramelException
   */
  void loadClusterDefinition(String clusterDefinition) throws KaramelException;

  /**
   * Get current cluster definition
   */
  Cluster getCluster() throws KaramelException;

  /**
   * Loads Karamel common keys
   *
   * @return
   * @throws KaramelException
   */
  List<SSHKeyPair> getAvailableSSHKeys() throws KaramelException;

  /**
   * Register ssh keys for the current runtime of karamel
   *
   * @param keypair
   * @return
   * @throws KaramelException
   */
  void registerSshKeys(SSHKeyPair keypair) throws KaramelException;

  /**
   * Validates user's credentials before starting the cluster
   *
   * @param credentials
   * @return
   * @throws KaramelException
   */
  void setEc2CredentialsIfValid(Ec2Credentials credentials) throws KaramelException;

  /**
   * Register password for Baremetal sudo account
   *
   * @param password
   * @throws KaramelException
   */
  void registerSudoPassword(String password) throws KaramelException;

  /**
   * Starts running the cluster by launching machines and installing software
   *
   * @throws KaramelException
   */
  void startCluster() throws KaramelException;

  void terminateCluster() throws KaramelException;

  void pause(Integer task, Integer nodeId, String group) throws KaramelException;

  void resume(Integer task, Integer nodeId, String group) throws KaramelException;

  void retry(Integer task, Integer nodeId, String group) throws KaramelException;

  void skip(Integer task, Integer nodeId, String group) throws KaramelException;

  /**
   * A list of tasks grouped by group (name only) and nodes
   * @return
   */
  Map<Node, List<Task>> getClusterDeploymentStatus();

  List<Task> getNodeDeploymentStatus(Node node);

  String getLogs(int task) throws KaramelException;
}
