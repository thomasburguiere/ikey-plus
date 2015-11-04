package fr.lis.ikeyplus.utils;

import com.google.common.collect.Sets;
import com.google.common.io.Closeables;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class IkeyConfig {

    private String errorMessage = null;
    private File errorMessageFile = null;

    public static final WeightValue DEFAULT_WEIGHT = WeightValue.THREE;

    // properties file
    private static ResourceBundle bundleConf = ResourceBundle.getBundle("fr.lis.ikeyplus.conf");
    private static ResourceBundle bundleConfOverridable = ResourceBundle
            .getBundle("fr.lis.ikeyplus.confOverridable");

    // options
    private OutputFormat format = OutputFormat.TXT;
    private KeyRepresentation representation = KeyRepresentation.FLAT;
    private boolean fewStatesCharacterFirst = false;
    private boolean mergeCharacterStatesIfSameDiscrimination = false;
    private boolean pruningEnabled = false;
    private Set<VerbosityLevel> verbosity = Sets.newHashSet();
    private ScoreMethod scoreMethod = ScoreMethod.XPER;
    private WeightContext weightContext = WeightContext.NO_WEIGHT;
    private WeightType weightType = WeightType.GLOBAL;

    private IkeyConfig() {}

    IkeyConfig(OutputFormat format,
                       KeyRepresentation representation,
                       boolean fewStatesCharacterFirst,
                       boolean mergeCharacterStatesIfSameDiscrimination,
                       boolean pruningEnabled,
                       Set<VerbosityLevel> verbosity,
                       ScoreMethod scoreMethod,
                       WeightContext weightContext,
                       WeightType weightType) {

        this.format = format;
        this.representation = representation;
        this.fewStatesCharacterFirst = fewStatesCharacterFirst;
        this.mergeCharacterStatesIfSameDiscrimination = mergeCharacterStatesIfSameDiscrimination;
        this.pruningEnabled = pruningEnabled;
        this.verbosity = verbosity;
        this.scoreMethod = scoreMethod;
        this.weightContext = weightContext;
        this.weightType = weightType;
    }

    public static IkeyConfigBuilder builder() {
        return new IkeyConfigBuilder();
    }


    public enum VerbosityLevel {
        HEADER("h"), OTHER("o"), WARNING("w"), STATISTICS("s");

        private final String flag;

        public String toString() {
            return flag;
        }

        public static Set<VerbosityLevel> fromString(String s) {
            if (s != null) {
                Set<VerbosityLevel> verbosity = new HashSet<VerbosityLevel>();
                for (VerbosityLevel level : values()) {
                    if (s.toLowerCase().contains(level.toString())) {
                        verbosity.add(level);
                    }
                }
                return verbosity;
            }
            return null;
        }

        VerbosityLevel(String flag) {
            this.flag = flag;
        }
    }

    public enum ScoreMethod {
        XPER("xper"),
        JACCARD("jaccard"),
        SOKAL_AND_MICHENER("sokalAndMichener");

        private final String method;

        public String toString() {
            return method;
        }

        ScoreMethod(String method) {
            this.method = method;
        }

        public static ScoreMethod fromString(String text) {
            if (text != null) {
                for (ScoreMethod entry : values()) {
                    if (entry.toString().equals(text)) {
                        return entry;
                    }
                }
            }
            return null;
        }
    }

    public enum KeyRepresentation {
        TREE("tree"),
        FLAT("flat");

        private final String representation;

        KeyRepresentation(String representation) {
            this.representation = representation;
        }

        public String toString() {
            return representation;
        }

        public static KeyRepresentation fromString(String text) {
            if (text != null) {
                for (KeyRepresentation entry : values()) {
                    if (entry.toString().equals(text)) {
                        return entry;
                    }
                }
            }
            return null;
        }
    }

    public enum OutputFormat {
        TXT("txt"),
        HTML("html"),
        INTERACTIVE_HTML("interactivehtml"),
        PDF("pdf"),
        SDD("sdd"),
        WIKI("wiki"),
        DOT("dot"),
        ZIP("zip");

        private final String format;

        OutputFormat(String format) {
            this.format = format;
        }

        public String toString() {
            return format;
        }

        public static OutputFormat fromString(String text) {
            if (text != null) {
                for (OutputFormat entry : values()) {
                    if (entry.toString().equals(text)) {
                        return entry;
                    }
                }
            }
            return null;
        }
    }

    public enum WeightValue {
        ONE("Rating1of5", 1),
        TWO("Rating2of5", 2),
        THREE("Rating3of5", 3),
        FOUR("Rating4of5", 4),
        FIVE("Rating5of5", 5);

        private final String description;
        private final int intWeight;

        public String toString() {
            return description;
        }

        public int getIntWeight() {
            return intWeight;
        }

        WeightValue(String description, int intWeight) {
            this.description = description;
            this.intWeight = intWeight;
        }

        public static WeightValue fromString(String text) {
            if (text != null) {
                for (WeightValue entry : values()) {
                    if (entry.toString().equals(text)) {
                        return entry;
                    }
                }
            }
            return THREE;
        }
    }

    public enum WeightType {
        GLOBAL("global"),
        CONTEXTUAL("contextual");

        private final String type;

        WeightType(String type) {
            this.type = type;
        }

        public String toString() {
            return type;
        }
    }

    public enum WeightContext {
        OBSERVATION_CONVENIENCE("ObservationConvenience"),
        AVAILABILITY("Availability"),
        REPEATABILITY("Repeatability"),
        COST_EFFECTIVENESS("CostEffectiveness"),
        PHYLOGENETIC_WEIGHTING("PhylogeneticWeighting"),
        REQUIRED_EXPERTISE("RequiredExpertise"),
        NO_WEIGHT("");

        private final String contextType;

        WeightContext(String contextType) {
            this.contextType = contextType;
        }

        public String toString() {
            return contextType;
        }

        public static WeightContext fromString(String text) {
            if (text != null) {
                for (WeightContext entry : values()) {
                    if (entry.toString().equals(text)) {
                        return entry;
                    }
                }
            }
            return null;
        }
    }



    public static String getBundleConfElement(String key) {
        return IkeyConfig.bundleConf.getString(key);
    }

    public static String getBundleConfOverridableElement(String key) {
        return IkeyConfig.bundleConfOverridable.getString(key);
    }

    public static void setBundleConf(ResourceBundle bundleConf) {
        IkeyConfig.bundleConf = bundleConf;
    }

    public static void setBundleConfOverridable(ResourceBundle bundleConfOverridable) {
        IkeyConfig.bundleConfOverridable = bundleConfOverridable;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String msg) {
        if (getErrorMessage() == null) {
            errorMessage = msg;
            setErrorMessageFile(createErrorFile());
        }
    }

    public void setErrorMessage(String msg, Throwable t) {
        if (getErrorMessage() == null) {
            errorMessage = msg + ": " + t.getMessage();
            setErrorMessageFile(createErrorFile());
        }
    }

    public File getErrorMessageFile() {
        return this.errorMessageFile;
    }

    private void setErrorMessageFile(File errorMessageFile) {
        this.errorMessageFile = errorMessageFile;
    }

    public File createErrorFile() {
        String path = IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.prefix")
                + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder");

        String lineReturn = System.getProperty("line.separator");
        File erroFile = null;
        FileOutputStream fileOutputStream = null;
        try {
            erroFile = File.createTempFile(IkeyUtils.ERROR, "." + OutputFormat.TXT, new File(path));

            fileOutputStream = new FileOutputStream(erroFile);
            fileOutputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            BufferedWriter txtFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream,
                    "UTF-8"));

            txtFileWriter.append(this.errorMessage);
            txtFileWriter.append(lineReturn)
                    .append(lineReturn)
                    .append(IkeyConfig.getBundleConfElement("message.webmaster"))
                    .append(IkeyConfig.getBundleConfOverridableElement("email.webmaster"));
            txtFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Closeables.close(fileOutputStream, true);
            } catch (IOException ignored) {
            }
        }
        return erroFile;
    }

    public OutputFormat getFormat() {
        return format;
    }


    public KeyRepresentation getRepresentation() {
        return representation;
    }


    public boolean isFewStatesCharacterFirst() {
        return fewStatesCharacterFirst;
    }

    public boolean isMergeCharacterStatesIfSameDiscrimination() {
        return mergeCharacterStatesIfSameDiscrimination;
    }

    public boolean isPruningEnabled() {
        return pruningEnabled;
    }

    public Set<VerbosityLevel> getVerbosity() {
        return verbosity;
    }

    public ScoreMethod getScoreMethod() {
        return scoreMethod;
    }


    public WeightContext getWeightContext() {
        return weightContext;
    }

    public WeightType getWeightType() {
        return weightType;
    }

}
