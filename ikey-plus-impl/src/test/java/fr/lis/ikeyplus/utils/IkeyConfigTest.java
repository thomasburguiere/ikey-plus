package fr.lis.ikeyplus.utils;

import com.google.common.collect.Sets;
import fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation;
import fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat;
import fr.lis.ikeyplus.utils.IkeyConfig.ScoreMethod;
import fr.lis.ikeyplus.utils.IkeyConfig.WeightType;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import static fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation.*;
import static fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat.*;
import static fr.lis.ikeyplus.utils.IkeyConfig.ScoreMethod.*;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.*;
import static fr.lis.ikeyplus.utils.IkeyConfig.WeightType.*;
import static org.assertj.core.api.Assertions.assertThat;

public class IkeyConfigTest {

    public static final String ERROR_MESSAGE = "Caralho Fehler";
    public static final String EXCEPTION_MESSAGE = "kurwa";

    @Test
    public void should_parse_verbosity() {
        assertThat(IkeyConfig.VerbosityLevel.fromString("hows")).containsOnly(HEADER, OTHER, WARNING, STATISTICS);
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
        assertThat(OutputFormat.fromString("dot")).isEqualTo(DOT);
        assertThat(OutputFormat.fromString("pdf")).isEqualTo(PDF);
        assertThat(OutputFormat.fromString("html")).isEqualTo(HTML);
        assertThat(OutputFormat.fromString("interactivehtml")).isEqualTo(INTERACTIVE_HTML);
        assertThat(OutputFormat.fromString("sdd")).isEqualTo(SDD);
        assertThat(OutputFormat.fromString("wiki")).isEqualTo(WIKI);
        assertThat(OutputFormat.fromString("txt")).isEqualTo(TXT);
    }

    @Test
    public void should_default_format_from_string_to_txt() {
        assertThat(OutputFormat.fromString("aaaa")).isEqualTo(TXT);
    }

    @Test
    public void should_create_ScoreMethod_from_string() {
        assertThat(ScoreMethod.fromString("xPer")).isEqualTo(XPER);
        assertThat(ScoreMethod.fromString("jaccard")).isEqualTo(JACCARD);
        assertThat(ScoreMethod.fromString("sokalAndMichener")).isEqualTo(SOKAL_AND_MICHENER);
    }

    @Test
    public void should_default_ScoreMethod_from_string_to_xper() {
        assertThat(ScoreMethod.fromString("anything")).isEqualTo(XPER);
    }

    @Test
    public void should_create_weightType_from_string() {
        assertThat(WeightType.fromString("global")).isEqualTo(GLOBAL);
        assertThat(WeightType.fromString("contextual")).isEqualTo(CONTEXTUAL);
    }

    @Test
    public void should_default_weightType_from_string_to_global() {
        assertThat(WeightType.fromString("anything")).isEqualTo(GLOBAL);
    }

    @Test
    public void should_create_keyRepresentation_from_string() {
        assertThat(KeyRepresentation.fromString("tree")).isEqualTo(TREE);
        assertThat(KeyRepresentation.fromString("flat")).isEqualTo(FLAT);
    }

    @Test
    public void should_default_keyRepresentation_from_string_to_flat() {
        assertThat(KeyRepresentation.fromString("anything")).isEqualTo(FLAT);
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