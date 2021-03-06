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

    public SingleAccessKeyTree(IkeyConfig config) {
        this(null, config);
    }

    private SingleAccessKeyTree(SingleAccessKeyNode root, IkeyConfig utils) {
        super();
        this.root = root;
        this.config = utils;
    }

    public SingleAccessKeyNode getRoot() {
        return root;
    }

    public void setRoot(SingleAccessKeyNode root) {
        this.root = root;
    }

    public String getLabel() {
        return dataSet.getLabel();
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    private void recursiveToString(SingleAccessKeyNode node, StringBuffer output, String tabulations,
                                   int firstNumbering, int secondNumbering) {

        if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {
            if (node.getCharacterState() instanceof QuantitativeMeasure) {
                output.append(tabulations)
                        .append(firstNumbering)
                        .append(".")
                        .append(secondNumbering)
                        .append(") ")
                        .append(node.getCharacter().getName())
                        .append(" | ")
                        .append((((QuantitativeMeasure) node.getCharacterState()))
                                .toStringInterval(((QuantitativeCharacter) node.getCharacter())
                                        .getMeasurementUnit()));
            } else {
                output.append(tabulations).append(firstNumbering).append(".")
                        .append(secondNumbering).append(") ").append(node.getCharacter()
                        .getName()).append(" | ").append(node.getStringStates());
            }
            output.append(nodeDescriptionAnalysis(node));
            output.append(node.getNodeLabel());
            tabulations = tabulations + "\t";
        }
        firstNumbering++;
        secondNumbering = 0;
        if (node != null) {
            for (SingleAccessKeyNode childNode : node.getChildren()) {
                secondNumbering++;
                recursiveToString(childNode, output, tabulations, firstNumbering, secondNumbering);
            }
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
     */
    public String nodeDescriptionAnalysis(SingleAccessKeyNode node) {
        if (node.getNodeDescription() != null && !node.getNodeDescription().trim().isEmpty()
                && config.getVerbosity().contains(IkeyConfig.VerbosityLevel.WARNING)) {
            return " (" + node.getNodeDescription() + ")";
        }
        return "";
    }

    public void gatherTaxonPathStatistics() {
        recursiveTaxonPathStatistics(root, 1);
    }

    /**
     * This traverses the SingleAccessKeyTree depth-first, and updates the path length statistics for each
     * taxon present in a terminal node
     */
    private void recursiveTaxonPathStatistics(SingleAccessKeyNode node, int treeDepth) {

        if (node != null) {
            if (node.getCharacter() != null && node.getCharacterState() != null) {
                if (!node.hasChild() && node.getCharacter().isSupportsCategoricalData()
                        && !((State) node.getCharacterState()).getName().equals(
                        IkeyConfig.getBundleConfElement("message.notDescribed"))) {
                    for (Taxon t : node.getRemainingTaxa()) {
                        t.updatePathStatistics((float) treeDepth);
                    }
                }
                treeDepth++;
            }
            for (SingleAccessKeyNode childNode : node.getChildren()) {
                recursiveTaxonPathStatistics(childNode, treeDepth);
            }
        }
    }
}
