package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import utils.Utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.html.simpleparser.StyleSheet;
import com.itextpdf.text.pdf.PdfWriter;

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
						+ node.getCharacter().getName() + " | "
						+ ((QuantitativeMeasure) node.getCharacterState()).toStringInterval());
			} else {
				output.append(tabulations + firstNumbering + "." + secondNumbering + ") "
						+ node.getCharacter().getName() + " | "
						+ ((State) node.getCharacterState()).getName());
			}
			if (node.getChildren().size() == 0) {
				output.append(" -> taxa= ");
				boolean firstLoop = true;
				for (Taxon taxon : node.getRemainingTaxa()) {
					if (!firstLoop) {
						output.append(", ");
					}
					output.append(taxon.getName());
					firstLoop = false;
				}
			} else {
				output.append(" (taxa=" + node.getRemainingTaxa().size() + ")");
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

	/**
	 * Iteratively traverses (breadth-first) the SingleAccessKeyTree, and returns a plain-text representation
	 * of its content
	 * 
	 * @param rootNode
	 * @param output
	 * @param firstNumbering
	 * @param secondNumbering
	 */
	public void iterativeBreadthFirstToString(SingleAccessKeyNode rootNode, StringBuffer output,
			String lineSeparator, int firstNumbering, int secondNumbering) {
		Queue<SingleAccessKeyNode> queue = new LinkedList<SingleAccessKeyNode>();
		queue.add(rootNode);

		ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<SingleAccessKeyNode>();

		// node output
		if (rootNode != null && rootNode.getCharacter() != null && rootNode.getCharacterState() != null) {
			if (rootNode.getCharacterState() instanceof QuantitativeMeasure) {
				output.append(firstNumbering + "." + secondNumbering + ") "
						+ rootNode.getCharacter().getName() + " | "
						+ ((QuantitativeMeasure) rootNode.getCharacterState()).toStringInterval());
			} else {
				output.append(firstNumbering + "." + secondNumbering + ") "
						+ rootNode.getCharacter().getName() + " | "
						+ ((State) rootNode.getCharacterState()).getName());
			}
			if (rootNode.getChildren().size() == 0) {
				output.append(" -> taxa= ");
				boolean firstLoop = true;
				for (Taxon taxon : rootNode.getRemainingTaxa()) {
					if (!firstLoop) {
						output.append(", ");
					}
					output.append(taxon.getName());
					firstLoop = false;
				}
			} else {
				output.append(" (taxa=" + rootNode.getRemainingTaxa().size() + ")");
			}
			output.append(lineSeparator);
			visitedNodes.add(rootNode);
		}

		String characterNameBuffer = "";

		while (!queue.isEmpty()) {
			SingleAccessKeyNode node = queue.remove();
			SingleAccessKeyNode child = null;

			while (Utils.exclusion(node.getChildren(), visitedNodes).size() > 0
					&& (child = (SingleAccessKeyNode) Utils.exclusion(node.getChildren(), visitedNodes)
							.get(0)) != null) {

				visitedNodes.add(child);

				// numbering iteration TODO un peu crado qd meme
				if (!characterNameBuffer.equals(child.getCharacter().getName())) {
					characterNameBuffer = child.getCharacter().getName();
					firstNumbering++;
					secondNumbering = 0;
				}
				secondNumbering++;

				// node output
				if (child.getCharacterState() instanceof QuantitativeMeasure) {
					output.append(firstNumbering + "." + secondNumbering + ") "
							+ child.getCharacter().getName() + " | "
							+ ((QuantitativeMeasure) child.getCharacterState()).toStringInterval());
				} else {
					output.append(firstNumbering + "." + secondNumbering + ") "
							+ child.getCharacter().getName() + " | "
							+ ((State) child.getCharacterState()).getName());
				}
				if (child.getChildren().size() == 0) {
					output.append(" -> taxa= ");
					boolean firstLoop = true;
					for (Taxon taxon : child.getRemainingTaxa()) {
						if (!firstLoop) {
							output.append(", ");
						}
						output.append(taxon.getName());
						firstLoop = false;
					}
				} else {
					output.append(" (taxa=" + child.getRemainingTaxa().size() + ")");
				}
				output.append(lineSeparator);

				queue.add(child);
			}
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
				characterName = "<span class='character'>" + "<b>" + characterName + "</b>" + "</span>";
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
				output.append("&nbsp;" + state + " (taxa=" + node.getRemainingTaxa().size() + ")");
			} else {
				output.append("&nbsp;" + state + "<span class='taxa'> -> taxa= ");
				boolean firstLoop = true;
				for (Taxon taxon : node.getRemainingTaxa()) {
					if (!firstLoop) {
						output.append(", ");
					}
					output.append(taxon.getName());
					firstLoop = false;
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
				recursiveToHTMLString(childNode, output, tabulations, true);
			} else {
				recursiveToHTMLString(childNode, output, tabulations, false);
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

	/**
	 * recursively traverses (depth-first) the SingleAccessKeyTree, and returns an HTML representation of this
	 * SingleAccessKeyTree for PDF file creation
	 * 
	 * @param node
	 *            a SingleAccessKeyNode object, which will be traversed
	 * @param output
	 *            a StringBuffer that contains the final output
	 * @param tabulations
	 * @param displayCharacterName
	 *            a boolean to know if characterName need to be displayed
	 */
	public void recursiveToHTMLStringForPdf(SingleAccessKeyNode node, StringBuffer output,
			String tabulations, int firstNumbering, int secondNumbering) {
		String characterName = null;
		String state = null;
		if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {

			characterName = node.getCharacter().getName().replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;");
			characterName = firstNumbering + "." + secondNumbering + ") " + "<span class='character'>"
					+ "<b>" + characterName + "</b>" + "</span>";
			output.append(tabulations + "\t<li>&nbsp;<span class='line'>" + characterName);

			if (node.getCharacterState() instanceof QuantitativeMeasure)
				state = ((QuantitativeMeasure) node.getCharacterState()).toStringInterval();
			else
				state = ((State) node.getCharacterState()).getName();
			state = "<span class='state'>" + state.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;")
					+ "</span>";

			if (node.hasChild()) {
				output.append(" | " + state + " (taxa=" + node.getRemainingTaxa().size() + ")");
			} else {
				output.append(" | " + state + "<span class='taxa'> -> taxa= ");
				boolean firstLoop = true;
				for (Taxon taxon : node.getRemainingTaxa()) {
					if (!firstLoop) {
						output.append(", ");
					}
					output.append(taxon.getName());
					firstLoop = false;
				}
				output.append("</span>");
			}
			if (node.hasChild())
				output.append("</span><ul>");
			output.append(System.getProperty("line.separator"));
			tabulations = tabulations + "\t";
		}
		firstNumbering++;
		secondNumbering = 0;
		for (SingleAccessKeyNode childNode : node.getChildren()) {
			secondNumbering++;
			recursiveToHTMLStringForPdf(childNode, output, tabulations, firstNumbering, secondNumbering);
		}
		if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {

			if (node.hasChild())
				output.append(tabulations + "</span></li></ul>\n");
			else
				output.append(tabulations + "</span></li>\n");
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
	 * generates an HTML string that contains the identification key
	 * 
	 * @return String the HTML String
	 */
	public String toHtmlString(String header) {
		String lineSep = System.getProperty("line.separator");
		StringBuffer slk = new StringBuffer();
		slk.append("<html>" + lineSep);
		slk.append("<head>" + lineSep);
		slk.append("<script src='" + Utils.getBundleElement("resources.jqueryPath") + "'></script>" + lineSep
				+ "<script type='text/javascript' src='" + Utils.getBundleElement("resources.treeviewJsPath")
				+ "'></script>" + lineSep + "<link rel='stylesheet' href='"
				+ Utils.getBundleElement("resources.treeviewCssPath") + "' type='text/css' />" + lineSep);

		slk.append("<style type='text/css'>" + lineSep);
		slk.append("body{" + lineSep);
		slk.append("   color:#333;" + lineSep);
		slk.append("   font-family: Verdana, helvetica, arial, sans-serif;" + lineSep);
		slk.append("   font-size: 78%;" + lineSep);
		slk.append("   background: #fff;" + lineSep);
		slk.append("}" + lineSep + lineSep);

		slk.append(".character{" + lineSep);
		slk.append("   color:#333;" + lineSep);
		slk.append("}" + lineSep + lineSep);

		slk.append(".state{" + lineSep);
		slk.append("   color:#fe8a22;" + lineSep);
		slk.append("}" + lineSep + lineSep);

		slk.append(".taxa{" + lineSep);
		slk.append("   color:#67bb1b;" + lineSep);
		slk.append("}" + lineSep + lineSep);
		slk.append("</style>" + lineSep);

		slk.append("<script>" + lineSep);
		slk.append("  $(document).ready(function(){" + lineSep);
		slk.append("      $('#tree').treeview({" + lineSep);
		slk.append("		collapsed: true," + lineSep);
		slk.append("		unique: true," + lineSep);
		slk.append("		persist: 'location'" + lineSep);
		slk.append("	});" + lineSep);
		slk.append(" });" + lineSep);
		slk.append("  </script>" + lineSep);

		slk.append("</head>" + lineSep);

		slk.append("<body>" + lineSep);
		slk.append("<div style='margin-left:30px;margin-top:20px;'>" + lineSep);
		slk.append(header.replaceAll(System.getProperty("line.separator"), "<br/>"));
		slk.append("<ul id='tree'>" + lineSep);

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
	 * get a TXT file containing the key
	 * 
	 * @param String
	 *            , header information
	 * @return File the text File
	 * @throws IOException
	 */
	public File toTxtFile(String header) throws IOException {
		String path = Utils.getBundleElement("generatedKeyFiles.prefix")
				+ Utils.getBundleElement("generatedKeyFiles.folder");

		File txtFile = File.createTempFile("key_", "." + Utils.TXT, new File(path));
		BufferedWriter txtFileWriter = new BufferedWriter(new FileWriter(txtFile));
		txtFileWriter.append(header);
		txtFileWriter.append(toString());
		txtFileWriter.close();

		return txtFile;
	}

	public String toFlatString() {
		StringBuffer output = new StringBuffer();
		iterativeBreadthFirstToString(root, output, System.getProperty("line.separator"), 0, 0);
		return output.toString();
	}

	public File toFlatStringFile(String header) throws IOException {
		String path = Utils.getBundleElement("generatedKeyFiles.prefix")
				+ Utils.getBundleElement("generatedKeyFiles.folder");

		File txtFile = File.createTempFile("keyFlat_", "." + Utils.TXT, new File(path));
		BufferedWriter txtFileWriter = new BufferedWriter(new FileWriter(txtFile));
		txtFileWriter.append(header);
		txtFileWriter.append(toFlatString());
		txtFileWriter.close();

		return txtFile;
	}

	/**
	 * get a HTML file containing the key
	 * 
	 * @param String
	 *            , header information
	 * @return File, the html file
	 * @throws IOException
	 */
	public File toHtmlFile(String header) throws IOException {

		String path = Utils.getBundleElement("generatedKeyFiles.prefix")
				+ Utils.getBundleElement("generatedKeyFiles.folder");

		File htmlFile = File.createTempFile("key_", "." + Utils.HTML, new File(path));
		BufferedWriter htmlFileWriter = new BufferedWriter(new FileWriter(htmlFile));
		htmlFileWriter.append(toHtmlString(header));
		htmlFileWriter.close();

		return htmlFile;
	}

	/**
	 * get a PDF file containing the key
	 * 
	 * @param String
	 *            , header information
	 * @return File, the pdf file
	 * @throws IOException
	 * @throws COSVisitorException
	 * @throws DocumentException
	 */
	public File toPdfFile(String header) throws IOException, DocumentException {

		String path = Utils.getBundleElement("generatedKeyFiles.prefix")
				+ Utils.getBundleElement("generatedKeyFiles.folder");
		File pdfFile = File.createTempFile("key_", "." + Utils.PDF, new File(path));

		Document pdfDocument = new Document(PageSize.A3, 50, 50, 50, 50);
		PdfWriter.getInstance(pdfDocument, new FileOutputStream(pdfFile));

		pdfDocument.open();

		StyleSheet styles = new StyleSheet();
		styles.loadTagStyle("body", "color", "#333");
		styles.loadTagStyle("body", "background", "#fff");
		styles.loadTagStyle("body", "margin-left", "-10px");
		styles.loadTagStyle("ul", "indent", "15");
		styles.loadTagStyle("li", "leading", "15");
		styles.loadTagStyle("li", "color", "#fff");

		styles.loadStyle("character", "color", "#333");
		styles.loadStyle("state", "color", "#fe8a22");
		styles.loadStyle("taxa", "color", "#67bb1b");
		styles.loadStyle("line", "color", "#333");

		HTMLWorker htmlWorker = new HTMLWorker(pdfDocument);
		htmlWorker.setStyleSheet(styles);

		StringBuffer output = new StringBuffer();
		output.append("<html><head></head><body>");
		output.append(header.replaceAll(System.getProperty("line.separator"), "<br/>"));
		output.append("<ul>");
		recursiveToHTMLStringForPdf(root, output, "", 0, 0);
		output.append("</ul></body></html>");

		htmlWorker.parse(new StringReader(output.toString()));

		pdfDocument.close();
		htmlWorker.close();

		return pdfFile;
	}

	/**
	 * get a SDD file containing the key
	 * 
	 * @param String
	 *            , header information
	 * @return File, the sdd file
	 */
	public File toSddFile(String header) {
		return null;
	}

	/**
	 * get a wiki file containing the key
	 * 
	 * @param String
	 *            , header information
	 * @return File, the Wikitext file
	 */
	public File toWikiFile(String header) throws IOException {
		String path = Utils.getBundleElement("generatedKeyFiles.prefix")
				+ Utils.getBundleElement("generatedKeyFiles.folder");

		File wikiFile = File.createTempFile("key_", "." + Utils.WIKI, new File(path));
		BufferedWriter wikiFileWriter = new BufferedWriter(new FileWriter(wikiFile));

		wikiFileWriter.append("== Info ==");
		wikiFileWriter.newLine();
		wikiFileWriter.append(header.replaceAll(System.getProperty("line.separator"), "<br>"));
		wikiFileWriter.newLine();
		wikiFileWriter.append("== Identification Key==");
		wikiFileWriter.newLine();
		wikiFileWriter.append(" <nowiki>");

		wikiFileWriter.append(toString());

		wikiFileWriter.append("</nowiki>");
		wikiFileWriter.close();

		return wikiFile;
	}
}
