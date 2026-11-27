package fr.lis.ikeyplus.rest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.regex.Pattern;

public final class PrivateAddressesUtils {
    private PrivateAddressesUtils() {
    }

    private static final Pattern IPV4_LITERAL = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}");

    static boolean isPrivate(final InetAddress a) {
        return a.isLoopbackAddress() || a.isLinkLocalAddress() || a.isSiteLocalAddress()
                || a.isAnyLocalAddress() || a.isMulticastAddress();
    }

    /**
     * Syntactic check only: never triggers a DNS lookup.
     */
    static boolean isObviouslyPrivateHost(final String host) {
        final String h = host.toLowerCase(Locale.ROOT);
        if (h.equals("localhost") || h.endsWith(".localhost")) {
            return true;
        }
        if (h.startsWith("[") || IPV4_LITERAL.matcher(h).matches()) {
            try {
                // getByName does not hit DNS for IP literals
                return isPrivate(InetAddress.getByName(h));
            } catch (final UnknownHostException _) {
                return false;
            }
        }
        return false;
    }
}
