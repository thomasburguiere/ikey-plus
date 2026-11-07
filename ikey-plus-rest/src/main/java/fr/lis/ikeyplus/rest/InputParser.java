package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.utils.IkeyConfig;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.validation.ValidationError;
import io.javalin.validation.Validator;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation.TREE;
import static fr.lis.ikeyplus.utils.IkeyConfig.ScoreMethod.XPER;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

public class InputParser {
    private InputParser() {
    }

    public static IkeyConfig initializeConfig(final Context ctx) {
        return IkeyConfig.builder()
                .format(getParamOrThrow(ctx, "format", IkeyConfig.OutputFormat.class, IkeyConfig.OutputFormat.TXT))
                .representation(getParamOrThrow(ctx, "keyRepresentation", IkeyConfig.KeyRepresentation.class, TREE))
                .fewStatesCharacterFirst(getParamOrThrow(ctx, "fewStatesCharacterFirst", Boolean.class, false))
                .mergeCharacterStatesIfSameDiscrimination(getParamOrThrow(ctx, "mergeCharacterStatesIfSameDiscrimination", Boolean.class, false))
                .enablePruning(getParamOrThrow(ctx, "enablePruning", Boolean.class, false))
                .verbosity(getParamOrThrow(ctx, "verbosity", IkeyConfig.Verbosity.class, IkeyConfig.Verbosity.getDefault()).levels())
                .scoreMethod(getParamOrThrow(ctx, "scoreMethod", IkeyConfig.ScoreMethod.class, XPER))
                .build();
    }

    static <T> T getParamOrThrow(final Context ctx, final String name, final Class<T> clazz, final T fallback) {
        final Validator<T> tValidator = ctx.queryParamAsClass(name, clazz);
        if (!tValidator.hasValue()) {
            return fallback;
        }
        return tValidator.getOrThrow(errorMap -> {
            final String errorMessage = errorMap.entrySet().stream()
                    .map(e -> {
                        final var message = ofNullable(e.getValue().getFirst()).map(ValidationError::exception).map(Throwable::getMessage).orElse("");
                        return format("for parameter %s, %s", e.getKey(), message);
                    })
                    .reduce("", String::concat);
            return new BadRequestResponse(errorMessage);
        });
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
