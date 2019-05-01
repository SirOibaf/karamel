package se.kth.karamel.core.dag;

import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;

public class DagFactory {

  public Dag buildDag(Cluster cluster) {
    Dag dag = new Dag();

    // 1. Add machine setup Task. It will be responsible of installing Chefdk,
    // downloading the vendored cookbooks and generating the solo.rb script.


    return dag;
  }

  private void addGroupSetupTasks(Cluster cluster, Group group) {
    // TODO(Fabio): This is not implemented yet.
    // In the current Karamel implementation there is support for
    // group level tasks such as setting up the security group
    // Find out if they are actually useful in some way.
  }

  private void addNodeSetupTasks(Dag dag, String node) {
  }
}
