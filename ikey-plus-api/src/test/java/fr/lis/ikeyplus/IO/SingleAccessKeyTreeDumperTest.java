package fr.lis.ikeyplus.IO;

import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.services.IdentificationKeyGenerator;
import fr.lis.ikeyplus.utils.IkeyConfig;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;

public class SingleAccessKeyTreeDumperTest {

    private static String generatedFilesFolder;

    @Test
    public void should_generate_cichorieae_flat_html_key() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/cichorieae.sdd.xml";

        IkeyConfig config = IkeyConfig.builder().enablePruning().verbosity(IkeyConfig.VerbosityLevel.HEADER).build();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator;

            try {
                identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
                identificationKeyGenerator.createIdentificationKey();
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
                boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
                File file = SingleAccessKeyTreeDumper.dumpFlatHtmlFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
                byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
                String result = new String(resultBytes, "UTF-8");

                byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/cichorieae_flat.html"));
                String fixture = new String(fixtureBytes, "UTF-8");
                assertThat(result).isEqualTo(fixture);
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
    public void should_generate_cichorieae_flat_tree_key() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/cichorieae.sdd.xml";

        IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator;

            try {
                identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
                identificationKeyGenerator.createIdentificationKey();
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
                boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
                File file = SingleAccessKeyTreeDumper.dumpHtmlFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
                byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
                String result = new String(resultBytes, "UTF-8");

                byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/cichorieae_tree.html"));
                String fixture = new String(fixtureBytes, "UTF-8");
                assertThat(result).isEqualTo(fixture);
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
    public void should_generate_genetta_flat_txt_key() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .representation(IkeyConfig.KeyRepresentation.FLAT)
                .build();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator;

            try {
                identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
                identificationKeyGenerator.createIdentificationKey();
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
                boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
                File file = SingleAccessKeyTreeDumper.dumpFlatTxtFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
                byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
                String result = new String(resultBytes, "UTF-8");

                byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_flat.txt"));
                String fixture = new String(fixtureBytes, "UTF-8");
                assertThat(result).isEqualTo(fixture);
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
    public void should_generate_genetta_tree_txt_key() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator;

            try {
                identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
                identificationKeyGenerator.createIdentificationKey();
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
                boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
                File file = SingleAccessKeyTreeDumper.dumpTxtFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
                byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
                String result = new String(resultBytes, "UTF-8");

                byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_tree.txt"));
                String fixture = new String(fixtureBytes, "UTF-8");
                assertThat(result).isEqualTo(fixture);
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
    public void should_generate_genetta_tree_wiki_key() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator;

            try {
                identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
                identificationKeyGenerator.createIdentificationKey();
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
                boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
                File file = SingleAccessKeyTreeDumper.dumpWikiFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
                byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
                String result = new String(resultBytes, "UTF-8");

                byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_tree.wiki"));
                String fixture = new String(fixtureBytes, "UTF-8");
                assertThat(result).isEqualTo(fixture);
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
    public void should_generate_genetta_flat_wiki_key() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator;

            try {
                identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
                identificationKeyGenerator.createIdentificationKey();
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
                boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
                File file = SingleAccessKeyTreeDumper.dumpFlatWikiFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
                byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
                String result = new String(resultBytes, "UTF-8");

                byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_flat.wiki"));
                String fixture = new String(fixtureBytes, "UTF-8");
                assertThat(result).isEqualTo(fixture);
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
    public void should_generate_genetta_sdd_key() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator;

            try {
                identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
                identificationKeyGenerator.createIdentificationKey();
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
                File file = SingleAccessKeyTreeDumper.dumpSddFile(tree2dump);
                byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
                String result = new String(resultBytes, "UTF-8");
                result = result.replaceFirst("created.*\"", "");


                byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_identification_key.sdd"));
                String fixture = new String(fixtureBytes, "UTF-8");
                fixture = fixture.replaceFirst("created.*\"", "");

                assertThat(result).isEqualTo(fixture);
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
    public void should_generate_genetta_with_statistics() throws Exception {
        String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .verbosity(IkeyConfig.VerbosityLevel.STATISTICS)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        SDDSaxParser sddSaxParser;
        try {
            sddSaxParser = new SDDSaxParser(stringUrl, config);

            IdentificationKeyGenerator identificationKeyGenerator;

            try {
                identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
                identificationKeyGenerator.createIdentificationKey();
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
                final boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
                File file = SingleAccessKeyTreeDumper.dumpFlatTxtFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
                byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
                String result = new String(resultBytes, "UTF-8");
                result = result.replaceFirst("created.*\"", "");


                byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_flat_stats.txt"));
                String fixture = new String(fixtureBytes, "UTF-8");
                fixture = fixture.replaceFirst("created.*\"", "");

                assertThat(result).isEqualTo(fixture);
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

    @BeforeClass
    public static void setUp() {
        IkeyConfig.setBundleConfOverridable(ResourceBundle.getBundle("confTest"));
        IkeyConfig.setBundleConf(ResourceBundle.getBundle("confTest"));

        generatedFilesFolder = IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.prefix")
                + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder");
        if (!new File(generatedFilesFolder).exists()) {
            new File(generatedFilesFolder).mkdirs();
        }
    }
}