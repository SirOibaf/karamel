package se.kth.karamel.core;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Settings;

import java.io.File;
import java.io.IOException;

/**
 * Stores/reads cluster definitions from Karamel home folder, does conversions between yaml and json definitions.
 */
public class ClusterDefinitionService {

  public void saveAsYaml(Cluster cluster) throws KaramelException {
    try {
      String name = cluster.getName().toLowerCase();
      File folder = new File(Settings.CLUSTER_ROOT_PATH(name));
      if (!folder.exists()) {
        folder.mkdirs();
      }

      DumperOptions options = new DumperOptions();
      options.setIndent(2);
      options.setWidth(120);
      options.setExplicitEnd(false);
      options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
      options.setPrettyFlow(true);
      options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
      Yaml yaml = new Yaml(options);

      File file = new File(Settings.CLUSTER_YAML_PATH(name));
      FileUtils.writeStringToFile(file, yaml.dump(cluster));
    } catch (IOException ex) {
      throw new KaramelException("Could not convert yaml to java ", ex);
    }
  }

  public Cluster loadYaml(String clusterName) throws KaramelException {
    try {
      String name = clusterName.toLowerCase();
      File folder = new File(Settings.CLUSTER_ROOT_PATH(name));
      if (!folder.exists()) {
        throw new KaramelException(String.format("cluster '%s' is not available", name));
      }

      String yamlPath = Settings.CLUSTER_YAML_PATH(name);
      File file = new File(yamlPath);
      if (!file.exists()) {
        throw new KaramelException(String.format("yaml '%s' is not available", yamlPath));
      }

      Yaml yaml = new Yaml(new Constructor(Cluster.class));
      Object document = yaml.load(FileUtils.readFileToString(file));
      return ((Cluster) document);
    } catch (IOException ex) {
      throw new KaramelException("Could not load the yaml ", ex);
    }
  }
}