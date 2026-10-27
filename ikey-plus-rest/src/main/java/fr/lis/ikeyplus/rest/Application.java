package fr.lis.ikeyplus.rest;

import io.javalin.Javalin;
import io.javalin.http.Handler;

public class Application {

    private static final IdentificationKeyImpl ik = new IdentificationKeyImpl();

    static void main() {
        Javalin.create(config -> {
            config.routes.get("/hello", ctx -> ctx.result("Hello World"));
            config.routes.get("/identification-key", handleGetKey);
        }).start(8080);
    }

    static Handler handleGetKey = ctx -> {
        final String sddURL = ctx.queryParam("sddURL");
        final String format = ctx.queryParam("format");
        final String representation = ctx.queryParam("representation");
        final boolean fewStatesCharacterFirst = "true".equals(ctx.queryParam("fewStatesCharacterFirst"));
        final boolean mergeCharacterStatesIfSameDiscrimination = "true".equals(ctx.queryParam("mergeCharacterStatesIfSameDiscrimination"));
        final boolean pruning = "true".equals(ctx.queryParam("pruning"));
        final String verbosity = ctx.queryParam("verbosity");
        final String scoreMethod = ctx.queryParam("scoreMethod");
        final String weightContext = ctx.queryParam("weightContext");
        final String weightType = ctx.queryParam("weightType");

        var key = ik.createIdentificationKey(
                sddURL,
                format,
                representation,
                fewStatesCharacterFirst,
                mergeCharacterStatesIfSameDiscrimination,
                pruning,
                verbosity,
                scoreMethod,
                weightContext,
                weightType
        );
        ctx.result(key);
    };
}
