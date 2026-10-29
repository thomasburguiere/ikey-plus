# ikey-plus-rest TODO

Follow-ups from the review of the Javalin migration (`refactor/rest-with-javalin`).

## Security / must fix

- [x] **Restrict `sddURL` to `http`/`https`**
  - [x] Today, `Application.handleGet`Key` passes the raw query param to `new SDDSaxParser(sddURL, config)`.
  - [x] If `new URL(uri)` throws `MalformedURLException`, `SDDSaxParser` falls back to `new FileInputStream(uri)`. So `?sddURL=/etc/passwd` (or any server path) gets opened.
  - [x] `file:///...` URLs pass `new URL(...)`, so they're read too.
  - [x] Fix: parse with `URI.create(sddURL)`, require the `http`/`https` scheme, and optionally check the host against an allow-list (config/env var). That also blocks SSRF to internal hosts such as `169.254.169.254` and `localhost`.

- [x] **Add an `InputStream`-based `SDDSaxParser` constructor**
  - [x] The REST layer should open the connection itself, with timeouts and a size limit (see below), and hand the stream to the parser.
  - [x] Keep the `String`/`File` constructors for the CLI and tests, but the REST path must never reach the `FileInputStream` fallback.

- [x] **Disable XXE in `SDDSaxParser`**
  - Both constructors create `org.apache.xerces.parsers.SAXParser` with default features, so external entities and DTDs are resolved.
  - A crafted SDD with `<!DOCTYPE x [<!ENTITY e SYSTEM "file:///etc/passwd">]>` can inject local files or internal URLs into labels/descriptions. Those then show up in the generated key.
  - [x] Set, in a shared helper used by both constructors:
    ```java
    saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    // if DOCTYPEs must be tolerated instead:
    saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
    saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    saxReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    ```
  - Check that real SDD files (test fixtures) don't rely on a DOCTYPE before choosing `disallow-doctype-decl`.
  - [x] `XMLReaderFactory` is deprecated; consider `SAXParserFactory.newInstance()` + `setNamespaceAware(true)` while touching this.

- [x] **Return 400 when `sddURL` is missing**
  - Right now `null` goes into `new URL(null)`, which throws `MalformedURLException`. Then `new FileInputStream(null)` throws `NullPointerException`, and the client gets a 500.
  - Use `ctx.queryParamAsClass("sddURL", String.class).check(...).get()`, which throws a `ValidationException` → 400 with a message, or throw `BadRequestResponse` explicitly.

- [ ] **Map parse/download failures to proper status codes**
  - Register handlers in `Javalin.create(config -> ...)`:
    - `IOException` while fetching (unreachable host, 404 upstream) → 502 Bad Gateway, with a short message.
    - `SAXException` (malformed XML / not SDD) → 400 or 422.
    - Anything else → 500, logged with its stack trace, generic body.
  - Replace the `printStackTrace()` habit with a logger (SLF4J comes with `javalin-bundle`).

