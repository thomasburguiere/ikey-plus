package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ResourceBundle;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.html.simpleparser.StyleSheet;
import com.itextpdf.text.pdf.PdfWriter;

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
				output.append("&nbsp;" + state + " (taxa=" + node.getRemainingTaxa().size() + ")");
			} else {
				output.append("&nbsp;" + state + " <span class='taxa'>-> taxa=");
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
		slk.append("<div style='margin-left:30px;margin-top:70px;'>" + lineSep);

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
	 * @throws COSVisitorException
	 * @throws DocumentException
	 */
	public File toPdfFile(ResourceBundle bundle) throws IOException, DocumentException {

		String path = bundle.getString("generatedKeyFiles.folder");
		File pdfFile = File.createTempFile("key_", ".pdf", new File(path));

		Document pdfDocument = new Document(PageSize.A3, 50, 50, 50, 50);
		PdfWriter.getInstance(pdfDocument, new FileOutputStream(pdfFile));

		pdfDocument.open();

		StyleSheet styles = new StyleSheet();
		styles.loadTagStyle("ul", "indent", "15");
		styles.loadTagStyle("li", "leading", "15");

		styles.loadStyle("character", "color", "#333");
		styles.loadStyle("state", "color", "#fe8a22");
		styles.loadStyle("taxa", "color", "#67bb1b");

		HTMLWorker htmlWorker = new HTMLWorker(pdfDocument);
		htmlWorker.setStyleSheet(styles);

		StringBuffer output = new StringBuffer();
		output.append("<html><head></head><boyd>");
		recursiveToHTMLString(root, output, "", true);
		output.append("</body></html>");

		htmlWorker.parse(new StringReader(output.toString()));

		pdfDocument.close();
		htmlWorker.close();

		return pdfFile;
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
