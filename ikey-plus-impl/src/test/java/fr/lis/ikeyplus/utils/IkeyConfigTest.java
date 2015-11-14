package fr.lis.ikeyplus.utils;

import com.google.common.collect.Sets;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.HEADER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.OTHER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.STATISTICS;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.WARNING;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.fromString;
import static org.assertj.core.api.Assertions.assertThat;

public class IkeyConfigTest {

    public static final String ERROR_MESSAGE = "Caralho Fehler";
    public static final String EXCEPTION_MESSAGE = "kurwa";

    @Test
    public void should_parse_verbosity() {
        assertThat(fromString("hows")).containsOnly(HEADER, OTHER, WARNING, STATISTICS);
    }

    @Test
    public void should_have_non_duplicate_verbosity_levels() {
        IkeyConfig config = IkeyConfig.builder().verbosity(Sets.newHashSet(HEADER, OTHER, WARNING, STATISTICS)).verbosity(HEADER).build();
        assertThat(config.getVerbosity()).containsOnly(HEADER, OTHER, WARNING, STATISTICS);
    }

    @Test
    public void should_create_error_file() throws Exception {
        final IkeyConfig ikeyConfig = IkeyConfig.builder().build();
        ikeyConfig.setErrorMessage(ERROR_MESSAGE);
        ikeyConfig.createErrorFile();
        final File errorMessageFile = ikeyConfig.getErrorMessageFile();
        byte[] resultBytes = Files.readAllBytes(Paths.get(errorMessageFile.toURI()));
        String result = new String(resultBytes, "UTF-8");
        assertThat(result).contains(ERROR_MESSAGE);
    }

    @Test
    public void should_create_error_file_with_exception() throws Exception {
        final IkeyConfig ikeyConfig = IkeyConfig.builder().build();
        ikeyConfig.setErrorMessage(ERROR_MESSAGE, new IllegalStateException(EXCEPTION_MESSAGE));
        ikeyConfig.createErrorFile();
        final File errorMessageFile = ikeyConfig.getErrorMessageFile();
        byte[] resultBytes = Files.readAllBytes(Paths.get(errorMessageFile.toURI()));
        String result = new String(resultBytes, "UTF-8");
        assertThat(result).contains(ERROR_MESSAGE + ": " + EXCEPTION_MESSAGE);
    }

    @Test
    public void should_create_format_from_string() {
        assertThat(IkeyConfig.OutputFormat.fromString("dot")).isEqualTo(IkeyConfig.OutputFormat.DOT);
        assertThat(IkeyConfig.OutputFormat.fromString("pdf")).isEqualTo(IkeyConfig.OutputFormat.PDF);
        assertThat(IkeyConfig.OutputFormat.fromString("html")).isEqualTo(IkeyConfig.OutputFormat.HTML);
        assertThat(IkeyConfig.OutputFormat.fromString("interactivehtml")).isEqualTo(IkeyConfig.OutputFormat.INTERACTIVE_HTML);
        assertThat(IkeyConfig.OutputFormat.fromString("sdd")).isEqualTo(IkeyConfig.OutputFormat.SDD);
        assertThat(IkeyConfig.OutputFormat.fromString("wiki")).isEqualTo(IkeyConfig.OutputFormat.WIKI);
        assertThat(IkeyConfig.OutputFormat.fromString("txt")).isEqualTo(IkeyConfig.OutputFormat.TXT);
    }

    @Test
    public void should_default_format_from_string_to_txt() {
        assertThat(IkeyConfig.OutputFormat.fromString("aaaa")).isEqualTo(IkeyConfig.OutputFormat.TXT);
    }

    @Test
    public void should_create_ScoreMethod_from_string() {
        assertThat(IkeyConfig.ScoreMethod.fromString("xPer")).isEqualTo(IkeyConfig.ScoreMethod.XPER);
        assertThat(IkeyConfig.ScoreMethod.fromString("jaccard")).isEqualTo(IkeyConfig.ScoreMethod.JACCARD);
        assertThat(IkeyConfig.ScoreMethod.fromString("sokalAndMichener")).isEqualTo(IkeyConfig.ScoreMethod.SOKAL_AND_MICHENER);
    }

    @Test
    public void should_default_ScoreMethod_from_string_to_xper() {
        assertThat(IkeyConfig.ScoreMethod.fromString("anything")).isEqualTo(IkeyConfig.ScoreMethod.XPER);
    }

    @Test
    public void should_create_weightType_from_string() {
        assertThat(IkeyConfig.WeightType.fromString("global")).isEqualTo(IkeyConfig.WeightType.GLOBAL);
        assertThat(IkeyConfig.WeightType.fromString("contextual")).isEqualTo(IkeyConfig.WeightType.CONTEXTUAL);
    }

    @Test
    public void should_default_weightType_from_string_to_global() {
        assertThat(IkeyConfig.WeightType.fromString("anything")).isEqualTo(IkeyConfig.WeightType.GLOBAL);
    }

    @Test
    public void should_create_keyRepresentation_from_string() {
        assertThat(IkeyConfig.KeyRepresentation.fromString("tree")).isEqualTo(IkeyConfig.KeyRepresentation.TREE);
        assertThat(IkeyConfig.KeyRepresentation.fromString("flat")).isEqualTo(IkeyConfig.KeyRepresentation.FLAT);
    }

    @Test
    public void should_default_keyRepresentation_from_string_to_flat() {
        assertThat(IkeyConfig.KeyRepresentation.fromString("anything")).isEqualTo(IkeyConfig.KeyRepresentation.FLAT);
    }
    @BeforeClass
    public static void setUp() {
        IkeyConfig.setBundleConfOverridable(ResourceBundle.getBundle("confTest"));
        IkeyConfig.setBundleConf(ResourceBundle.getBundle("confTest"));

        final String generatedFilesFolder = IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.prefix")
                + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder");
        if (!new File(generatedFilesFolder).exists()) {
            new File(generatedFilesFolder).mkdirs();
        }
    }

}