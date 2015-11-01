package fr.lis.ikeyplus.utils;

import com.google.common.collect.Sets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * This class allow to manage all external functionalities : error massage, properties...
 *
 * @author Florian Causse
 * @created 07-04-2011
 */

/**
 * @author Utilisateur
 */
public class IkeyConfig {

    private String errorMessage = null;
    private File errorMessageFile = null;

    // static variable
    public static final String UNKNOWN_DATA = "unknownData";
    public static final List<String> ratings = new ArrayList<String>();
    public static final String YES = "yes";
    public static final String NO = "no";
    public static final int DEFAULT_WEIGHT = 3;

    // properties file
    public static ResourceBundle bundleConf = ResourceBundle.getBundle("fr.lis.ikeyplus.conf");
    public static ResourceBundle bundleConfOverridable = ResourceBundle
            .getBundle("fr.lis.ikeyplus.confOverridable");

    // buffer size
    public static int BUFFER = 2048;

    // file prefix
    public static final String KEY = "key_";
    public static final String ERROR = "error_";

    // specific file extension
    public static final String GV = "gv";

    // options
    private OutputFormat format = OutputFormat.TXT;
    private KeyRepresentation representation = KeyRepresentation.FLAT;
    private boolean fewStatesCharacterFirst = false;
    private boolean mergeCharacterStatesIfSameDiscrimination = false;
    private boolean pruning = false;
    private Set<VerbosityLevel> verbosity = Sets.newHashSet();
    private ScoreMethod scoreMethod = ScoreMethod.XPER;
    private WeightContext weightContext = WeightContext.NO_WEIGHT;
    private WeightType weightType = WeightType.GLOBAL;

    public enum VerbosityLevel {
        HEADER("h"), OTHER("o"), WARNING("w"), STATISTIC("s");

        private final String flag;

        public String toString(){
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
        SPECIES_ID_WIKI_STATEMENT("speciesidwikistatement"),
        SPECIES_ID_WIKI_QUESTION_ANSWER("speciesidwikiquestionanswer"),
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


    /**
     * Constructor
     */
    public IkeyConfig() {
        super();
        // initialize the list of rating values
        ratings.add("Rating1of5");
        ratings.add("Rating2of5");
        ratings.add("Rating3of5");
        ratings.add("Rating4of5");
        ratings.add("Rating5of5");
    }

    /**
     * Getter for configuration elements or messages
     *
     * @return String the element corresponding to the key
     */
    public static String getBundleConfElement(String key) {
        return IkeyConfig.bundleConf.getString(key);
    }

    /**
     * Getter for overridable configuration elements or messages
     *
     * @return String the element corresponding to the key
     */
    public static String getBundleConfOverridableElement(String key) {
        return IkeyConfig.bundleConfOverridable.getString(key);
    }

    /**
     * setter for configuration ResourceBundle
     *
     * @param bundle
     */
    public static void setBundleConf(ResourceBundle bundleConf) {
        IkeyConfig.bundleConf = bundleConf;
    }

    /**
     * setter for overridable configuration ResourceBundle
     *
     * @param bundle
     */
    public static void setBundleConfOverridable(ResourceBundle bundleConfOverridable) {
        IkeyConfig.bundleConfOverridable = bundleConfOverridable;
    }

    /**
     * getter for the error message
     *
     * @return String, the error file name
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * setter for the error message
     *
     * @param msg
     */
    public void setErrorMessage(String msg) {
        if (getErrorMessage() == null) {
            errorMessage = msg;
            setErrorMessageFile(createErrorFile());
        }
    }

    /**
     * setter for the error message with Throwable object
     *
     * @param msg , the readable message
     * @param t   , the exception
     */
    public void setErrorMessage(String msg, Throwable t) {
        if (getErrorMessage() == null) {
            errorMessage = msg + ": " + t.getMessage();
            setErrorMessageFile(createErrorFile());
        }
    }

    /**
     * getter for file message
     *
     * @return File, the error file
     */
    public File getErrorMessageFile() {
        return this.errorMessageFile;
    }

    /**
     * setter for file message
     *
     * @param errorMessageFile , the error file
     */
    public void setErrorMessageFile(File errorMessageFile) {
        this.errorMessageFile = errorMessageFile;
    }

    /**
     * method creating the error message
     *
     * @return File, the error file
     */
    public File createErrorFile() {
        String path = IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.prefix")
                + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder");

        String lineReturn = System.getProperty("line.separator");
        File erroFile = null;
        try {
            erroFile = File.createTempFile(IkeyConfig.ERROR, "." + OutputFormat.TXT, new File(path));

            FileOutputStream fileOutputStream = new FileOutputStream(erroFile);
            fileOutputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            BufferedWriter txtFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream,
                    "UTF-8"));

            txtFileWriter.append(this.errorMessage);
            txtFileWriter.append(lineReturn + lineReturn + IkeyConfig.getBundleConfElement("message.webmaster")
                    + IkeyConfig.getBundleConfOverridableElement("email.webmaster"));
            txtFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return erroFile;
    }

