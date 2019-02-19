package se.kth.karamel.backend;

import java.io.File;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.launcher.amazon.Ec2Context;
import se.kth.karamel.backend.launcher.google.GceContext;
import se.kth.karamel.backend.launcher.nova.NovaContext;
import se.kth.karamel.backend.launcher.novav3.NovaV3Context;
import se.kth.karamel.backend.launcher.occi.OcciContext;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.util.SshKeyService;

/**
 * Keeps repository of running clusters with a unique name for each. Privacy sensitive data such as credentials is
 * stored inside a context. There is a common context with shared values between clusters and each cluster has its own
 * context inside which values can be overwritten.
 */
// TODO(Fabio) this class is total BS. We run 1 fucking cluster at the time
// First you learn to walk then you run.
public class ClusterService {

  private final Logger logger = Logger.getLogger(ClusterService.class);
  private ClusterDefinitionService clusterDefinitionService = new ClusterDefinitionService();

  private ClusterManager clusterManager = null;
  private ClusterContext clusterContext = new ClusterContext();

  private static ClusterService instance = null;
  public static ClusterService getInstance() {
    if (instance == null) {
      instance = new ClusterService();
    }
    return instance;
  }

  public synchronized void registerSudoAccountPassword(String password) {
    clusterContext.setSudoAccountPassword(password);
  }

  public synchronized String getSudoAccountPassword() {
    return clusterContext.getSudoAccountPassword();
  }

  public synchronized void registerEc2Context(Ec2Context ec2Context) throws KaramelException {
    clusterContext.setEc2Context(ec2Context);
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

  public synchronized void startCluster(Cluster cluster) throws KaramelException {
    clusterManager = new ClusterManager(cluster, clusterContext);
    clusterManager.start();
    clusterManager.enqueue(ClusterManager.Command.LAUNCH_CLUSTER);
    clusterManager.enqueue(ClusterManager.Command.SUBMIT_INSTALL_DAG);
  }

  public synchronized void submitInstallationDag() throws KaramelException {
    clusterManager.enqueue(ClusterManager.Command.INTERRUPT_DAG);
    clusterManager.enqueue(ClusterManager.Command.SUBMIT_INSTALL_DAG);
  }

  public synchronized void submitPurgeDag() throws KaramelException {
    clusterManager.enqueue(ClusterManager.Command.INTERRUPT_DAG);
    clusterManager.enqueue(ClusterManager.Command.SUBMIT_PURGE_DAG);
  }

  public synchronized void pauseDag() throws KaramelException {
    clusterManager.enqueue(ClusterManager.Command.PAUSE_DAG);
  }

  public synchronized void resumeDag() throws KaramelException {
    clusterManager.enqueue(ClusterManager.Command.RESUME_DAG);
  }

  public synchronized void terminateCluster() throws KaramelException {
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          ClusterRuntime runtime = clusterManager.getRuntime();
          clusterManager.enqueue(ClusterManager.Command.INTERRUPT_CLUSTER);
          clusterManager.enqueue(ClusterManager.Command.TERMINATE_CLUSTER);
          while (runtime.getPhase() != ClusterRuntime.ClusterPhases.NOT_STARTED) {
            Thread.sleep(100);
          }
          String name = runtime.getName().toLowerCase();
          logger.info(String.format("Cluster '%s' terminated, removing it from the list of running clusters",
              runtime.getName()));
          clusterManager = null;
          clusterContext = new ClusterContext();
        } catch (InterruptedException ex) {
        } catch (KaramelException ex) {
          logger.error("", ex);
        }
      }
    };
    t.start();
  }

  public synchronized void registerNovaV3Context(NovaV3Context context) {
    clusterContext.setNovaV3Context(context);
  }

  public synchronized void registerNovaContext(NovaContext context) {
    clusterContext.setNovaContext(context);
  }

  public synchronized void registerOcciContext(OcciContext context) {
    clusterContext.setOcciContext(context);
  }
}
