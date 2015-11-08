package fr.lis.ikeyplus.utils;

import com.google.common.collect.Sets;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.*;
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
        assertThat(result).contains(ERROR_MESSAGE+": " +EXCEPTION_MESSAGE );
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