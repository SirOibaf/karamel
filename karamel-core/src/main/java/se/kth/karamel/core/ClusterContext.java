package se.kth.karamel.core;

import se.kth.karamel.common.clusterdef.GCE;
import se.kth.karamel.core.provisioner.jcloud.amazon.Ec2Context;
import se.kth.karamel.core.provisioner.jcloud.google.GceContext;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.EC2;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.SshKeyPair;

/**
 * Authenticated APIs and privacy-sensitive data, that must not be revealed by storing them in the file-system, is
 * stored here in memory. It is valid just until the system is running otherwise it will disappear.
 */
public class ClusterContext {

  private Ec2Context ec2Context;
  private GceContext gceContext;
  private SshKeyPair sshKeyPair;
  private String sudoPassword;

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

  public SshKeyPair getSshKeyPair() {
    return sshKeyPair;
  }

  public void setSshKeyPair(SshKeyPair sshKeyPair) {
    this.sshKeyPair = sshKeyPair;
  }

  public void mergeContext(ClusterContext commonContext) {
    if (ec2Context == null) {
      ec2Context = commonContext.getEc2Context();
    }
    if (gceContext == null) {
      gceContext = commonContext.getGceContext();
    }
    if (sshKeyPair == null) {
      sshKeyPair = commonContext.getSshKeyPair();
    }
  }

  public static ClusterContext validateContext(Cluster definition,
                                               ClusterContext context, ClusterContext commonContext)
      throws KaramelException {
    if (context == null) {
      context = new ClusterContext();
    }
    context.mergeContext(commonContext);

    for (Group group : definition.getGroups()) {
      Provider provider = group.getProvider();
      if (provider instanceof EC2 && context.getEc2Context() == null) {
        throw new KaramelException("No valid EC2 credentials registered :-|");
      } else if (provider instanceof GCE && context.getGceContext() == null) {
        throw new KaramelException("No valid GCE credentials registered :-|");
      }
    }

    if (context.getSshKeyPair() == null) {
      throw new KaramelException("No ssh keypair chosen :-|");
    }
    return context;
  }

  /**
   * @return the gceContext
   */
  public GceContext getGceContext() {
    return gceContext;
  }

  /**
   * @param gceContext the gceContext to set
   */
  public void setGceContext(GceContext gceContext) {
    this.gceContext = gceContext;
  }
}
