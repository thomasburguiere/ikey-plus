package fr.lis.ikeyplus.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.Set;

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
        final IkeyConfig config = IkeyConfig.builder().verbosity(Set.of(HEADER, OTHER, WARNING, STATISTICS)).verbosity(HEADER).build();
        assertThat(config.getVerbosity()).containsOnly(HEADER, OTHER, WARNING, STATISTICS);
    }

    @Test
    public void should_create_error_file() throws Exception {
        final IkeyConfig ikeyConfig = IkeyConfig.builder().build();
        ikeyConfig.setErrorMessage(ERROR_MESSAGE);
        ikeyConfig.createErrorFile();
        final File errorMessageFile = ikeyConfig.getErrorMessageFile();
        final byte[] resultBytes = Files.readAllBytes(Paths.get(errorMessageFile.toURI()));
        final String result = new String(resultBytes, StandardCharsets.UTF_8);
        assertThat(result).contains(ERROR_MESSAGE);
    }

    @Test
    public void should_create_error_file_with_exception() throws Exception {
        final IkeyConfig ikeyConfig = IkeyConfig.builder().build();
        ikeyConfig.setErrorMessage(ERROR_MESSAGE, new IllegalStateException(EXCEPTION_MESSAGE));
        ikeyConfig.createErrorFile();
        final File errorMessageFile = ikeyConfig.getErrorMessageFile();
        final byte[] resultBytes = Files.readAllBytes(Paths.get(errorMessageFile.toURI()));
        final String result = new String(resultBytes, StandardCharsets.UTF_8);
        assertThat(result).contains(ERROR_MESSAGE+": " +EXCEPTION_MESSAGE );
    }


    @BeforeAll
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