package se.kth.karamel.core;

import lombok.Getter;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.dag.Dag;
import se.kth.karamel.core.dag.DagFactory;
import se.kth.karamel.core.execution.ExecutionEngine;
import se.kth.karamel.core.provisioner.Provisioner;
import se.kth.karamel.core.provisioner.ProvisionerFactory;

public class DeploymentManager {

  private Settings settings;

  @Getter
  private Dag dag;
  @Getter
  private ExecutionEngine executionEngine;

  public DeploymentManager(Settings settings) {
    this.settings = settings;
    this.executionEngine = new ExecutionEngine();
  }

  /**
   * This function is the entrypoint for a cluster deployment
   * There are several steps:
   *  - Provision the HW
   *  - Build the DAG
   *  - Executing the DAG
   * @throws KaramelException
   */
  public void deploy(ClusterContext clusterContext, Cluster cluster) throws KaramelException {

    // Provision HW
    Provisioner provisioner = ProvisionerFactory.getProvisioner(settings);
    int numNodes = 0;
    for (Group group : cluster.getGroups()) {
      numNodes += provisioner.provisionGroup(clusterContext, cluster, group, numNodes);
    }

    // Build the DAG
    DagFactory dagFactory = new DagFactory();
    dag = dagFactory.buildDag(cluster);

    // Execute the DAG
    executionEngine.execute(dag, numNodes);
  }
}
