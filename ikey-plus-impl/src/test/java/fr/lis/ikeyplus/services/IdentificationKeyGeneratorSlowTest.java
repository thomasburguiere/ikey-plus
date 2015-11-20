package fr.lis.ikeyplus.services;

import com.google.common.collect.Sets;
import fr.lis.ikeyplus.IO.SDDParser;
import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.utils.IkeyConfig;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.HEADER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.STATISTICS;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.WARNING;
import static org.assertj.core.api.Assertions.assertThat;

public class IdentificationKeyGeneratorSlowTest {



    @Test
    public void should_generate_cichorieae_identification_key_with_default_options() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/cichorieae.sdd.xml";

        IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(Sets.newHashSet(HEADER, WARNING, STATISTICS))
                .build();

        SDDParser sddParser;
        sddParser = new SDDSaxParser();

        IdentificationKeyGenerator identificationKeyGenerator = new IdentificationKeyGeneratorImpl();

        SingleAccessKeyTree tree2dump = identificationKeyGenerator.getIdentificationKey(sddParser.parseDataset(stringUrl, config), config);


        byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/fixtures/cichorieae.txt"));
        String fixture = new String(encoded, "UTF-8");
        assertThat(fixture).isEqualTo(tree2dump.toString());
    }

}
