package se.kth.karamel.common.cookbookmeta;

import java.util.ArrayList;
import java.util.List;

public class KaramelFileYamlRep {

  private List<KaramelFileDeps> dependencies = new ArrayList<>();

  public List<KaramelFileDeps> getDependencies() {
    return dependencies;
  }

  public void setDependencies(List<KaramelFileDeps> dependencies) {
    if (dependencies != null) {
      this.dependencies = dependencies;
    }
  }

}
