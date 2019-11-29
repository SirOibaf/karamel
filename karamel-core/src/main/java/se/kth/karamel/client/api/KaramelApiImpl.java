package se.kth.karamel.client.api;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import se.kth.karamel.common.node.Node;
import se.kth.karamel.core.ClusterService;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Ec2Credentials;
import se.kth.karamel.common.util.SSHKeyPair;
import se.kth.karamel.core.execution.Task;
import se.kth.karamel.core.provisioner.jcloud.amazon.Ec2Context;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Implementation of the Karamel Api for UI
 */
public class KaramelApiImpl implements KaramelApi {

  private static final Logger logger = Logger.getLogger(KaramelApiImpl.class);

  private ClusterService clusterService = ClusterService.getInstance();

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
    // TODO(Fabio): fix this
    return Arrays.asList(new SSHKeyPair("/home/vagrant/.ssh/id_rsa",
      "/home/vagrant/.ssh/id_rsa.pub", ""));
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
  public void pauseCluster() throws KaramelException {
    clusterService.pauseDag();
  }

  @Override
  public void resumeCluster() throws KaramelException {
    clusterService.resumeDag();
  }

  @Override
  public void terminateCluster() throws KaramelException {
    clusterService.terminateCluster();
  }

  @Override
  public Map<Node, List<Task>> getClusterDeploymentStatus() {
    return null;
  }

  @Override
  public List<Task> getNodeDeploymentStatus(Node node) {
    return null;
  }
}
