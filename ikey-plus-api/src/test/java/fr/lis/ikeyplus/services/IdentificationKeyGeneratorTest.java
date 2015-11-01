package fr.lis.ikeyplus.services;

import com.google.common.collect.Sets;
import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.utils.IkeyConfig;
import fr.lis.ikeyplus.utils.IkeyUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.HEADER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.OTHER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.STATISTIC;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.WARNING;
import static org.junit.Assert.assertEquals;

public class IdentificationKeyGeneratorTest {

    @Test
    public void should_generate_genetta_identification_key_with_default_options() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        IkeyConfig config  = IkeyConfig.builder()
            .pruning()
            .verbosity(Sets.newHashSet(HEADER, OTHER, WARNING, STATISTIC))
        .build();

        // define time before parsing SDD file
        long beforeTime = System.currentTimeMillis();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator = null;

            try {
                identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
                identificationKeyGenerator.createIdentificationKey();
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();

                byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta.txt"));
                String genettaFixture = new String(encoded, "UTF-8");
                assertEquals(genettaFixture, tree2dump.toString());
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

        IkeyConfig config = IkeyConfig.builder()
            .pruning()
            .verbosity(Sets.newHashSet(HEADER, OTHER, WARNING, STATISTIC))
            .weightContext(IkeyConfig.WeightContext.OBSERVATION_CONVENIENCE)
        .build();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator = null;

            try {
                identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
                identificationKeyGenerator.createIdentificationKey();
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();

                byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_weights.txt"));
                String genettaFixture = new String(encoded, "UTF-8");
                assertEquals(genettaFixture, tree2dump.toString());
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
        String stringUrl = "src/test/resources/inputFiles/cichorieae.sdd.xml";

        IkeyConfig config = IkeyConfig.builder()
            .pruning()
            .verbosity(Sets.newHashSet(HEADER, WARNING, STATISTIC))
        .build();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator = null;

            try {
                identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
                identificationKeyGenerator.createIdentificationKey();
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();

                byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/fixtures/cichorieae.txt"));
                String fixture = new String(encoded, "UTF-8");
                assertEquals(fixture, tree2dump.toString());
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
