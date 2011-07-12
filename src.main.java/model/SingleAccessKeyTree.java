package model;

import java.io.File;

/**
 * This class represents a single access key tree
 * 
 * @author Florian Causse
 * @created 18-04-2011
 */
public class SingleAccessKeyTree {

	private SingleAccessKeyNode root = null;

	/**
	 * constructor
	 */
	public SingleAccessKeyTree() {
		this(null);
	}

	/**
	 * constructor with root node param
	 * 
	 * @param SingleAccessKeyNode
	 *            , the root node
	 */
	public SingleAccessKeyTree(SingleAccessKeyNode root) {
		super();
		this.root = root;
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
	 * @param SingleAccessKeyNode
	 *            , the root node
	 */
	public void setRoot(SingleAccessKeyNode root) {
		this.root = root;
	}

	/**
	 * @param parentNode
	 * @param output
	 */
	public void recursivetoString(SingleAccessKeyNode node, StringBuffer output, String tabulations) {

		if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {
			if (node.getCharacterState() instanceof QuantitativeMeasure) {
				output.append(tabulations + node.getCharacter().getName() + ": "
						+ ((QuantitativeMeasure) node.getCharacterState()).toStringInterval());
			} else {
				output.append(tabulations + node.getCharacter().getName() + ": "
						+ ((State) node.getCharacterState()).getName());
			}
			if (node.getChildren().size() == 0) {
				output.append(tabulations);
				output.append("taxa=");
				for (Taxon taxon : node.getRemainingTaxa()) {
					output.append(taxon.getName() + ",");
				}
			} else {
				output.append(tabulations + "taxa=" + node.getRemainingTaxa().size());
			}
			output.append(System.getProperty("line.separator"));
			tabulations = tabulations + "\t";
		}
		for (SingleAccessKeyNode childNode : node.getChildren()) {
			recursivetoString(childNode, output, tabulations);
		}
	}

	/* (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString() */
	public String toString() {
		StringBuffer output = new StringBuffer();
		recursivetoString(root, output, System.getProperty("line.separator"));
		return output.toString();
	}

	/**
	 * get a HTML file containing the key
	 * 
	 * @return File, the html file
	 */
	public File toHtml() {
		return null;
	}

	/**
	 * get a PDF file containing the key
	 * 
	 * @return File, the pdf file
	 */
	public File toPdf() {
		return null;
	}

	/**
	 * get a SDD file containing the key
	 * 
	 * @return File, the sdd file
	 */
	public File toSdd() {
		return null;
	}

	/**
	 * get a wiki file containing the key
	 * 
	 * @return File, the Wikitext file
	 */
	public File toWikiText() {
		return null;
	}
}
