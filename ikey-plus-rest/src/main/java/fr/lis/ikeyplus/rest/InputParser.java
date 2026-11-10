package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.utils.IkeyConfig;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation.TREE;
import static fr.lis.ikeyplus.utils.IkeyConfig.ScoreMethod.XPER;
import static fr.lis.ikeyplus.utils.IkeyConfig.builder;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

public class InputParser {

    private final Function<String, Optional<String>> queryParamValueRetriever;

    public InputParser(Function<String, Optional<String>> queryParamValueRetriever) {
        this.queryParamValueRetriever = queryParamValueRetriever;
    }

    static final Function<String, Boolean> BOOLEAN_PARSER = s -> {
        if (s.equalsIgnoreCase("true")) return true;
        if (s.equalsIgnoreCase("false")) return false;
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

    <T> T parseQueryParam(final String name, final Function<String, T> parser, final T fallback) {
        final Optional<String> rawQueryParam = queryParamValueRetriever.apply(name);
        if (rawQueryParam.isEmpty() || rawQueryParam.get().isEmpty()) {
            return fallback;
        }
        try {
            return parser.apply(rawQueryParam.get());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(format("for parameter %s, %s", name, e.getMessage()));
        }
    }

    IkeyConfig initializeConfig() {
        return builder()
                .format(parseQueryParam("format", OUTPUT_FORMAT_PARSER, IkeyConfig.OutputFormat.TXT))
                .representation(parseQueryParam("keyRepresentation", REPRESENTATION_PARSER, TREE))
                .fewStatesCharacterFirst(parseQueryParam("fewStatesCharacterFirst", BOOLEAN_PARSER, false))
                .mergeCharacterStatesIfSameDiscrimination(parseQueryParam("mergeCharacterStatesIfSameDiscrimination", BOOLEAN_PARSER, false))
                .enablePruning(parseQueryParam("enablePruning", BOOLEAN_PARSER, false))
                .verbosity(parseQueryParam("verbosity", VERBOSITY_PARSER, IkeyConfig.Verbosity.getDefault()).levels())
                .scoreMethod(parseQueryParam("scoreMethod", SCORE_METHOD_PARSER, XPER))
                .build();
    }

}
