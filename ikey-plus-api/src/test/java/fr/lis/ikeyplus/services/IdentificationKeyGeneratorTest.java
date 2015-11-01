package fr.lis.ikeyplus.services;

import com.google.common.collect.Sets;
import fr.lis.ikeyplus.io.SDDSaxParser;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.utils.IkeyConfig;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.HEADER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.OTHER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.STATISTIC;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.WARNING;
import static org.junit.Assert.assertEquals;

public class IdentificationKeyGeneratorTest {

    private IkeyConfig config = new IkeyConfig();
    public Logger logger = Logger.getAnonymousLogger();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // creation of IkeyConfig object (containing options)

        // set the confTest ResourceBundle
        // IkeyConfig.setBundleConfOverridable(ResourceBundle.getBundle("fr.lis.ikeyplus.confTest"));
        // IkeyConfig.setBundleConf(ResourceBundle.getBundle("fr.lis.ikeyplus.confTest"));

    }

    public void should_generate_key() {

    }

    @Test
    public void test() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        // options
        config.setFewStatesCharacterFirst(false);
        config.setMergeCharacterStatesIfSameDiscrimination(false);
        config.setPruning(true);
        config.setVerbosity(Sets.newHashSet(HEADER, OTHER, WARNING, STATISTIC));
        config.setScoreMethod(IkeyConfig.ScoreMethod.XPER);
        config.setWeightContext(IkeyConfig.WeightContext.COST_EFFECTIVENESS);
        config.setWeightType(IkeyConfig.WeightType.GLOBAL);

        logger.info("testIdentificationKeyGenerator");
        // define time before parsing SDD file
        long beforeTime = System.currentTimeMillis();

        SDDSaxParser sddSaxParser = null;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator = null;

            try {
                identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
                identificationKeyGenerator.createIdentificationKey();
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
                assertEquals(tree2dump.getRoot().getChildren().get(0).toString(), "Rings on tail --> present");

                byte[] encoded =  Files.readAllBytes(Paths.get("src/test/resources/genettaFixtures/tree/genetta.txt"));
                String genettaFixture = new String(encoded, "UTF-8");
                assertEquals(tree2dump.toString(), genettaFixture);
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
