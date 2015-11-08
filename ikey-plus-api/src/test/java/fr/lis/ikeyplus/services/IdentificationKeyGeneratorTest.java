package fr.lis.ikeyplus.services;

import com.google.common.collect.Sets;
import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.utils.IkeyConfig;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.HEADER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.OTHER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.STATISTICS;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.WARNING;
import static org.assertj.core.api.Assertions.assertThat;

public class IdentificationKeyGeneratorTest {

    @Test
    public void should_generate_genetta_identification_key_with_default_options() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(Sets.newHashSet(HEADER, OTHER, WARNING, STATISTICS))
                .build();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator = new IdentificationKeyGenerator();

            try {
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getIdentificationKey(sddSaxParser.getDataset(), config);

                byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta.txt"));
                String genettaFixture = new String(encoded, "UTF-8");
                assertThat(genettaFixture).isEqualTo(tree2dump.toString());
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
                .enablePruning()
                .verbosity(Sets.newHashSet(HEADER, OTHER, WARNING, STATISTICS))
                .weightContext(IkeyConfig.WeightContext.OBSERVATION_CONVENIENCE)
                .build();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator = new IdentificationKeyGenerator();

            try {
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getIdentificationKey(sddSaxParser.getDataset(), config);


                byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_weights.txt"));
                String genettaFixture = new String(encoded, "UTF-8");
                assertThat(genettaFixture).isEqualTo(tree2dump.toString());
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
    public void should_generate_genetta_identification_key_with_jaccard_score_option() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(Sets.newHashSet(HEADER, OTHER, WARNING, STATISTICS))
                .scoreMethod(IkeyConfig.ScoreMethod.JACCARD)
                .build();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator = new IdentificationKeyGenerator();

            try {
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getIdentificationKey(sddSaxParser.getDataset(), config);


                byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_jaccard.txt"));
                String genettaFixture = new String(encoded, "UTF-8");
                assertThat(genettaFixture).isEqualTo(tree2dump.toString());
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
    public void should_generate_genetta_identification_key_with_sokalAndMichener_score_option() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(Sets.newHashSet(HEADER, OTHER, WARNING, STATISTICS))
                .scoreMethod(IkeyConfig.ScoreMethod.SOKAL_AND_MICHENER)
                .build();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator = new IdentificationKeyGenerator();

            try {
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getIdentificationKey(sddSaxParser.getDataset(), config);


                byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_sokal_michener.txt"));
                String genettaFixture = new String(encoded, "UTF-8");
                assertThat(genettaFixture).isEqualTo(tree2dump.toString());
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
                .enablePruning()
                .verbosity(Sets.newHashSet(HEADER, WARNING, STATISTICS))
                .build();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator = new IdentificationKeyGenerator();

            try {
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getIdentificationKey(sddSaxParser.getDataset(), config);


                byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/fixtures/cichorieae.txt"));
                String fixture = new String(encoded, "UTF-8");
                assertThat(fixture).isEqualTo(tree2dump.toString());
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
