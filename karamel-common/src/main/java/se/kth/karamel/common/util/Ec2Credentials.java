package se.kth.karamel.common.util;

import lombok.Getter;
import lombok.Setter;

public class Ec2Credentials {

  @Getter @Setter
  private String accessKey="";
  @Getter @Setter
  private String secretKey="";
}
