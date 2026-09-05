package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper;
import fr.lis.ikeyplus.model.key.SingleAccessKeyTree;
import fr.lis.ikeyplus.services.IdentificationKeyGenerator;
import fr.lis.ikeyplus.utils.IkeyConfig;
import fr.lis.ikeyplus.utils.IkeyConfigBuilder;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Identification key webservice using REST protocol
 *
 * @author Florian Causse
 * @created 06-04-2011
 */
@Path("/identificationKey")
public class IdentificationKeyImpl {

    @GET
    public String getIdentificationKey(
            @QueryParam("sddURL") final String sddURL,
            @QueryParam("format") final String format,
            @QueryParam("representation") final String representation,
            @QueryParam("fewStatesCharacterFirst") final boolean fewStatesCharacterFirst,
            @QueryParam("mergeCharacterStatesIfSameDiscrimination") final boolean mergeCharacterStatesIfSameDiscrimination,
            @QueryParam("pruning") final boolean pruning,
            @QueryParam("verbosity") final String verbosity,
            @QueryParam("scoreMethod") final String scoreMethod,
            @QueryParam("weightContext") final String weightContext,
            @QueryParam("weightType") final String weightType) {
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
            @FormParam("sddURL") final String sddURL,
            @FormParam("format") final String format,
            @FormParam("representation") final String representation,
            @FormParam("fewStatesCharacterFirst") final boolean fewStatesCharacterFirst,
            @FormParam("mergeCharacterStatesIfSameDiscrimination") final boolean mergeCharacterStatesIfSameDiscrimination,
            @FormParam("pruning") final boolean pruning,
            @FormParam("verbosity") final String verbosity,
            @FormParam("scoreMethod") final String scoreMethod,
            @FormParam("weightContext") final String weightContext,
            @FormParam("weightType") final String weightType) {

        // creation of IkeyConfig object (containing options)
        IkeyConfig config;
        // String containing the name of the result file
        String resultFileName = null;
        // String containing the URL of the result file
        final String resultFileUrl;
        final String lineReturn = System.getProperty("line.separator");

        final String generatedFilesFolder = IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.prefix")
                + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder");

        try {

            // define header string
            final StringBuilder header = new StringBuilder();

            config = initializeConfig(format, representation, fewStatesCharacterFirst, mergeCharacterStatesIfSameDiscrimination, pruning, verbosity, scoreMethod, weightContext, weightType);
//			// calculate CPU usage
//			double usageCPU = 0;
//			try {
//				usageCPU = new Sigar().getCpuPerc().getCombined();
//			} catch (SigarException e) {
//				e.printStackTrace();
//				config.setErrorMessage(IkeyConfig.getBundleConfElement("message.cpuUsageError"), e);
//			}

            // if CPU usage is less than 80%
//			if (usageCPU < 0.8) {

            long beforeTime = System.currentTimeMillis();

            // call SDD parser
            SDDSaxParser sddSaxParser = null;
            try {
                // test if the URL is valid
                final URLConnection urlConnection;
                try {
                    final URL fileURL = new URL(sddURL);
                    // open URL (HTTP query)
                    urlConnection = fileURL.openConnection();
                    // Open data stream
                    urlConnection.getInputStream();
                } catch (final java.net.MalformedURLException e) {
                    e.printStackTrace();
                    config.setErrorMessage(IkeyConfig.getBundleConfElement("message.urlError"), e);
                } catch (final IOException e) {
                    e.printStackTrace();
                    config.setErrorMessage(IkeyConfig.getBundleConfElement("message.urlError"), e);
                }
                sddSaxParser = new SDDSaxParser(sddURL, config);
                // construct header
                header.append(lineReturn + sddSaxParser.getDataset().getLabel() + ", "
                        + IkeyConfig.getBundleConfOverridableElement("message.createdBy") + lineReturn);
                header.append(lineReturn + "Options:");
                header.append(lineReturn + "sddURL=" + sddURL);
                header.append(lineReturn + "format=" + config.getFormat());
                header.append(lineReturn + "representation=" + config.getRepresentation());
                header.append(lineReturn + "fewStatesCharacterFirst=" + config.isFewStatesCharacterFirst());
                header.append(lineReturn + "mergeCharacterStatesIfSameDiscrimination="
                        + config.isMergeCharacterStatesIfSameDiscrimination());
                header.append(lineReturn + "pruning=" + config.isPruningEnabled());
                header.append(lineReturn + "verbosity=" + config.getVerbosity());
                header.append(lineReturn + "scoreMethod=" + config.getScoreMethod());
                header.append(lineReturn + "weightContext=" + config.getWeightContext());
                header.append(lineReturn + "weightType=" + config.getWeightType());
                header.append(lineReturn);
            } catch (final Throwable t) {
                t.printStackTrace();
                config.setErrorMessage(IkeyConfig.getBundleConfElement("message.parsingError"), t);
            }
            final double parseDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;
            beforeTime = System.currentTimeMillis();

            // call identification key service
            IdentificationKeyGenerator identificationKeyGenerator = null;
            try {
                identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(),
                        config);
                identificationKeyGenerator.createIdentificationKey();
            } catch (final Throwable t) {
                t.printStackTrace();
                config.setErrorMessage(IkeyConfig.getBundleConfElement("message.creatingKeyError"), t);
            }

            final double keyCreationDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;
            // construct header
            header.append(System.getProperty("line.separator") + "parseDuration= " + parseDuration + "s");
            header.append(System.getProperty("line.separator") + "keyCreationDuration= "
                    + keyCreationDuration + "s");

            File resultFile = null;

            if (identificationKeyGenerator != null
                    && identificationKeyGenerator.getSingleAccessKeyTree() != null) {

                try {
                    // creation of the directory containing key files
                    if (!new File(IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.prefix")
                            + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder")).exists()) {
                        new File(IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.prefix")
                                + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder"))
                                .mkdir();
                    }

                    final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();

                    header.append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));

                    if (!config.getVerbosity().contains(IkeyConfig.VerbosityLevel.HEADER)) {
                        header.setLength(0);
                    }
                    if (config.getFormat() == IkeyConfig.OutputFormat.HTML) {
                        if (config.getRepresentation() == IkeyConfig.KeyRepresentation.FLAT) {
                            resultFile = SingleAccessKeyTreeDumper.dumpFlatHtmlFile(header.toString(),
                                    tree2dump, config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolder);
                        } else {
                            resultFile = SingleAccessKeyTreeDumper.dumpHtmlFile(header.toString(),
                                    tree2dump, config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolder);
                        }
                    } else if (config.getFormat() == IkeyConfig.OutputFormat.WIKI) {
                        if (config.getRepresentation() == IkeyConfig.KeyRepresentation.FLAT) {
                            resultFile = SingleAccessKeyTreeDumper.dumpFlatWikiFile(header.toString(),
                                    tree2dump, config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolder);
                        } else {
                            resultFile = SingleAccessKeyTreeDumper.dumpWikiFile(header.toString(),
                                    tree2dump, config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolder);
                        }
                    } else if (config.getFormat() == IkeyConfig.OutputFormat.INTERACTIVE_HTML) {
                        resultFile = SingleAccessKeyTreeDumper.dumpInteractiveHtmlFile(header.toString(),
                                tree2dump, config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolder);
                    } else if (config.getFormat() == IkeyConfig.OutputFormat.DOT) {
                        resultFile = SingleAccessKeyTreeDumper.dumpDotFile(header.toString(), tree2dump, generatedFilesFolder);
                    } else if (config.getFormat() == IkeyConfig.OutputFormat.SDD) {
                        resultFile = SingleAccessKeyTreeDumper.dumpSddFile(tree2dump);
                    } else if (config.getFormat() == IkeyConfig.OutputFormat.ZIP) {
                        resultFile = SingleAccessKeyTreeDumper.dumpZipFile(header.toString(), tree2dump,
                                config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolder);
                    } else {
                        if (config.getRepresentation() == IkeyConfig.KeyRepresentation.FLAT) {
                            resultFile = SingleAccessKeyTreeDumper.dumpFlatTxtFile(header.toString(),
                                    tree2dump, config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolder);
                        } else {
                            resultFile = SingleAccessKeyTreeDumper.dumpTxtFile(header.toString(),
                                    tree2dump, config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolder);
                        }
                    }
                } catch (final IOException e) {
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

            // if CPU usage is more than 80%
//			} else {
//				config.setErrorMessage(IkeyConfig.getBundleConfElement("message.serverBusy"));
//			}

        } catch (final Exception e) {
            e.printStackTrace();
            config = IkeyConfig.builder().build();
            config.setErrorMessage(IkeyConfig.getBundleConfElement("message.error"), e);
        }

        // initialize the file name with error file name if exist
        if (config.getErrorMessageFile() != null) {
            resultFileName = config.getErrorMessageFile().getName();
        }

        resultFileUrl = IkeyConfig.getBundleConfOverridableElement("host")
                + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder") + resultFileName;

        return resultFileUrl;
    }

    private IkeyConfig initializeConfig(final String format,
                                        final String representation,
                                        final boolean fewStatesCharacterFirst,
                                        final boolean mergeCharacterStatesIfSameDiscrimination,
                                        final boolean pruning,
                                        final String verbosity,
                                        final String scoreMethod,
                                        final String weightContext,
                                        final String weightType) {
        final IkeyConfig config;
        final IkeyConfigBuilder configBuilder = IkeyConfig.builder();
        // options initialization
        if (format != null && IkeyConfig.OutputFormat.fromString(format) != null) {
            configBuilder.format(IkeyConfig.OutputFormat.fromString(format));
        } else {
            configBuilder.format(IkeyConfig.OutputFormat.TXT);
        }

        if (representation != null && IkeyConfig.KeyRepresentation.fromString(representation) != null) {
            configBuilder.representation(IkeyConfig.KeyRepresentation.fromString(representation));
        } else {
            configBuilder.representation(IkeyConfig.KeyRepresentation.FLAT);
        }
        if (fewStatesCharacterFirst) {
            configBuilder.fewStatesCharacterFirst();
        }
        if (mergeCharacterStatesIfSameDiscrimination) {
            configBuilder.mergeCharacterStatesIfSameDiscrimination();
        }
        if (pruning) {
            configBuilder.enablePruning();
        }
        if (verbosity != null && IkeyConfig.VerbosityLevel.fromString(verbosity) != null) {
            configBuilder.verbosity(IkeyConfig.VerbosityLevel.fromString(verbosity));
        }
        if (scoreMethod != null && IkeyConfig.ScoreMethod.fromString(scoreMethod) != null) {
            configBuilder.scoreMethod(IkeyConfig.ScoreMethod.fromString(scoreMethod));
        }
        if (weightContext != null && IkeyConfig.WeightContext.fromString(weightContext) != null) {
            configBuilder.weightContext(IkeyConfig.WeightContext.fromString(weightContext));
        }
        if (weightType != null && weightType.equalsIgnoreCase(IkeyConfig.WeightType.CONTEXTUAL.toString())) {
            configBuilder.weightType(IkeyConfig.WeightType.CONTEXTUAL);
        }

        config = configBuilder.build();
        return config;
    }
}
