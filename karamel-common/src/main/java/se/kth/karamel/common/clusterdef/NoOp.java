package se.kth.karamel.common.clusterdef;

import lombok.Getter;
import lombok.Setter;
import se.kth.karamel.common.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

public class NoOp extends Provider {

  @Getter @Setter
  private List<String> ips = new ArrayList<>();

  @Override
  public void validate() throws ValidationException {
    // Noop
  }
}
