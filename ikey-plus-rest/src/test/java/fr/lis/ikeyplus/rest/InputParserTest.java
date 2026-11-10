package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.utils.IkeyConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation;
import static fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation.FLAT;
import static fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation.TREE;
import static fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat;
import static fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat.DOT;
import static fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat.HTML;
import static fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat.INTERACTIVE_HTML;
import static fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat.PDF;
import static fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat.SDD;
import static fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat.TXT;
import static fr.lis.ikeyplus.utils.IkeyConfig.ScoreMethod;
import static fr.lis.ikeyplus.utils.IkeyConfig.ScoreMethod.JACCARD;
import static fr.lis.ikeyplus.utils.IkeyConfig.ScoreMethod.SOKAL_AND_MICHENER;
import static fr.lis.ikeyplus.utils.IkeyConfig.ScoreMethod.XPER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.HEADER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.WARNING;
import static fr.lis.ikeyplus.utils.IkeyConfig.WeightContext.NO_WEIGHT;
import static fr.lis.ikeyplus.utils.IkeyConfig.WeightType.GLOBAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InputParserTest {

    @Nested
    @DisplayName("validateSddUrl")
    class ValidateSddUrlTest {
        InputParser parser = new InputParser(a -> Optional.of("dummy"));

        @Test
        void should_validate_normal_http_url() {
            final var result = parser.validateSddUrl("http://google.com");
            assertThat(result).isNotNull();
        }

        @Test
        void should_validate_normal_https_url() {
            final var result = parser.validateSddUrl("https://google.com");
            assertThat(result).isNotNull();
        }

        @Test
        void should_not_validate_normal_ftp_url() {
            try {
                parser.validateSddUrl("ftp://google.com");
                throw new RuntimeException();
            } catch (final Exception expected) {
                assertThat(expected.getMessage()).isEqualTo("sddUrl protocol should be http or https");
            }
        }

        @Test
        void should_not_validate_null_url() {
            try {
                parser.validateSddUrl(null);
                throw new RuntimeException();
            } catch (final Exception expected) {
                assertThat(expected.getMessage()).isEqualTo("sddUrl cannot be null");
            }
        }

        @Test
        void should_not_validate_malformed_url() {
            try {
                parser.validateSddUrl("../etc");
                throw new RuntimeException();
            } catch (final IllegalArgumentException expected) {
                assertThat(expected.getMessage()).isEqualTo("sddUrl is malformed");
            }
        }
    }

    private static Function<String, Optional<String>> paramProvider(Map<String, String> queryString) {
        return n -> Optional.ofNullable(queryString.get(n));
    }

    @Nested
    @DisplayName("initializeConfig")
    class InitializeConfigTest {

        @Test
        void should_init_with_defaults() {
            final IkeyConfig result = new InputParser(a -> Optional.empty()).initializeConfig();
            assertThat(result.getFormat()).isSameAs(TXT);
            assertThat(result.getScoreMethod()).isSameAs(XPER);
            assertThat(result.getVerbosity()).isEmpty();
            assertThat(result.getRepresentation()).isSameAs(TREE);
            assertThat(result.getWeightType()).isSameAs(GLOBAL);
            assertThat(result.getWeightContext()).isSameAs(NO_WEIGHT);
            assertThat(result.isFewStatesCharacterFirst()).isFalse();
            assertThat(result.isPruningEnabled()).isFalse();
            assertThat(result.isMergeCharacterStatesIfSameDiscrimination()).isFalse();
        }

        @Test
        void should_reject_invalid_enablePruning() {
            final var parser = new InputParser(paramProvider(Map.of("enablePruning", "noop")));
            assertThatThrownBy(parser::initializeConfig)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("for parameter enablePruning, expected 'true' or 'false', got 'noop'");
        }

        private static Stream<Arguments> pruningValidArgs() {
            return Stream.of(
                    Arguments.of("true", true),
                    Arguments.of("TRUE", true),
                    Arguments.of("True", true),
                    Arguments.of("false", false),
                    Arguments.of("FALSE", false),
                    Arguments.of("fAlse", false)
            );
        }

        @ParameterizedTest
        @MethodSource("pruningValidArgs")
        void should_accept_valid_enablePruning_ignoring_case(final String paramValue, final boolean expected) {
            final var parser = new InputParser(paramProvider(Map.of("enablePruning", paramValue)));
            assertThat(parser.initializeConfig().isPruningEnabled()).isEqualTo(expected);
        }

        @Test
        void should_default_empty_enablePruning_to_false() {
            final var parser = new InputParser(paramProvider(Map.of("enablePruning", "")));
            assertThat(parser.initializeConfig().isPruningEnabled()).isFalse();
        }

        @Test
        void should_reject_invalid_fewStatesCharacterFirst() {
            final var parser = new InputParser(paramProvider(Map.of("fewStatesCharacterFirst", "noop")));
            assertThatThrownBy(parser::initializeConfig)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("for parameter fewStatesCharacterFirst, expected 'true' or 'false', got 'noop'");
        }

        private static Stream<Arguments> fewStatesCharacterFirstValidArgs() {
            return Stream.of(
                    Arguments.of("true", true),
                    Arguments.of("TRUE", true),
                    Arguments.of("True", true),
                    Arguments.of("false", false),
                    Arguments.of("FALSE", false),
                    Arguments.of("fAlse", false)
            );
        }

        @ParameterizedTest
        @MethodSource("fewStatesCharacterFirstValidArgs")
        void should_accept_valid_fewStatesCharacterFirst_ignoring_case(final String value, final boolean expected) {
            final var parser = new InputParser(paramProvider(Map.of("fewStatesCharacterFirst", value)));
            assertThat(parser.initializeConfig().isFewStatesCharacterFirst()).isEqualTo(expected);
        }

        @Test
        void should_default_empty_fewStatesCharacterFirst_to_false() {
            final var parser = new InputParser(paramProvider(Map.of("fewStatesCharacterFirst", "")));
            assertThat(parser.initializeConfig().isFewStatesCharacterFirst()).isFalse();
        }

        @Test
        void should_reject_invalid_format() {
            final var parser = new InputParser(paramProvider(Map.of("format", "noop")));
            assertThatThrownBy(() -> parser.initializeConfig())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("for parameter format, expected one of [txt, html, interactivehtml, pdf, sdd, wiki, dot, zip], got 'noop'");
        }

        private static Stream<Arguments> validFormatArguments() {
            return Stream.of(
                    Arguments.of("pdf", PDF),
                    Arguments.of("Sdd", SDD),
                    Arguments.of("HTML", HTML),
                    Arguments.of("interactivehtml", INTERACTIVE_HTML),
                    Arguments.of("", TXT)
            );
        }

        @ParameterizedTest(name = "for query param value {0} -> should return {1}")
        @MethodSource("validFormatArguments")
        void should_accept_valid_format_ignoring_case(final String value, final OutputFormat expected) {
            final var parser = new InputParser(paramProvider(Map.of("format", value)));
            assertThat(parser.initializeConfig().getFormat()).isSameAs(expected);
        }

        @Test
        void should_default_empty_format_to_txt() {
            final var parser = new InputParser(paramProvider(Map.of("format", "")));
            assertThat(parser.initializeConfig().getFormat()).isSameAs(TXT);
        }

        @Test
        void should_reject_invalid_keyRepresentation() {
            final var parser = new InputParser(paramProvider(Map.of("keyRepresentation", "noop")));
            assertThatThrownBy(() -> parser.initializeConfig())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("for parameter keyRepresentation, expected one of [tree, flat], got 'noop'");
        }

        private static Stream<Arguments> validKeyRepresentationArguments() {
            return Stream.of(
                    Arguments.of("flat", FLAT),
                    Arguments.of("Flat", FLAT),
                    Arguments.of("tree", TREE),
                    Arguments.of("Tree", TREE),
                    Arguments.of("", TREE)
            );
        }

        @ParameterizedTest(name = "for query param value {0} -> should return {1}")
        @MethodSource("validKeyRepresentationArguments")
        void should_accept_valid_keyRepresentation_ignoring_case(final String value, final KeyRepresentation expected) {
            final var parser = new InputParser(paramProvider(Map.of("keyRepresentation", value)));
            assertThat(parser.initializeConfig().getRepresentation()).isSameAs(expected);
        }

        @Test
        void should_default_empty_keyRepresentation_to_tree() {
            final var parser = new InputParser(paramProvider(Map.of("keyRepresentation", "")));
            assertThat(parser.initializeConfig().getRepresentation()).isSameAs(TREE);
        }

        @Test
        void should_reject_invalid_scoreMethod() {
            final var parser = new InputParser(paramProvider(Map.of("scoreMethod", "noop")));
            assertThatThrownBy(() -> parser.initializeConfig())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("for parameter scoreMethod, expected one of [xper, jaccard, sokalAndMichener], got 'noop'");
        }

        private static Stream<Arguments> validScoreMethodArguments() {
            return Stream.of(
                    Arguments.of("xper", XPER),
                    Arguments.of("Jaccard", JACCARD),
                    Arguments.of("sokalAndMichener", SOKAL_AND_MICHENER),
                    Arguments.of("", XPER)
            );
        }

        @ParameterizedTest(name = "for query param value {0} -> should return {1}")
        @MethodSource("validScoreMethodArguments")
        void should_accept_valid_scoreMethod_ignoring_case(final String value, final ScoreMethod expected) {
            final var parser = new InputParser(paramProvider(Map.of("scoreMethod", value)));
            assertThat(parser.initializeConfig().getScoreMethod()).isSameAs(expected);
        }

        @Test
        void should_default_empty_scoreMethod_to_xper() {
            final var parser = new InputParser(paramProvider(Map.of("scoreMethod", "")));
            assertThat(parser.initializeConfig().getScoreMethod()).isSameAs(XPER);
        }

        @Test
        void should_reject_invalid_verbosity() {
            final var parser = new InputParser(paramProvider(Map.of("verbosity", "hoxs")));
            assertThatThrownBy(() -> parser.initializeConfig())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("for parameter verbosity, invalid flag(s) [x] in 'hoxs', allowed flags are [h, o, w, s]");
        }

        @Test
        void should_init_with_all_params_set() {
            final var parser = new InputParser(paramProvider(Map.of(
                    "format", "html",
                    "keyRepresentation", "flat",
                    "fewStatesCharacterFirst", "true",
                    "mergeCharacterStatesIfSameDiscrimination", "true",
                    "enablePruning", "true",
                    "verbosity", "hw",
                    "scoreMethod", "jaccard"
            )));
            final IkeyConfig result = parser.initializeConfig();
            assertThat(result.getFormat()).isSameAs(HTML);
            assertThat(result.getRepresentation()).isSameAs(FLAT);
            assertThat(result.isFewStatesCharacterFirst()).isTrue();
            assertThat(result.isMergeCharacterStatesIfSameDiscrimination()).isTrue();
            assertThat(result.isPruningEnabled()).isTrue();
            assertThat(result.getVerbosity()).containsExactlyInAnyOrder(HEADER, WARNING);
            assertThat(result.getScoreMethod()).isSameAs(ScoreMethod.JACCARD);
        }

        @Test
        void should_ignore_unknown_params() {
            final var parser = new InputParser(paramProvider(Map.of(
                    "kurwa", "mac",
                    "format", "dot"
            )));
            final IkeyConfig result = parser.initializeConfig();
            assertThat(result.getFormat()).isSameAs(DOT);
            assertThat(result.getRepresentation()).isSameAs(TREE);
        }
    }
}