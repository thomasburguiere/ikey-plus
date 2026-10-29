package fr.lis.ikeyplus.rest;

import io.javalin.Javalin;

public class Application {

    public static void main(final String[] args) {
        Javalin.create(config -> {
            config.routes.get("/health", ctx -> ctx.result("ok"));
            config.routes.get("/identification-key", RestHandler::handleGetKey);
            ExceptionMapper.mapExceptions(config);
        }).start(8080);
    }

}
