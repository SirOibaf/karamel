package se.kth.karamel.core;

import se.kth.karamel.core.converter.UserClusterDataExtractor;
import se.kth.karamel.core.launcher.amazon.Ec2Context;
import se.kth.karamel.core.launcher.google.GceContext;
import se.kth.karamel.core.launcher.nova.NovaContext;
import se.kth.karamel.core.launcher.novav3.NovaV3Context;
import se.kth.karamel.core.launcher.occi.OcciContext;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Ec2;
import se.kth.karamel.common.clusterdef.Gce;
import se.kth.karamel.common.clusterdef.Nova;
import se.kth.karamel.common.clusterdef.Occi;
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
  private NovaContext novaContext;
  private NovaV3Context novaV3Context;
  private OcciContext occiContext;
  private String sudoPassword = "";

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
    if (novaContext == null) {
      novaContext = commonContext.getNovaContext();
    }
    if (novaV3Context == null) {
      novaV3Context = commonContext.getNovaV3Context();
    }
    if (occiContext == null) {
      occiContext = commonContext.getOcciContext();
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
      Provider provider = UserClusterDataExtractor.getGroupProvider(definition, group.getName());
      if (provider instanceof Ec2 && context.getEc2Context() == null) {
        throw new KaramelException("No valid Ec2 credentials registered :-|");
      } else if (provider instanceof Gce && context.getGceContext() == null) {
        throw new KaramelException("No valid Gce credentials registered :-|");
      } else if (provider instanceof Nova) {
        if (context.getNovaContext() == null && context.getNovaV3Context() == null) {
          throw new KaramelException("No valid Novai(v2 or v3) credentials registered :-|");
        }
      } else if (provider instanceof Occi && context.getOcciContext() == null) {
        throw new KaramelException("No valid Occi credentials registered :-|");
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

  public void setNovaContext(NovaContext novaContext) {
    this.novaContext = novaContext;
  }

  public NovaContext getNovaContext() {
    return novaContext;
  }

  public void setNovaV3Context(NovaV3Context novaV3Context) {
    this.novaV3Context = novaV3Context;
  }

  public NovaV3Context getNovaV3Context() {
    return novaV3Context;
  }

  public void setOcciContext(OcciContext occiContext) {
    this.occiContext = occiContext;
  }

  public OcciContext getOcciContext() {
    return occiContext;
  }

}