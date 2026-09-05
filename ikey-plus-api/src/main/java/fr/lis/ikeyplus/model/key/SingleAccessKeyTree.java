package fr.lis.ikeyplus.model.key;

import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.model.Taxon;
import fr.lis.ikeyplus.model.character.QuantitativeCharacter;
import fr.lis.ikeyplus.model.description.QuantitativeMeasure;
import fr.lis.ikeyplus.model.description.State;
import fr.lis.ikeyplus.utils.IkeyConfig;

/**
 * This class represents a single access key tree
 *
 * @author Florian Causse
 * @created 18-04-2011
 */
public class SingleAccessKeyTree {

    private SingleAccessKeyNode root;
    private DataSet dataSet = null;
    // the config object (containing options)
    private IkeyConfig config;

    public SingleAccessKeyTree(final IkeyConfig config) {
        this(null, config);
    }

    public SingleAccessKeyTree(final SingleAccessKeyNode root, final IkeyConfig utils) {
        this.root = root;
        this.config = utils;
    }

    public SingleAccessKeyNode getRoot() {
        return root;
    }

    public void setRoot(final SingleAccessKeyNode root) {
        this.root = root;
    }

    public String getLabel() {
        return dataSet.getLabel();
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(final DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public IkeyConfig getConfig() {
        return config;
    }

    private void recursiveToString(
            final SingleAccessKeyNode node,
            final StringBuffer output,
            String tabulations,
            int firstNumbering,
            int secondNumbering
    ) {

        if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {
            if (node.getCharacterState() instanceof QuantitativeMeasure) {
                output.append(tabulations).append(firstNumbering).append(".").append(secondNumbering).append(") ").append(node.getCharacter().getName()).append(" | ").append(((QuantitativeMeasure) node.getCharacterState())
                        .toStringInterval(((QuantitativeCharacter) node.getCharacter())
                                .getMeasurementUnit()));
            } else {
                output.append(tabulations).append(firstNumbering).append(".").append(secondNumbering).append(") ").append(node.getCharacter().getName()).append(" | ").append(node.getStringStates());
            }
            output.append(nodeDescriptionAnalysis(node));
            if (node.getChildren().isEmpty()) {
                output.append(" -> ");
                boolean firstLoop = true;
                for (final Taxon taxon : node.getRemainingTaxa()) {
                    if (!firstLoop) {
                        output.append(", ");
                    }
                    output.append(taxon.getName());
                    firstLoop = false;
                }
            } else {
                output.append(" (items=").append(node.getRemainingTaxa().size()).append(")");
            }
            tabulations = tabulations + "\t";
        }
        firstNumbering++;
        secondNumbering = 0;
        if (node != null) {
            for (final SingleAccessKeyNode childNode : node.getChildren()) {
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
        final StringBuffer output = new StringBuffer();
        recursiveToString(root, output, System.lineSeparator(), 0, 0);
        return output.toString();
    }

    /**
     * Analyses the node description and returns it if it is not an empty string, and if the verbose level
     * requires it to be displayed. Returns an empty String otherwise.
     */
    public String nodeDescriptionAnalysis(final SingleAccessKeyNode node) {
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
    private void recursiveTaxonPathStatistics(final SingleAccessKeyNode node, int treeDepth) {

        if (node != null) {
            if (node.getCharacter() != null && node.getCharacterState() != null) {
                if (!node.hasChild() && node.getCharacter().isCategorical()
                        && !((State) node.getCharacterState()).getName().equals(
                        IkeyConfig.getBundleConfElement("message.notDescribed"))) {
                    for (final Taxon t : node.getRemainingTaxa()) {
                        t.updatePathStatistics((float) treeDepth);
                    }
                }
                treeDepth++;
            }
            for (final SingleAccessKeyNode childNode : node.getChildren()) {
                recursiveTaxonPathStatistics(childNode, treeDepth);
            }
        }
    }
}
