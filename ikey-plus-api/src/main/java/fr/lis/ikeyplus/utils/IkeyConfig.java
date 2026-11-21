package fr.lis.ikeyplus.utils;

import org.immutables.value.Value;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toSet;

@Value.Immutable
public interface IkeyConfig {
    WeightValue DEFAULT_WEIGHT = WeightValue.THREE;
    @Value.Default
    default OutputFormat format() { return OutputFormat.TXT; }

    @Value.Default
    default KeyRepresentation representation() { return KeyRepresentation.TREE; }
    @Value.Default
    default boolean fewStatesCharacterFirst() { return false; }
    @Value.Default
    default boolean mergeCharacterStatesIfSameDiscrimination() { return false; }
    @Value.Default
    default boolean pruningEnabled() { return false; }
    @Value.Default
    default Set<VerbosityLevel> verbosity() { return new HashSet<>(); }
    @Value.Default
    default ScoreMethod scoreMethod() { return ScoreMethod.XPER; }
    @Value.Default
    default WeightContext weightContext() {return  WeightContext.NO_WEIGHT; }
    @Value.Default
    default WeightType weightType() {return  WeightType.GLOBAL; }
    enum VerbosityLevel {
        HEADER("h"), OTHER("o"), WARNING("w"), STATISTICS("s");

        private final String flag;

        @Override
        public String toString() {
            return flag;
        }

        public static Set<VerbosityLevel> fromString(@Nullable final String s) {
            if (s != null) {
                return Arrays.stream(values())
                        .filter(level -> s.toLowerCase().contains(level.toString()))
                        .collect(toSet());
            }
            return Set.of();
        }

        VerbosityLevel(final String flag) {
            this.flag = flag;
        }

    }
    record Verbosity(Set<VerbosityLevel> levels) {

        public static Verbosity getDefault() {
            return new Verbosity(Set.of());
        }
    }
    enum ScoreMethod {
        XPER("xper"),
        JACCARD("jaccard"),
        SOKAL_AND_MICHENER("sokalAndMichener");

        private final String method;

        @Override
        public String toString() {
            return method;
        }

        ScoreMethod(final String method) {
            this.method = method;
        }

        public static Optional<ScoreMethod> fromString(@Nullable final String text) {
            if (text != null) {
                for (final ScoreMethod entry : values()) {
                    if (entry.toString().equalsIgnoreCase(text)) {
                        return of(entry);
                    }
                }
            }
            return empty();
        }

    }
    enum KeyRepresentation {
        TREE("tree"),
        FLAT("flat");

        private final String representation;

        KeyRepresentation(final String representation) {
            this.representation = representation;
        }

        @Override
        public String toString() {
            return representation;
        }

        public static Optional<KeyRepresentation> fromString(@Nullable final String text) {
            if (text != null) {
                for (final KeyRepresentation entry : values()) {
                    if (entry.toString().equalsIgnoreCase(text)) {
                        return of(entry);
                    }
                }
            }
            return empty();
        }

    }
    enum OutputFormat {
        TXT("txt"),
        HTML("html"),
        INTERACTIVE_HTML("interactivehtml"),
        PDF("pdf"),
        SDD("sdd"),
        WIKI("wiki"),
        DOT("dot"),
        ZIP("zip");

        private final String format;

        OutputFormat(final String format) {
            this.format = format;
        }

        @Override
        public String toString() {
            return format;
        }

        public static Optional<OutputFormat> fromString(@Nullable final String text) {
            if (text != null) {
                for (final OutputFormat entry : values()) {
                    if (entry.toString().equalsIgnoreCase(text)) {
                        return of(entry);
                    }
                }
            }
            return empty();
        }

    }
    enum WeightValue {
        ONE("Rating1of5", 1),
        TWO("Rating2of5", 2),
        THREE("Rating3of5", 3),
        FOUR("Rating4of5", 4),
        FIVE("Rating5of5", 5);

        private final String description;

        private final int intWeight;
        @Override
        public String toString() {
            return description;
        }

        public int getIntWeight() {
            return intWeight;
        }

        WeightValue(final String description, final int intWeight) {
            this.description = description;
            this.intWeight = intWeight;
        }

        public static WeightValue fromString(@Nullable final String text) {
            if (text != null) {
                for (final WeightValue entry : values()) {
                    if (entry.toString().equalsIgnoreCase(text)) {
                        return entry;
                    }
                }
            }
            return THREE;
        }

    }
    enum WeightType {
        GLOBAL("global"),
        CONTEXTUAL("contextual");

        private final String type;

        WeightType(final String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

        public static Optional<WeightType> fromString(@Nullable final String s) {
            if (s != null) {
                for (final WeightType entry : values()) {
                    if (entry.toString().equalsIgnoreCase(s)) {
                        return of(entry);
                    }
                }
            }
            return empty();
        }

    }
    enum WeightContext {
        OBSERVATION_CONVENIENCE("ObservationConvenience"),
        AVAILABILITY("Availability"),
        REPEATABILITY("Repeatability"),
        COST_EFFECTIVENESS("CostEffectiveness"),
        PHYLOGENETIC_WEIGHTING("PhylogeneticWeighting"),
        REQUIRED_EXPERTISE("RequiredExpertise"),
        NO_WEIGHT("");

        private final String contextType;

        WeightContext(final String contextType) {
            this.contextType = contextType;
        }

        @Override
        public String toString() {
            return contextType;
        }

        public static Optional<WeightContext> fromString(@Nullable final String text) {
            if (text != null) {
                for (final WeightContext entry : values()) {
                    if (entry.toString().equalsIgnoreCase(text)) {
                        return of(entry);
                    }
                }
            }
            return empty();
        }

    }

    ResourceBundle bundleConf = ResourceBundle.getBundle("fr.lis.ikeyplus.conf");
    ResourceBundle overridableBundleConf = ResourceBundle.getBundle("fr.lis.ikeyplus.confOverridable");

    static String getBundleConfElement(final String key) {
        return IkeyConfig.bundleConf.getString(key);
    }

    static String getBundleConfOverridableElement(final String key) {
        return IkeyConfig.overridableBundleConf.getString(key);
    }
}
