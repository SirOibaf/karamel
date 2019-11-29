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

  /**
   * In case user wants to pause the running cluster for inspection reasons. It implies that machines won't receive any
   * new ssh command form the karamel-core. User can either terminate or resume a paused cluster.
   *
   * @throws KaramelException
   */
  void pauseCluster() throws KaramelException;

  /**
   * It resumes an already paused cluster, machines will go on and run ssh commands.
   *
   * @throws KaramelException
   */
  void resumeCluster() throws KaramelException;

  /**
   * It stops sending new ssh command to machines, destroys the automatic allocated machines and disconnects ssh clients
   * from machines. User, however, shouldn't expect that bare-metal machines be destroyed as well.
   *
   * @throws KaramelException
   */
  void terminateCluster() throws KaramelException;

  Map<Node, List<Task>> getClusterDeploymentStatus();

  List<Task> getNodeDeploymentStatus(Node node);
}
