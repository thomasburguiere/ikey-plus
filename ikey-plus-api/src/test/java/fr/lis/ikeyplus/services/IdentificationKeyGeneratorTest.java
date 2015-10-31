package fr.lis.ikeyplus.services;

import java.io.IOException;
import java.util.logging.Logger;

import fr.lis.ikeyplus.utils.IkeyConfig;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;

@Ignore
public class IdentificationKeyGeneratorTest {

	private IkeyConfig utils = new IkeyConfig();
	public Logger logger = Logger.getAnonymousLogger();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// creation of IkeyConfig object (containing options)

		// set the confTest ResourceBundle
		// IkeyConfig.setBundleConfOverridable(ResourceBundle.getBundle("fr.lis.ikeyplus.confTest"));
		// IkeyConfig.setBundleConf(ResourceBundle.getBundle("fr.lis.ikeyplus.confTest"));

	}

	@Test
	public void test() throws Exception {
		// String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";
		String stringUrl = "src/test/resources/inputFiles/Key_test.sdd.xml";
		
		// options
		utils.setFewStatesCharacterFirst(false);
		utils.setMergeCharacterStatesIfSameDiscrimination(false);
		utils.setPruning(true);
		utils.setVerbosity("hows");
		utils.setScoreMethod(IkeyConfig.ScoreMethod.XPER);
		utils.setWeightContext(IkeyConfig.WeightContext.COST_EFFECTIVENESS);
		utils.setWeightType(IkeyConfig.WeightType.GLOBAL);

		logger.info("testIdentificationKeyGenerator");
		// define time before parsing SDD file
		long beforeTime = System.currentTimeMillis();

		SDDSaxParser sddSaxParser = null;
		try {
			sddSaxParser = new SDDSaxParser(stringUrl, utils);

			sddSaxParser = new SDDSaxParser(stringUrl, utils);

			IdentificationKeyGenerator identificationKeyGenerator = null;

			try {
				identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), utils);
				identificationKeyGenerator.createIdentificationKey();
				SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
				logger.info("done");
			} catch (Exception e) {
				e.printStackTrace();
				throw (e);
			}

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
