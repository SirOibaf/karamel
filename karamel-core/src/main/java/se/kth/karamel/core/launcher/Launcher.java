/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.core.launcher;

import java.util.List;
import se.kth.karamel.core.running.model.ClusterRuntime;
import se.kth.karamel.core.running.model.MachineRuntime;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public abstract class Launcher {

  /**
   * It cleans up all the groups have this type of provider
   * @param definition
   * @param runtime
   * @throws KaramelException 
   */
  public abstract void cleanup(Cluster definition, ClusterRuntime runtime) throws KaramelException;

  /**
   * 
   * @param definition
   * @param runtime
   * @param name
   * @return group id 
   * @throws se.kth.karamel.common.exception.KaramelException 
   */
  public abstract String forkGroup(Cluster definition, ClusterRuntime runtime, String name)
      throws KaramelException;

  /**
   * 
   * @param definition
   * @param runtime
   * @param name
   * @return 
   * @throws KaramelException 
   */
  public abstract List<MachineRuntime> forkMachines(Cluster definition, ClusterRuntime runtime, String name)
      throws KaramelException;


}
