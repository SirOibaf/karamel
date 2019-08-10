package se.kth.karamel.common.clusterdef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import se.kth.karamel.common.util.IpAddressUtil;
import se.kth.karamel.common.exception.ValidationException;

public class Baremetal extends Provider {

  @Getter @Setter
  private List<String> ips = new ArrayList<>();

  /**
   * We support ip ranges (TODO)
   * ip: 10.0.0.1-10.0.0.10
   * @return
   * @throws ValidationException
   */
  public HashSet<String> retriveAllIps() throws ValidationException {
    HashSet<String> indivIps = new HashSet<>();
    for (String iprange : ips) {
      for (String parsedIp : IpAddressUtil.parseIPRange(iprange)) {
        if (!indivIps.add(parsedIp)) {
          throw new ValidationException("ip-address already exist " + parsedIp);
        }
      }
    }
    return indivIps;
  }

  @Override
  public void validate() throws ValidationException {
    // Validate number of IPs in the Baremetal case
    // TODO(Fabio)
    // int ipSize = ips.size();
    // if (ipSize != size) {
    //   throw new ValidationException(
    //       String.format("Number of ip addresses is not equal to the group size %d != %d", ipSize, size));
    // }

    retriveAllIps();
  }
}
