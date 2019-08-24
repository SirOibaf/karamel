package se.kth.karamel.common.cookbookmeta;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class KaramelFileDeps {

  @Getter @Setter
  // TODO(Fabio) replace this with the Recicpe object
  private String recipe = null;
  @Getter @Setter
  private List<String> local = new ArrayList<>();
  @Getter @Setter
  private List<String> global = new ArrayList<>();

  public KaramelFileDeps() {}

  public KaramelFileDeps(String recipe, List<String> local, List<String> global) {
    this.recipe = recipe;
    this.local = local;
    this.global = global;
  }
}
