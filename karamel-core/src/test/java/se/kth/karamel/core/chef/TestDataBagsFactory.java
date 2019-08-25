package se.kth.karamel.core.chef;

import org.junit.Test;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.clusterdef.NoOp;
import se.kth.karamel.common.clusterdef.Recipe;
import se.kth.karamel.common.exception.KaramelException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDataBagsFactory {

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

  private Cluster buildCluster() {
    Cluster cluster = new Cluster();

    NoOp noopGlobal = new NoOp();
    noopGlobal.setUsername("Username");
    cluster.setNoop(noopGlobal);

    firstGroup = buildGroup("first");

    ArrayList<Group> groups = new ArrayList<>(Arrays.asList(firstGroup));
    cluster.setGroups(groups);

    return cluster;
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
  public void testIPs() throws KaramelException, IOException {
    throw new RuntimeException("Not implemented yet");
  }

  @Test
  public void testGlobalAttrs() throws KaramelException, IOException {
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
  public void testKeepTypeInt() throws KaramelException, IOException {
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
  public void testKeepTypeList() throws KaramelException, IOException {
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
  public void testMultipleAttrs() throws KaramelException, IOException {
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
  public void testMerge() throws KaramelException, IOException {
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
  public void testGroupAttrs() throws KaramelException, IOException {
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
  public void testMultipleGroups() throws KaramelException, IOException {
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
  public void testGroupOverwrite() throws KaramelException, IOException {
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
  public void testAddRecipe() throws KaramelException, IOException {
    throw new RuntimeException("Not implemented yet");
  }

  @Test
  public void testAddResults() throws KaramelException, IOException {
    throw new RuntimeException("Not implemented yet");
  }
}
