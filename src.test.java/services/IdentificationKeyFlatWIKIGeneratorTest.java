package services;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import model.SingleAccessKeyTree;

import org.junit.Test;

import utils.Utils;
import IO.SDDSaxParser;

/**
 * This class allow to test the WIKI output of IdentificationKeyGenerator service
 * 
 * @author Thomas Burguiere
 * @created 18-07-2011
 */
public class IdentificationKeyFlatWIKIGeneratorTest {

	public Logger logger = Logger.getAnonymousLogger();

	@Test
	public void testIdentificationKeyGenerator() {

		// set the confTest ResourceBundle
		Utils.setBundleConfOverridable(ResourceBundle.getBundle("confOverridableTest"));

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
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/smallSDD.xml";
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/wrongSDD.xml";
				String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/pruningSDD.xml";

				// options
				Utils.fewStatesCharacterFirst = false;
				Utils.mergeCharacterStatesIfSameDiscimination = false;
				Utils.reduceSameConclusionPath = false;
				Utils.pruning = false;
				Utils.verbosity = "";
				Utils.scoreMethod = Utils.XPER;

				// test if the URL is valid
				URLConnection urlConnection;
				try {
					URL fileURL = new URL(stringUrl);
					// open URL (HTTP query)
					urlConnection = fileURL.openConnection();
					// Open data stream
					urlConnection.getInputStream();
				} catch (java.net.MalformedURLException e) {
					resultFileName = Utils.setErrorMessage(Utils.getBundleConfElement("message.urlError"), e);
					e.printStackTrace();
				} catch (java.io.IOException e) {
					resultFileName = Utils.setErrorMessage(Utils.getBundleConfElement("message.urlError"), e);
					e.printStackTrace();
				}
				sddSaxParser = new SDDSaxParser(stringUrl);
				// construct header
				header.append(System.getProperty("line.separator") + sddSaxParser.getDataset().getLabel()
						+ ", " + Utils.getBundleConfOverridableElement("message.createdBy")
						+ System.getProperty("line.separator"));
				header.append(System.getProperty("line.separator") + "Options:");
				header.append(System.getProperty("line.separator") + "sddURL=" + stringUrl);
				header.append(System.getProperty("line.separator") + "fewStatesCharacterFirst="
						+ Utils.fewStatesCharacterFirst);
				header.append(System.getProperty("line.separator")
						+ "mergeCharacterStatesIfSameDiscimination="
						+ Utils.mergeCharacterStatesIfSameDiscimination);
				header.append(System.getProperty("line.separator") + "reduceSameConclusionPath="
						+ Utils.reduceSameConclusionPath);
				header.append(System.getProperty("line.separator") + "pruning=" + Utils.pruning);
				header.append(System.getProperty("line.separator") + "verbosity=" + Utils.verbosity);
				header.append(System.getProperty("line.separator") + "scoreMethod=" + Utils.scoreMethod
						+ System.getProperty("line.separator"));

			} catch (Throwable t) {
				resultFileName = Utils.setErrorMessage(Utils.getBundleConfElement("message.parsingError"), t);
				t.printStackTrace();
			}

			// define parse duration
			double parseDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;
			// define time before processing key
			beforeTime = System.currentTimeMillis();

			IdentificationKeyGenerator identificationKeyGenerator = null;
			try {
				identificationKeyGenerator = new IdentificationKeyGenerator(new SingleAccessKeyTree(),
						sddSaxParser.getDataset());
				identificationKeyGenerator.createIdentificationKey();
			} catch (Throwable t) {
				resultFileName = Utils.setErrorMessage(
						Utils.getBundleConfElement("message.creatingKeyError"), t);
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
				if(!Utils.verbosity.contains(Utils.HEADERTAG)){
					header.setLength(0); 
				}
				resultFileName = identificationKeyGenerator.getSingleAccessKeyTree()
						.toFlatWikiFile(header.toString()).getName();
			} catch (IOException e) {
				resultFileName = Utils.setErrorMessage(
						Utils.getBundleConfElement("message.creatingFileError"), e);
				e.printStackTrace();
			}
		} catch (Throwable t) {
			resultFileName = Utils.setErrorMessage(Utils.getBundleConfElement("message.error"), t);
			t.printStackTrace();
		}

		// display the URL of file result
		System.out.println(resultFileName);
	}
}
