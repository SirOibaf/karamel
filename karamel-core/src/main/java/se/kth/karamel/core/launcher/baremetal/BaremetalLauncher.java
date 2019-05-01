/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.core.launcher.baremetal;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import se.kth.karamel.core.converter.UserClusterDataExtractor;
import se.kth.karamel.core.launcher.Launcher;
import se.kth.karamel.core.running.model.ClusterRuntime;
import se.kth.karamel.core.running.model.GroupRuntime;
import se.kth.karamel.core.running.model.MachineRuntime;
import se.kth.karamel.common.clusterdef.Baremetal;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class BaremetalLauncher extends Launcher {

  private static final Logger logger = Logger.getLogger(BaremetalLauncher.class);

  public final SshKeyPair sshKeyPair;

  public BaremetalLauncher(SshKeyPair sshKeyPair) {
    this.sshKeyPair = sshKeyPair;
    logger.info(String.format("Public-key='%s'", sshKeyPair.getPublicKeyPath()));
    logger.info(String.format("Private-key='%s'", sshKeyPair.getPrivateKeyPath()));
  }

  @Override
  public void cleanup(Cluster definition, ClusterRuntime runtime) throws KaramelException {
    logger.debug("It is baremetal, cleanup is skipped.");
  }

  @Override
  public String forkGroup(Cluster definition, ClusterRuntime runtime, String groupName) throws KaramelException {
    logger.debug(String.format("Provider of %s is baremetal, fork-group is skipped.", groupName));
    return groupName;
  }

  @Override
  public List<MachineRuntime> forkMachines(Cluster definition, ClusterRuntime runtime, String groupName)
      throws KaramelException {
    logger.debug(String.format("Provider of %s is baremetal, available machines expected.", groupName));
    GroupRuntime gr = UserClusterDataExtractor.findGroup(runtime, groupName);
    Baremetal baremetal = (Baremetal) UserClusterDataExtractor.getGroupProvider(definition, groupName);
    String username = baremetal.getUsername();
    List<MachineRuntime> machines = new ArrayList<>();
    for (String ip : baremetal.retriveAllIps()) {
      MachineRuntime machine = new MachineRuntime(gr);
      machine.setMachineType("baremetal");
      machine.setName(ip);
      machine.setPrivateIp(ip);
      machine.setPublicIp(ip);
      machine.setSshPort(Settings.BAREMETAL_DEFAULT_SSH_PORT);
      machine.setSshUser(username);
      machines.add(machine);
    }
    return machines;
  }

}
