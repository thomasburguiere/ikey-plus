package fr.lis.ikeyplus.model;

import fr.lis.ikeyplus.utils.IkeyConfig;

/**
 * This class represents a single access key tree
 *
 * @author Florian Causse
 * @created 18-04-2011
 */
public class SingleAccessKeyTree {

    private SingleAccessKeyNode root = null;
    private DataSet dataSet = null;
    // the config object (containing options)
    private IkeyConfig config = null;

    /**
     * constructor
     */
    public SingleAccessKeyTree(IkeyConfig config) {
        this(null, config);
    }

    /**
     * constructor with root node param
     *
     * @param SingleAccessKeyNode , the root node
     */
    public SingleAccessKeyTree(SingleAccessKeyNode root, IkeyConfig utils) {
        super();
        this.root = root;
        this.config = utils;
    }

    /**
     * get the root node
     *
     * @return SingleAccessKeyNode, the root node
     */
    public SingleAccessKeyNode getRoot() {
        return root;
    }

    /**
     * set the root node
     *
     * @param SingleAccessKeyNode , the root node
     */
    public void setRoot(SingleAccessKeyNode root) {
        this.root = root;
    }

    /**
     * get the label of the key
     *
     * @return String, the label
     */
    public String getLabel() {
        return dataSet.getLabel();
    }

    /**
     * get the dataset
     *
     * @return DataSet , the dataSet
     */
    public DataSet getDataSet() {
        return dataSet;
    }

    /**
     * set the dataset
     *
     * @param String , the label
     */
    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    /**
     * get the config object
     *
     * @return IkeyConfig, the config object
     */
    public IkeyConfig getConfig() {
        return config;
    }

    /**
     * set the config object
     *
     * @param Utils , the config object
     */
    public void setConfig(IkeyConfig config) {
        this.config = config;
    }

    /**
     * recursively method to be abbe to display String representation of this SingleAccessKeyTree
     *
     * @param node
     * @param output
     * @param tabulations
     * @param firstNumbering
     * @param secondNumbering
     */
    private void recursiveToString(SingleAccessKeyNode node, StringBuffer output, String tabulations,
                                   int firstNumbering, int secondNumbering) {

        if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {
            if (node.getCharacterState() instanceof QuantitativeMeasure) {
                output.append(tabulations
                        + firstNumbering
                        + "."
                        + secondNumbering
                        + ") "
                        + node.getCharacter().getName()
                        + " | "
                        + ((QuantitativeMeasure) node.getCharacterState())
                        .toStringInterval(((QuantitativeCharacter) node.getCharacter())
                                .getMeasurementUnit()));
            } else {
                output.append(tabulations + firstNumbering + "." + secondNumbering + ") "
                        + node.getCharacter().getName() + " | " + node.getStringStates());
            }
            output.append(nodeDescriptionAnalysis(node));
            if (node.getChildren().size() == 0) {
                output.append(" -> ");
                boolean firstLoop = true;
                for (Taxon taxon : node.getRemainingTaxa()) {
                    if (!firstLoop) {
                        output.append(", ");
                    }
                    output.append(taxon.getName());
                    firstLoop = false;
                }
            } else {
                output.append(" (items=" + node.getRemainingTaxa().size() + ")");
            }
            tabulations = tabulations + "\t";
        }
        firstNumbering++;
        secondNumbering = 0;
        for (SingleAccessKeyNode childNode : node.getChildren()) {
            secondNumbering++;
            recursiveToString(childNode, output, tabulations, firstNumbering, secondNumbering);
        }
    }

    /* (non-Javadoc)
     *
     * @see java.lang.Object#toString() */
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
        recursiveToString(root, output, System.getProperty("line.separator"), 0, 0);
        return output.toString();
    }

    /**
     * Analyses the node description and returns it if it is not an empty string, and if the verbose level
     * requires it to be displayed. Returns an empty String otherwise.
     *
     * @param node
     * @return
     */
    public String nodeDescriptionAnalysis(SingleAccessKeyNode node) {
        if (node.getNodeDescription() != null && node.getNodeDescription().trim().length() > 0
                && config.getVerbosity().contains(IkeyConfig.VerbosityLevel.WARNING)) {
            return " (" + node.getNodeDescription() + ")";
        }
        return "";
    }

    /**
     * this method calls the private {@link recursiveTaxonPathStatistics} method
     */
    public void gatherTaxonPathStatistics() {
        recursiveTaxonPathStatistics(root, 1);
    }

    /**
     * This traverses the SingleAccessKeyTree depth-first, and updates the path length statistics for each
     * taxon present in a terminal node
     *
     * @param node
     * @param treeDepth
     */
    private void recursiveTaxonPathStatistics(SingleAccessKeyNode node, int treeDepth) {
        if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {
            if (node.hasChild() == false) {
                if (node.getCharacter().isSupportsCategoricalData()
                        && !((State) node.getCharacterState()).getName().equals(
                        IkeyConfig.getBundleConfElement("message.notDescribed"))) {
                    for (Taxon t : node.getRemainingTaxa()) {
                        t.updatePathStatistics(new Float(treeDepth));
                    }
                }
            }
            treeDepth++;
        }
        for (SingleAccessKeyNode childNode : node.getChildren()) {
            recursiveTaxonPathStatistics(childNode, treeDepth);
        }
    }
}
