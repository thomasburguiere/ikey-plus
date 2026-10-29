package fr.lis.ikeyplus.rest;

import io.javalin.config.JavalinConfig;
import io.javalin.http.BadRequestResponse;
import org.xml.sax.SAXException;

public class ExceptionMapper {
    private ExceptionMapper() {
    }

    public static void mapExceptions(final JavalinConfig config) {
        config.routes.exception(SAXException.class, (exception, ctx) -> {
            throw new BadRequestResponse("Bad request");
        });
    }
}
