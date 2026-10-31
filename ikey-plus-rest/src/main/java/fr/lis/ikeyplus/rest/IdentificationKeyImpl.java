package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.services.IdentificationKeyGenerator;
import fr.lis.ikeyplus.utils.IkeyConfig;
import fr.lis.ikeyplus.utils.IkeyConfigBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Identification key webservice using REST protocol
 *
 * @author Florian Causse
 * @created 06-04-2011
 */
public class IdentificationKeyImpl {

    public String getIdentificationKey(
            final String sddURL,
            final String format,
            final String representation,
            final boolean fewStatesCharacterFirst,
            final boolean mergeCharacterStatesIfSameDiscrimination,
            final boolean pruning,
            final String verbosity,
            final String scoreMethod,
            final String weightContext,
            final String weightType) {
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

    public String createIdentificationKey(
            final String sddURL,
            final String format,
            final String representation,
            final boolean fewStatesCharacterFirst,
            final boolean mergeCharacterStatesIfSameDiscrimination,
            final boolean pruning,
            final String verbosity,
            final String scoreMethod,
            final String weightContext,
            final String weightType) {

        // creation of IkeyConfig object (containing options)
        IkeyConfig config;
        // String containing the name of the result file
        String resultFileName = null;
        // String containing the URL of the result file
        final String resultFileUrl;
        final String lineSeparator = System.lineSeparator();

        final String prefix = IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.prefix");
        final String generatedFilesFolder = IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder");
        final String generatedFilesFolderWithPrefix = prefix + generatedFilesFolder;

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
                } catch (final IOException e) {
                    e.printStackTrace();
                    config.setErrorMessage(IkeyConfig.getBundleConfElement("message.urlError"), e);
                }
                sddSaxParser = new SDDSaxParser(new FileInputStream(sddURL), config);
                // construct header
                header.append(lineSeparator).append(sddSaxParser.getDataset().getLabel()).append(", ").append(IkeyConfig.getBundleConfOverridableElement("message.createdBy")).append(lineSeparator);
                header.append(lineSeparator).append("Options:");
                header.append(lineSeparator).append("sddURL=").append(sddURL);
                header.append(lineSeparator).append("format=").append(config.getFormat());
                header.append(lineSeparator).append("representation=").append(config.getRepresentation());
                header.append(lineSeparator).append("fewStatesCharacterFirst=").append(config.isFewStatesCharacterFirst());
                header.append(lineSeparator).append("mergeCharacterStatesIfSameDiscrimination=").append(config.isMergeCharacterStatesIfSameDiscrimination());
                header.append(lineSeparator).append("pruning=").append(config.isPruningEnabled());
                header.append(lineSeparator).append("verbosity=").append(config.getVerbosity());
                header.append(lineSeparator).append("scoreMethod=").append(config.getScoreMethod());
                header.append(lineSeparator).append("weightContext=").append(config.getWeightContext());
                header.append(lineSeparator).append("weightType=").append(config.getWeightType());
                header.append(lineSeparator);
            } catch (final Exception t) {
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
            header.append(lineSeparator).append("parseDuration= ").append(parseDuration).append("s");
            header.append(lineSeparator).append("keyCreationDuration= ").append(keyCreationDuration).append("s");

            File resultFile = null;

            if (identificationKeyGenerator != null
                    && identificationKeyGenerator.getSingleAccessKeyTree() != null) {

                try {
                    // creation of the directory containing key files
                    if (!new File(prefix
                            + generatedFilesFolder).exists()) {
                        new File(prefix + generatedFilesFolder).mkdir();
                    }

                    final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();

                    header.append(lineSeparator).append(lineSeparator);

                    if (!config.getVerbosity().contains(IkeyConfig.VerbosityLevel.HEADER)) {
                        header.setLength(0);
                    }
                    if (config.getFormat() == IkeyConfig.OutputFormat.HTML) {
                        if (config.getRepresentation() == IkeyConfig.KeyRepresentation.FLAT) {
                            resultFile = SingleAccessKeyTreeDumper.dumpFlatHtmlFile(header.toString(),
                                    tree2dump, config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolderWithPrefix);
                        } else {
                            resultFile = SingleAccessKeyTreeDumper.dumpHtmlFile(header.toString(),
                                    tree2dump, config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolderWithPrefix);
                        }
                    } else if (config.getFormat() == IkeyConfig.OutputFormat.WIKI) {
                        if (config.getRepresentation() == IkeyConfig.KeyRepresentation.FLAT) {
                            resultFile = SingleAccessKeyTreeDumper.dumpFlatWikiFile(header.toString(),
                                    tree2dump, config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolderWithPrefix);
                        } else {
                            resultFile = SingleAccessKeyTreeDumper.dumpWikiFile(header.toString(),
                                    tree2dump, config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolderWithPrefix);
                        }
                    } else if (config.getFormat() == IkeyConfig.OutputFormat.INTERACTIVE_HTML) {
                        resultFile = SingleAccessKeyTreeDumper.dumpInteractiveHtmlFile(header.toString(),
                                tree2dump, config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolderWithPrefix);
                    } else if (config.getFormat() == IkeyConfig.OutputFormat.DOT) {
                        resultFile = SingleAccessKeyTreeDumper.dumpDotFile(header.toString(), tree2dump, generatedFilesFolderWithPrefix);
                    } else if (config.getFormat() == IkeyConfig.OutputFormat.SDD) {
                        resultFile = SingleAccessKeyTreeDumper.dumpSddFile(tree2dump);
                    } else if (config.getFormat() == IkeyConfig.OutputFormat.ZIP) {
                        resultFile = SingleAccessKeyTreeDumper.dumpZipFile(header.toString(), tree2dump,
                                config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolderWithPrefix);
                    } else {
                        if (config.getRepresentation() == IkeyConfig.KeyRepresentation.FLAT) {
                            resultFile = SingleAccessKeyTreeDumper.dumpFlatTxtFile(header.toString(),
                                    tree2dump, config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolderWithPrefix);
                        } else {
                            resultFile = SingleAccessKeyTreeDumper.dumpTxtFile(header.toString(),
                                    tree2dump, config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS), generatedFilesFolderWithPrefix);
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
                + generatedFilesFolder + resultFileName;

        return resultFileUrl;
    }

    public static IkeyConfig initializeConfig(final String format,
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
