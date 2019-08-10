package se.kth.karamel.core;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.provisioner.jcloud.amazon.Ec2Context;
import se.kth.karamel.core.provisioner.jcloud.google.GceContext;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.util.SshKeyPair;

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

  public synchronized Cluster getCurrentCluster() {
    return clusterContext.getCluster();
  }

  public synchronized void registerSudoAccountPassword(String password) {
    clusterContext.setSudoPassword(password);
  }

  public synchronized void registerEc2Context(Ec2Context ec2Context) throws KaramelException {
    clusterContext.setEc2Context(ec2Context);
  }

  public synchronized Ec2Context getEc2Context() {
    return clusterContext.getEc2Context();
  }

  public synchronized void registerGceContext(GceContext gceContext) {
    clusterContext.setGceContext(gceContext);
  }

  public synchronized void registerSshKeyPair(SshKeyPair sshKeyPair) throws KaramelException {
    File pubKey = new File(sshKeyPair.getPublicKeyPath());
    if (!pubKey.exists()) {
      throw new KaramelException("Could not find public key: " + sshKeyPair.getPublicKeyPath());
    }
    File privKey = new File(sshKeyPair.getPrivateKeyPath());
    if (!privKey.exists()) {
      throw new KaramelException("Could not find private key: " + sshKeyPair.getPrivateKeyPath());
    }

    clusterContext.setSshKeyPair(sshKeyPair);
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
    deploymentManager.pause();
    LOGGER.log(Level.INFO, "Deployment paused");
  }

  public synchronized void resumeDag() throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.resume();
    LOGGER.log(Level.INFO, "Deployment resumed");
  }

  public synchronized void terminateCluster() throws KaramelException {
    if (deploymentManager == null) {
      throw new KaramelException("No cluster is running at the moment");
    }
    deploymentManager.terminate();
    LOGGER.log(Level.INFO, "Deployment terminated");
  }
}
