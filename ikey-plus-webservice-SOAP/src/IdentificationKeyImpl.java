import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.jws.WebService;

import main.java.IO.SDDSaxParser;
import main.java.IO.SingleAccessKeyTreeDumper;
import main.java.model.SingleAccessKeyTree;
import main.java.services.IdentificationKeyGenerator;
import main.java.utils.Utils;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * Identification key webservice using SOAP protocol
 * 
 * @author Florian Causse
 * @created 06-04-2011
 */
@WebService(endpointInterface = "IIdentificationKey", serviceName = "identificationKey")
public class IdentificationKeyImpl implements IIdentificationKey {

	@Override
	public String createIdentificationKey(String sddURL, String format, String representation,
			String fewStatesCharacterFirst, String mergeCharacterStatesIfSameDiscrimination, String pruning,
			String verbosity, String scoreMethod, String weightContext, String weightType) {

		// creation of Utils object (containing options)
		Utils utils = new Utils();
		// String containing the name of the result file
		String resultFileName = null;
		// String containing the URL of the result file
		String resultFileUrl = null;
		String lineReturn = System.getProperty("line.separator");

		try {

			// define header string
			StringBuffer header = new StringBuffer();

			// options initialization
			if (format != null
					&& (format.equalsIgnoreCase(utils.TXT) || format.equalsIgnoreCase(utils.HTML)
							|| format.equalsIgnoreCase(utils.PDF) || format.equalsIgnoreCase(utils.SDD)
							|| format.equalsIgnoreCase(utils.WIKI)
							|| format.equalsIgnoreCase(utils.INTERACTIVE_HTML)
							|| format.equalsIgnoreCase(utils.SPECIESIDWIKISTATEMENT)
							|| format.equalsIgnoreCase(utils.SPECIESIDWIKIQUESTIONANSWER)
							|| format.equalsIgnoreCase(utils.DOT) || format.equalsIgnoreCase(utils.ZIP))) {
				utils.setFormat(format.toLowerCase());
			}
			if (representation != null
					&& (representation.equalsIgnoreCase(utils.TREE) || representation
							.equalsIgnoreCase(utils.FLAT))) {
				utils.setRepresentation(representation.toLowerCase());
			}
			if (fewStatesCharacterFirst != null && fewStatesCharacterFirst.equalsIgnoreCase(Utils.YES)) {
				utils.setFewStatesCharacterFirst(true);
			}
			if (mergeCharacterStatesIfSameDiscrimination != null
					&& mergeCharacterStatesIfSameDiscrimination.equalsIgnoreCase(Utils.YES)) {
				utils.setMergeCharacterStatesIfSameDiscimination(true);
			}
			if (pruning != null && pruning.equalsIgnoreCase(Utils.YES)) {
				utils.setPruning(true);
			}
			if (verbosity != null) {
				utils.setVerbosity(verbosity.toLowerCase());
			}
			if (scoreMethod != null
					&& (scoreMethod.equalsIgnoreCase(Utils.JACCARD) || scoreMethod
							.equalsIgnoreCase(Utils.SOKALANDMICHENER))) {
				utils.setScoreMethod(scoreMethod.toLowerCase());
			}
			if (weightContext != null) {
				utils.setWeightContext(weightContext);
			}
			if (weightType != null && weightType.equalsIgnoreCase(Utils.CONTEXTUAL_CHARACTER_WEIGHT)) {
				utils.setWeightType(weightType);
			}

			// calculate CPU usage
			double usageCPU = 0;
			try {
				usageCPU = new Sigar().getCpuPerc().getCombined();
			} catch (SigarException e) {
				e.printStackTrace();
				utils.setErrorMessage(Utils.getBundleConfElement("message.cpuUsageError"), e);
			}

			// if CPU usage is less than 80%
			if (usageCPU < 0.8) {

				long beforeTime = System.currentTimeMillis();

				// call SDD parser
				SDDSaxParser sddSaxParser = null;
				try {
					// test if the URL is valid
					URLConnection urlConnection;
					InputStream httpStream;
					try {
						URL fileURL = new URL(sddURL);
						// open URL (HTTP query)
						urlConnection = fileURL.openConnection();
						// Open data stream
						httpStream = urlConnection.getInputStream();
					} catch (java.net.MalformedURLException e) {
						e.printStackTrace();
						utils.setErrorMessage(Utils.getBundleConfElement("message.urlError"), e);
					} catch (java.io.IOException e) {
						e.printStackTrace();
						utils.setErrorMessage(Utils.getBundleConfElement("message.urlError"), e);
					}
					sddSaxParser = new SDDSaxParser(sddURL, utils);
					// construct header
					header.append(lineReturn + sddSaxParser.getDataset().getLabel() + ", "
							+ Utils.getBundleConfOverridableElement("message.createdBy") + lineReturn);
					header.append(lineReturn + "Options:");
					header.append(lineReturn + "sddURL=" + sddURL);
					header.append(lineReturn + "format=" + utils.getFormat());
					header.append(lineReturn + "representation=" + utils.getRepresentation());
					header.append(lineReturn + "fewStatesCharacterFirst=" + utils.isFewStatesCharacterFirst());
					header.append(lineReturn + "mergeCharacterStatesIfSameDiscimination="
							+ utils.isMergeCharacterStatesIfSameDiscimination());
					header.append(lineReturn + "pruning=" + utils.isPruning());
					header.append(lineReturn + "verbosity=" + utils.getVerbosity());
					header.append(lineReturn + "scoreMethod=" + utils.getScoreMethod());
					header.append(lineReturn + "weightContext=" + utils.getWeightContext());
					header.append(lineReturn + "weightType=" + utils.getWeightType());
					header.append(lineReturn);
				} catch (Throwable t) {
					t.printStackTrace();
					utils.setErrorMessage(Utils.getBundleConfElement("message.parsingError"), t);
				}
				double parseDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;
				beforeTime = System.currentTimeMillis();

				// call identification key service
				IdentificationKeyGenerator identificationKeyGenerator = null;
				try {
					identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(),
							utils);
					identificationKeyGenerator.createIdentificationKey();
				} catch (Throwable t) {
					t.printStackTrace();
					utils.setErrorMessage(Utils.getBundleConfElement("message.creatingKeyError"), t);
				}

				double keyCreationDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;
				// construct header
				header.append(System.getProperty("line.separator") + "parseDuration= " + parseDuration + "s");
				header.append(System.getProperty("line.separator") + "keyCreationDuration= "
						+ keyCreationDuration + "s");

				File resultFile = null;

				if (identificationKeyGenerator != null
						&& identificationKeyGenerator.getSingleAccessKeyTree() != null) {

					try {
						// creation of the directory containing key files
						if (!new File(Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
								+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder")).exists()) {
							new File(Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
									+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder"))
									.mkdir();
						}

						SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();

						header.append(System.getProperty("line.separator")
								+ System.getProperty("line.separator"));

						if (!utils.getVerbosity().contains(Utils.HEADER_TAG)) {
							header.setLength(0);
						}
						if (utils.getFormat().equalsIgnoreCase(Utils.PDF)) {
							if (utils.getRepresentation().equalsIgnoreCase(Utils.FLAT)) {
								resultFile = SingleAccessKeyTreeDumper.dumpFlatPdfFile(header.toString(),
										tree2dump, utils.getVerbosity().contains(Utils.STATISTIC_TAG));
							} else {
								resultFile = SingleAccessKeyTreeDumper.dumpPdfFile(header.toString(),
										tree2dump, utils.getVerbosity().contains(Utils.STATISTIC_TAG));
							}
						} else if (utils.getFormat().equalsIgnoreCase(Utils.HTML)) {
							if (utils.getRepresentation().equalsIgnoreCase(Utils.FLAT)) {
								resultFile = SingleAccessKeyTreeDumper.dumpFlatHtmlFile(header.toString(),
										tree2dump, utils.getVerbosity().contains(Utils.STATISTIC_TAG));
							} else {
								resultFile = SingleAccessKeyTreeDumper.dumpHtmlFile(header.toString(),
										tree2dump, utils.getVerbosity().contains(Utils.STATISTIC_TAG));
							}
						} else if (utils.getFormat().equalsIgnoreCase(Utils.WIKI)) {
							if (utils.getRepresentation().equalsIgnoreCase(Utils.FLAT)) {
								resultFile = SingleAccessKeyTreeDumper.dumpFlatWikiFile(header.toString(),
										tree2dump, utils.getVerbosity().contains(Utils.STATISTIC_TAG));
							} else {
								resultFile = SingleAccessKeyTreeDumper.dumpWikiFile(header.toString(),
										tree2dump, utils.getVerbosity().contains(Utils.STATISTIC_TAG));
							}
						} else if (utils.getFormat().equalsIgnoreCase(Utils.SPECIESIDWIKISTATEMENT)) {
							resultFile = SingleAccessKeyTreeDumper.dumpFlatSpeciesIDStatementWikiFile(
									header.toString(), tree2dump);
						} else if (utils.getFormat().equalsIgnoreCase(Utils.INTERACTIVE_HTML)) {
							resultFile = SingleAccessKeyTreeDumper.dumpInteractiveHtmlFile(header.toString(),
									tree2dump, utils.getVerbosity().contains(Utils.STATISTIC_TAG));
						} else if (utils.getFormat().equalsIgnoreCase(Utils.SPECIESIDWIKIQUESTIONANSWER)) {
							resultFile = SingleAccessKeyTreeDumper.dumpFlatSpeciesIDQuestionAnswerWikiFile(
									header.toString(), tree2dump);
						} else if (utils.getFormat().equalsIgnoreCase(Utils.DOT)) {
							resultFile = SingleAccessKeyTreeDumper.dumpDotFile(header.toString(), tree2dump);
						} else if (utils.getFormat().equalsIgnoreCase(Utils.SDD)) {
							resultFile = SingleAccessKeyTreeDumper.dumpSddFile(header.toString(), tree2dump);
						} else if (utils.getFormat().equalsIgnoreCase(Utils.ZIP)) {
							resultFile = SingleAccessKeyTreeDumper.dumpZipFile(header.toString(), tree2dump,
									utils.getVerbosity().contains(Utils.STATISTIC_TAG));
						} else {
							if (utils.getRepresentation().equalsIgnoreCase(Utils.FLAT)) {
								resultFile = SingleAccessKeyTreeDumper.dumpFlatTxtFile(header.toString(),
										tree2dump, utils.getVerbosity().contains(Utils.STATISTIC_TAG));
							} else {
								resultFile = SingleAccessKeyTreeDumper.dumpTxtFile(header.toString(),
										tree2dump, utils.getVerbosity().contains(Utils.STATISTIC_TAG));
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
						utils.setErrorMessage(Utils.getBundleConfElement("message.creatingFileError"));
					}
					// initiate the result file name
					resultFileName = resultFile.getName();

				} else {
					utils.setErrorMessage(Utils.getBundleConfElement("message.creatingKeyError"));
				}

				// if CPU usage is more than 80%
			} else {
				utils.setErrorMessage(Utils.getBundleConfElement("message.serverBusy"));

			}

		} catch (Exception e) {
			e.printStackTrace();
			utils.setErrorMessage(Utils.getBundleConfElement("message.error"), e);
		}

		// initialize the file name with error file name if exist
		if (utils.getErrorMessageFile() != null) {
			resultFileName = utils.getErrorMessageFile().getName();
		}

		resultFileUrl = Utils.getBundleConfOverridableElement("host")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder") + resultFileName;

		return resultFileUrl;
	}
}
