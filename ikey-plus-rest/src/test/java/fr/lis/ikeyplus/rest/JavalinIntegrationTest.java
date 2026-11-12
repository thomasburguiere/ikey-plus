package fr.lis.ikeyplus.rest;

import com.sun.net.httpserver.HttpServer;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import io.javalin.testtools.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static io.javalin.http.HttpStatus.BAD_REQUEST;
import static io.javalin.http.HttpStatus.NOT_IMPLEMENTED;
import static io.javalin.http.HttpStatus.OK;
import static org.assertj.core.api.Assertions.assertThat;

public class JavalinIntegrationTest {

    private static final byte[] BROKEN_SDD = "<Datasets><not-closed>".getBytes(StandardCharsets.UTF_8);

    private static HttpServer sddServer;
    private static String genettaUrl;
    private static String brokenSddUrl;

    private final Javalin app = Application.buildApplication();

    /**
     * Serves SDD fixtures over HTTP, since the API only accepts http/https sddURLs.
     */
    @BeforeAll
    static void startSddServer() throws IOException {
        final byte[] genetta = Files.readAllBytes(Path.of("src/test/resources/genetta.sdd.xml"));
        final InetSocketAddress randomPortLocalhost = new InetSocketAddress("localhost", 0);
        sddServer = HttpServer.create(randomPortLocalhost, 0);
        sddServer.createContext("/genetta.sdd.xml", exchange -> {
            exchange.sendResponseHeaders(200, genetta.length);
            try (final OutputStream os = exchange.getResponseBody()) {
                os.write(genetta);
            }
        });
        sddServer.createContext("/broken.sdd.xml", exchange -> {
            exchange.sendResponseHeaders(200, BROKEN_SDD.length);
            try (final OutputStream os = exchange.getResponseBody()) {
                os.write(BROKEN_SDD);
            }
        });
        sddServer.start();
        final String base = "http://localhost:" + sddServer.getAddress().getPort();
        genettaUrl = base + "/genetta.sdd.xml";
        brokenSddUrl = base + "/broken.sdd.xml";
    }

    @AfterAll
    static void stopSddServer() {
        sddServer.stop(0);
    }

    private static String keyPath(final String sddUrl, final String extraQuery) {
        final String base = "/identification-key?sddURL=" + URLEncoder.encode(sddUrl, StandardCharsets.UTF_8);
        return extraQuery.isEmpty() ? base : base + "&" + extraQuery;
    }

    @Test
    void should_get_health_ok() {
        JavalinTest.test(app, (server, client) -> {
            final Response response = client.get("/health");
            assertThat(response.code()).isEqualTo(OK.getCode());
            assertThat(response.body().string()).isEqualTo("ok");
        });
    }

    @Nested
    class SddUrlValidation {

        @Test
        void should_return_400_for_null_sdd_url() {
            JavalinTest.test(app, (server, client) -> {
                final Response response = client.get("/identification-key");
                assertThat(response.code()).isEqualTo(BAD_REQUEST.getCode());
                assertThat(response.body().string()).isEqualTo("sddUrl cannot be null");
            });
        }

        @Test
        void should_return_400_for_malformed_sdd_url() {
            JavalinTest.test(app, (server, client) -> {
                final Response response = client.get(keyPath("../etc", ""));
                assertThat(response.code()).isEqualTo(BAD_REQUEST.getCode());
                assertThat(response.body().string()).isEqualTo("sddUrl is malformed");
            });
        }

        @ParameterizedTest
        @ValueSource(strings = {"ftp://localhost/genetta.sdd.xml", "file:///etc/passwd"})
        void should_return_400_for_non_http_sdd_url(final String sddUrl) {
            JavalinTest.test(app, (server, client) -> {
                final Response response = client.get(keyPath(sddUrl, ""));
                assertThat(response.code()).isEqualTo(BAD_REQUEST.getCode());
                assertThat(response.body().string()).isEqualTo("sddUrl protocol should be http or https");
            });
        }

        @Test
        void should_return_400_for_invalid_sdd_content() {
            JavalinTest.test(app, (server, client) -> {
                final Response response = client.get(keyPath(brokenSddUrl, ""));
                assertThat(response.code()).isEqualTo(BAD_REQUEST.getCode());
                assertThat(response.body().string()).isEqualTo("provided sdd is not valid xml");
            });
        }
    }

    @Nested
    class ParameterValidation {

        public static Stream<Arguments> provideQueryParametersTestArguments() {
            return Stream.of(
                Arguments.of("enablePruning=1", "for parameter enablePruning, expected 'true' or 'false', got '1'"),
                Arguments.of("fewStatesCharacterFirst=noop", "for parameter fewStatesCharacterFirst, expected 'true' or 'false', got 'noop'"),
                Arguments.of("mergeCharacterStatesIfSameDiscrimination=yes", "for parameter mergeCharacterStatesIfSameDiscrimination, expected 'true' or 'false', got 'yes'"),
                Arguments.of("format=noop", "for parameter format, expected one of [txt, html, interactivehtml, pdf, sdd, wiki, dot, zip], got 'noop'"),
                Arguments.of("keyRepresentation=noop", "for parameter keyRepresentation, expected one of [tree, flat], got 'noop'"),
                Arguments.of("verbosity=hoxs", "for parameter verbosity, invalid flag(s) [x] in 'hoxs', allowed flags are [h, o, w, s]"),
                Arguments.of("scoreMethod=noop", "for parameter scoreMethod, expected one of [xper, jaccard, sokalAndMichener], got 'noop'")
            );
        }

