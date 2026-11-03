package fr.lis.ikeyplus.rest;

import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import org.xml.sax.SAXParseException;

import static io.javalin.http.HttpStatus.BAD_REQUEST;

public class ExceptionMapper {
    private ExceptionMapper() {
    }

    public static void mapExceptions(final JavalinConfig config) {
        config.routes.exception(SAXParseException.class, (_, ctx) -> {
            ctx.status(BAD_REQUEST).result("provided sdd is not valid xml");
        });
    }
}
