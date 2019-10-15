package se.kth.karamel.core.chef;

import org.junit.Ignore;
import org.junit.Test;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.clusterdef.NoOp;
import se.kth.karamel.common.clusterdef.Recipe;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Constants;
import se.kth.karamel.core.ClusterContext;
import se.kth.karamel.core.provisioner.jcloud.baremetal.NoopProvisioner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestDataBagsFactory {

  private ClusterContext clusterContext = null;
  private Group firstGroup;

  private Group buildGroup(String name) {
    Group group = new Group();
    NoOp noopGroup = new NoOp();
    noopGroup.setIps(Arrays.asList("127.0.0.1", "127.0.0.2", "127.0.0.3"));

    List<Recipe> recipesList = new ArrayList<>();
    recipesList.add(new Recipe( "test::default"));

    group.setNoop(noopGroup);
    group.setName(name);
    group.setRecipes(recipesList);

    return group;
  }

  private Cluster buildCluster() throws KaramelException {
    Cluster cluster = new Cluster();

    NoOp noopGlobal = new NoOp();
    noopGlobal.setUsername("Username");
    cluster.setNoop(noopGlobal);

    firstGroup = buildGroup("first");

    ArrayList<Group> groups = new ArrayList<>(Arrays.asList(firstGroup));
    cluster.setGroups(groups);

    clusterContext = new ClusterContext(cluster);

    provisionGroup(cluster, firstGroup, 0);

    return cluster;
  }

  private void provisionGroup(Cluster cluster, Group group, int currentNodeId) throws KaramelException {
    NoopProvisioner noopProvisioner = new NoopProvisioner();
    noopProvisioner.provisionGroup(clusterContext,
      cluster, group, currentNodeId);
  }

  private void addAttributes(Map<String, Object> attributes,
                             String[] keySplits, Object value, int currentSplit) {
    if (currentSplit == (keySplits.length - 1)) {
      attributes.put(keySplits[currentSplit], value);
    } else {
      Map<String, Object> nestedAttributes = (HashMap)attributes.getOrDefault(keySplits[currentSplit], new HashMap<>());
      addAttributes(nestedAttributes, keySplits, value, currentSplit + 1);
      attributes.put(keySplits[currentSplit], nestedAttributes);
    }
  }

  @Test
  public void testIPs() throws KaramelException {
    Cluster cluster = buildCluster();
    DataBagsFactory dataBagsFactory = new DataBagsFactory(cluster);
    DataBag dataBag = dataBagsFactory.getGroupDataBag(firstGroup);
    Map recipeDataBag = (HashMap)((HashMap)dataBag.get("test")).get("default");
    assertNotNull(recipeDataBag);
    List<String> privateIPs = (List)recipeDataBag.get(Constants.PRIVATE_IPS);
    assertEquals(3, privateIPs.size());
    assertTrue(privateIPs.contains("127.0.0.1"));

    List<String> publicIPs = (List)recipeDataBag.get(Constants.PUBLIC_IPS);
    assertEquals(3, publicIPs.size());
    assertTrue(publicIPs.contains("127.0.0.1"));
  }

  @Test
  public void testIPsMultipleGroups() throws KaramelException {
    Cluster cluster = buildCluster();
    Group secondGroup = buildGroup("second");
    NoOp noopGroup = new NoOp();
    noopGroup.setIps(Arrays.asList("127.0.0.5", "127.0.0.6", "127.0.0.7"));
    secondGroup.setNoop(noopGroup);
    cluster.getGroups().add(secondGroup);

    provisionGroup(cluster, secondGroup, 3);

    DataBagsFactory dataBagsFactory = new DataBagsFactory(cluster);
    DataBag dataBag = dataBagsFactory.getGroupDataBag(firstGroup);
    Map recipeDataBag = (HashMap)((HashMap)dataBag.get("test")).get("default");
    assertNotNull(recipeDataBag);
    List<String> privateIPs = (List)recipeDataBag.get(Constants.PRIVATE_IPS);
    assertEquals(6, privateIPs.size());
    assertTrue(privateIPs.contains("127.0.0.1"));
    assertTrue(privateIPs.contains("127.0.0.5"));

    List<String> publicIPs = (List)recipeDataBag.get(Constants.PUBLIC_IPS);
    assertEquals(6, publicIPs.size());
    assertTrue(publicIPs.contains("127.0.0.1"));
    assertTrue(publicIPs.contains("127.0.0.5"));
  }

  @Test
  public void testGlobalAttrs() throws KaramelException {
    Cluster cluster = buildCluster();
    Map<String, Object> attributes = new HashMap<>();
    addAttributes(attributes, "test/attribute".split("/"), "value", 0);
    cluster.setAttributes(attributes);

    DataBagsFactory dataBagsFactory = new DataBagsFactory(cluster);
    DataBag dataBag = dataBagsFactory.getGroupDataBag(firstGroup);
    assertTrue(dataBag.get("test") instanceof Map);
    assertTrue(((HashMap)dataBag.get("test")).get("attribute") instanceof String);
  }

  @Test
  public void testKeepTypeInt() throws KaramelException {
    Cluster cluster = buildCluster();
    Map<String, Object> attributes = new HashMap<>();
    addAttributes(attributes, "test/attribute".split("/"), 10, 0);
    cluster.setAttributes(attributes);

    DataBagsFactory dataBagsFactory = new DataBagsFactory(cluster);
    DataBag dataBag = dataBagsFactory.getGroupDataBag(firstGroup);
    assertTrue(dataBag.get("test") instanceof Map);
    assertTrue(((HashMap)dataBag.get("test")).get("attribute") instanceof Integer);
  }


  @Test
  public void testKeepTypeList() throws KaramelException {
    Cluster cluster = buildCluster();
    Map<String, Object> attributes = new HashMap<>();
    addAttributes(attributes, "test/attribute".split("/"), Arrays.asList(1,2,3), 0);
    cluster.setAttributes(attributes);

    DataBagsFactory dataBagsFactory = new DataBagsFactory(cluster);
    DataBag dataBag = dataBagsFactory.getGroupDataBag(firstGroup);
    assertTrue(dataBag.get("test") instanceof Map);
    assertTrue(((HashMap)dataBag.get("test")).get("attribute") instanceof List);
  }

  @Test
  public void testMultipleAttrs() throws KaramelException {
    Cluster cluster = buildCluster();
    Map<String, Object> attributes = new HashMap<>();
    addAttributes(attributes, "test/attribute".split("/"), Arrays.asList(1,2,3), 0);
    addAttributes(attributes, "second".split("/"), "second", 0);
    cluster.setAttributes(attributes);

    DataBagsFactory dataBagsFactory = new DataBagsFactory(cluster);
    DataBag dataBag = dataBagsFactory.getGroupDataBag(firstGroup);
    assertTrue(dataBag.get("test") instanceof Map);
    assertTrue(((HashMap)dataBag.get("test")).get("attribute") instanceof List);
    assertTrue(dataBag.get("second") instanceof String);
  }

  @Test
  public void testMerge() throws KaramelException {
    Cluster cluster = buildCluster();
    Map<String, Object> attributes = new HashMap<>();
    addAttributes(attributes, "test/attribute".split("/"), Arrays.asList(1,2,3), 0);
    addAttributes(attributes, "test/map/ciao".split("/"), "Ciao", 0);
    cluster.setAttributes(attributes);

    DataBagsFactory dataBagsFactory = new DataBagsFactory(cluster);
    DataBag dataBag = dataBagsFactory.getGroupDataBag(firstGroup);
    assertTrue(dataBag.get("test") instanceof Map);
    assertTrue(((HashMap)dataBag.get("test")).get("attribute") instanceof List);
    assertTrue(((HashMap)dataBag.get("test")).get("map") instanceof Map);
    assertTrue(((HashMap)((HashMap)dataBag.get("test")).get("map")).get("ciao") instanceof String);
  }

  @Test
  public void testGroupAttrs() throws KaramelException {
    Cluster cluster = buildCluster();
    Map<String, Object> attributes = new HashMap<>();
    addAttributes(attributes, "test/attribute".split("/"), Arrays.asList(1,2,3), 0);
    addAttributes(attributes, "test/newattr".split("/"), 10, 0);
    addAttributes(attributes, "second".split("/"), "second", 0);
    cluster.getGroups().get(0).setAttributes(attributes);

    DataBagsFactory dataBagsFactory = new DataBagsFactory(cluster);
    DataBag dataBag = dataBagsFactory.getGroupDataBag(firstGroup);
    assertTrue(dataBag.get("test") instanceof Map);
    assertTrue(((HashMap)dataBag.get("test")).get("attribute") instanceof List);
    assertTrue(((HashMap)dataBag.get("test")).get("newattr") instanceof Integer);
    assertTrue(dataBag.get("second") instanceof String);
  }

  @Test
  public void testMultipleGroups() throws KaramelException {
    Cluster cluster = buildCluster();
    Map<String, Object> attributes = new HashMap<>();
    addAttributes(attributes, "test/attribute".split("/"), Arrays.asList(1,2,3), 0);
    addAttributes(attributes, "test/newattr".split("/"), 10, 0);
    addAttributes(attributes, "g".split("/"), "first", 0);
    cluster.getGroups().get(0).setAttributes(attributes);

    Group secondGroup = buildGroup("second");
    attributes = new HashMap<>();
    addAttributes(attributes, "g".split("/"), "second", 0);
    secondGroup.setAttributes(attributes);
    cluster.getGroups().add(secondGroup);

    provisionGroup(cluster, secondGroup, 3);

    DataBagsFactory dataBagsFactory = new DataBagsFactory(cluster);

    DataBag dataBag = dataBagsFactory.getGroupDataBag(firstGroup);
    assertTrue(dataBag.get("test") instanceof Map);
    assertTrue(((HashMap)dataBag.get("test")).get("attribute") instanceof List);
    assertTrue(((HashMap)dataBag.get("test")).get("newattr") instanceof Integer);
    assertTrue(dataBag.get("g") instanceof String);
    assertEquals("first", dataBag.get("g"));


    dataBag = dataBagsFactory.getGroupDataBag(secondGroup);
    assertEquals("second", dataBag.get("g"));
  }

  @Test
  public void testGroupOverwrite() throws KaramelException {
    Cluster cluster = buildCluster();
    Map<String, Object> attributes = new HashMap<>();
    addAttributes(attributes, "test/newattr".split("/"), 10, 0);
    addAttributes(attributes, "test/attr".split("/"), "Ciao", 0);
    cluster.setAttributes(attributes);

    attributes = new HashMap<>();
    addAttributes(attributes, "test/newattr".split("/"), 11, 0);
    addAttributes(attributes, "test/gattr".split("/"), "Gattr", 0);
    firstGroup.setAttributes(attributes);

    DataBagsFactory dataBagsFactory = new DataBagsFactory(cluster);

    DataBag dataBag = dataBagsFactory.getGroupDataBag(firstGroup);
    assertEquals(11, ((HashMap)dataBag.get("test")).get("newattr"));
    assertEquals("Ciao", ((HashMap)dataBag.get("test")).get("attr"));
    assertEquals("Gattr", ((HashMap)dataBag.get("test")).get("gattr"));
  }


  @Test
  @Ignore
  public void testAddResults() {
    throw new RuntimeException("Not implemented yet");
  }
}
