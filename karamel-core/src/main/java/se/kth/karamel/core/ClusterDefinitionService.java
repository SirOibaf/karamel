package se.kth.karamel.core;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Stores/reads cluster definitions from Karamel home folder
 */
public class ClusterDefinitionService {

  public void saveAsYaml(Cluster cluster) throws KaramelException {
    try {
      String name = cluster.getName().toLowerCase();
      Path clusterFolder = Paths.get(System.getenv(Constants.KARAMEL_HOME), Constants.KARAMEL_CLUSTERS_DIRNAME);
      File folder = clusterFolder.toFile();
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

      File file = clusterFolder.resolve(name).toFile();
      FileUtils.writeStringToFile(file, yaml.dump(cluster));
    } catch (IOException ex) {
      throw new KaramelException("Could not convert yaml to java ", ex);
    }
  }

  public Cluster loadYaml(String clusterName) throws KaramelException {
    try {
      String name = clusterName.toLowerCase();
      Path clusterFolder = Paths.get(System.getenv(Constants.KARAMEL_HOME), Constants.KARAMEL_CLUSTERS_DIRNAME);
      File folder = clusterFolder.toFile();
      if (!folder.exists()) {
        throw new KaramelException(String.format("cluster '%s' is not available", name));
      }

      String yamlPath = clusterFolder.resolve(name).toString();
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
