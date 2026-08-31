package fr.lis.ikeyplus.services;

import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.utils.IkeyConfig;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.HEADER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.OTHER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.STATISTICS;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.WARNING;
import static org.assertj.core.api.Assertions.assertThat;

public class IdentificationKeyGeneratorTest {

    @Test
    public void should_generate_genetta_identification_key_with_default_options() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(Set.of(HEADER, OTHER, WARNING, STATISTICS))
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        IdentificationKeyGenerator identificationKeyGenerator = null;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();

        final byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta.txt"));
        final String genettaFixture = new String(encoded, StandardCharsets.UTF_8);
        assertThat(genettaFixture).isEqualTo(tree2dump.toString());
    }

    @Test
    public void should_generate_genetta_identification_key_with_weights_options() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(Set.of(HEADER, OTHER, WARNING, STATISTICS))
                .weightContext(IkeyConfig.WeightContext.OBSERVATION_CONVENIENCE)
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        IdentificationKeyGenerator identificationKeyGenerator = null;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();

        final byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_weights.txt"));
        final String genettaFixture = new String(encoded, StandardCharsets.UTF_8);
        assertThat(genettaFixture).isEqualTo(tree2dump.toString());
    }


    @Test
    public void should_generate_genetta_identification_key_with_jaccard_score_option() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(Set.of(HEADER, OTHER, WARNING, STATISTICS))
                .scoreMethod(IkeyConfig.ScoreMethod.JACCARD)
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        IdentificationKeyGenerator identificationKeyGenerator = null;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();

        final byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_jaccard.txt"));
        final String genettaFixture = new String(encoded, StandardCharsets.UTF_8);
        assertThat(genettaFixture).isEqualTo(tree2dump.toString());
    }


    @Test
    public void should_generate_genetta_identification_key_with_sokalAndMichener_score_option() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(Set.of(HEADER, OTHER, WARNING, STATISTICS))
                .scoreMethod(IkeyConfig.ScoreMethod.SOKAL_AND_MICHENER)
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        IdentificationKeyGenerator identificationKeyGenerator = null;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();

        final byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_sokal_michener.txt"));
        final String genettaFixture = new String(encoded, StandardCharsets.UTF_8);
        assertThat(genettaFixture).isEqualTo(tree2dump.toString());
    }

    @Test
    public void should_generate_cichorieae_identification_key_with_default_options() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/cichorieae.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(Set.of(HEADER, WARNING, STATISTICS))
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        IdentificationKeyGenerator identificationKeyGenerator = null;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();

        final byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/fixtures/cichorieae.txt"));
        final String fixture = new String(encoded, StandardCharsets.UTF_8);
        assertThat(fixture).isEqualTo(tree2dump.toString());
    }

}
