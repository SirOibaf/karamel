package se.kth.karamel.core;

import lombok.Getter;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.chef.DataBagsFactory;
import se.kth.karamel.core.dag.Dag;
import se.kth.karamel.core.dag.DagFactory;
import se.kth.karamel.core.execution.ExecutionEngine;
import se.kth.karamel.core.provisioner.Provisioner;
import se.kth.karamel.core.provisioner.ProvisionerFactory;

import java.io.IOException;

public class DeploymentManager {

  private Settings settings;

  @Getter
  private Dag dag;
  @Getter
  private ExecutionEngine executionEngine;

  public DeploymentManager(Settings settings) {
    this.settings = settings;
    this.executionEngine = new ExecutionEngine(settings);
  }

  /**
   * This function is the entrypoint for a cluster deployment
   * There are several steps:
   *  - Provision the HW
   *  - Build the DAG
   *  - Executing the DAG
   * @throws KaramelException
   */
  public void deploy(ClusterContext clusterContext) throws KaramelException {

    // Provision HW
    Provisioner provisioner = ProvisionerFactory.getProvisioner(settings);
    int numNodes = 0;
    for (Group group : clusterContext.getCluster().getGroups()) {
      numNodes += provisioner.provisionGroup(clusterContext, clusterContext.getCluster(), group, numNodes);
    }

    // Build the DAG
    DagFactory dagFactory = new DagFactory();
    try {
      dag = dagFactory.buildDag(clusterContext.getCluster(), settings,
          new DataBagsFactory(clusterContext.getCluster()));
    } catch (IOException e) {
      throw new KaramelException("Could not build the dag", e);
    }

    // Execute the DAG
    executionEngine.execute(dag, numNodes);
  }

  public void pause() {
    executionEngine.pause();
  }

  public void resume() {
    executionEngine.resume();
  }

  public void terminate() {
    executionEngine.terminate();
  }
}
