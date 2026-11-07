package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation;
import fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat;
import fr.lis.ikeyplus.utils.IkeyConfig.ScoreMethod;
import fr.lis.ikeyplus.utils.IkeyConfig.Verbosity;
import fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel;
import fr.lis.ikeyplus.utils.IkeyConfig.WeightContext;
import fr.lis.ikeyplus.utils.IkeyConfig.WeightType;
import io.javalin.config.JavalinConfig;
import io.javalin.http.BadRequestResponse;

import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

public class ConverterRegistrar {

    private static final Set<String> ALLOWED_VERBOSITY_FLAGS = stream(VerbosityLevel.values())
            .map(VerbosityLevel::toString)
            .collect(toSet());

    private ConverterRegistrar() {
    }

    public static void registerConverters(final JavalinConfig config) {
        config.validation.register(OutputFormat.class, OutputFormat::fromString);
        config.validation.register(KeyRepresentation.class, KeyRepresentation::fromString);
        config.validation.register(Boolean.class, s -> {
            if (s.equalsIgnoreCase("true")) return true;
            if (s.equalsIgnoreCase("false")) return false;
            throw new BadRequestResponse(String.format("expected 'true' or 'false', got '%s'", s));
        });
        config.validation.register(Verbosity.class, s -> {
            if (!ALLOWED_VERBOSITY_FLAGS.containsAll(stream(s.split("")).toList())) {
                throw new BadRequestResponse("ku");
            }
            return new Verbosity(VerbosityLevel.fromString(s));
        });
        config.validation.register(ScoreMethod.class, ScoreMethod::fromString);
        config.validation.register(WeightContext.class, WeightContext::fromString);
        config.validation.register(WeightType.class, WeightType::fromString);
    }
}
