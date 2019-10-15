package se.kth.karamel.common.util;

import se.kth.karamel.common.exception.UnsupportedImageType;

import java.net.URI;
import java.net.URISyntaxException;

public class GceSettings {

  public enum ImageType {
    debian("debian-cloud"),
    centos("centos-cloud"),
    ceros("ceros-cloud"),
    opensuse("opensuse-cloud"),
    ubuntu("ubuntu-os-cloud"),
    redhat("rhel-cloud"),
    suse("suse-cloud");
    private final String type;

    private ImageType(String t) {
      type = t;
    }

    @Override
    public String toString() {
      return type;
    }

    public boolean equalsName(String otherType) {
      return (otherType == null) ? false : type.equals(otherType);
    }
  }

  public static URI buildMachineTypeUri(String projectName, String zone, String machineType)
      throws URISyntaxException {
    return new URI(String.format("https://www.googleapis.com/compute/v1/projects/%s/zones/%s/machineTypes/%s",
        projectName, zone, machineType));
  }

  public static URI buildNetworkUri(String projectName, String networkName) throws URISyntaxException {
    return new URI(String.format("https://www.googleapis.com/compute/v1/projects/%s/global/networks/%s",
        projectName, networkName));
  }

  public static URI buildSubnetUri(String projectName, String region, String subnetName)
      throws URISyntaxException {
    return new URI(String.format("https://www.googleapis.com/compute/v1/projects/%s/regions/%s/subnetworks/%s",
        projectName, region, subnetName));
  }
  
  public static URI buildGlobalImageUri(String imageName) throws
      URISyntaxException, UnsupportedImageType {
    ImageType type;
    if (imageName.contains("ubuntu")) {
      type = ImageType.ubuntu;
    } else if (imageName.contains("debian")) {
      type = ImageType.debian;
    } else if (imageName.contains("centos")) {
      type = ImageType.centos;
    } else if (imageName.contains("ceros")) {
      type = ImageType.ceros;
    } else if (imageName.contains("opensuse")) {
      type = ImageType.opensuse;
    } else if (imageName.contains("rhel")) {
      type = ImageType.redhat;
    } else if (imageName.contains("suse")) {
      type = ImageType.suse;
    } else {
      throw new UnsupportedImageType(String.format("No image type is found for image %s", imageName));
    }
    return new URI(String.format("https://www.googleapis.com/compute/v1/projects/%s/global/images/%s",
        type.toString(), imageName));
  }
  
  public static URI buildProjectImageUri(String projectName, String
      imageName) throws URISyntaxException {
    return new URI(String.format("https://www.googleapis.com/compute/v1/projects/%s/global/images/%s",
        projectName, imageName));
  }
  
}
