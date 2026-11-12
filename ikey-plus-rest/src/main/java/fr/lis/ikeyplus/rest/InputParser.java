package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.utils.IkeyConfig;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation.TREE;
import static fr.lis.ikeyplus.utils.IkeyConfig.ScoreMethod.XPER;
import static fr.lis.ikeyplus.utils.IkeyConfig.builder;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class InputParser {

    private final Function<String, Optional<String>> queryParamValueRetriever;

    public InputParser(final Function<String, Optional<String>> queryParamValueRetriever) {
        this.queryParamValueRetriever = queryParamValueRetriever;
    }

    static final Function<String, Boolean> BOOLEAN_PARSER = s -> {
        if (s.equalsIgnoreCase("true")) {
            return true;
        }
        if (s.equalsIgnoreCase("false")) {
            return false;
        }
        throw new IllegalArgumentException(format("expected 'true' or 'false', got '%s'", s));
    };

    static final Function<String, IkeyConfig.OutputFormat> OUTPUT_FORMAT_PARSER =
        s -> Optional.ofNullable(IkeyConfig.OutputFormat.fromString(s))
            .orElseThrow(() -> new IllegalArgumentException(
                format("expected one of %s, got '%s'", Arrays.toString(IkeyConfig.OutputFormat.values()), s))
            );

    static final Function<String, IkeyConfig.KeyRepresentation> REPRESENTATION_PARSER =
        s -> Optional.ofNullable(IkeyConfig.KeyRepresentation.fromString(s))
            .orElseThrow(() -> new IllegalArgumentException(
                format("expected one of %s, got '%s'", Arrays.toString(IkeyConfig.KeyRepresentation.values()), s))
            );

    static final Function<String, IkeyConfig.ScoreMethod> SCORE_METHOD_PARSER =
        s -> Optional.ofNullable(IkeyConfig.ScoreMethod.fromString(s))
            .orElseThrow(() -> new IllegalArgumentException(
                format("expected one of %s, got '%s'", Arrays.toString(IkeyConfig.ScoreMethod.values()), s))
            );

    static final Set<String> ALLOWED_VERBOSITY_FLAGS = stream(IkeyConfig.VerbosityLevel.values())
        .map(IkeyConfig.VerbosityLevel::toString)
        .collect(toSet());

    static final Function<String, IkeyConfig.Verbosity> VERBOSITY_PARSER = s -> {
        final List<String> invalidFlags = stream(s.toLowerCase(Locale.ROOT).split(""))
            .filter(flag -> !ALLOWED_VERBOSITY_FLAGS.contains(flag))
            .distinct()
            .toList();
        if (!invalidFlags.isEmpty()) {
            throw new IllegalArgumentException(format("invalid flag(s) %s in '%s', allowed flags are %s",
                invalidFlags, s, stream(IkeyConfig.VerbosityLevel.values()).map(IkeyConfig.VerbosityLevel::toString).toList()));
        }
        return new IkeyConfig.Verbosity(IkeyConfig.VerbosityLevel.fromString(s));
    };

    URL validateSddUrl(final String sddUrl) {
        if (sddUrl == null) {
            throw new IllegalArgumentException("sddUrl cannot be null");
        }
        final URL url;
        try {
            final URI uri = URI.create(sddUrl);
            url = uri.toURL();
        } catch (IllegalArgumentException | MalformedURLException _) {
            throw new IllegalArgumentException("sddUrl is malformed");
        }
        if (!List.of("http", "https").contains(url.getProtocol())) {
            throw new IllegalArgumentException("sddUrl protocol should be http or https");
        }
        return url;
    }

    sealed interface Either<L, R> {
        record Left<L, R>(L value) implements Either<L, R> {}

        record Right<L, R>(R value) implements Either<L, R> {}

        default boolean isLeft() {
            return this instanceof Left<?, ?>;
        }

        default boolean isRight() {
            return this instanceof Right<?, ?>;
        }

        default R getRight() throws IllegalStateException {
            return switch (this) {
                case Right(final var rValue) -> rValue;
                case Left(final var lValue) -> throw new IllegalStateException("Left: " + lValue);
            };
        }

        default L getLeft() throws IllegalStateException {
            return switch (this) {
                case Left(final var lValue) -> lValue;
                case Right(final var rValue) -> throw new IllegalStateException("Right: " + rValue);
            };
        }

        static <L, R> Either<L, R> left(final L value) {
            return new Left<>(value);
        }

        static <L, R> Either<L, R> right(final R value) {
            return new Right<>(value);
        }
    }

    private record ParamError(String param, String message) {}

    private <T> Either<ParamError, T> parseQueryParam(final String name, final Function<String, T> parser, final T fallback) {
        final Optional<String> rawQueryParam = queryParamValueRetriever.apply(name);
        if (rawQueryParam.isEmpty() || rawQueryParam.get().isEmpty()) {
            return Either.right(fallback);
        }
        try {
            return Either.right(parser.apply(rawQueryParam.get()));
        } catch (final IllegalArgumentException e) {
            return Either.left(new ParamError(name, format("for parameter %s, %s", name, e.getMessage())));
        }
    }

    Either<Map<String, String>, IkeyConfig> initializeConfig() {

        final IkeyConfig config;
        final var format = parseQueryParam("format", OUTPUT_FORMAT_PARSER, IkeyConfig.OutputFormat.TXT);
        final var keyRepresentation = parseQueryParam("keyRepresentation", REPRESENTATION_PARSER, TREE);
        final var fewStatesCharacterFirst = parseQueryParam("fewStatesCharacterFirst", BOOLEAN_PARSER, false);
        final var mergeCharacterStatesIfSameDiscrimination = parseQueryParam("mergeCharacterStatesIfSameDiscrimination", BOOLEAN_PARSER, false);
        final var enablePruning = parseQueryParam("enablePruning", BOOLEAN_PARSER, false);
        final var verbosity = parseQueryParam("verbosity", VERBOSITY_PARSER, IkeyConfig.Verbosity.getDefault());
        final var scoreMethod = parseQueryParam("scoreMethod", SCORE_METHOD_PARSER, XPER);

        final Map<String, String> errorDetails = Stream.of(
                format,
                keyRepresentation,
                fewStatesCharacterFirst,
                mergeCharacterStatesIfSameDiscrimination,
                enablePruning,
                verbosity,
                scoreMethod
            )
            .filter(Either::isLeft)
            .map(Either::getLeft)
            .collect(toMap(ParamError::param, ParamError::message));

        if (!errorDetails.isEmpty()) {
            return Either.left(errorDetails);
        }

        config = builder()
            .format(format.getRight())
            .representation(keyRepresentation.getRight())
            .fewStatesCharacterFirst(fewStatesCharacterFirst.getRight())
            .mergeCharacterStatesIfSameDiscrimination(mergeCharacterStatesIfSameDiscrimination.getRight())
            .enablePruning(enablePruning.getRight())
            .verbosity(verbosity.getRight().levels())
            .scoreMethod(scoreMethod.getRight())
            .build();
        return Either.right(config);
    }
}