    /**
     * get the format of the generated key
     *
     * @return String, the format
     */
    public OutputFormat getFormat() {
        return format;
    }

    /**
     * set the format of the generated key
     *
     * @param format
     */
    public void setFormat(OutputFormat format) {
        this.format = format;
    }

    /**
     * get the representation of the generated key
     *
     * @return String, the representation
     */
    public KeyRepresentation getRepresentation() {
        return representation;
    }

    /**
     * set the representation of the generated key
     *
     * @param representation
     */
    public void setRepresentation(KeyRepresentation representation) {
        this.representation = representation;
    }

    /**
     * test if the fewStatesCharacterFirst option is active
     *
     * @return true if fewStatesCharacterFirst is selected
     */
    public boolean isFewStatesCharacterFirst() {
        return fewStatesCharacterFirst;
    }

    /**
     * set the fewStatesCharacterFirst option
     *
     * @param fewStatesCharacterFirst
     */
    public void setFewStatesCharacterFirst(boolean fewStatesCharacterFirst) {
        this.fewStatesCharacterFirst = fewStatesCharacterFirst;
    }

    /**
     * test if the mergeCharacterStatesIfSameDiscrimination option is active
     *
     * @return true if mergeCharacterStatesIfSameDiscrimination is selected
     */
    public boolean isMergeCharacterStatesIfSameDiscrimination() {
        return mergeCharacterStatesIfSameDiscrimination;
    }

    /**
     * set the mergeCharacterStatesIfSameDiscrimination option
     *
     * @param mergeCharacterStatesIfSameDiscrimination
     */
    public void setMergeCharacterStatesIfSameDiscrimination(boolean mergeCharacterStatesIfSameDiscrimination) {
        this.mergeCharacterStatesIfSameDiscrimination = mergeCharacterStatesIfSameDiscrimination;
    }

    /**
     * test if the pruning option is active
     *
     * @return true if pruning is selected
     */
    public boolean isPruning() {
        return pruning;
    }

    /**
     * set the pruning option
     *
     * @param pruning
     */
    public void setPruning(boolean pruning) {
        this.pruning = pruning;
    }

    /**
     * get the verbosity value
     *
     * @return String, the verbosity string
     */
    public Set<VerbosityLevel> getVerbosity() {
        return verbosity;
    }

    /**
     * set the verbosity value
     *
     * @param verbosity
     */
    public void setVerbosity(Set<VerbosityLevel> verbosity) {
        this.verbosity = verbosity;
    }

    /**
     * get the method method
     *
     * @return String, the method method selected
     */
    public ScoreMethod getScoreMethod() {
        return scoreMethod;
    }

    /**
     * set the method method
     *
     * @param scoreMethod
     */
    public void setScoreMethod(ScoreMethod scoreMethod) {
        this.scoreMethod = scoreMethod;
    }

    /**
     * get the weight context
     *
     * @return String, the weight context
     */
    public WeightContext getWeightContext() {
        return weightContext;
    }

    /**
     * set the weight context
     *
     * @param weightContext , the weight context
     */
    public void setWeightContext(WeightContext weightContext) {
        this.weightContext = weightContext;
    }

    /**
     * get the weight type
     *
     * @return String, the weight type
     */
    public WeightType getWeightType() {
        return weightType;
    }

    /**
     * set the weight type
     *
     * @param weightType
     */
    public void setWeightType(WeightType weightType) {
        this.weightType = weightType;
    }

}
