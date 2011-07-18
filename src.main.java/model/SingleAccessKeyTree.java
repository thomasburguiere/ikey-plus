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

	/**
	 * recursively traverses (depth-first) the SingleAccessKeyTree, and returns an HTML representation of this
	 * SingleAccessKeyTree in an unordered list (&lt;ul&gt;)
	 * 
	 * @param node
	 *            a SingleAccessKeyNode object, which will be traversed
	 * @param output
	 *            a StringBuffer that contains the final output
	 * @param tabulations
	 */
	public void recursiveToHTMLString(SingleAccessKeyNode node, StringBuffer output, String tabulations) {
		String characterName = null;
		String state = null;
		if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {
			characterName = node.getCharacter().getName().replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;");
			characterName = "<span class='character'>" + characterName + "</span>";

			if (node.getCharacterState() instanceof QuantitativeMeasure)
				state = ((QuantitativeMeasure) node.getCharacterState()).toStringInterval();
			else
				state = ((State) node.getCharacterState()).getName();
			state = "<span class='state'>" + state.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;")
					+ "</span>";

			output.append(tabulations + "\t<li>" + characterName);

			if (node.hasChild()) {
				output.append(" | " + state + " (taxa=" + node.getRemainingTaxa().size() + ")");
			} else {
				output.append(" | " + state + " <span class='taxa'>-> taxa=");
				for (Taxon taxon : node.getRemainingTaxa()) {
					output.append(taxon.getName() + ",");
				}
				output.append("</span>");
			}
			if (node.hasChild())
				output.append("<ul>");
			output.append(System.getProperty("line.separator"));
			tabulations = tabulations + "\t";
		}
		for (SingleAccessKeyNode childNode : node.getChildren()) {
			recursiveToHTMLString(childNode, output, tabulations);
		}
		if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {

			if (node.hasChild())
				output.append(tabulations + "</li></ul>\n");
			else
				output.append(tabulations + "</li>\n");

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
	 * generates an HTML string that contains the identification key
	 * 
	 * @return String the HTML String
	 */
	public String toHtmlString() {
		String lineSep = System.getProperty("line.separator");
		StringBuffer slk = new StringBuffer();
		slk.append("<html>" + lineSep);
		slk.append("<head>" + lineSep);
		slk.append("<link rel='STYLESHEET' type='text/css' href='http://queen.snv.jussieu.fr/vibrant/resources/js/dhtmlxTree/dhtmlxTree/codebase/dhtmlxtree.css'/>"
				+ lineSep
				+ "<script type='text/javascript'  src='http://queen.snv.jussieu.fr/vibrant/resources/js/dhtmlxTree/dhtmlxTree/codebase/dhtmlxcommon.js'></script>"
				+ lineSep
				+ "<script type='text/javascript'  src='http://queen.snv.jussieu.fr/vibrant/resources/js/dhtmlxTree/dhtmlxTree/codebase/dhtmlxtree.js'></script>"
				+ lineSep
				+ "<script type='text/javascript'  src='http://queen.snv.jussieu.fr/vibrant/resources/js/dhtmlxTree/dhtmlxTree/codebase/ext/dhtmlxtree_start.js'></script>"
				+ lineSep);

		slk.append("<style type='text/css'>");
		slk.append("body{");
		slk.append("   color:#333;");
		slk.append("}");

		slk.append(".character{");
		slk.append("   color:#333;");
		slk.append("}");

		slk.append(".state{");
		slk.append("   color:#fe8a22;");
		slk.append("}");

		slk.append(".taxa{");
		slk.append("   color:#67bb1b;");
		slk.append("}");
		slk.append("</style>");

		slk.append("</head>" + lineSep);

		slk.append("<body>" + lineSep);
		slk.append("<div class='dhtmlxTree' id='treeboxbox_tree' setImagePath='http://queen.snv.jussieu.fr/vibrant/resources/js/dhtmlxTree/dhtmlxTree/codebase/imgs/' >"
				+ lineSep);

		slk.append("<ul>" + lineSep);

		StringBuffer output = new StringBuffer();

		recursiveToHTMLString(root, output, "");

		slk.append(output.toString());

		slk.append("</ul>" + lineSep);
		slk.append("</div>" + lineSep);

		slk.append("</body>");
		slk.append("</html>");

		return slk.toString();

	}

	/**
	 * get a HTML file containing the key
	 * 
	 * @return File, the html file
	 */
	public File toHtmlFile() {
		return null;
	}

	/**
	 * get a PDF file containing the key
	 * 
	 * @return File, the pdf file
	 */
	public File toPdfFile() {
		return null;
	}

	/**
	 * get a SDD file containing the key
	 * 
	 * @return File, the sdd file
	 */
	public File toSddFile() {
		return null;
	}

	/**
	 * get a wiki file containing the key
	 * 
	 * @return File, the Wikitext file
	 */
	public File toWikiFile() {
		return null;
	}
}
