package se.kth.karamel.common.cookbookmeta;

import java.util.ArrayList;
import java.util.List;

public class KaramelFileYamlRep {

  private List<KaramelFileYamlDeps> dependencies = new ArrayList<>();

  public List<KaramelFileYamlDeps> getDependencies() {
    return dependencies;
  }

  public void setDependencies(List<KaramelFileYamlDeps> dependencies) {
    if (dependencies != null) {
      this.dependencies = dependencies;
    }
  }

}
