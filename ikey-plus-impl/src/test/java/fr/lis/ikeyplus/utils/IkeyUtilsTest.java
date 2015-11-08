package fr.lis.ikeyplus.utils;


import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IkeyUtilsTest {

    private static final List<String> LIST_A_B_C = ImmutableList.of("A", "B", "C");
    private static final List<String> LIST_C_D_E = ImmutableList.of("C", "D", "E");

    @Test
    public void should_return_the_intersection() {
        assertThat(IkeyUtils.intersection(LIST_A_B_C, LIST_C_D_E)).containsOnly("C");
    }

    @Test
    public void should_return_the_exclusion() {
        assertThat(IkeyUtils.exclusion(LIST_A_B_C, LIST_C_D_E)).containsOnly("A", "B");
    }

    @Test
    public void should_return_the_union() {
        assertThat(IkeyUtils.union(LIST_A_B_C, LIST_C_D_E)).containsOnly("A", "B", "C", "D", "E");
    }
}