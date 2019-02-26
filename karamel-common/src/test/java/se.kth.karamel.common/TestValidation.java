package se.kth.karamel.common;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.cookbookmeta.KaramelFile;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.cookbookmeta.MetadataRb;
import se.kth.karamel.common.cookbookmeta.Recipe;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.NoKaramelizedCookbookException;
import se.kth.karamel.common.exception.ValidationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestValidation {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Yaml yaml = new Yaml(new Constructor(Cluster.class));

  @BeforeClass
  public static void setup() {
    List<Recipe> cookbookRecipes = new ArrayList<>();
    cookbookRecipes.add(new Recipe("install", "install recipe"));
    cookbookRecipes.add(new Recipe("default", "default recipe"));
    cookbookRecipes.add(new Recipe("magic", "magic recipe"));

    MetadataRb testMetadataRb = new MetadataRb();
    testMetadataRb.setName("test");
    testMetadataRb.setRecipes(cookbookRecipes);

    KaramelizedCookbook karamelizedCookbook =
        new KaramelizedCookbook(testMetadataRb, new KaramelFile());
    CookbookCache.getInstance().addToCache("test", karamelizedCookbook);
  }

  @Test
  public void testValidCluster() throws IOException, KaramelException {
    String clusterDefinition = IOUtils.toString(
        this.getClass().getResourceAsStream("/validation/testValid.yml"), "UTF-8");
    Cluster cluster = (Cluster) yaml.load(clusterDefinition);
    cluster.validate();
  }

  @Test
  public void testNoRecipes() throws IOException, KaramelException {
    String clusterDefinition = IOUtils.toString(
        this.getClass().getResourceAsStream("/validation/testNoRecipes.yml"), "UTF-8");
    Cluster cluster = (Cluster) yaml.load(clusterDefinition);

    thrown.expect(ValidationException.class);
    thrown.expectMessage("The group does not contain any recipe");
    cluster.validate();
  }

  @Test
  public void testDuplicatedRecipe() throws IOException, KaramelException {
    String clusterDefinition = IOUtils.toString(
        this.getClass().getResourceAsStream(
            "/validation/testDuplicatedRecipes.yml"), "UTF-8");
    Cluster cluster = (Cluster) yaml.load(clusterDefinition);

    thrown.expect(ValidationException.class);
    thrown.expectMessage("Duplicated recipes found in group");
    cluster.validate();
  }

  @Test
  public void testCookbookNotAvailable() throws IOException, KaramelException {
    String clusterDefinition = IOUtils.toString(
        this.getClass().getResourceAsStream(
            "/validation/testNoCookbookAvailable.yml"), "UTF-8");
    Cluster cluster = (Cluster) yaml.load(clusterDefinition);

    thrown.expect(NoKaramelizedCookbookException.class);
    cluster.validate();
  }


  @Test
  public void testWrongSize() throws IOException, KaramelException {
    String clusterDefinition = IOUtils.toString(
        this.getClass().getResourceAsStream(
            "/validation/testWrongSize.yml"), "UTF-8");
    Cluster cluster = (Cluster) yaml.load(clusterDefinition);

    thrown.expect(ValidationException.class);
    cluster.validate();
  }

  // TODO(Fabio): Test attribute validation
  // TODO(Fabio): Test cookbook validation
}
