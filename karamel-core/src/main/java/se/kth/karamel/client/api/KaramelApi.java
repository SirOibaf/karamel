package se.kth.karamel.client.api;

import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Ec2Credentials;
import se.kth.karamel.common.util.OcciCredentials;
import se.kth.karamel.common.util.NovaCredentials;
import se.kth.karamel.common.util.SshKeyPair;


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
  Cluster getCluster();

  /**
   * Loads Karamel common keys
   *
   * @return
   * @throws KaramelException
   */
  SshKeyPair loadSshKeysIfExist() throws KaramelException;

  /**
   * Loads cluster specific keys
   *
   * @param clusterName
   * @return
   * @throws KaramelException
   */
  SshKeyPair loadSshKeysIfExist(String clusterName) throws KaramelException;

  /**
   * Register ssh keys for the current runtime of karamel
   *
   * @param keypair
   * @return
   * @throws KaramelException
   */
  SshKeyPair registerSshKeys(SshKeyPair keypair) throws KaramelException;

  /**
   * Register ssh keys for the specified cluster
   *
   * @param clusterName
   * @param keypair
   * @return
   * @throws KaramelException
   */
  SshKeyPair registerSshKeys(String clusterName, SshKeyPair keypair) throws KaramelException;

  /**
   * Reads it from default karamel conf file
   *
   * @return
   * @throws KaramelException
   */
  Ec2Credentials loadEc2CredentialsIfExist() throws KaramelException;

  /**
   * Validates user's credentials before starting the cluster
   *
   * @param credentials
   * @return
   * @throws KaramelException
   */
  boolean updateEc2CredentialsIfValid(Ec2Credentials credentials) throws KaramelException;

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
   * @param clusterName
   * @throws KaramelException
   */
  void pauseCluster(String clusterName) throws KaramelException;

  /**
   * It resumes an already paused cluster, machines will go on and run ssh commands.
   *
   * @param clusterName
   * @throws KaramelException
   */
  void resumeCluster(String clusterName) throws KaramelException;

  /**
   * It stops sending new ssh command to machines, destroys the automatic allocated machines and disconnects ssh clients
   * from machines. User, however, shouldn't expect that bare-metal machines be destroyed as well.
   *
   * @param clusterName
   * @throws KaramelException
   */
  void terminateCluster(String clusterName) throws KaramelException;

  /**
   * Returns installation flow DAG that each node is a task assigned to a certain machine with the current status of the
   * task.
   *
   * @param clusterName
   * @return
   * @throws KaramelException
   */
  String getInstallationDag(String clusterName) throws KaramelException;

  /**
   * Register password for Baremetal sudo account
   *
   * @param password
   * @throws KaramelException
   */
  void registerSudoPassword(String password) throws KaramelException;

  String loadGceCredentialsIfExist() throws KaramelException;

  boolean updateGceCredentialsIfValid(String jsonFilePath) throws KaramelException;

  NovaCredentials loadNovaCredentialsIfExist() throws KaramelException;

  boolean updateNovaCredentialsIfValid(NovaCredentials credentials) throws KaramelException;

  OcciCredentials loadOcciCredentialsIfExist() throws KaramelException;

  boolean updateOcciCredentialsIfValid(OcciCredentials credentials) throws KaramelException;
}
