package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.IO.SDDParser;
import fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper;
import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.services.IdentificationKeyGenerator;
import fr.lis.ikeyplus.utils.IkeyConfig;
import fr.lis.ikeyplus.utils.IkeyConfigBuilder;
import fr.lis.ikeyplus.utils.IkeyException;
import fr.lis.ikeyplus.utils.IkeyUtils;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;

import static fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation.FLAT;
import static fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat.WIKI;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.*;

/**
 * Identification key webservice using REST protocol
 *
 * @author Florian Causse
 * @created 06-04-2011
 */
@Path("/identificationKey")
public class IdentificationKeyResource {

    private SDDParser sddParser;
    private IdentificationKeyGenerator identificationKeyGenerator;

    public IdentificationKeyResource() {

    }

    @Inject
    public IdentificationKeyResource(SDDParser sddParser, IdentificationKeyGenerator identificationKeyGenerator) {
        this.sddParser = sddParser;
        this.identificationKeyGenerator = identificationKeyGenerator;
    }


    @GET
    public String getIdentificationKey(
            @QueryParam("sddURL") String sddURL,
            @QueryParam("format") String format,
            @QueryParam("representation") String representation,
            @QueryParam("fewStatesCharacterFirst") boolean fewStatesCharacterFirst,
            @QueryParam("mergeCharacterStatesIfSameDiscrimination") boolean mergeCharacterStatesIfSameDiscrimination,
            @QueryParam("pruning") boolean pruning,
            @QueryParam("verbosity") String verbosity,
            @QueryParam("scoreMethod") String scoreMethod,
            @QueryParam("weightContext") String weightContext,
            @QueryParam("weightType") String weightType) {
        return createIdentificationKey(
                sddURL,
                format,
                representation,
                fewStatesCharacterFirst,
                mergeCharacterStatesIfSameDiscrimination,
                pruning,
                verbosity,
                scoreMethod,
                weightContext,
                weightType);
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String createIdentificationKey(
            @FormParam("sddURL") String sddURL,
            @FormParam("format") String format,
            @FormParam("representation") String representation,
            @FormParam("fewStatesCharacterFirst") boolean fewStatesCharacterFirst,
            @FormParam("mergeCharacterStatesIfSameDiscrimination") boolean mergeCharacterStatesIfSameDiscrimination,
            @FormParam("pruning") boolean pruning,
            @FormParam("verbosity") String verbosity,
            @FormParam("scoreMethod") String scoreMethod,
            @FormParam("weightContext") String weightContext,
            @FormParam("weightType") String weightType) {

        // creation of IkeyConfig object (containing options)
        IkeyConfig config;
        // String containing the URL of the result file
        String resultFileUrl;

        config = initializeConfig(
                format,
                representation, fewStatesCharacterFirst,
                mergeCharacterStatesIfSameDiscrimination,
                pruning,
                verbosity,
                scoreMethod,
                weightContext,
                weightType);


        resultFileUrl = generateKey(sddURL, config);

        return resultFileUrl;
    }

    private String generateKey(String sddURL, IkeyConfig config) {
        final String generatedKeyFolderPath = IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.prefix")
                + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder");


        // define header string
        StringBuilder header = new StringBuilder();


        String lineReturn = System.getProperty("line.separator");
        long beforeTime = System.currentTimeMillis();

        // call SDD parser
        // test if the URL is valid
        URLConnection urlConnection;
        try {
            URL fileURL = new URL(sddURL);
            // open URL (HTTP query)
            urlConnection = fileURL.openConnection();
            // Open data stream
            urlConnection.getInputStream();
        } catch (ConnectException e) {
            final String message = IkeyConfig.getBundleConfElement("message.url.connectionError") + " " + sddURL;
            config.setErrorMessage(message, e);
            throw new IkeyException(message, e);
        } catch (IOException e) {
            // e.printStackTrace();// TODO log properly
            final String message = IkeyConfig.getBundleConfElement("message.urlError");
            config.setErrorMessage(message, e);
            throw new IkeyException(message, e);
        }

        DataSet dataSet;
        try {
            dataSet = sddParser.parseDataset(sddURL, config);
            // construct header
            header.append(lineReturn).append(dataSet.getLabel()).append(", ").
                    append(IkeyConfig.getBundleConfOverridableElement("message.createdBy")).append(lineReturn);
            header.append(lineReturn).append("Options:");
            header.append(lineReturn).append("sddURL=").append(sddURL);
            header.append(lineReturn).append("format=").append(config.getFormat());
            header.append(lineReturn).append("representation=").append(config.getRepresentation());
            header.append(lineReturn).append("fewStatesCharacterFirst=").append(config.isFewStatesCharacterFirst());
            header.append(lineReturn).append("mergeCharacterStatesIfSameDiscrimination=").append(config.isMergeCharacterStatesIfSameDiscrimination());
            header.append(lineReturn).append("pruning=").append(config.isPruningEnabled());
            header.append(lineReturn).append("verbosity=").append(config.getVerbosity());
            header.append(lineReturn).append("scoreMethod=").append(config.getScoreMethod());
            header.append(lineReturn).append("weightContext=").append(config.getWeightContext());
            header.append(lineReturn).append("weightType=").append(config.getWeightType());
            header.append(lineReturn);
        } catch (IOException | SAXException e) {
            //e.printStackTrace(); // TODO log properly
            final String message = IkeyConfig.getBundleConfElement("message.parsingError");
            config.setErrorMessage(message, e);
            throw new IkeyException(message, e);
        }
        double parseDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;
        beforeTime = System.currentTimeMillis();

        double keyCreationDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;
        // construct header
        header.append(System.getProperty("line.separator")).append("parseDuration= ").append(parseDuration).append("s");
        header.append(System.getProperty("line.separator")).append("keyCreationDuration= ").append(keyCreationDuration).append("s");

        File resultFile = null;

        // String containing the name of the result file
        String resultFileName = null;
        final SingleAccessKeyTree key = identificationKeyGenerator.getIdentificationKey(dataSet, config);
        if (key != null) {

            try {
                // creation of the directory containing key files
                IkeyUtils.generatedKeyFolderPathIfNeeded();


                header.append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));

                if (!config.getVerbosity().contains(HEADER)) {
                    header.setLength(0);
                }
                if (config.getFormat() == IkeyConfig.OutputFormat.HTML) {
                    if (config.getRepresentation() == FLAT) {
                        resultFile = SingleAccessKeyTreeDumper.dumpFlatHtmlFile(header.toString(),
                                key, config.getVerbosity().contains(STATISTICS), generatedKeyFolderPath);
                    } else {
                        resultFile = SingleAccessKeyTreeDumper.dumpHtmlFile(header.toString(),
                                key, config.getVerbosity().contains(STATISTICS), generatedKeyFolderPath);
                    }
                } else if (config.getFormat() == WIKI) {
                    if (config.getRepresentation() == FLAT) {
                        resultFile = SingleAccessKeyTreeDumper.dumpFlatWikiFile(header.toString(),
                                key, config.getVerbosity().contains(STATISTICS), generatedKeyFolderPath);
                    } else {
                        resultFile = SingleAccessKeyTreeDumper.dumpWikiFile(header.toString(),
                                key, config.getVerbosity().contains(STATISTICS), generatedKeyFolderPath);
                    }
                } else if (config.getFormat() == IkeyConfig.OutputFormat.INTERACTIVE_HTML) {
                    resultFile = SingleAccessKeyTreeDumper.dumpInteractiveHtmlFile(header.toString(),
                            key, config.getVerbosity().contains(STATISTICS), generatedKeyFolderPath);
                } else if (config.getFormat() == IkeyConfig.OutputFormat.DOT) {
                    resultFile = SingleAccessKeyTreeDumper.dumpDotFile(header.toString(), key, generatedKeyFolderPath);
                } else if (config.getFormat() == IkeyConfig.OutputFormat.SDD) {
                    resultFile = SingleAccessKeyTreeDumper.dumpSddFile(key);
                } else {
                    if (config.getRepresentation() == FLAT) {
                        resultFile = SingleAccessKeyTreeDumper.dumpFlatTxtFile(header.toString(),
                                key, config.getVerbosity().contains(STATISTICS), generatedKeyFolderPath);
                    } else {
                        resultFile = SingleAccessKeyTreeDumper.dumpTxtFile(header.toString(),
                                key, config.getVerbosity().contains(STATISTICS), generatedKeyFolderPath);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                config.setErrorMessage(IkeyConfig.getBundleConfElement("message.creatingFileError"));
            }

            // initiate the result file name
            if (resultFile != null) {
                resultFileName = resultFile.getName();
            }

        } else {
            config.setErrorMessage(IkeyConfig.getBundleConfElement("message.creatingKeyError"));
        }


        // initialize the file name with error file name if exist
        if (config.getErrorMessageFile() != null) {
            resultFileName = config.getErrorMessageFile().getName();
        }

        return IkeyConfig.getBundleConfOverridableElement("host")
                + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder") + resultFileName;
    }

    private IkeyConfig initializeConfig(String format,
                                        String representation,
                                        boolean fewStatesCharacterFirst,
                                        boolean mergeCharacterStatesIfSameDiscrimination,
                                        boolean pruning,
                                        String verbosity,
                                        String scoreMethod,
                                        String weightContext,
                                        String weightType) {
        IkeyConfig config;
        IkeyConfigBuilder configBuilder = IkeyConfig.builder();
        // options initialization
        configBuilder.format(IkeyConfig.OutputFormat.fromString(format));

        configBuilder.representation(IkeyConfig.KeyRepresentation.fromString(representation));
        if (fewStatesCharacterFirst) {
            configBuilder.fewStatesCharacterFirst();
        }
        if (mergeCharacterStatesIfSameDiscrimination) {
            configBuilder.mergeCharacterStatesIfSameDiscrimination();
        }
        if (pruning) {
            configBuilder.enablePruning();
        }
        configBuilder.verbosity(fromString(verbosity));
        configBuilder.scoreMethod(IkeyConfig.ScoreMethod.fromString(scoreMethod));
        configBuilder.weightContext(IkeyConfig.WeightContext.fromString(weightContext));
        configBuilder.weightType(IkeyConfig.WeightType.fromString(weightType));

        config = configBuilder.build();
        return config;
    }
}
