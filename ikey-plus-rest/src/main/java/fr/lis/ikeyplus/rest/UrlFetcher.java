package fr.lis.ikeyplus.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;

public class UrlFetcher {
    private final boolean allowPrivateAddresses;

    public UrlFetcher(final boolean allowPrivateAddresses) {
        this.allowPrivateAddresses = allowPrivateAddresses;
    }

    public InputStream getStream(final URL url) throws IOException {
        final InetAddress address = InetAddress.getByName(url.getHost());
        if(!allowPrivateAddresses && PrivateAddressesUtils.isPrivate(address)) {
            throw new IllegalArgumentException("private URLs are forbidden");
        }
        return url.openStream();
    }
}
