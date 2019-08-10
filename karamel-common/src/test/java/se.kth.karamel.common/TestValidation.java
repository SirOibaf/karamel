package se.kth.karamel.common;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.cookbookmeta.KaramelFile;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.cookbookmeta.MetadataRb;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.NoKaramelizedCookbookException;
import se.kth.karamel.common.exception.ValidationException;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TestValidation {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Yaml yaml = new Yaml(new Constructor(Cluster.class));

  @BeforeClass
  public static void setup() throws KaramelException, IOException {
    Map<String, String> cookbookRecipes = new HashMap<>();
    cookbookRecipes.put("test::install", "Install recipe");
    cookbookRecipes.put("test::default", "Default recipe");

    MetadataRb testMetadataRb = new MetadataRb();
    testMetadataRb.setName("test");
    testMetadataRb.setRecipes(cookbookRecipes);

    KaramelizedCookbook karamelizedCookbook =
        new KaramelizedCookbook(testMetadataRb, new KaramelFile());
    CookbookCache.getInstance().addToCache("test", karamelizedCookbook);
  }

  private Cluster loadCluster(String clusterName) throws IOException {
    String clusterDefinition = IOUtils.toString(
        this.getClass().getResourceAsStream("/validation/" + clusterName), "UTF-8");
    Cluster cluster = (Cluster) yaml.load(clusterDefinition);

    // If cluster definition is in local mode, we need to fix the directory to point to the
    // resource directory
    for (Cookbook cookbook : cluster.getCookbooks().values()) {
      if (cookbook.getLocalPath() != null) {
        URL cookbookDir = this.getClass().getResource("/" + cookbook.getLocalPath());
        cookbook.setLocalPath(cookbookDir.getPath());
      }
    }

    return cluster;
  }

  private Object buildAttributeObject(String attrName, Object value) {
    String[] splits = attrName.split("/");
    Map<String, Object> nestedMap = new HashMap<>();
    nestedMap.put(splits[1], value);
    return nestedMap;
  }

  @Test
  public void testValidCluster() throws IOException, KaramelException {
    Cluster cluster = loadCluster("testValid.yml");
    cluster.validate();
  }

  @Test
  public void testBadRecipe() throws IOException, KaramelException {
    Cluster cluster = loadCluster("testValid.yml");
    cluster.getGroups().get(0).getRecipes().add(new se.kth.karamel.common.clusterdef.Recipe("test::bad"));

    thrown.expect(ValidationException.class);
    thrown.expectMessage("Recipe: test::bad does not exists");
    cluster.validate();
  }

  @Test
  public void testNoRecipes() throws IOException, KaramelException {
    Cluster cluster = loadCluster("testNoRecipes.yml");

    thrown.expect(ValidationException.class);
    thrown.expectMessage("The group does not contain any recipe");
    cluster.validate();
  }

  @Test
  public void testDuplicatedRecipe() throws IOException, KaramelException {
    Cluster cluster = loadCluster("testDuplicatedRecipes.yml");

    thrown.expect(ValidationException.class);
    thrown.expectMessage("Duplicated recipes found in group");
    cluster.validate();
  }

  @Test
  public void testCookbookNotAvailable() throws IOException, KaramelException {
    Cluster cluster = loadCluster("testNoCookbookAvailable.yml");

    thrown.expect(NoKaramelizedCookbookException.class);
    cluster.validate();
  }


  @Test
  public void testWrongSize() throws IOException, KaramelException {
    Cluster cluster = loadCluster("testWrongSize.yml");

    thrown.expect(ValidationException.class);
    cluster.validate();
  }

  @Test
  public void testValidAttribute() throws IOException, KaramelException {
    Cluster cluster = loadCluster("testValid.yml");
    cluster.getAttributes().put("test", buildAttributeObject("test/attribute", "test"));
    cluster.validate();
  }

  @Test
  public void testMissingAttribute() throws IOException, KaramelException {
    Cluster cluster = loadCluster("testValid.yml");
    cluster.getAttributes().put("test", buildAttributeObject("test/missing", "test"));

    thrown.expect(ValidationException.class);
    thrown.expectMessage("Invalid attributes");
    cluster.validate();
  }

  @Test
  public void testWrongType() throws IOException, KaramelException {
    Cluster cluster = loadCluster("testValid.yml");
    cluster.getAttributes().put("test", buildAttributeObject("test/numeric_attribute", "ciao"));

    thrown.expect(ValidationException.class);
    thrown.expectMessage("Invalid type");
    cluster.validate();
  }

  @Test
  public void testNumericAttributeFloat() throws IOException, KaramelException {
    Cluster cluster = loadCluster("testValid.yml");
    cluster.getAttributes().put("test", buildAttributeObject("test/numeric_attribute", 1.0f));

    cluster.validate();
  }

  @Test
  public void testNumericAttributeInt() throws IOException, KaramelException {
    Cluster cluster = loadCluster("testValid.yml");
    cluster.getAttributes().put("test", buildAttributeObject("test/numeric_attribute", 1));

    cluster.validate();
  }

  @Test
  public void testBooleanAttribute() throws IOException, KaramelException {
    Cluster cluster = loadCluster("testValid.yml");
    cluster.getAttributes().put("test", buildAttributeObject("test/boolean_attribute", false));

    cluster.validate();
  }

  @Test
  public void testArrayAttribute() throws IOException, KaramelException {
    Cluster cluster = loadCluster("testValid.yml");
    Integer[] intArray = new Integer[]{1,2,3};
    cluster.getAttributes().put("test", buildAttributeObject("test/array_attribute",
        Arrays.asList(intArray)));

    cluster.validate();
  }

  @Test
  public void testValidGroupAttribute() throws IOException, KaramelException {
    Cluster cluster = loadCluster("testValid.yml");
    cluster.getGroups().get(0)
        .getAttributes().put("test", buildAttributeObject("test/attribute", "test"));

    cluster.validate();
  }

  @Test
  public void testInvalidGroupAttribute() throws IOException, KaramelException {
    Cluster cluster = loadCluster("testValid.yml");
    cluster.getGroups().get(0)
        .getAttributes().put("test", buildAttributeObject("test/invalid", "test"));

    thrown.expect(ValidationException.class);
    thrown.expectMessage("Invalid attributes");
    cluster.validate();
  }
}
