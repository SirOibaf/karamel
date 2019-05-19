package se.kth.karamel.common.clusterdef;

import lombok.Getter;
import lombok.Setter;
import se.kth.karamel.common.util.Constants;
import se.kth.karamel.common.exception.ValidationException;

public class EC2 extends Provider {

  @Getter @Setter
  private String type = Constants.AWS_VM_TYPE_DEFAULT;
  @Getter @Setter
  private String region = Constants.AWS_REGION_CODE_DEFAULT;
  @Getter @Setter
  private String ami;
  @Getter @Setter
  private Float price;
  @Getter @Setter
  private String vpc;
  @Getter @Setter
  private String subnet;

  public EC2() {
    username = Constants.AWS_VM_USERNAME_DEFAULT;
  }

  @Override
  public void validate() throws ValidationException {
    if ((subnet != null && vpc == null) || (subnet == null && vpc != null)) {
      throw new ValidationException("Both subnet and vpc ids are required for vpc settings on ec2");
    }
  }

}
