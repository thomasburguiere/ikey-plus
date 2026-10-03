package fr.lis.ikeyplus.IO;

import fr.lis.ikeyplus.model.key.SingleAccessKeyTree;
import fr.lis.ikeyplus.services.IdentificationKeyGenerator;
import fr.lis.ikeyplus.utils.IkeyConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;

public class SingleAccessKeyTreeDumperTest {

    private static String generatedFilesFolder;

    @Test
    public void should_generate_cichorieae_flat_html_key() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/cichorieae.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder().enablePruning().verbosity(IkeyConfig.VerbosityLevel.HEADER).build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        final IdentificationKeyGenerator identificationKeyGenerator;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
        final boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
        final File file = SingleAccessKeyTreeDumper.dumpFlatHtmlFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
        final byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
        final String result = new String(resultBytes, StandardCharsets.UTF_8);

        final byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/cichorieae_flat.html"));
        final String fixture = new String(fixtureBytes, StandardCharsets.UTF_8);
        assertThat(result).isEqualTo(fixture);
    }

    @Test
    public void should_generate_cichorieae_html_tree_key() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/cichorieae.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        final IdentificationKeyGenerator identificationKeyGenerator;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
        final boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
        final File file = SingleAccessKeyTreeDumper.dumpHtmlFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
        final byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
        final String result = new String(resultBytes, StandardCharsets.UTF_8);

        final byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/cichorieae_tree.html"));
        final String fixture = new String(fixtureBytes, StandardCharsets.UTF_8);
        assertThat(result).isEqualTo(fixture);
    }

    @Test
    public void should_generate_genetta_flat_txt_key() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .representation(IkeyConfig.KeyRepresentation.FLAT)
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        final IdentificationKeyGenerator identificationKeyGenerator;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
        final boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
        final File file = SingleAccessKeyTreeDumper.dumpFlatTxtFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
        final byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
        final String result = new String(resultBytes, StandardCharsets.UTF_8);

        final byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_flat.txt"));
        final String fixture = new String(fixtureBytes, StandardCharsets.UTF_8);
        assertThat(result).isEqualTo(fixture);
    }

    @Test
    public void should_generate_genetta_tree_txt_key() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        final IdentificationKeyGenerator identificationKeyGenerator;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
        final boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
        final File file = SingleAccessKeyTreeDumper.dumpTxtFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
        final byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
        final String result = new String(resultBytes, StandardCharsets.UTF_8);

        final byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_tree.txt"));
        final String fixture = new String(fixtureBytes, StandardCharsets.UTF_8);
        assertThat(result).isEqualTo(fixture);
    }

    @Test
    public void should_generate_genetta_tree_wiki_key() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        final IdentificationKeyGenerator identificationKeyGenerator;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
        final boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
        final File file = SingleAccessKeyTreeDumper.dumpWikiFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
        final byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
        final String result = new String(resultBytes, StandardCharsets.UTF_8);

        final byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_tree.wiki"));
        final String fixture = new String(fixtureBytes, StandardCharsets.UTF_8);
        assertThat(result).isEqualTo(fixture);
    }

    @Test
    public void should_generate_genetta_flat_wiki_key() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        final IdentificationKeyGenerator identificationKeyGenerator;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
        final boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
        final File file = SingleAccessKeyTreeDumper.dumpFlatWikiFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
        final byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
        final String result = new String(resultBytes, StandardCharsets.UTF_8);

        final byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_flat.wiki"));
        final String fixture = new String(fixtureBytes, StandardCharsets.UTF_8);
        assertThat(result).isEqualTo(fixture);
    }

    @Test
    public void should_generate_genetta_sdd_key() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        final IdentificationKeyGenerator identificationKeyGenerator;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
        final File file = SingleAccessKeyTreeDumper.dumpSddFile(tree2dump);
        final byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
        String result = new String(resultBytes, StandardCharsets.UTF_8);
        result = result.replaceFirst("created.*\"", "");


        final byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_identification_key.sdd"));
        String fixture = new String(fixtureBytes, StandardCharsets.UTF_8);
        fixture = fixture.replaceFirst("created.*\"", "");

        assertThat(result).isEqualTo(fixture);
    }

    @Test
    public void should_generate_genetta_with_statistics() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .verbosity(IkeyConfig.VerbosityLevel.STATISTICS)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        final IdentificationKeyGenerator identificationKeyGenerator;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
        final boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
        final File file = SingleAccessKeyTreeDumper.dumpFlatTxtFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
        final byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
        String result = new String(resultBytes, StandardCharsets.UTF_8);
        result = result.replaceFirst("created.*\"", "");


        final byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_flat_stats.txt"));
        String fixture = new String(fixtureBytes, StandardCharsets.UTF_8);
        fixture = fixture.replaceFirst("created.*\"", "");

        assertThat(result).isEqualTo(fixture);
    }

    @Test
    public void should_generate_genetta_wiki_with_statistics() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .verbosity(IkeyConfig.VerbosityLevel.STATISTICS)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        final IdentificationKeyGenerator identificationKeyGenerator;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
        final boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
        final File file = SingleAccessKeyTreeDumper.dumpFlatWikiFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
        final byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
        final String result = new String(resultBytes, StandardCharsets.UTF_8);


        final byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_flat_stats.wiki"));
        final String fixture = new String(fixtureBytes, StandardCharsets.UTF_8);

        assertThat(result).isEqualTo(fixture);

    }

    @Test
    public void should_generate_genetta_html_with_statistics() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .verbosity(IkeyConfig.VerbosityLevel.STATISTICS)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        final IdentificationKeyGenerator identificationKeyGenerator;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
        final boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
        final File file = SingleAccessKeyTreeDumper.dumpFlatHtmlFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
        final byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
        final String result = new String(resultBytes, StandardCharsets.UTF_8);


        final byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_flat_stats.html"));
        final String fixture = new String(fixtureBytes, StandardCharsets.UTF_8);

        assertThat(result).isEqualTo(fixture);
    }

    @Test
    @Disabled
    public void should_generate_genetta_interactive_html() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        final IdentificationKeyGenerator identificationKeyGenerator;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
        final boolean statisticsEnabled = config.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTICS);
        final File file = SingleAccessKeyTreeDumper.dumpInteractiveHtmlFile("", tree2dump, statisticsEnabled, generatedFilesFolder);
        final byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
        final String result = new String(resultBytes, StandardCharsets.UTF_8);


        final byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta_interactive.html"));
        final String fixture = new String(fixtureBytes, StandardCharsets.UTF_8);

        assertThat(result).isEqualTo(fixture);

    }


    @Test
    public void should_generate_genetta_dot() throws Exception {
        final String stringUrl = "src/test/resources/inputFiles/genetta.sdd.xml";

        final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(IkeyConfig.VerbosityLevel.HEADER)
                .representation(IkeyConfig.KeyRepresentation.TREE)
                .build();

        final SDDSaxParser sddSaxParser;
        sddSaxParser = new SDDSaxParser(stringUrl, config);

        final IdentificationKeyGenerator identificationKeyGenerator;

        identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
        final File file = SingleAccessKeyTreeDumper.dumpDotFile("", tree2dump, generatedFilesFolder);
        final byte[] resultBytes = Files.readAllBytes(Paths.get(file.toURI()));
        final String result = new String(resultBytes, StandardCharsets.UTF_8).replaceFirst("digraph key_\\d.* ", "");


        final byte[] fixtureBytes = Files.readAllBytes(Paths.get("src/test/resources/fixtures/genetta.gv"));
        final String fixture = new String(fixtureBytes, StandardCharsets.UTF_8).replaceFirst("digraph key_\\d.* ", "");

        assertThat(result).isEqualTo(fixture);
    }

    @BeforeAll
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