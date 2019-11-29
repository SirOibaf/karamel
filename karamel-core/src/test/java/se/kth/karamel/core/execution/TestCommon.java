package se.kth.karamel.core.execution;

import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.clusterdef.NoOp;
import se.kth.karamel.common.clusterdef.Recipe;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.ClusterContext;
import se.kth.karamel.core.dag.Dag;
import se.kth.karamel.core.dag.DagFactory;
import se.kth.karamel.core.provisioner.jcloud.baremetal.NoopProvisioner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestCommon {
  public static Cluster buildCluster(KaramelizedCookbook karamelizedCookbook, List<String> nodes) {
    Cluster cluster = new Cluster();

    NoOp noopGlobal = new NoOp();
    noopGlobal.setUsername("Username");
    cluster.setNoop(noopGlobal);

    Group firstGroup = new Group();
    NoOp noopGroup = new NoOp();
    noopGroup.setIps(nodes);

    List<Recipe> recipesList = new ArrayList<>();
    recipesList.add(new Recipe(karamelizedCookbook, "test"));

    firstGroup.setNoop(noopGroup);
    firstGroup.setRecipes(recipesList);

    ArrayList<Group> groups = new ArrayList<>(Arrays.asList(firstGroup));
    cluster.setGroups(groups);

    return cluster;
  }

  public static void provisionNoopNodes(Cluster cluster) throws KaramelException {
    NoopProvisioner noopProvisioner = new NoopProvisioner();
    ClusterContext clusterContext = new ClusterContext(cluster);
    int numNodes = 0;
    for (Group group : clusterContext.getCluster().getGroups()) {
      numNodes += noopProvisioner.provisionGroup(clusterContext,
          clusterContext.getCluster(), group, numNodes);
    }
  }

  public static Dag buildDag(Cluster cluster, Settings settings) throws IOException, KaramelException {
    provisionNoopNodes(cluster);
    DagFactory dagFactory = new DagFactory();
    return dagFactory.buildDag(cluster, settings, null);
  }
}
