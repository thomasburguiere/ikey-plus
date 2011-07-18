package services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import model.SingleAccessKeyTree;

import org.junit.Test;

import utils.IdentificationKeyErrorMessage;
import utils.Utils;
import IO.SDDSaxParser;

/**
 * This class allow to test the TEXT output of IdentificationKeyGenerator service
 * 
 * @author Florian Causse
 * @created 18-04-2011
 */
public class IdentificationKeyTXTGeneratorTest {

	public Logger logger = Logger.getAnonymousLogger();

	@Test
	public void testIdentificationKeyGenerator() {
		logger.info("testIdentificationKeyGenerator");
		long beforeTime = System.currentTimeMillis();

		SDDSaxParser sddSaxParser = null;
		try {
			String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/Cichorieae-fullSDD.xml";
			// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/milichia_revision-sdd.xml";
			// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/testSDD.xml";
			// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/feuillesSDD.xml";
			// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/smallSDD.xml";
			// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/wrongSDD.xml";

			// test if the URL is valid
			URLConnection urlConnection;
			InputStream httpStream;
			try {
				URL fileURL = new URL(stringUrl);
				// open URL (HTTP query)
				urlConnection = fileURL.openConnection();
				// Open data stream
				httpStream = urlConnection.getInputStream();
			} catch (java.net.MalformedURLException e) {
				new IdentificationKeyErrorMessage("URL to SDD file not correct", e);
				e.printStackTrace();
			} catch (java.io.IOException e) {
				new IdentificationKeyErrorMessage("URL to SDD file not correct", e);
				e.printStackTrace();
			}
			sddSaxParser = new SDDSaxParser(stringUrl);
		} catch (Throwable t) {
			new IdentificationKeyErrorMessage("SDD parsing error", t);
			t.printStackTrace();
		}
		double parseDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;
		beforeTime = System.currentTimeMillis();

		IdentificationKeyGenerator identificationKeyGenerator = null;
		try {
			identificationKeyGenerator = new IdentificationKeyGenerator(new SingleAccessKeyTree(),
					sddSaxParser.getDataset());
			identificationKeyGenerator.createIdentificationKey();
		} catch (Throwable t) {
			new IdentificationKeyErrorMessage("Creating key error", t);
			t.printStackTrace();
		}

		// display error message
		if (Utils.errorMessage != null)
			System.out.println("ErrorMessage= " + Utils.errorMessage);
		// display key
		ResourceBundle bundle = ResourceBundle.getBundle("confTest");

		try {
			System.out.println(identificationKeyGenerator.getSingleAccessKeyTree().toTextFile((bundle)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		double keyDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;
		System.out.println(System.getProperty("line.separator") + "parseDuration= " + parseDuration + "s");
		System.out.println("keyDuration= " + keyDuration + "s");
	}
}
