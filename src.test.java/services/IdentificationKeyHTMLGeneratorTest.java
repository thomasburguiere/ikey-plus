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
 * This class allow to test the HTML output of IdentificationKeyGenerator service
 * 
 * @author Thomas Burguiere
 * @created 10-07-2011
 */
public class IdentificationKeyHTMLGeneratorTest {

	public Logger logger = Logger.getAnonymousLogger();

	@Test
	public void testIdentificationKeyGenerator() {

		// set the confTest ResourceBundle
		Utils.setBundle(ResourceBundle.getBundle("confTest"));

		// String containing the URL to result file
		String resultURL = null;
		try {
			// define logger
			logger.info("testIdentificationKeyGenerator");
			// define time before parsing SDD file
			long beforeTime = System.currentTimeMillis();

			// define header string
			StringBuffer header = new StringBuffer();
			header.append(Utils.getBundleElement("message.createdBy"));

			SDDSaxParser sddSaxParser = null;
			try {
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/Cichorieae-fullSDD.xml";
				String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/phlebotomes-SDD.xml";
				// String stringUrl =
				// "http://www.infosyslab.fr/vibrant/project/test/milichia_revision-sdd.xml";
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/testSDD.xml";
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/feuillesSDD.xml";
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/smallSDD.xml";
				// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/wrongSDD.xml";

				// options
				// Utils.twoStatesCharacterFirst = true;

				// test if the URL is valid
				URLConnection urlConnection;

				try {
					URL fileURL = new URL(stringUrl);
					// open URL (HTTP query)
					urlConnection = fileURL.openConnection();
					// Open data stream
					urlConnection.getInputStream();
				} catch (java.net.MalformedURLException e) {
					resultURL = Utils.setErrorMessage(Utils.getBundleElement("message.urlError"), e);
					e.printStackTrace();
				} catch (java.io.IOException e) {
					resultURL = Utils.setErrorMessage(Utils.getBundleElement("message.urlError"), e);
					e.printStackTrace();
				}
				sddSaxParser = new SDDSaxParser(stringUrl);
				// construct header
				header.append(System.getProperty("line.separator") + "file=" + stringUrl);

			} catch (Throwable t) {
				resultURL = Utils.setErrorMessage(Utils.getBundleElement("message.parsingError"), t);
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
				resultURL = Utils.setErrorMessage(Utils.getBundleElement("message.creatingKeyError"), t);
				t.printStackTrace();
			}

			// define creating key duration
			double keyDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;

			// construct header
			header.append(System.getProperty("line.separator") + "parseDuration= " + parseDuration + "s");
			header.append(System.getProperty("line.separator") + "keyDuration= " + keyDuration + "s");
			header.append(System.getProperty("line.separator") + System.getProperty("line.separator")
					+ System.getProperty("line.separator"));

			// create key file
			try {
				header.append(Utils.getBundleElement("message.title") + ": "
						+ sddSaxParser.getDataset().getLabel() + System.getProperty("line.separator")
						+ System.getProperty("line.separator") + System.getProperty("line.separator"));
				resultURL = identificationKeyGenerator.getSingleAccessKeyTree().toHtmlFile(header.toString())
						.getName();
			} catch (IOException e) {
				resultURL = Utils.setErrorMessage(Utils.getBundleElement("message.creatingFileError"), e);
				e.printStackTrace();
			}
		} catch (Throwable t) {
			resultURL = Utils.setErrorMessage(Utils.getBundleElement("message.error"), t);
			t.printStackTrace();
		}

		// display the URL of file result
		System.out.println(resultURL);
	}
}
