package se.kth.karamel.webservice.calls.cluster;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum ClusterActions {
  VALIDATE,
  START,
  PAUSE,
  STOP;

  public static ClusterActions fromString(String action) {
    return valueOf(action.toUpperCase());
  }
}
