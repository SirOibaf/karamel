package se.kth.karamel.common.clusterdef;

import lombok.Getter;
import lombok.Setter;
import se.kth.karamel.common.util.Constants;
import se.kth.karamel.common.exception.ValidationException;

public class GCE extends Provider {

  @Getter @Setter
  private String type = Constants.GCE_DEFAULT_MACHINE_TYPE;
  @Getter @Setter
  private String zone = Constants.GCE_DEFAULT_ZONE;
  @Getter @Setter
  private String image = Constants.GCE_DEFAULT_IMAGE;
  @Getter @Setter
  private String vpc = Constants.GCE_DEFAULT_NETWORK_NAME;
  @Getter @Setter
  private Long diskSize = Constants.GCE_DEFAULT_DISKSIZE_IN_GB;
  @Getter @Setter
  private String subnet = Constants.GCE_DEFAULT_IP_RANGE;
  @Getter @Setter
  private Boolean preemptible = Constants.GCE_DEFAULT_IS_PRE_EMPTIBLE;

  public GCE() {
    username = Constants.AWS_VM_USERNAME_DEFAULT;
  }

  @Override
  public void validate() throws ValidationException {
    // Currently nothing to validate. But IP range can be validate here.
  }
}
