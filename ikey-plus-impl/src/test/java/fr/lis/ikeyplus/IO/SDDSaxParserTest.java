package fr.lis.ikeyplus.IO;

import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.utils.IkeyConfig;
import org.junit.Test;

import java.io.File;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class allows to test the SDDSaxParser
 *
 * @author Florian Causse
 */
public class SDDSaxParserTest {

    private static final int EXPECTED_NB_OF_CHARACTERS = 303;
    private static final int EXPECTED_NB_OF_TAXA = 144;
    private static final String DATASET_NAME = "Project: Cichorieae";

    @Test
    public void should_parse_local_file() throws Exception {
        // creation of IkeyConfig object (containing options)
        IkeyConfig config = IkeyConfig.builder().build();
        SDDParser sddParser = new SDDSaxParser();
        DataSet dataset = sddParser.parseDataset(new File("src/test/resources/inputFiles/cichorieae.sdd.xml"), config);
        assertThat(dataset.getLabel()).isEqualToIgnoringCase(DATASET_NAME);
        assertThat(dataset.getCharacters()).hasSize(EXPECTED_NB_OF_CHARACTERS);
        assertThat(dataset.getTaxa()).hasSize(EXPECTED_NB_OF_TAXA);
    }
}
