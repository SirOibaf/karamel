package se.kth.karamel.core;

import se.kth.karamel.common.clusterdef.GCE;
import se.kth.karamel.core.provisioner.jcloud.amazon.Ec2Context;
import se.kth.karamel.core.provisioner.jcloud.google.GceContext;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.EC2;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.SSHKeyPair;

/**
 * Store information about a running cluster. Has a cluster definition and API authentication
 * information can be added
 */

public class ClusterContext {

  private Cluster cluster;

  private SSHKeyPair SSHKeyPair;
  private String sudoPassword;
  private Ec2Context ec2Context;
  private GceContext gceContext;

  public ClusterContext(Cluster cluster) {
    this.cluster = cluster;
  }

  public Cluster getCluster() {
    return cluster;
  }

  public void setCluster(Cluster cluster) {
    this.cluster = cluster;
  }

  public void setSudoPassword(String sudoPassword) {
    this.sudoPassword = sudoPassword;
  }

  public String getSudoPassword() {
    return sudoPassword;
  }

  public Ec2Context getEc2Context() {
    return ec2Context;
  }

  public void setEc2Context(Ec2Context ec2Context) {
    this.ec2Context = ec2Context;
  }

  public SSHKeyPair getSSHKeyPair() {
    return SSHKeyPair;
  }

  public void setSSHKeyPair(SSHKeyPair SSHKeyPair) {
    this.SSHKeyPair = SSHKeyPair;
  }

  public GceContext getGceContext() {
    return gceContext;
  }

  public void setGceContext(GceContext gceContext) {
    this.gceContext = gceContext;
  }

  public void validateContext() throws KaramelException {
    // Check that, if one of the groups is using a cloud provider,
    // the corresponding context has been configured
    for (Group group : cluster.getGroups()) {
      Provider provider = group.getProvider();
      if (provider instanceof EC2 && ec2Context == null) {
        throw new KaramelException("No valid EC2 credentials registered");
      } else if (provider instanceof GCE && gceContext == null) {
        throw new KaramelException("No valid GCE credentials registered");
      }
    }

    if (SSHKeyPair == null) {
      throw new KaramelException("No ssh keypair chosen");
    }
  }

}
