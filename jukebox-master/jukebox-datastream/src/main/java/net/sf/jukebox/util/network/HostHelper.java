package net.sf.jukebox.util.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to perform common network operations.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2001-2008
 */
public class HostHelper {

  private HostHelper() {
  }

  /**
   * Figure out if the address is local.
   *
   * @param address Address to analyze.
   *
   * @return {@code true} if the address is local.
   * @throws SocketException if there was a problem talking to the network.
   * @throws UnknownHostException if the address given can't be resolved.
   */
  public static boolean isLocalAddress(String address) throws SocketException, UnknownHostException {

    InetAddress targetAddress = InetAddress.getByName(address);

    // Since the network configuration is dynamic, we'd rather collect
    // the addresses right now

    return getLocalAddresses().contains(targetAddress);
  }

  /**
   * Get a set of local host addresses.
   *
   * @return Set of local host addresses.
   * @throws SocketException if there was a problem talking to the network.
   */
  public static Set<InetAddress> getLocalAddresses() throws SocketException {

    Set<InetAddress> result = new HashSet<InetAddress>();

    for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {

      NetworkInterface ni = e.nextElement();

      // complain(LOG_ALERT, getLogChannel(), "Interface: " +
      // ni.getDisplayName());

      for (Enumeration<InetAddress> e2 = ni.getInetAddresses(); e2.hasMoreElements();) {

        InetAddress niAddress = e2.nextElement();

        // complain(LOG_ALERT, getLogChannel(), "Address: " +
        // niAddress);

        result.add(niAddress);
      }
    }

    return result;
  }
}
