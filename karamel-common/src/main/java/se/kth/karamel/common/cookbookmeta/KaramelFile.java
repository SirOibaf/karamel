package se.kth.karamel.common.cookbookmeta;

import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import se.kth.karamel.common.exception.MetadataParseException;

public final class KaramelFile {

  private List<KaramelFileDeps> dependencies;

  public KaramelFile() {
    dependencies = new ArrayList<>();
  }

  public KaramelFile(String fileContent) throws MetadataParseException {
    Yaml yaml = new Yaml(new Constructor(KaramelFileYamlRep.class));
    KaramelFileYamlRep file = null;
    try {
      file = (KaramelFileYamlRep) yaml.load(fileContent);
    } catch (YAMLException ex) {
      throw new MetadataParseException(ex.getMessage());
    }
    dependencies = new ArrayList<>();

    if (file != null) {
      dependencies.addAll(file.getDependencies());
    }
  }

  public List<KaramelFileDeps> getDependencies() {
    return dependencies;
  }

  public void setDependencies(List<KaramelFileDeps> dependencies) {
    this.dependencies = dependencies;
  }

}
