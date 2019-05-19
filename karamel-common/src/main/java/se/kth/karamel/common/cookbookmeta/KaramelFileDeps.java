package se.kth.karamel.common.cookbookmeta;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class KaramelFileDeps {

  @Getter
  private String recipe = null;
  @Getter
  private List<String> local = new ArrayList<>();
  @Getter
  private List<String> global = new ArrayList<>();

  public KaramelFileDeps() {}

}
