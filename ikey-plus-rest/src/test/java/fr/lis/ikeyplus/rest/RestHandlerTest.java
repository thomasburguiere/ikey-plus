package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.utils.IkeyConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static fr.lis.ikeyplus.utils.IkeyConfig.KeyRepresentation.TREE;
import static fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat.TXT;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.HEADER;
import static org.assertj.core.api.Assertions.assertThat;

class RestHandlerTest {

    @Nested
    @DisplayName("generateKey")
    class GenerateKeyTest {
        @Test
        void should_generate_key() throws Exception {
            final String expected;
            try (final var stream = Files.lines(Path.of("src/test/resources/expected_genetta_txt_key.txt"))) {
                expected = stream.collect(Collectors.joining("\n"));
            }

            final IkeyConfig config = IkeyConfig.builder()
                .enablePruning()
                .verbosity(HEADER)
                .representation(TREE)
                .enablePruning(false)
                .format(TXT)
                .build();
            final String result = RestHandler.generateKey(
                new FileInputStream("src/test/resources/genetta.sdd.xml"),
                config,
                "src/test/resources/genetta.sdd.xml"
            ).keyContent();

            assertThat(result).isEqualTo(expected);
        }
    }
}