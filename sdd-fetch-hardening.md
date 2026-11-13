# SDD fetch hardening & configuration

Design notes for hardening the `sddURL` download in the REST layer and driving it through
`application.properties`. High-level sketches only — not implemented yet.

## Problem

`RestHandler.handle` fetches any `http`/`https` URL supplied by the caller via
`URL.openStream()`:

- **SSRF**: anyone reaching the endpoint can make the server issue GET requests to internal
  addresses (`localhost`, `10.x`, `172.16-31.x`, `192.168.x`, cloud metadata at
  `169.254.169.254`). Impacts: cloud credential theft (IMDSv1), triggering side effects on
  internal GET endpoints, internal port scanning via status codes / timings / error messages.
- **No timeouts, no size cap**: a slow or endless response pins a Javalin worker thread
  indefinitely (thread exhaustion).

Constraint: manual testing fetches SDD files from the local nginx started by
`ikey-plus-rest/docker-compose.yml` (`http://localhost:10080/...`, see `src/test/test.http`),
and `JavalinIntegrationTest` serves files on `localhost` with a random port. Blocking private
addresses must therefore be configurable.

## Design

- Configuration via plain `java.util.Properties` (no new dependency).
- Layered loading: bundled defaults → optional external file → (optional) env var overrides.
- Secure by default: private addresses blocked unless explicitly allowed.
- Strict parsing: invalid config fails at startup, not at request time.
- The private-address check lives in the fetcher (after DNS resolution), not in `InputParser`
  (which only sees the URL string) — this is what defeats DNS rebinding.
- Tests build the config in code; they don't depend on classpath files.

### 1. Properties files

```properties
# src/main/resources/application.properties  (bundled in the jar — secure defaults)
sdd.fetch.allow-private-addresses=false
sdd.fetch.connect-timeout=5s
sdd.fetch.read-timeout=30s
sdd.fetch.max-size-bytes=52428800
```

```properties
# ikey-plus-rest/application-local.properties  (manual testing against docker-compose nginx)
sdd.fetch.allow-private-addresses=true
```

### 2. Typed config + loading

```java
record AppConfig(boolean allowPrivateAddresses, Duration connectTimeout,
                 Duration readTimeout, long maxSizeBytes) {

    static AppConfig load() {
        Properties props = new Properties();
        props.load(classpath("application.properties"));                     // defaults
        externalPath().ifPresent(p -> props.load(Files.newInputStream(p)));  // -Dikey.config / IKEY_CONFIG
        // optional: env overrides, SDD_FETCH_ALLOW_PRIVATE_ADDRESSES -> sdd.fetch.allow-private-addresses
        return fromProperties(props);
    }

    static AppConfig fromProperties(Properties p) {
        // strict parsing: invalid/missing value -> IllegalStateException at startup
        return new AppConfig(
            parseBoolean(p, "sdd.fetch.allow-private-addresses"),
            parseDuration(p, "sdd.fetch.connect-timeout"),
            ...);
    }
}
```

### 3. Wiring

```java
public static void main(String[] args) {
    buildApplication(AppConfig.load()).start(8080);
}

public static Javalin buildApplication(AppConfig appConfig) {
    var fetcher = new SddFetcher(appConfig);
    var handler = new RestHandler(fetcher);          // instance, no longer static
    return Javalin.create(config -> {
        config.routes.get("/identification-key", handler::handleGetKey);
        ...
    });
}
```

### 4. Where the flag is used

```java
class SddFetcher {
    InputStream fetch(URL url) throws IOException {
        InetAddress address = InetAddress.getByName(url.getHost());
        if (!config.allowPrivateAddresses() && isPrivate(address)) {
            throw new ForbiddenSddHostException(url.getHost());     // -> 400
        }
        // connect to the resolved address (no re-resolution), no redirects,
        // connect/read timeouts, cap body at maxSizeBytes
    }

    static boolean isPrivate(InetAddress a) {
        return a.isLoopbackAddress() || a.isLinkLocalAddress() || a.isSiteLocalAddress()
            || a.isAnyLocalAddress() || a.isMulticastAddress();
    }
}
```

Note: also check IPv6 unique-local addresses (`fc00::/7`) — `isSiteLocalAddress()` doesn't
cover them.

### 5. Usage

```
# IntelliJ run config, VM options (manual testing with docker-compose nginx):
-Dikey.config=application-local.properties

# Docker / prod: nothing to set — bundled defaults block private addresses
```

If the app is later added to `docker-compose.yml`, the SDD URL becomes
`http://test-files/...`, which resolves to a private Docker network IP (172.x) — the same
opt-in applies (mount the local properties file or use an env override).

```java
// integration test: config built in code, no file involved
var app = buildApplication(new AppConfig(true, ofSeconds(5), ofSeconds(30), 50_000_000));
```

### Tests to add

- Integration test with default config: localhost `sddURL` → 400.
- `AppConfigTest`: layering (external file overrides bundled defaults) and fail-fast on invalid
  values (e.g. `allow-private-addresses=maybe`).
- Adjust `InputParserTest#should_not_validate_localhost_url`: `InputParser` keeps syntax checks
  only (well-formed, http/https); host blocking moves to `UrlFetcher`.

## Open questions

- Env var overrides (layer 3): include or skip?
- `application-local.properties`: commit it (shared dev setup) or gitignore it (per developer)?
- Scope: implement the fetcher (timeouts, size cap, private-address check) together with the
  config plumbing, or config plumbing first?
- Alternative to the blocklist: host allowlist (`sdd.fetch.allowed-hosts=...`) — only viable if
  production SDD sources are known in advance.
