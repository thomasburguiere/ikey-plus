package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.services.IdentificationKeyGenerator;
import fr.lis.ikeyplus.utils.IkeyConfig;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateDotString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateFlatHtmlString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateFlatString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateFlatWikiString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateHtmlString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateInteractiveHtmlString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateSddString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateTreeString;
import static fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper.generateTreeWiki;
import static fr.lis.ikeyplus.rest.InputParser.Either.Left;
import static fr.lis.ikeyplus.rest.InputParser.Either.Right;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.HEADER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.STATISTICS;
import static io.javalin.http.HttpStatus.NOT_IMPLEMENTED;

public class RestHandler {
    private static final SDDSaxParser parser = new SDDSaxParser();

    private static final String CONTENT_TYPE_XML = "text/xml";
    private static final String CONTENT_TYPE_HTML = "text/html";
    private static final String CONTENT_TYPE_TXT = "text/plain";

    private RestHandler() {
    }

    public static void handleGetKey(final Context ctx) throws Exception {
        final InputParser parser = new InputParser(name -> Optional.ofNullable(ctx.queryParam(name)));

        final String sddURL = ctx.queryParam("sddURL");
        final URL validatedSddUrl;
        try {
            validatedSddUrl = parser.validateSddUrl(sddURL);
        } catch (final IllegalArgumentException e) {
            throw new BadRequestResponse(e.getMessage());
        }

        final var configOrErrors = parser.initializeConfig();
        final IkeyConfig config = switch (configOrErrors) {
            case Left<Map<String, String>, ?> l -> throw new BadRequestResponse("", l.value());
            case Right<?, IkeyConfig> r -> r.value();
        };


        try (final InputStream sddInputStream = validatedSddUrl.openStream()) {
            final var result = generateKey(sddInputStream, config, sddURL);
            ctx.result(result.keyContent).contentType(result.contentType);
        }
    }

    record KeyGenerationResult(String keyContent, String contentType) {
    }

    static KeyGenerationResult generateKey(final InputStream sddInputStream,
                                           final IkeyConfig config,
                                           final String sddURL) throws Exception {
        // call identification key service
        final DataSet dataset = parser.parseDataset(sddInputStream, config);
        final IdentificationKeyGenerator identificationKeyGenerator =
            new IdentificationKeyGenerator(dataset, config);
        identificationKeyGenerator.createIdentificationKey();

        final var tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();

        final String header = config.getVerbosity().contains(HEADER) ? generateHeader(config, sddURL, dataset.getLabel()) : "";
        final boolean showStatistics = config.getVerbosity().contains(STATISTICS);
        return switch (config.getFormat()) {
            case TXT -> switch (config.getRepresentation()) {
                case TREE -> new KeyGenerationResult(generateTreeString(header, tree2dump), CONTENT_TYPE_TXT);
                case FLAT -> new KeyGenerationResult(generateFlatString(header, tree2dump), CONTENT_TYPE_TXT);
            };
            case WIKI -> switch (config.getRepresentation()) {
                case TREE -> new KeyGenerationResult(generateTreeWiki(tree2dump, showStatistics), CONTENT_TYPE_TXT);
                case FLAT -> new KeyGenerationResult(generateFlatWikiString(tree2dump), CONTENT_TYPE_TXT);
            };
            case HTML -> switch (config.getRepresentation()) {
                case TREE ->
                    new KeyGenerationResult(generateHtmlString(header, tree2dump, showStatistics), CONTENT_TYPE_HTML);
                case FLAT ->
                    new KeyGenerationResult(generateFlatHtmlString(header, tree2dump, showStatistics), CONTENT_TYPE_HTML);
            };
            case INTERACTIVE_HTML ->
                new KeyGenerationResult(generateInteractiveHtmlString(header, tree2dump, showStatistics), CONTENT_TYPE_HTML);
            case DOT -> new KeyGenerationResult(generateDotString(tree2dump), CONTENT_TYPE_TXT);
            case SDD -> new KeyGenerationResult(generateSddString(tree2dump), CONTENT_TYPE_XML);
            case PDF, ZIP ->
                throw new HttpResponseException(NOT_IMPLEMENTED.getCode(), "format not currently supported");
        };
    }

    private static String generateHeader(final IkeyConfig config, final String sddURL, final String label) {
        final String lineSeparator = System.lineSeparator();

        return "Generated by IKey+, Laboratoire Informatique et Systematique, UMR 7205, MNHN Paris" + lineSeparator +
            lineSeparator + label + lineSeparator +
            lineSeparator + "Options:" +
            lineSeparator + "sddURL=" + sddURL +
            lineSeparator + "format=" + config.getFormat() +
            lineSeparator + "representation=" + config.getRepresentation() +
            lineSeparator + "fewStatesCharacterFirst=" + config.isFewStatesCharacterFirst() +
            lineSeparator + "mergeCharacterStatesIfSameDiscrimination=" + config.isMergeCharacterStatesIfSameDiscrimination() +
            lineSeparator + "pruning=" + config.isPruningEnabled() +
            lineSeparator + "verbosity=" + config.getVerbosity() +
            lineSeparator + "scoreMethod=" + config.getScoreMethod() +
            lineSeparator + "weightContext=" + config.getWeightContext() +
            lineSeparator + "weightType=" + config.getWeightType() +
            lineSeparator;
    }
}