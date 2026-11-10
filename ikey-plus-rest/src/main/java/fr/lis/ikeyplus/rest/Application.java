package fr.lis.ikeyplus.rest;

import io.javalin.Javalin;

public class Application {

    public static void main(final String[] args) {
        buildApplication().start(8080);
    }

    public static Javalin buildApplication() {
        return Javalin.create(config -> {
            config.routes.get("/health", ctx -> ctx.result("ok"));
            config.routes.get("/identification-key", RestHandler::handleGetKey);
            ExceptionMapper.mapExceptions(config);
        });
    }
}
