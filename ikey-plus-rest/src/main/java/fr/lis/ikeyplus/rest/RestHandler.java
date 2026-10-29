package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.services.IdentificationKeyGenerator;
import fr.lis.ikeyplus.utils.IkeyConfig;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;

import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateDotString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateFlatHtmlString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateFlatString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateFlatWikiString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateHtmlString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateInteractiveHtmlString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateSddString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateTreeString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateTreeWiki;
import static fr.lis.ikeyplus.utils.IkeyConfig.ScoreMethod.XPER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.STATISTICS;
import static io.javalin.http.HttpStatus.NOT_IMPLEMENTED;
import static java.util.Optional.ofNullable;

public class RestHandler {
    private static final String CONTENT_TYPE_XML = "text/xml";
    private static final String CONTENT_TYPE_TXT = "text/plain";

    private static final IdentificationKeyImpl ik = new IdentificationKeyImpl();
    static void handleGetKey(final Context ctx) throws Exception {
        final String sddURL = ctx.queryParam("sddURL");
        if (sddURL == null) {
            throw new BadRequestResponse("sddURL is not valid" + sddURL);
        }


        final String format = ctx.queryParam("format");
        final String representation = ctx.queryParam("representation");
        final boolean fewStatesCharacterFirst = "true".equals(ctx.queryParam("fewStatesCharacterFirst"));
        final boolean mergeCharacterStatesIfSameDiscrimination = "true".equals(ctx.queryParam("mergeCharacterStatesIfSameDiscrimination"));
        final boolean pruning = "true".equals(ctx.queryParam("pruning"));
        final String verbosity = ctx.queryParam("verbosity");
        final String scoreMethod = ofNullable(ctx.queryParam("scoreMethod")).orElse(XPER.name());
        final String weightContext = ctx.queryParam("weightContext");
        final String weightType = ctx.queryParam("weightType");

        final IkeyConfig config = ik.initializeConfig(format,
                representation,
                fewStatesCharacterFirst,
                mergeCharacterStatesIfSameDiscrimination,
                pruning,
                verbosity,
                scoreMethod,
                weightContext,
                weightType);

        final var sddSaxParser = new SDDSaxParser(sddURL, config);


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
}