- [ ] **Return 400/501 for `pdf`/`zip`**
  - [x] `case PDF, ZIP -> throw new RuntimeException("UNSUPPORTED")` gives a 500.
  - [x] Throw `new HttpResponseException(501, "format not supported yet")` (or 400 if they'll never be supported).
  - For ZIP, the old `SingleAccessKeyTreeDumper.dumpZipFile` could be adapted to write into `ctx.outputStream()` with `ContentType.APPLICATION_ZIP`.

- [ ] **Reject unknown `format`/`representation` values**
  - `IdentificationKeyImpl.initializeConfig` falls back to TXT/FLAT when `fromString` returns `null`, so `?format=htm` silently returns plain text.
  - Same for `verbosity`, `scoreMethod`, `weightContext` and `weightType`: typos are ignored.
  - Only apply the defaults when the param is absent. A present but unknown value should give a 400 that lists the accepted values (`Arrays.toString(OutputFormat.values())`).
  - Note that `OutputFormat.fromString` and friends are case-sensitive. Decide whether to accept `HTML`/`Html`.

- [ ] **Cap SDD download size and bound generation time**
  - Test inputs already include ~94k- and ~150k-line SDDs. Nothing stops someone from pointing at a multi-GB file or a slow endpoint.
  - Set connect/read timeouts on the `HttpURLConnection` / `HttpClient`.
  - Wrap the stream in a size-limited `InputStream` (e.g. abort after N MB, configurable).
  - Key generation runs on the Jetty request thread. Consider running it in a bounded executor with a timeout (`CompletableFuture.orTimeout`) and returning 503 when saturated. The old CPU-usage guard in `IdentificationKeyImpl` (commented out, Sigar) was the previous attempt at this.

## Correctness

- [ ] **Fix the `scoreMethod` default**
  - `ofNullable(ctx.queryParam("scoreMethod")).orElse(XPER.name())` yields `"XPER"`, but `ScoreMethod.fromString` compares against `toString()`, which is `"xper"`. The lookup returns `null`, and the builder's own default (XPER) applies only by accident.
  - Remove the `ofNullable(...).orElse(...)` and pass the raw param; `initializeConfig` already handles `null`.
  - Once that's done, drop the unused `XPER` and `ofNullable` imports.

- [ ] **Replace `"TODO HEADER"` with the real header**
  - The old `IdentificationKeyImpl.createIdentificationKey` built it: dataset label + `message.createdBy`, the list of options (`sddURL`, `format`, `representation`, ...), `parseDuration` and `keyCreationDuration`.
  - It was cleared when `VerbosityLevel.HEADER` was not requested. Keep that behaviour.
  - Extract it to a helper, e.g. `static String buildHeader(DataSet, IkeyConfig, String sddURL, Duration parse, Duration generate)`.
  - Currently only the HTML / interactive HTML outputs receive a header. TXT/WIKI/DOT used to get one through the `dump*File` methods, so check whether the `generate*String` variants should take one too.

- [x] **Set content types explicitly**
  - TXT, WIKI and DOT rely on Javalin's default (`text/plain`).
  - Use `ctx.html(...)` for HTML / interactive HTML.
  - Use `ctx.contentType(ContentType.TEXT_PLAIN)` for TXT/WIKI, and `"text/vnd.graphviz"` for DOT.
  - Use `ContentType.APPLICATION_XML` (or `TEXT_XML`) for SDD.
  - Then remove the hand-rolled `CONTENT_TYPE*` constants.
  - Consider setting `charset=utf-8` and, optionally, `Content-Disposition` with a sensible filename for downloads.

## Cleanup

- [ ] **Tighten `handleGetKey`**
  - It's a package-private, non-final, mutable static field (`static Handler handleGetKey = ctx -> {...}`).
  - Turn it into a `private static void handleGetKey(Context ctx)` method registered as `Application::handleGetKey`. It reads better, stack traces are clearer, and it's easier to unit test.
  - Consider splitting it: param parsing → config, key generation, rendering by format (the nested `switch`).

- [ ] **Make `initializeConfig` static or move it**
  - `private static final IdentificationKeyImpl ik` exists only to call `initializeConfig`, which keeps no state.
  - Move it to `IkeyConfigBuilder` (e.g. `IkeyConfig.fromParams(...)`) or a small `KeyRequestParams` record with a `toConfig()` method. That also gives a natural place for the validation above.

- [ ] **Delete `IdentificationKeyImpl.createIdentificationKey` / `getIdentificationKey`**
  - The file-based flow (writes into `generatedKeyFiles.folder`, returns a URL built from `host`) no longer matches the API, which streams the result.
  - Port the header logic first (see above), then delete the class.
  - Also check whether the `generatedKeyFiles.*` and `host` properties in `confOverridable.properties` are still used anywhere.

- [ ] **Delete the commented-out code**
  - `Worker.java` and `SchedulerServlet.java`: Quartz cleanup of generated files, pointless now that nothing is written to disk.
  - `Landing.java`: the JAX-RS hello world.
  - The javax annotations in `IdentificationKeyImpl`.
  - The jersey/quartz/servlet-api dependency blocks in `pom.xml`.
  - Git history keeps all of it.

- [ ] **Delete `src/main/webapp/`**
  - `web.xml` still declares the Jersey `ServletContainer`, `QuartzInitializerServlet` and `SchedulerServlet`, and `outputFiles/empty.txt` is a leftover.
  - Since the module now builds a `jar`, none of it is used.
  - Also check the root `pom.xml` / `.idea/runConfigurations/Tomcat.xml` for leftover WAR/Tomcat config.

- [ ] **Remove `/hello` or turn it into `/health`**
  - Keep a lightweight endpoint for Docker/compose healthchecks (`HEALTHCHECK` / `healthcheck:`), returning 200 + `ok` or build info (the old `IkeyInfo` resource did something like that).

- [ ] **Copy the verbosity set in `IkeyConfigBuilder.build()`**
  - `build()` passes the builder's internal `HashSet` straight into `IkeyConfig`. A builder reused after `build()`, or a caller mutating `getVerbosity()`, changes the config.
  - Pass `Set.copyOf(verbosity)`, or an `EnumSet.copyOf` when it's non-empty.

## Docker

- [x] **Change the base image**
  - [x] `openjdk:25-ea-1-slim` is an early-access build, and the `openjdk` images are deprecated/unmaintained.
  - [x] Use `eclipse-temurin:25-jre` (or `-alpine`), and pin the tag.
  - [x] Add a non-root `USER`, and a `HEALTHCHECK` once `/health` exists.

- [x] **Stop hard-coding the jar name**
  - `COPY target/ikey-plus-rest-2.0-SNAPSHOT-jar-with-dependencies.jar` breaks on any version bump.
  - [x] Either set `<finalName>ikey-plus-rest</finalName>` + `<appendAssemblyId>false</appendAssemblyId>` in the assembly plugin, or `COPY target/*-jar-with-dependencies.jar app.jar`.
  - [x] Consider a multi-stage build (Maven stage → JRE stage) so the image builds without a local `mvn package`.
  - Add the app service to `docker-compose.yml` next to the nginx `test-files` service. Note that `localhost:10080` in `test.http` won't resolve from inside the app container, so use the service name `test-files`.

## Tests

- [ ] **Add endpoint tests with `javalin-testtools`**
  - The old `IdentificationKeyResourceTest` was deleted, and nothing covers the new endpoint.
  - Add `io.javalin:javalin-testtools` (test scope) and use `JavalinTest.test(app, (server, client) -> ...)`. This needs the app built from a factory method instead of inside `main`.
  - Serve fixtures from `ikey-plus-api/src/test/resources/inputFiles`, either with a tiny Javalin static-files server in the test or by allowing a test-only host.
  - Cases:
    - each `format` × `representation` returns 200, with the right content type and output matching the existing `fixtures/genetta*` files
    - missing `sddURL` → 400
    - non-http scheme / local path → 400
    - unreachable host → 502
    - malformed XML → 400/422
    - XXE payload → rejected, no file contents leaked
    - `pdf`/`zip` → 501
    - unknown `format` → 400
