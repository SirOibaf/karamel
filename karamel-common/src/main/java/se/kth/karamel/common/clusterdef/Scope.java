package se.kth.karamel.common.clusterdef;

import lombok.Getter;
import lombok.Setter;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.ValidationException;

import java.util.HashMap;
import java.util.Map;

public class Scope {

  @Setter @Getter
  private NoOp noop = null;

  @Setter @Getter
  private Baremetal baremetal = null;

  @Setter @Getter
  private EC2 ec2 = null;

  @Setter @Getter
  private GCE gce = null;

  @Getter @Setter
  protected Map<String, Object> attributes = new HashMap<>();

  public Scope() {
  }

  public Provider getProvider() {
    if (noop != null) {
      return noop;
    } else if (baremetal != null) {
      return baremetal;
    } else if (ec2 != null) {
      return ec2;
    } else {
      return gce;
    }
  }

  public void validate() throws ValidationException, KaramelException, InterruptedException {
    // TODO(Fabio): make sure that only one provider is set
    getProvider().validate();
  }
}
