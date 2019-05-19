package se.kth.karamel.client.api;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import se.kth.karamel.core.ClusterService;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Ec2Credentials;
import se.kth.karamel.common.util.SshKeyPair;


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
  public Ec2Credentials loadEc2CredentialsIfExist() throws KaramelException {
    return null;
  }

  @Override
  public boolean updateEc2CredentialsIfValid(Ec2Credentials credentials) throws KaramelException {
    return true;
  }

  @Override
  public String loadGceCredentialsIfExist() throws KaramelException {
    return null;
  }

  @Override
  public boolean updateGceCredentialsIfValid(String jsonFilePath) throws KaramelException {
    return true;
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
  public void terminateCluster(String clusterName) throws KaramelException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void startCluster() throws KaramelException {
    clusterService.startCluster();
  }

  @Override
  public String getInstallationDag(String clusterName) throws KaramelException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SshKeyPair loadSshKeysIfExist() throws KaramelException {
    return null;
  }

  @Override
  public SshKeyPair loadSshKeysIfExist(String clusterName) throws KaramelException {
    return null;
  }

  @Override
  public SshKeyPair registerSshKeys(SshKeyPair keypair) throws KaramelException {
    return null;
  }

  @Override
  public SshKeyPair registerSshKeys(String clusterName, SshKeyPair keypair) throws KaramelException {
    return null;
  }

  @Override
  public void registerSudoPassword(String password) {
    ClusterService.getInstance().registerSudoAccountPassword(password);
  }
}
