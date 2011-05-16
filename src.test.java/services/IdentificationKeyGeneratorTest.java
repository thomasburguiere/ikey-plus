package services;

import java.util.logging.Logger;

import model.PolytomousKeyTree;

import org.junit.Test;

public class IdentificationKeyGeneratorTest {

	public Logger logger = Logger.getAnonymousLogger();

	@Test
	public void testIdentificationKeyGenerator() {
		logger.info("testIdentificationKeyGenerator");
		IdentificationKeyGenerator identificationKeyGenerator = new IdentificationKeyGenerator(
				new PolytomousKeyTree(), null);
	}
}
