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

    public Logger logger = Logger.getAnonymousLogger();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // creation of IkeyConfig object (containing options)

        // set the confTest ResourceBundle
        // IkeyConfig.setBundleConfOverridable(ResourceBundle.getBundle("fr.lis.ikeyplus.confTest"));
        // IkeyConfig.setBundleConf(ResourceBundle.getBundle("fr.lis.ikeyplus.confTest"));

    }

    @Test
    public void should_generate_genetta_identification_key_with_default_options() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";
        IkeyConfig config = new IkeyConfig();
        // options
        config.setFewStatesCharacterFirst(false);
        config.setMergeCharacterStatesIfSameDiscrimination(false);
        config.setPruning(true);
        config.setVerbosity(Sets.newHashSet(HEADER, OTHER, WARNING, STATISTIC));

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

                byte[] encoded =  Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta.txt"));
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

    @Test
    public void should_generate_genetta_identification_key_with_weights_options() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";
        IkeyConfig config = new IkeyConfig();
        // options
        config.setFewStatesCharacterFirst(false);
        config.setMergeCharacterStatesIfSameDiscrimination(false);
        config.setPruning(true);
        config.setVerbosity(Sets.newHashSet(HEADER, OTHER, WARNING, STATISTIC));
        config.setWeightContext(IkeyConfig.WeightContext.OBSERVATION_CONVENIENCE);

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

                byte[] encoded =  Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_weights.txt"));
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

    @Test
    public void should_generate_cichorieae_identification_key_with_default_options() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/Cichorieae-fullSDD.xml";
        IkeyConfig config = new IkeyConfig();
        // options
        config.setFewStatesCharacterFirst(false);
        config.setMergeCharacterStatesIfSameDiscrimination(false);
        config.setPruning(true);
        config.setVerbosity(Sets.newHashSet(HEADER, WARNING, STATISTIC));

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

                byte[] encoded =  Files.readAllBytes(Paths.get("src/test/resources/fixtures/cichorieae.txt"));
                String fixture = new String(encoded, "UTF-8");
                assertEquals(tree2dump.toString(), fixture);
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
