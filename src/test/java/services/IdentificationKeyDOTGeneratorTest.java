package test.java.services;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import main.java.IO.SDDSaxParser;
import main.java.IO.SingleAccessKeyTreeDumper;
import main.java.model.SingleAccessKeyTree;
import main.java.services.IdentificationKeyGenerator;
import main.java.utils.Utils;

import org.junit.Test;

/**
 * This class allow to test the DOT output of IdentificationKeyGenerator service
 * 
 * @author Thomas Burguiere
 * @created 31-08-2011
 */
public class IdentificationKeyDOTGeneratorTest {

	public Logger logger = Logger.getAnonymousLogger();

	@Test
	public void testIdentificationKeyGenerator() {

		// creation of Utils object (containing options)
		Utils utils = new Utils();

		// set the confTest ResourceBundle
		Utils.setBundleConfOverridable(ResourceBundle.getBundle("test.resources.confTest"));
		Utils.setBundleConf(ResourceBundle.getBundle("test.resources.confTest"));

		// String containing the name of the result file
		String resultFileName = "";
		try {
			// define logger
			logger.info("testIdentificationKeyGenerator");
			// define time before parsing SDD file
			long beforeTime = System.currentTimeMillis();

			// define header string
			StringBuffer header = new StringBuffer();

			SDDSaxParser sddSaxParser = null;
			try {
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/Cichorieae-fullSDD.xml";
				// String stringUrl =
				// "http://www.infosyslab.fr/vibrant/project/test/Cichorieae-unknownData-fullSDD.xml";
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/phlebotomes-SDD.xml";
				// String stringUrl =
				// "http://www.infosyslab.fr/vibrant/project/test/milichia_revision-sdd.xml";
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/testSDD.xml";
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/feuillesSDD.xml";
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/feuillesImagesURL.xml";
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/smallSDD.xml";
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/wrongSDD.xml";
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/pruningSDD.xml";
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/archaeoSDD.xml";
				// String stringUrl =
				// "http://www.infosyslab.fr/vibrant/project/test/varanusSDD_RatingExample.xml";
				String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/varanusSDD_RatingExample3_contextual.xml";

				// options
				utils.setFewStatesCharacterFirst(false);
				utils.setMergeCharacterStatesIfSameDiscimination(false);
				utils.setPruning(false);
				utils.setVerbosity("how");
				utils.setScoreMethod(Utils.XPER);
				utils.setWeightContext("CostEffectiveness");
				utils.setWeightType(Utils.GLOBAL_CHARACTER_WEIGHT);

				// test if the URL is valid
				URLConnection urlConnection;
				try {
					URL fileURL = new URL(stringUrl);
					// open URL (HTTP query)
					urlConnection = fileURL.openConnection();
					// Open data stream
					urlConnection.getInputStream();
				} catch (java.net.MalformedURLException e) {
					utils.setErrorMessage(Utils.getBundleConfElement("message.urlError"), e);
					e.printStackTrace();
				} catch (java.io.IOException e) {
					utils.setErrorMessage(Utils.getBundleConfElement("message.urlError"), e);
					e.printStackTrace();
				}
				sddSaxParser = new SDDSaxParser(stringUrl, utils);
				// construct header
				header.append(System.getProperty("line.separator") + sddSaxParser.getDataset().getLabel()
						+ ", " + Utils.getBundleConfOverridableElement("message.createdBy")
						+ System.getProperty("line.separator"));
				header.append(System.getProperty("line.separator") + "Options:");
				header.append(System.getProperty("line.separator") + "sddURL=" + stringUrl);
				header.append(System.getProperty("line.separator") + "fewStatesCharacterFirst="
						+ utils.isFewStatesCharacterFirst());
				header.append(System.getProperty("line.separator")
						+ "mergeCharacterStatesIfSameDiscimination="
						+ utils.isMergeCharacterStatesIfSameDiscimination());
				header.append(System.getProperty("line.separator") + "pruning=" + utils.isPruning());
				header.append(System.getProperty("line.separator") + "verbosity=" + utils.getVerbosity());
				header.append(System.getProperty("line.separator") + "scoreMethod=" + utils.getScoreMethod());
				header.append(System.getProperty("line.separator") + "weightContext="
						+ utils.getWeightContext() + System.getProperty("line.separator"));

			} catch (Throwable t) {
				utils.setErrorMessage(Utils.getBundleConfElement("message.parsingError"), t);
				t.printStackTrace();
			}

			// define parse duration
			double parseDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;
			// define time before processing key
			beforeTime = System.currentTimeMillis();

			IdentificationKeyGenerator identificationKeyGenerator = null;
			try {
				identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), utils);
				identificationKeyGenerator.createIdentificationKey();
			} catch (Throwable t) {
				utils.setErrorMessage(Utils.getBundleConfElement("message.creatingKeyError"), t);
				t.printStackTrace();
			}

			// define creating key duration
			double keyCreationDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;

			// construct header
			header.append(System.getProperty("line.separator") + "parseDuration= " + parseDuration + "s");
			header.append(System.getProperty("line.separator") + "keyCreationDuration= "
					+ keyCreationDuration + "s");
			header.append(System.getProperty("line.separator") + System.getProperty("line.separator"));

			// create key file
			try {
				if (!utils.getVerbosity().contains(Utils.HEADER_TAG)) {
					header.setLength(0);
				}
				SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
				resultFileName = SingleAccessKeyTreeDumper.dumpDotFile(header.toString(), tree2dump)
						.getName();
			} catch (IOException e) {
				utils.setErrorMessage(Utils.getBundleConfElement("message.creatingFileError"), e);
				e.printStackTrace();
			}
		} catch (Throwable t) {
			utils.setErrorMessage(Utils.getBundleConfElement("message.error"), t);
			t.printStackTrace();
		}

		// if error exist use error file as result file
		if (utils.getErrorMessageFile() != null) {
			resultFileName = utils.getErrorMessageFile().getName();
		}

		// display the URL of file result
		System.out.println(resultFileName);
	}
}
