package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.services.IdentificationKeyGenerator;
import fr.lis.ikeyplus.utils.IkeyConfig;
import io.javalin.Javalin;
import io.javalin.http.Handler;

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
import static java.util.Optional.ofNullable;

public class Application {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_HTML = "text/html";
    private static final String CONTENT_TYPE_XML = "text/xml";

    private static final IdentificationKeyImpl ik = new IdentificationKeyImpl();

    public static void main(String[] args) {
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

        var sddSaxParser = new SDDSaxParser(sddURL, config);


        // call identification key service
        final IdentificationKeyGenerator identificationKeyGenerator =
                new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();

        var tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();

        final String header = "TODO HEADER";
        final boolean showStatistics = config.getVerbosity().contains(STATISTICS);
        switch (config.getFormat()) {
            case TXT -> {
                switch (config.getRepresentation()) {
                    case TREE -> ctx.result(generateTreeString(tree2dump));
                    case FLAT -> ctx.result(generateFlatString(tree2dump));
                }
            }
            case WIKI -> {
                switch (config.getRepresentation()) {
                    case TREE -> ctx.result(generateTreeWiki(tree2dump, showStatistics));
                    case FLAT -> ctx.result(generateFlatWikiString(tree2dump));
                }
            }
            case HTML -> {
                switch (config.getRepresentation()) {
                    case TREE -> ctx.result(generateHtmlString(header, tree2dump, showStatistics)).header(CONTENT_TYPE, CONTENT_TYPE_HTML);
                    case FLAT -> ctx.result(generateFlatHtmlString(header, tree2dump, showStatistics)).header(CONTENT_TYPE, CONTENT_TYPE_HTML);
                }
            }
            case INTERACTIVE_HTML -> ctx.result(generateInteractiveHtmlString(header, tree2dump, showStatistics)).header(CONTENT_TYPE, CONTENT_TYPE_HTML);
            case DOT -> ctx.result(generateDotString(tree2dump));
            case SDD -> ctx.result(generateSddString(tree2dump)).header(CONTENT_TYPE, CONTENT_TYPE_XML);
            case PDF, ZIP -> throw new RuntimeException("UNSUPPORTED");
        }
    };
}
