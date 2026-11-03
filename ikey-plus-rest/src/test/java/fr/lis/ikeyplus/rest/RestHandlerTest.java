package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.utils.IkeyConfig;
import fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation;
import fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat;
import fr.lis.ikeyplus.utils.IkeyConfig.Verbosity;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.mock.ContextMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static fr.lis.ikeyplus.rest.RestHandler.getParamOrThrow;
import static fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation.FLAT;
import static fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation.TREE;
import static fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat.DOT;
import static fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat.TXT;
import static fr.lis.ikeyplus.utils.IkeyConfig.ScoreMethod.XPER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.HEADER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.OTHER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.STATISTICS;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.WARNING;
import static fr.lis.ikeyplus.utils.IkeyConfig.WeightContext.NO_WEIGHT;
import static fr.lis.ikeyplus.utils.IkeyConfig.WeightType.GLOBAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestHandlerTest {

    private static void doTest(final String queryString, final Consumer<Context> assertion) {
        ContextMock
                .create(c -> {
                    c.getReq().queryString = queryString;
                    c.javalinConfig(ConverterRegistrar::registerConverters);
                })
                .execute(assertion);
    }

    @Nested
    @DisplayName("getParamOrThrow")
    class GetParamOrThrowTest {


        @Test
        void should_extract_format() {
            doTest(
                    "format=dot&keyRepresentation=flat",
                    ctx -> assertThat(getParamOrThrow(ctx, "format", OutputFormat.class, TXT))
                            .isSameAs(DOT));
        }

        @Test
        void should_extract_format_ignoring_case() {
            doTest(
                    "format=DoT&keyRepresentation=flat",
                    ctx -> assertThat(getParamOrThrow(ctx, "format", OutputFormat.class, TXT))
                            .isSameAs(DOT));
        }

        @Test
        void should_default_format() {
            doTest(
                    "keyRepresentation=flat",
                    ctx -> assertThat(getParamOrThrow(ctx, "format", OutputFormat.class, TXT))
                            .isSameAs(TXT)
            );
        }

        @Test
        void should_throw_when_wrong_format() {
            doTest("format=NOOP&keyRepresentation=flat",
                    ctx ->
                            assertThatThrownBy(() -> getParamOrThrow(ctx, "format", OutputFormat.class, TXT))
                                    .isInstanceOf(BadRequestResponse.class)
            );
        }

        @Test
        void should_extract_key_representation() {
            doTest("keyRepresentation=flat",
                    ctx ->
                            assertThat(getParamOrThrow(ctx, "keyRepresentation", KeyRepresentation.class, TREE))
                                    .isSameAs(FLAT)

            );
        }

        @Test
        void should_default_key_representation() {
            doTest("format=txt",
                    ctx ->
                            assertThat(getParamOrThrow(ctx, "keyRepresentation", KeyRepresentation.class, TREE))
                                    .isSameAs(TREE)

            );
        }

        @Test
        void should_throw_when_wrong_key_representation() {
            doTest("format=txt&keyRepresentation=noop",
                    ctx ->
                            assertThatThrownBy(() -> getParamOrThrow(ctx, "keyRepresentation", OutputFormat.class, TXT))
                                    .isInstanceOf(BadRequestResponse.class)
            );
        }

        @Test
        void should_extract_fewStatesCharacterFirst() {
            doTest("fewStatesCharacterFirst=true",
                    ctx ->
                            assertThat(getParamOrThrow(ctx, "fewStatesCharacterFirst", Boolean.class, false))
                                    .isSameAs(true)

            );
        }

        @Test
        void should_default_fewStatesCharacterFirst() {
            doTest("format=txt",
                    ctx ->
                            assertThat(getParamOrThrow(ctx, "fewStatesCharacterFirst", Boolean.class, false))
                                    .isSameAs(false)

            );
        }

        @Test
        void should_throw_when_wrong_fewStatesCharacterFirst() {
            doTest("format=txt&fewStatesCharacterFirst=noop",
                    ctx ->
                            assertThatThrownBy(() -> getParamOrThrow(ctx, "fewStatesCharacterFirst", Boolean.class, false))
                                    .isInstanceOf(BadRequestResponse.class)
            );
        }

        @Test
        void should_extract_verbosity() {
            doTest("verbosity=hows",
                    ctx -> {
                        final Verbosity verbosity = getParamOrThrow(ctx, "verbosity", Verbosity.class, Verbosity.getDefault());
                        assertThat(verbosity.levels())
                                .containsExactlyInAnyOrderElementsOf(Set.of(HEADER, OTHER, WARNING, STATISTICS));
                    }
            );
        }

        @Test
        void should_default_verbosity_flags() {
            final Verbosity defVerbosity = Verbosity.getDefault();
            doTest("format=dot",
                    ctx -> assertThat(getParamOrThrow(ctx, "verbosity", Verbosity.class, defVerbosity).levels())
                            .isEmpty()
            );
        }

        @Test
        void should_throw_when_wrong_verbosity_flag() {
            final Verbosity defVerbosity = Verbosity.getDefault();
            doTest("verbosity=hoxs",
                    ctx -> assertThatThrownBy(() -> getParamOrThrow(ctx, "verbosity", Verbosity.class, defVerbosity))
                            .isInstanceOf(BadRequestResponse.class)
            );
        }
    }

    @Nested
    @DisplayName("validateSddUrl")
    class ValidateSddUrlTest {

        @Test
        void should_validate_normal_http_url() {
            final var result = RestHandler.validateSddUrl("http://google.com");
            assertThat(result).isNotNull();
        }

        @Test
        void should_validate_normal_https_url() {
            final var result = RestHandler.validateSddUrl("https://google.com");
            assertThat(result).isNotNull();
        }

        @Test
        void should_not_validate_normal_ftp_url() {
            try {
                RestHandler.validateSddUrl("ftp://google.com");
                throw new RuntimeException();
            } catch (final Exception expected) {
                assertThat(expected.getMessage()).isEqualTo("sddUrl protocol should be http or https");
            }
        }

        @Test
        void should_not_validate_null_url() {
            try {
                RestHandler.validateSddUrl(null);
                throw new RuntimeException();
            } catch (final Exception expected) {
                assertThat(expected.getMessage()).isEqualTo("sddUrl cannot be null");
            }
        }

        @Test
        void should_not_validate_malformed_url() {
            try {
                RestHandler.validateSddUrl("../etc");
                throw new RuntimeException();
            } catch (final BadRequestResponse expected) {
                assertThat(expected.getMessage()).isEqualTo("sddUrl is malformed");
            }
        }
    }

    @Nested
    @DisplayName("initializeConfig")
    class InitializeConfigTest {

        @Test
        void should_init_with_defaults() {
            doTest("", ctx -> {
                final IkeyConfig result = RestHandler.initializeConfig(ctx);
                assertThat(result.getFormat()).isSameAs(TXT);
                assertThat(result.getScoreMethod()).isSameAs(XPER);
                assertThat(result.getVerbosity()).isEmpty();
                assertThat(result.getRepresentation()).isSameAs(TREE);
                assertThat(result.getWeightType()).isSameAs(GLOBAL);
                assertThat(result.getWeightContext()).isSameAs(NO_WEIGHT);
                assertThat(result.isFewStatesCharacterFirst()).isFalse();
                assertThat(result.isPruningEnabled()).isFalse();
                assertThat(result.isMergeCharacterStatesIfSameDiscrimination()).isFalse();
            });
        }
    }

    @Nested
    @DisplayName("generateKey")
    class GenerateKeyTest {
        @Test
        void should_generate_key() throws Exception {
            final String expected;
            try (final var stream = Files.lines(Path.of("src/test/resources/expected_genetta_txt_key.txt"))) {
                expected = stream.collect(Collectors.joining("\n"));
            }

            final IkeyConfig config = IkeyConfig.builder()
                    .enablePruning()
                    .verbosity(HEADER)
                    .representation(TREE)
                    .enablePruning(false)
                    .format(TXT)
                    .build();
            final String result = RestHandler.generateKey(
                    new FileInputStream("src/test/resources/genetta.sdd.xml"),
                    config,
                    "src/test/resources/genetta.sdd.xml"
            ).keyContent();

            assertThat(result).isEqualTo(expected);
        }
    }
}