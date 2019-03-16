package se.kth.karamel.common;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Cookbook;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestCluster {

  private Yaml yaml = new Yaml(new Constructor(Cluster.class));

  @Test
  public void testParseClusterDefinition() throws IOException {
    String clusterDefinition = IOUtils.toString(
        this.getClass().getResourceAsStream("/cluster/test1.yml"), "UTF-8");
    Cluster cluster = (Cluster) yaml.load(clusterDefinition);
    assertEquals("test1", cluster.getName());
    assertEquals(4, cluster.getAttributes().size());
    assertEquals(2, cluster.getGroups().size());
    assertEquals(13,
        cluster.getGroups().stream().filter(g -> g.getName().equals("group1")).findFirst().get().getRecipes().size());
  }

  @Test
  public void testGroupAttributes() throws IOException {
    String clusterDefinition = IOUtils.toString(
        this.getClass().getResourceAsStream("/cluster/testGroupAttributes.yml"), "UTF-8");
    Cluster cluster = (Cluster) yaml.load(clusterDefinition);
    assertEquals(1,
        cluster.getGroups().stream().filter(g -> g.getName().equals("group1"))
            .findFirst().get().getAttributes().size());

    assertNull(cluster.getGroups().stream().filter(g -> g.getName().equals("group2"))
            .findFirst().get().getAttributes());
  }

  @Test
  public void testCookbooks() throws IOException {
    String clusterDefinition = IOUtils.toString(
        this.getClass().getResourceAsStream("/cluster/testCookbooks.yml"), "UTF-8");
    Cluster cluster = (Cluster) yaml.load(clusterDefinition);

    Cookbook localChef = cluster.getCookbooks().get("local-chef");
    assertEquals(Cookbook.CookbookType.LOCAL, localChef.getCookbookType());
    assertNull(localChef.getGithub());

    Cookbook gitChef = cluster.getCookbooks().get("git-chef");
    assertEquals(Cookbook.CookbookType.GIT, gitChef.getCookbookType());
    assertNull(gitChef.getLocalPath());
  }
}
