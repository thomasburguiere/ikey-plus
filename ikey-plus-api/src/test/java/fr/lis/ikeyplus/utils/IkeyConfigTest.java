package fr.lis.ikeyplus.utils;

import com.google.common.collect.Sets;
import org.junit.Test;

import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.*;
import static org.assertj.core.api.Assertions.assertThat;

public class IkeyConfigTest {

    @Test
    public void should_parse_verbosity() {
        assertThat(fromString("hows")).containsOnly(HEADER, OTHER, WARNING, STATISTICS);
    }


    @Test
    public void should_have_non_duplicate_verbosity_levels() {
        IkeyConfig config = IkeyConfig.builder().verbosity(Sets.newHashSet(HEADER, OTHER, WARNING, STATISTICS)).verbosity(HEADER).build();
        assertThat(config.getVerbosity()).containsOnly(HEADER, OTHER, WARNING, STATISTICS);
    }

}