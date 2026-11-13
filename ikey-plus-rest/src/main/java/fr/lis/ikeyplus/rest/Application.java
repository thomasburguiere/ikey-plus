package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.rest.config.AppConfig;
import io.javalin.Javalin;
import io.javalin.http.Handler;

import java.io.IOException;

public class Application {

    public static void main(final String[] args) throws IOException {
        buildApplication(AppConfig.load()).start(8080);
    }

    public static Javalin buildApplication(final AppConfig appConfig) {
        final UrlFetcher fetcher = new UrlFetcher(appConfig.allowPrivateAddresses());
        final Handler getKeyHandler = new RestHandler(fetcher);

        return Javalin.create(config -> {
            config.routes.get("/health", ctx -> ctx.result("ok"));
            config.routes.get("/identification-key", getKeyHandler);
            ExceptionMapper.mapExceptions(config);
        });
    }
}
