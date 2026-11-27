package fr.lis.ikeyplus.rest.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public record AppConfig(boolean allowPrivateAddresses) {

    public static AppConfig load() throws IOException {
        final Properties props = new Properties();
        props.load(new FileInputStream(AppConfig.class.getClassLoader().getResource("application.properties").getPath()));
        return fromProperties(props);
    }

    static AppConfig fromProperties(final Properties p) {
        return new AppConfig(p.getProperty("sdd.fetch.allow-private-addresses").equalsIgnoreCase("true"));
    }
}
