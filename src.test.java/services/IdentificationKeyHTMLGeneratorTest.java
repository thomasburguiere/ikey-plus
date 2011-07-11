package services;

import java.util.logging.Logger;
import model.SingleAccessKeyTree;
import IO.SDDSaxParser;

import org.junit.Test;

import utils.IdentificationKeyErrorMessage;

public class IdentificationKeyHTMLGeneratorTest {

	public Logger logger = Logger.getAnonymousLogger();

	@Test
	public void testIdentificationKeyGenerator() {
		logger.info("testIdentificationKeyGenerator");
		long beforeTime = System.currentTimeMillis();

		SDDSaxParser sddSaxParser = null;
		try{
			// sddSaxParser = new
			// SDDSaxParser("http://www.infosyslab.fr/vibrant/project/test/milichia_revision-sdd.xml");
			// sddSaxParser = new
			// SDDSaxParser("http://www.infosyslab.fr/vibrant/project/test/testSDD.xml");
			// sddSaxParser = new
			// SDDSaxParser("http://www.infosyslab.fr/vibrant/project/test/Cichorieae-fullSDD.xml");
			sddSaxParser = new SDDSaxParser("http://www.infosyslab.fr/vibrant/project/test/feuillesSDD.xml");
		}catch(Throwable t){
			new IdentificationKeyErrorMessage("SDD parsing error", t);
			t.printStackTrace();
		}
		double parseDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;
		beforeTime = System.currentTimeMillis();

		IdentificationKeyGenerator identificationKeyGenerator = new IdentificationKeyGenerator(
				new SingleAccessKeyTree(), sddSaxParser.getDataset());
		try{
			identificationKeyGenerator.createIdentificationKey();
		}catch(Throwable t){
			new IdentificationKeyErrorMessage("Creating key error", t);
			t.printStackTrace();
		}

		System.out.println(identificationKeyGenerator.getSingleAccessKeyTree().toHtml());

		double keyDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;
		System.out.println(System.getProperty("line.separator") + "parseDuration= " + parseDuration + "s");
		System.out.println("keyDuration= " + keyDuration + "s");
	}
}
