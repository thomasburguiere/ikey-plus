package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.services.IdentificationKeyGenerator;
import fr.lis.ikeyplus.utils.IkeyConfig;
import fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation;
import fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat;
import fr.lis.ikeyplus.utils.IkeyConfig.ScoreMethod;
import fr.lis.ikeyplus.utils.IkeyConfig.Verbosity;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.validation.Validator;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateDotString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateFlatHtmlString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateFlatString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateFlatWikiString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateHtmlString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateInteractiveHtmlString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateSddString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateTreeString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateTreeWiki;
import static fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation.TREE;
import static fr.lis.ikeyplus.utils.IkeyConfig.ScoreMethod.XPER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.STATISTICS;
import static io.javalin.http.HttpStatus.NOT_IMPLEMENTED;

public class RestHandler {
    private static final String CONTENT_TYPE_XML = "text/xml";
    private static final String CONTENT_TYPE_TXT = "text/plain";

    private static final IdentificationKeyImpl ik = new IdentificationKeyImpl();

    static void handleGetKey(final Context ctx) throws Exception {
        final String sddURL = ctx.queryParam("sddURL");
        validateSddUrl(sddURL);

        final IkeyConfig config = initializeConfig(ctx);

        final var validatedSddUrl = validateSddUrl(sddURL);
        final var sddSaxParser = new SDDSaxParser(validatedSddUrl.openStream(), config);

        // call identification key service
        final IdentificationKeyGenerator identificationKeyGenerator =
                new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();

        final var tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();

        final String header = "TODO HEADER";
        final boolean showStatistics = config.getVerbosity().contains(STATISTICS);
        switch (config.getFormat()) {
            case TXT -> {
                switch (config.getRepresentation()) {
                    case TREE -> ctx.result(generateTreeString(tree2dump)).contentType(CONTENT_TYPE_TXT);
                    case FLAT -> ctx.result(generateFlatString(tree2dump)).contentType(CONTENT_TYPE_TXT);
                }
            }
            case WIKI -> {
                switch (config.getRepresentation()) {
                    case TREE -> ctx.result(generateTreeWiki(tree2dump, showStatistics)).contentType(CONTENT_TYPE_TXT);
                    case FLAT -> ctx.result(generateFlatWikiString(tree2dump)).contentType(CONTENT_TYPE_TXT);
                }
            }
            case HTML -> {
                switch (config.getRepresentation()) {
                    case TREE -> ctx.html(generateHtmlString(header, tree2dump, showStatistics));
                    case FLAT -> ctx.html(generateFlatHtmlString(header, tree2dump, showStatistics));
                }
            }
            case INTERACTIVE_HTML -> ctx.html(generateInteractiveHtmlString(header, tree2dump, showStatistics));
            case DOT -> ctx.result(generateDotString(tree2dump)).contentType(CONTENT_TYPE_TXT);
            case SDD -> ctx.result(generateSddString(tree2dump)).contentType(CONTENT_TYPE_XML);
            case PDF, ZIP ->
                    throw new HttpResponseException(NOT_IMPLEMENTED.getCode(), "format not currently supported");
        }
    }

    static <T> T getParamOrThrow(final Context ctx, final String name, final Class<T> clazz, final T fallback) {
        final Validator<T> tValidator = ctx.queryParamAsClass(name, clazz);
        if (!tValidator.hasValue()) {
            return fallback;
        }
        return tValidator.getOrThrow(_ -> new BadRequestResponse());
    }

    static IkeyConfig initializeConfig(final Context ctx) {
        return IkeyConfig.builder()
                .format(getParamOrThrow(ctx, "format", OutputFormat.class, OutputFormat.TXT))
                .representation(getParamOrThrow(ctx, "keyRepresentation", KeyRepresentation.class, TREE))
                .fewStatesCharacterFirst(getParamOrThrow(ctx, "fewStatesCharacterFirst", Boolean.class, false))
                .mergeCharacterStatesIfSameDiscrimination(getParamOrThrow(ctx, "mergeCharacterStatesIfSameDiscrimination", Boolean.class, false))
                .enablePruning(getParamOrThrow(ctx, "enablePruning", Boolean.class, false))
                .verbosity(getParamOrThrow(ctx, "verbosity", Verbosity.class, Verbosity.getDefault()).levels())
                .scoreMethod(getParamOrThrow(ctx, "scoreMethod", ScoreMethod.class, XPER))
                .build();
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