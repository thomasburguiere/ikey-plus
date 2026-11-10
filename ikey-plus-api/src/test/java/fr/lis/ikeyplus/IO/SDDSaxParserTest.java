package fr.lis.ikeyplus.IO;

import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.utils.IkeyConfig;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * This class allows to test the SDDSaxParser
 *
 * @author Florian Causse
 */
public class SDDSaxParserTest {
    final SDDSaxParser sddSaxParser = new SDDSaxParser();

    final IkeyConfig config = IkeyConfig.builder().build();

    @Test
    void should_parse_local_file() throws Exception {
        final DataSet dataset = sddSaxParser.parseDataset(new FileInputStream("src/test/resources/inputFiles/cichorieae.sdd.xml"), config);
        assertThat(dataset.getLabel()).isEqualToIgnoringCase("Project: Cichorieae");
        assertThat(dataset.getCharacters()).hasSize(303);
        assertThat(dataset.getTaxa()).hasSize(144);
    }

    @Test
    void should_throw_exception_for_invalid_xml() {
        final var invalidXml = "<Datasets><not-closed>".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() ->sddSaxParser.parseDataset(new ByteArrayInputStream(invalidXml), config))
                .isInstanceOf(SAXParseException.class);
    }
}
