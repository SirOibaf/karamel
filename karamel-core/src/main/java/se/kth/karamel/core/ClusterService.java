package se.kth.karamel.core;

import java.io.File;
import org.apache.log4j.Logger;
import se.kth.karamel.core.provisioner.jcloud.amazon.Ec2Context;
import se.kth.karamel.core.provisioner.jcloud.google.GceContext;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.util.SshKeyService;

/**
 * Keeps repository of running clusters with a unique name for each. Privacy sensitive data such as credentials is
 * stored inside a context. There is a common context with shared values between clusters and each cluster has its own
 * context inside which values can be overwritten.
 */
// TODO(Fabio) this class is total BS. We run 1 cluster at the time
// First you learn to walk then you run.
public class ClusterService {

  private final Logger logger = Logger.getLogger(ClusterService.class);
  private ClusterDefinitionService clusterDefinitionService = new ClusterDefinitionService();

  private ClusterContext clusterContext = new ClusterContext();

  private Cluster currentCluster = null;

  private static ClusterService instance = null;
  public static ClusterService getInstance() {
    if (instance == null) {
      instance = new ClusterService();
    }
    return instance;
  }

  public synchronized void setCurrentCluster(Cluster currentCluster) {
    this.currentCluster = currentCluster;
  }

  public synchronized Cluster getCurrentCluster() {
    return this.currentCluster;
  }

  public synchronized void registerSudoAccountPassword(String password) {
    clusterContext.setSudoPassword(password);
  }

  public synchronized String getSudoAccountPassword() {
    return clusterContext.getSudoPassword();
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
    sshKeyPair.setNeedsPassword(SshKeyService.checkIfPasswordNeeded(sshKeyPair));

    clusterContext.setSshKeyPair(sshKeyPair);
  }

  public synchronized SshKeyPair getSshKeyPair() {
    return clusterContext.getSshKeyPair();
  }

  public synchronized void startCluster() throws KaramelException {
  }

  public synchronized void submitInstallationDag() throws KaramelException {
  }

  public synchronized void submitPurgeDag() throws KaramelException {
  }

  public synchronized void pauseDag() throws KaramelException {
  }

  public synchronized void resumeDag() throws KaramelException {
  }

  public synchronized void terminateCluster() throws KaramelException {
  }
}
