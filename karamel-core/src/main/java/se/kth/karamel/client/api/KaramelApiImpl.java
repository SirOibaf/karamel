package se.kth.karamel.client.api;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import se.kth.karamel.core.ClusterService;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Ec2Credentials;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.core.provisioner.jcloud.amazon.Ec2Context;

import java.io.IOException;


/**
 * Implementation of the Karamel Api for UI
 */
public class KaramelApiImpl implements KaramelApi {

  private static final Logger logger = Logger.getLogger(KaramelApiImpl.class);

  private ClusterService clusterService = ClusterService.getInstance();

  @Override
  public void loadClusterDefinition(String clusterDefinition) throws KaramelException {
    Yaml yaml = new Yaml(new Constructor(Cluster.class));
    Cluster cluster = (Cluster) yaml.load(clusterDefinition);
    clusterService.setCurrentCluster(cluster);
  }

  @Override
  public Cluster getCluster() {
    return clusterService.getCurrentCluster();
  }

  @Override
  public SshKeyPair loadSshKeysIfExist() throws KaramelException {
    // TODO(Fabio) fix this
    return null;
  }

  @Override
  public void registerSshKeys(SshKeyPair keyPair) throws KaramelException {
    clusterService.registerSshKeyPair(keyPair);
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
  public void pauseCluster(String clusterName) throws KaramelException {
    clusterService.pauseDag();
  }

  @Override
  public void resumeCluster(String clusterName) throws KaramelException {
    clusterService.resumeDag();
  }

  @Override
  public void terminateCluster() throws KaramelException {
    clusterService.terminateCluster();
  }
}