        @ParameterizedTest
        @MethodSource("provideQueryParametersTestArguments")
        void should_return_400_for_invalid_parameter(final String query, final String expectedBody) {
            JavalinTest.test(app, (server, client) -> {
                final Response response = client.get(keyPath(genettaUrl, query));
                assertThat(response.code()).isEqualTo(BAD_REQUEST.getCode());
                assertThat(response.body().string()).isEqualTo(expectedBody);
            });
        }

        @ParameterizedTest
        @ValueSource(strings = {"pdf", "zip"})
        void should_return_501_for_unsupported_format(final String format) {
            JavalinTest.test(app, (server, client) -> {
                final Response response = client.get(keyPath(genettaUrl, "format=" + format));
                assertThat(response.code()).isEqualTo(NOT_IMPLEMENTED.getCode());
                assertThat(response.body().string()).isEqualTo("format not currently supported");
            });
        }
    }

    @Nested
    class KeyGeneration {

        @Test
        void should_generate_txt_tree_key_by_default() {
            JavalinTest.test(app, (server, client) -> {
                final Response response = client.get(keyPath(genettaUrl, ""));
                assertThat(response.code()).isEqualTo(OK.getCode());
                assertThat(response.headers().get("Content-Type").getFirst()).startsWith("text/plain");
                final String body = response.body().string();
                assertThat(body.strip())
                    .startsWith("1.1) Rings on tail | present")
                    .doesNotContain("Generated by IKey+");
            });
        }

        @Test
        void should_match_expected_txt_key_with_header() throws IOException {
            final String expected = Files.readString(Path.of("src/test/resources/expected_genetta_txt_key.txt"))
                .replace("sddURL=src/test/resources/genetta.sdd.xml", "sddURL=" + genettaUrl);
            JavalinTest.test(app, (server, client) -> {
                final Response response = client.get(keyPath(genettaUrl, "verbosity=h"));
                assertThat(response.code()).isEqualTo(OK.getCode());
                assertThat(response.body().string()).isEqualToIgnoringNewLines(expected);
            });
        }

        @Test
        void should_reflect_options_in_header() {
            JavalinTest.test(app, (server, client) -> {
                final Response response = client.get(keyPath(genettaUrl,
                    "verbosity=h&keyRepresentation=flat&enablePruning=true&fewStatesCharacterFirst=true"
                        + "&mergeCharacterStatesIfSameDiscrimination=true&scoreMethod=jaccard"));
                assertThat(response.code()).isEqualTo(OK.getCode());
                assertThat(response.body().string())
                    .contains("Genets")
                    .contains("sddURL=" + genettaUrl)
                    .contains("representation=flat")
                    .contains("pruning=true")
                    .contains("fewStatesCharacterFirst=true")
                    .contains("mergeCharacterStatesIfSameDiscrimination=true")
                    .contains("scoreMethod=jaccard");
            });
        }

        @ParameterizedTest(name = "format={0}, keyRepresentation={1} -> {2}")
        @CsvSource({
            "txt,             tree, text/plain",
            "txt,             flat, text/plain",
            "wiki,            tree, text/plain",
            "wiki,            flat, text/plain",
            "html,            tree, text/html",
            "html,            flat, text/html",
            "interactivehtml, tree, text/html",
            "dot,             tree, text/plain",
            "sdd,             tree, text/xml",
            "HTML,            FLAT, text/html",
        })
        void should_generate_key_in_requested_format(final String format,
                                                     final String representation,
                                                     final String expectedContentType) {
            JavalinTest.test(app, (server, client) -> {
                final Response response = client.get(keyPath(genettaUrl,
                    "format=" + format + "&keyRepresentation=" + representation));
                assertThat(response.code()).isEqualTo(OK.getCode());
                assertThat(response.headers().get("Content-Type").getFirst()).startsWith(expectedContentType);
                assertThat(response.body().string()).isNotBlank();
            });
        }

        @Test
        void should_generate_dot_graph() {
            JavalinTest.test(app, (server, client) -> {
                final Response response = client.get(keyPath(genettaUrl, "format=dot"));
                assertThat(response.code()).isEqualTo(OK.getCode());
                assertThat(response.body().string())
                    .contains("1 -> 2 [label=\"present\"];")
                    .contains("1 [label=\"Rings on tail\"];");
            });
        }

        @Test
        void should_generate_sdd_xml() {
            JavalinTest.test(app, (server, client) -> {
                final Response response = client.get(keyPath(genettaUrl, "format=sdd"));
                assertThat(response.code()).isEqualTo(OK.getCode());
                assertThat(response.body().string()).contains("<Datasets");
            });
        }

        @ParameterizedTest
        @ValueSource(strings = {"xper", "jaccard", "sokalAndMichener"})
        void should_generate_key_with_each_score_method(final String scoreMethod) {
            JavalinTest.test(app, (server, client) -> {
                final Response response = client.get(keyPath(genettaUrl, "scoreMethod=" + scoreMethod));
                assertThat(response.code()).isEqualTo(OK.getCode());
                assertThat(response.body().string()).contains("-> Genetta");
            });
        }

        @Test
        void should_produce_different_key_with_different_score_methods() {
            JavalinTest.test(app, (server, client) -> {
                final String xper = client.get(keyPath(genettaUrl, "scoreMethod=xper")).body().string();
                final String jaccard = client.get(keyPath(genettaUrl, "scoreMethod=jaccard")).body().string();
                final String sokal = client.get(keyPath(genettaUrl, "scoreMethod=sokalAndMichener")).body().string();
                assertThat(jaccard).isNotEqualTo(xper);
                assertThat(sokal).isNotEqualTo(xper);
            });
        }
    }
}
