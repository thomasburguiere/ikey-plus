package fr.lis.ikeyplus.io;

import com.google.common.collect.Sets;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.services.IdentificationKeyGenerator;
import fr.lis.ikeyplus.utils.IkeyConfig;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class SingleAccessKeyTreeDumperTest extends TestCase {


    @Test
    public void testDumpFlatHtmlFile() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/cichorieae.sdd.xml";
        IkeyConfig config = new IkeyConfig();
        // options
        config.setFewStatesCharacterFirst(false);
        config.setMergeCharacterStatesIfSameDiscrimination(false);
        config.setVerbosity(Sets.newHashSet(IkeyConfig.VerbosityLevel.HEADER));
        config.setPruning(true);

        setup();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator = null;

            try {
                identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
                identificationKeyGenerator.createIdentificationKey();
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
                File file = SingleAccessKeyTreeDumper.dumpFlatHtmlFile("", tree2dump, false);
                byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
                String result = new String(resultBytes, "UTF-8");

                byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/cichorieae_flat.html"));
                String fixture = new String(fixtureBytes, "UTF-8");
                assertEquals(fixture, result);
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

    private void setup() {
        IkeyConfig.setBundleConfOverridable(ResourceBundle.getBundle("confTest"));
        IkeyConfig.setBundleConf(ResourceBundle.getBundle("confTest"));

        String path = IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.prefix")
                + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder");
        if (!new File(path).exists()) {
            new File(path).mkdirs();
        }
    }
}