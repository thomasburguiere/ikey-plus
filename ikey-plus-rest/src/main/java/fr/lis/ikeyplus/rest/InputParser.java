package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.utils.IkeyConfig;
import fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

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
import static fr.lis.ikeyplus.utils.IkeyConfig.Verbosity;
import static fr.lis.ikeyplus.utils.IkeyConfig.builder;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

public class InputParser  {
    private InputParser() {
    }

    private static final Function<String, Boolean> booleanParser = s -> {
        if (s.equalsIgnoreCase("true")) return true;
        if (s.equalsIgnoreCase("false")) return false;
        throw new IllegalArgumentException(format("expected 'true' or 'false', got '%s'", s));
    };

    private static final Function<String, IkeyConfig.OutputFormat> outputFormatParser =
            s -> Optional.ofNullable(IkeyConfig.OutputFormat.fromString(s))
                    .orElseThrow(() -> new IllegalArgumentException(format("expected one of %s, got '%s'", Arrays.toString(IkeyConfig.OutputFormat.values()), s)));

    private static final Function<String, IkeyConfig.KeyRepresentation> representationParser =
            s -> Optional.ofNullable(IkeyConfig.KeyRepresentation.fromString(s))
                    .orElseThrow(() -> new IllegalArgumentException(format("expected one of %s, got '%s'", Arrays.toString(IkeyConfig.KeyRepresentation.values()), s)));

    private static final Function<String, IkeyConfig.ScoreMethod> scoreMethodParser =
            s -> Optional.ofNullable(IkeyConfig.ScoreMethod.fromString(s))
                    .orElseThrow(() -> new IllegalArgumentException(format("expected one of %s, got '%s'", Arrays.toString(IkeyConfig.ScoreMethod.values()), s)));

    private static final Set<String> ALLOWED_VERBOSITY_FLAGS = stream(IkeyConfig.VerbosityLevel.values())
            .map(IkeyConfig.VerbosityLevel::toString)
            .collect(toSet());

    private static final Function<String, IkeyConfig.Verbosity> verbosityParser = s -> {
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

    public static IkeyConfig initializeConfig(Context ctx) {
        return builder()
                .format(parseQueryParam(ctx, "format", outputFormatParser, OutputFormat.TXT))
                .representation(parseQueryParam(ctx, "keyRepresentation", representationParser, TREE))
                .fewStatesCharacterFirst(parseQueryParam(ctx, "fewStatesCharacterFirst", booleanParser, false))
                .mergeCharacterStatesIfSameDiscrimination(parseQueryParam(ctx, "mergeCharacterStatesIfSameDiscrimination", booleanParser, false))
                .enablePruning(parseQueryParam(ctx, "enablePruning", booleanParser, false))
                .verbosity(parseQueryParam(ctx, "verbosity", verbosityParser, Verbosity.getDefault()).levels())
                .scoreMethod(parseQueryParam(ctx, "scoreMethod", scoreMethodParser, XPER))
                .build();
    }

    static <T> T parseQueryParam(final Context ctx, final String name, final Function<String, T> parser, final T fallback) {
        final String rawQueryParam = ctx.queryParam(name);
        if (rawQueryParam == null || rawQueryParam.isEmpty()) {
            return fallback;
        }
        try {
            return parser.apply(rawQueryParam);
        } catch (final IllegalArgumentException e) {
            throw new BadRequestResponse(format("for parameter %s, %s", name, e.getMessage()));
        }
    }

    static URL validateSddUrl(final String sddUrl) {
        if (sddUrl == null) {
            throw new BadRequestResponse("sddUrl cannot be null");
        }
        final URL url;
        try {
            final URI uri = URI.create(sddUrl);
            url = uri.toURL();
        } catch (IllegalArgumentException | MalformedURLException _) {
            throw new BadRequestResponse("sddUrl is malformed");
        }
        if (!List.of("http", "https").contains(url.getProtocol())) {
            throw new BadRequestResponse("sddUrl protocol should be http or https");
        }
        return url;
    }
}
