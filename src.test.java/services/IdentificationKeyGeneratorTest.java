package services;

import java.util.logging.Logger;
import model.PolytomousKeyTree;
import org.junit.Test;
import IO.SDDSaxParser;

public class IdentificationKeyGeneratorTest {

	public Logger logger = Logger.getAnonymousLogger();

	@Test
	public void testIdentificationKeyGenerator() {
		logger.info("testIdentificationKeyGenerator");
		long beforeTime = System.currentTimeMillis();
		
		SDDSaxParser sddSaxParser = null;
		try {
			//sddSaxParser = new SDDSaxParser("http://www.infosyslab.fr/vibrant/project/test/milichia_revision-sdd.xml");
			//sddSaxParser = new SDDSaxParser("http://www.infosyslab.fr/vibrant/project/test/testSDD.xml");
			sddSaxParser = new SDDSaxParser("http://www.infosyslab.fr/vibrant/project/test/Cichorieae-fullSDD.xml");
		} catch (Throwable t) {
			t.printStackTrace();
		}
		double parseDuration = (double)(System.currentTimeMillis() - beforeTime) / 1000;
		beforeTime = System.currentTimeMillis();
		
		IdentificationKeyGenerator identificationKeyGenerator = new IdentificationKeyGenerator(
				new PolytomousKeyTree(), sddSaxParser.getDataset());
		identificationKeyGenerator.createIdentificationKey();
		
		double keyDuration = (double)(System.currentTimeMillis() - beforeTime) / 1000;
		System.out.println("\nparseDuration= " + parseDuration + "s");
		System.out.println("keyDuration= " + keyDuration + "s");
	}
}
