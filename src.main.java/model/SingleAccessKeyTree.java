package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ResourceBundle;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDPage;
import org.pdfbox.pdmodel.edit.PDPageContentStream;
import org.pdfbox.pdmodel.font.PDFont;
import org.pdfbox.pdmodel.font.PDType1Font;

import utils.Utils;

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
	 * recursively method to be abbe to display String representation of this SingleAccessKeyTree
	 * 
	 * @param parentNode
	 * @param output
	 */
	public void recursiveToString(SingleAccessKeyNode node, StringBuffer output, String tabulations,
			int firstNumbering, int secondNumbering) {

		if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {
			if (node.getCharacterState() instanceof QuantitativeMeasure) {
				output.append(tabulations + firstNumbering + "." + secondNumbering + ") "
						+ node.getCharacter().getName() + ": "
						+ ((QuantitativeMeasure) node.getCharacterState()).toStringInterval());
			} else {
				output.append(tabulations + firstNumbering + "." + secondNumbering + ") "
						+ node.getCharacter().getName() + ": " + ((State) node.getCharacterState()).getName());
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
		firstNumbering++;
		secondNumbering = 0;
		for (SingleAccessKeyNode childNode : node.getChildren()) {
			secondNumbering++;
			recursiveToString(childNode, output, tabulations, firstNumbering, secondNumbering);
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
	 * @param displayCharacterName
	 *            a boolean to know if characterName need to be displayed
	 */
	public void recursiveToHTMLString(SingleAccessKeyNode node, StringBuffer output, String tabulations,
			boolean displayCharacterName) {
		String characterName = null;
		String state = null;
		if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {

			if (displayCharacterName) {
				characterName = node.getCharacter().getName().replaceAll("\\<", "&lt;")
						.replaceAll("\\>", "&gt;");
				characterName = "<span class='character'>" + "<b>[" + characterName + "]</b>" + "</span>";
				output.append(tabulations + "\t<li>" + characterName + "</li>");
			}

			if (node.getCharacterState() instanceof QuantitativeMeasure)
				state = ((QuantitativeMeasure) node.getCharacterState()).toStringInterval();
			else
				state = ((State) node.getCharacterState()).getName();
			state = "<span class='state'>" + state.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;")
					+ "</span>";

			output.append("\n" + tabulations + "\t<li>");

			if (node.hasChild()) {
				output.append(state + " (taxa=" + node.getRemainingTaxa().size() + ")");
			} else {
				output.append(state + " <span class='taxa'>-> taxa=");
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
		boolean firstLoop = true;
		for (SingleAccessKeyNode childNode : node.getChildren()) {
			if (firstLoop) {
				recursiveToHTMLString(childNode, output, tabulations, firstLoop);
			} else {
				recursiveToHTMLString(childNode, output, tabulations, firstLoop);
			}
			firstLoop = false;
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
		recursiveToString(root, output, System.getProperty("line.separator"), 0, 0);
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
		slk.append("<link rel='STYLESHEET' type='text/css' href='" + Utils.getBundleElement("resources.path")
				+ "js/dhtmlxTree/dhtmlxTree/codebase/dhtmlxtree.css'/>" + lineSep
				+ "<script type='text/javascript'  src='" + Utils.getBundleElement("resources.path")
				+ "js/dhtmlxTree/dhtmlxTree/codebase/dhtmlxcommon.js'></script>" + lineSep
				+ "<script type='text/javascript'  src='" + Utils.getBundleElement("resources.path")
				+ "js/dhtmlxTree/dhtmlxTree/codebase/dhtmlxtree.js'></script>" + lineSep
				+ "<script type='text/javascript'  src='" + Utils.getBundleElement("resources.path")
				+ "js/dhtmlxTree/dhtmlxTree/codebase/ext/dhtmlxtree_start.js'></script>" + lineSep);

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
		slk.append("<div class='dhtmlxTree' id='treeboxbox_tree' setImagePath='"
				+ Utils.getBundleElement("resources.path")
				+ "/js/dhtmlxTree/dhtmlxTree/codebase/imgs/csh_dhx_skyblue/' >" + lineSep);

		slk.append("<ul>" + lineSep);

		StringBuffer output = new StringBuffer();

		recursiveToHTMLString(root, output, "", true);

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
	 * @param bundle
	 *            A ResourceBundle used to retrieve the path of the folder in which the file must be generated
	 * @return File, the html file
	 * @throws IOException
	 */
	public File toHtmlFile(ResourceBundle bundle) throws IOException {

		String path = bundle.getString("generatedKeyFiles.folder");

		File htmlFile = File.createTempFile("key_", ".html", new File(path));
		BufferedWriter htmlFileWriter = new BufferedWriter(new FileWriter(htmlFile));
		htmlFileWriter.append(toHtmlString());
		htmlFileWriter.close();

		return htmlFile;
	}

	/**
	 * get a PDF file containing the key
	 * 
	 * @return File, the pdf file
	 * @throws IOException
	 */
	public File toPdfFile() throws IOException {

		PDDocument document = new PDDocument();
		PDFont baseFont = PDType1Font.COURIER;
		PDPage page = new PDPage();
		document.addPage(page);
		PDPageContentStream contentStream = new PDPageContentStream(document, page);

		contentStream.beginText();
		contentStream.setFont(baseFont, 12);
		contentStream.moveTextPositionByAmount(50, 50);
		contentStream.drawString("Hello World");
		contentStream.endText();

		contentStream.close();

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
	 * @param bundle
	 *            A ResourceBundle used to retrieve the path of the folder in which the file must be generated
	 * @return File, the Wikitext file
	 */
	public File toWikiFile(ResourceBundle bundle) throws IOException {
		String path = bundle.getString("generatedKeyFiles.folder");

		File wikiFile = File.createTempFile("key_", ".wiki", new File(path));
		BufferedWriter wikiFileWriter = new BufferedWriter(new FileWriter(wikiFile));

		wikiFileWriter.append("== Info ==");
		wikiFileWriter.newLine();
		wikiFileWriter.append("== Identification Key==");
		wikiFileWriter.newLine();
		wikiFileWriter.append(" <nowiki>");

		wikiFileWriter.append(toString());

		wikiFileWriter.append("</nowiki>");
		return wikiFile;
	}

	/**
	 * @param bundle
	 *            A ResourceBundle used to retrieve the path of the folder in which the file must be generated
	 * @return File the wikitext File
	 * @throws IOException
	 */
	public File toTextFile(ResourceBundle bundle) throws IOException {
		String path = bundle.getString("generatedKeyFiles.folder");

		File txtFile = File.createTempFile("key_", ".txt", new File(path));
		BufferedWriter txtFileWriter = new BufferedWriter(new FileWriter(txtFile));
		txtFileWriter.append(toString());
		txtFileWriter.close();

		return txtFile;
	}
}
