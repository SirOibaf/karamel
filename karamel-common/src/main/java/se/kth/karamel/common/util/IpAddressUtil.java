package se.kth.karamel.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.common.exception.IpAddressException;

import javax.ws.rs.NotSupportedException;


public class IpAddressUtil {

  private static Pattern IP_PATTERN =
      Pattern.compile("^(25[0-5]|2[0-4][0-9]|[1][0-9][0-9]|[1-9][0-9]|[0-9]?)" +
          "(\\.(25[0-5]|2[0-4][0-9]|[1][0-9][0-9]|[1-9][0-9]|[0-9]?)){3}$");

  public static List<String> parseIPRange(String ipRange) throws IpAddressException {
    // TODO(Fabio): add proper support to IPranges
    // For the moment we assume that all IP passed as parameters are not ranges
    if (ipRange.contains(Constants.IP_RANGE_DIVIDER)) {
      throw new NotSupportedException("IP ranges are not supported yet.");
    }

    if (validIP(ipRange)) {
      return Arrays.asList(ipRange);
    }

    return new ArrayList<>();
  }

  private static boolean validIP(String ip) throws IpAddressException {
    Matcher m = IP_PATTERN.matcher(ip);
    if (!m.matches()) {
      throw new IpAddressException("Ip format is invalid " + ip);
    }

    return true;
  }
}
