package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.utils.IkeyConfig;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.mock.ContextMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.Consumer;

import static fr.lis.ikeyplus.rest.InputParser.getParamOrThrow;
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

class InputParserTest {

    private static void doTest(final String queryString, final Consumer<Context> assertion) {
        ContextMock
                .create(c -> {
                    c.getReq().queryString = queryString;
                    c.javalinConfig(ConverterRegistrar::registerConverters);
                })
                .execute(assertion);
    }

    @Nested
    @DisplayName("validateSddUrl")
    class ValidateSddUrlTest {

        @Test
        void should_validate_normal_http_url() {
            final var result = InputParser.validateSddUrl("http://google.com");
            assertThat(result).isNotNull();
        }

        @Test
        void should_validate_normal_https_url() {
            final var result = InputParser.validateSddUrl("https://google.com");
            assertThat(result).isNotNull();
        }

        @Test
        void should_not_validate_normal_ftp_url() {
            try {
                InputParser.validateSddUrl("ftp://google.com");
                throw new RuntimeException();
            } catch (final Exception expected) {
                assertThat(expected.getMessage()).isEqualTo("sddUrl protocol should be http or https");
            }
        }

        @Test
        void should_not_validate_null_url() {
            try {
                InputParser.validateSddUrl(null);
                throw new RuntimeException();
            } catch (final Exception expected) {
                assertThat(expected.getMessage()).isEqualTo("sddUrl cannot be null");
            }
        }

        @Test
        void should_not_validate_malformed_url() {
            try {
                InputParser.validateSddUrl("../etc");
                throw new RuntimeException();
            } catch (final BadRequestResponse expected) {
                assertThat(expected.getMessage()).isEqualTo("sddUrl is malformed");
            }
        }
    }

    @Nested
    @DisplayName("getParamOrThrow")
    class GetParamOrThrowTest {

        @Test
        void should_extract_format() {
            doTest(
                    "format=dot&keyRepresentation=flat",
                    ctx -> assertThat(getParamOrThrow(ctx, "format", IkeyConfig.OutputFormat.class, TXT))
                            .isSameAs(DOT));
        }

        @Test
        void should_extract_format_ignoring_case() {
            doTest(
                    "format=DoT&keyRepresentation=flat",
                    ctx -> assertThat(getParamOrThrow(ctx, "format", IkeyConfig.OutputFormat.class, TXT))
                            .isSameAs(DOT));
        }

        @Test
        void should_default_format() {
            doTest(
                    "keyRepresentation=flat",
                    ctx -> assertThat(getParamOrThrow(ctx, "format", IkeyConfig.OutputFormat.class, TXT))
                            .isSameAs(TXT)
            );
        }

        @Test
        void should_throw_when_wrong_format() {
            doTest("format=NOOP&keyRepresentation=flat",
                    ctx ->
                            assertThatThrownBy(() -> getParamOrThrow(ctx, "format", IkeyConfig.OutputFormat.class, TXT))
                                    .isInstanceOf(BadRequestResponse.class)
            );
        }

        @Test
        void should_extract_key_representation() {
            doTest("keyRepresentation=flat",
                    ctx ->
                            assertThat(getParamOrThrow(ctx, "keyRepresentation", IkeyConfig.KeyRepresentation.class, TREE))
                                    .isSameAs(FLAT)

            );
        }

        @Test
        void should_default_key_representation() {
            doTest("format=txt",
                    ctx ->
                            assertThat(getParamOrThrow(ctx, "keyRepresentation", IkeyConfig.KeyRepresentation.class, TREE))
                                    .isSameAs(TREE)

            );
        }

        @Test
        void should_throw_when_wrong_key_representation() {
            doTest("format=txt&keyRepresentation=noop",
                    ctx ->
                            assertThatThrownBy(() -> getParamOrThrow(ctx, "keyRepresentation", IkeyConfig.OutputFormat.class, TXT))
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
                        final IkeyConfig.Verbosity verbosity = getParamOrThrow(ctx, "verbosity", IkeyConfig.Verbosity.class, IkeyConfig.Verbosity.getDefault());
                        assertThat(verbosity.levels())
                                .containsExactlyInAnyOrderElementsOf(Set.of(HEADER, OTHER, WARNING, STATISTICS));
                    }
            );
        }

        @Test
        void should_default_verbosity_flags() {
            final IkeyConfig.Verbosity defVerbosity = IkeyConfig.Verbosity.getDefault();
            doTest("format=dot",
                    ctx -> assertThat(getParamOrThrow(ctx, "verbosity", IkeyConfig.Verbosity.class, defVerbosity).levels())
                            .isEmpty()
            );
        }

        @Test
        void should_throw_when_wrong_verbosity_flag() {
            final IkeyConfig.Verbosity defVerbosity = IkeyConfig.Verbosity.getDefault();
            doTest("verbosity=hoxs",
                    ctx -> assertThatThrownBy(() -> getParamOrThrow(ctx, "verbosity", IkeyConfig.Verbosity.class, defVerbosity))
                            .isInstanceOf(BadRequestResponse.class)
            );
        }
    }

    @Nested
    @DisplayName("initializeConfig")
    class InitializeConfigTest {

        @Test
        void should_init_with_defaults() {
            doTest("", ctx -> {
                final IkeyConfig result = InputParser.initializeConfig(ctx);
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
}