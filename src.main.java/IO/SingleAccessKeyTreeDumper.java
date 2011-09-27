package IO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.html.simpleparser.StyleSheet;
import com.itextpdf.text.pdf.PdfWriter;

import utils.Utils;

import model.QuantitativeMeasure;
import model.SingleAccessKeyNode;
import model.SingleAccessKeyTree;
import model.State;
import model.Taxon;

public abstract class SingleAccessKeyTreeDumper {

	// SDD DUMP
	public static File dumpSddFile(String header, SingleAccessKeyTree tree2dump) throws IOException {
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File sddFile = File.createTempFile(Utils.KEY, "." + Utils.SDD, new File(path));

		FileOutputStream fileOutputStream = new FileOutputStream(sddFile);
		fileOutputStream.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
		BufferedWriter sddFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
		sddFileWriter.append(generateSddString(tree2dump));
		sddFileWriter.close();

		return sddFile;
	}

	/**
	 * generates a SDD-formated String representation of the SingleAccessKeyTree passed in paramater
	 * 
	 * @param tree2dump
	 *            the SingleAccessKeyTree which is to be dumped in a SDD-formatted String
	 * @return
	 */
	private static String generateSddString(SingleAccessKeyTree tree2dump) {
		StringBuffer output = new StringBuffer();
		String lineSeparator = System.getProperty("line.separator");

		output.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator);
		output.append("<Datasets xmlns=\"http://rs.tdwg.org/UBIF/2006/\" ");
		output.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:schemaLocation=\"http://rs.tdwg.org/UBIF/2006/ "
				+ "http://rs.tdwg.org/UBIF/2006/Schema/1.1/SDD.xsd\">" + lineSeparator);
		Date generationDate = new Date();
		output.append("<TechnicalMetadata created=\"" + generationDate + "\">" + lineSeparator);
		output.append("<Generator name=\"Identification Key generation WebService\" ");
		output.append("notes=\"This software is developed and distributed by LIS -"
				+ " Laboratoire Informatique et Systématique (LIS) -"
				+ " Université Pierre et Marie Curie - Paris VI - within the ViBRANT project\" version=\"1.1\"/>"
				+ lineSeparator);
		output.append("</TechnicalMetadata>" + lineSeparator);
		multipleTraversalToSddString(tree2dump.getRoot(), output, lineSeparator);
		output.append("</Datasets>");
		return output.toString();
	}

	/**
	 * This methods outputs the {@link #SingleAccesKeyTree} as a flat wiki-formatted String that complies with
	 * the wiki format used on <a href="http://species-id.net">species-id.net</a>, with mediawiki hyperlinks.
	 * In order to do this, the <tt>SingleAccesKeyTree</tt> is traversed 3 times. The first traversal is a
	 * breadth-first traversal, in order to generate an HashMap ( <tt>nodeBreadthFirstIterationMap</tt>) that
	 * associates each node with an arbitrary Integer. The second traversal is a depth-first traversal, in
	 * order to associate (in another HashMap : <tt>nodeChildParentNumberingMap</tt>), for each node, the node
	 * number and the number of its parent node. Finally, the last traversal is another breadh-first traversal
	 * that generates the flat key String
	 * 
	 * @param rootNode
	 * @param output
	 * @param lineSeparator
	 */
	private static void multipleTraversalToSddString(SingleAccessKeyNode rootNode, StringBuffer output,
			String lineSeparator) {

		// // FIRST TRAVERSAL, breadth-first ////
		HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap = new HashMap<SingleAccessKeyNode, Integer>();
		int counter = 1;
		iterativeBreadthFirst(rootNode, nodeBreadthFirstIterationMap, counter);
		// // END FIRST TRAVERSAL, breadth-first ////

		// // SECOND TRAVERSAL, depth-first ////
		HashMap<Integer, Integer> nodeChildParentNumberingMap = new HashMap<Integer, Integer>();
		List<Integer> rootNodeChildrenIntegerList = new ArrayList<Integer>();
		recursiveDepthFirst(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap,
				rootNodeChildrenIntegerList);
		// // END SECOND TRAVERSAL, depth-first ////

		// // THIRD TRAVERSAL, breadth-first ////
		Queue<SingleAccessKeyNode> queue = new LinkedList<SingleAccessKeyNode>();
		ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<SingleAccessKeyNode>();
		counter = 1;
		int currentParentNumber = -1;
		queue.add(rootNode);

		int taxonCounter = 1;
		// root node treatment

		counter++;
		// end root node treatment
		visitedNodes.add(rootNode);

		output.append("<IdentificationKey>" + lineSeparator);

		while (!queue.isEmpty()) {
			SingleAccessKeyNode node = queue.remove();
			SingleAccessKeyNode child = null;

			while (Utils.exclusion(node.getChildren(), visitedNodes).size() > 0
					&& (child = (SingleAccessKeyNode) Utils.exclusion(node.getChildren(), visitedNodes)
							.get(0)) != null) {
				visitedNodes.add(child);

				// / child node treatment

				if (nodeChildParentNumberingMap.get(new Integer(counter)) != currentParentNumber) {
					currentParentNumber = nodeChildParentNumberingMap.get(new Integer(counter));
				}

				if (counter == 2) {// first child node of the root node
					output.append("<Question>" + lineSeparator);
					output.append("<Label>" + escapeHTMLSpecialCharacters(child.getCharacter().getName())
							+ "</Label>" + lineSeparator);
					output.append("</Question>" + lineSeparator);
					output.append("<Leads>" + lineSeparator);
				}
				// other child nodes of the root node
				if (rootNodeChildrenIntegerList.contains(new Integer(counter))) {
					if (child.hasChild()) {
						output.append("<Lead id=\"lead" + (counter - 1) + "\">" + lineSeparator);
						output.append("<Statement>" + lineSeparator);
						output.append("<Label>"
								+ child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")
										.replace("&", "&amp;") + "</Label>" + lineSeparator);
						output.append("</Statement>" + lineSeparator);
						output.append("<Question>" + lineSeparator);
						output.append("<Label>"
								+ child.getChildren().get(0).getCharacter().getName().replace(">", "&gt;")
										.replace("<", "&lt;").replace("&", "&amp;") + "</Label>"
								+ lineSeparator);
						output.append("</Question>" + lineSeparator);
						output.append("</Lead>" + lineSeparator);
					} else {

						output.append("<Result>" + lineSeparator);
						output.append("<Statement>" + lineSeparator);
						output.append("<Label>"
								+ child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")
										.replace("&", "&amp;") + "</Label>" + lineSeparator);
						output.append("</Statement>" + lineSeparator);
						output.append("<TaxonNames>" + lineSeparator);
						for (Taxon t : child.getRemainingTaxa()) {
							output.append("<TaxonName id=\"taxon" + taxonCounter + "\">" + lineSeparator);
							taxonCounter++;
							output.append("<Label>");
							output.append(t.getName().replace(">", "&gt;").replace("<", "&lt;")
									.replace("&", "&amp;"));
							output.append("</Label>" + lineSeparator);
							output.append("</TaxonName>" + lineSeparator);
						}
						output.append("</TaxonNames>" + lineSeparator);
						output.append("</Result>" + lineSeparator);
					}
				} else {
					if (child.hasChild()) {
						output.append("<Lead id=\"lead" + (counter - 1) + "\">" + lineSeparator);
						output.append("<Parent ref=\"lead" + (currentParentNumber - 1) + "\"/>"
								+ lineSeparator);
						output.append("<Statement>" + lineSeparator);
						output.append("<Label>"
								+ child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")
										.replace("&", "&amp;") + "</Label>" + lineSeparator);
						output.append("</Statement>" + lineSeparator);
						output.append("<Question>" + lineSeparator);
						output.append("<Label>"
								+ child.getChildren().get(0).getCharacter().getName().replace(">", "&gt;")
										.replace("<", "&lt;").replace("&", "&amp;") + "</Label>"
								+ lineSeparator);
						output.append("</Question>" + lineSeparator);
						output.append("</Lead>" + lineSeparator);

					} else {
						output.append("<Result>" + lineSeparator);
						output.append("<Parent ref=\"lead" + (currentParentNumber - 1) + "\"/>"
								+ lineSeparator);
						output.append("<Statement>" + lineSeparator);
						output.append("<Label>"
								+ child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")
										.replace("&", "&amp;") + "</Label>" + lineSeparator);
						output.append("</Statement>" + lineSeparator);
						output.append("<TaxonNames>" + lineSeparator);
						for (Taxon t : child.getRemainingTaxa()) {
							output.append("<TaxonName id=\"taxon" + taxonCounter + "\">" + lineSeparator);
							taxonCounter++;
							output.append("<Label>");
							output.append(t.getName().replace(">", "&gt;").replace("<", "&lt;")
									.replace("&", "&amp;"));
							output.append("</Label>" + lineSeparator);
							output.append("</TaxonName>" + lineSeparator);
						}
						output.append("</TaxonNames>" + lineSeparator);
						output.append("</Result>" + lineSeparator);
					}
				}

				queue.add(child);
				counter++;
				// / end child node treatment

			}
		}
		output.append("</Leads>" + lineSeparator);
		output.append("</IdentificationKey>" + lineSeparator);
		// // end third traversal, breadth-first ////

	}

	// END SDD DUMP

	// TXT DUMP, TREE
	/**
	 * generate a TXT file containing the key
	 * 
	 * @param String
	 *            , header information
	 * @return File the text File
	 * @throws IOException
	 */
	public static File dumpTxtFile(String header, SingleAccessKeyTree tree2dump) throws IOException {
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File txtFile = File.createTempFile(Utils.KEY, "." + Utils.TXT, new File(path));

		FileOutputStream fileOutputStream = new FileOutputStream(txtFile);
		fileOutputStream.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
		BufferedWriter txtFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));

		txtFileWriter.append(header);
		txtFileWriter.append(generateTreeString(tree2dump));
		txtFileWriter.close();

		return txtFile;
	}

	private static String generateTreeString(SingleAccessKeyTree tree2dump) {
		StringBuffer output = new StringBuffer();
		recursiveToString(tree2dump.getRoot(), output, System.getProperty("line.separator"), 0, 0, tree2dump);
		return output.toString();
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
	private static void recursiveToString(SingleAccessKeyNode node, StringBuffer output, String tabulations,
			int firstNumbering, int secondNumbering, SingleAccessKeyTree tree2dump) {

		if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {
			if (node.getCharacterState() instanceof QuantitativeMeasure) {
				output.append(tabulations + firstNumbering + "." + secondNumbering + ") "
						+ node.getCharacter().getName() + " | "
						+ ((QuantitativeMeasure) node.getCharacterState()).toStringInterval());
			} else {
				output.append(tabulations + firstNumbering + "." + secondNumbering + ") "
						+ node.getCharacter().getName() + " | " + node.getStringStates());
			}
			output.append(tree2dump.nodeDescriptionAnalysis(node));
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
				output.append(" (taxa=" + node.getRemainingTaxa().size() + ")");
			}
			tabulations = tabulations + "\t";
		}
		firstNumbering++;
		secondNumbering = 0;
		for (SingleAccessKeyNode childNode : node.getChildren()) {
			secondNumbering++;
			recursiveToString(childNode, output, tabulations, firstNumbering, secondNumbering, tree2dump);
		}
	}

	// END TXT DUMP, TREE

	// TXT DUMP, FLAT
	/**
	 * generates a txt file containing the key, in a flat representation
	 * 
	 * @param header
	 *            a String that contains the header
	 * @return a txt File
	 * @throws IOException
	 */
	public static File dumpFlatTxtFile(String header, SingleAccessKeyTree tree2dump) throws IOException {
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File txtFile = File.createTempFile(Utils.KEY, "." + Utils.TXT, new File(path));

		FileOutputStream fileOutputStream = new FileOutputStream(txtFile);
		fileOutputStream.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
		BufferedWriter txtFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
		txtFileWriter.append(header);
		txtFileWriter.append(generateFlatString(tree2dump));
		txtFileWriter.close();

		return txtFile;
	}

	/**
	 * generates a flat representation of a key, in a String object, by calling the
	 * {@link #multipleTraversalToString} helper method
	 * 
	 * @return
	 */
	private static String generateFlatString(SingleAccessKeyTree tree2dump) {
		StringBuffer output = new StringBuffer();
		multipleTraversalToString(tree2dump.getRoot(), output, System.getProperty("line.separator"),
				tree2dump);
		return output.toString();
	}

	/**
	 * This methods outputs the {@link #SingleAccesKeyTree} as a flat key. In order to do this, the
	 * <tt>SingleAccesKeyTree</tt> is traversed 3 times. The first traversal is a breadth-first traversal, in
	 * order to generate an HashMap (<tt>nodeBreadthFirstIterationMap</tt>) that associates each node with an
	 * arbitrary Integer. The second traversal is a depth-first traversal, in order to associate (in another
	 * HashMap : <tt>nodeChildParentNumberingMap</tt>), for each node, the node number and the number of its
	 * parent node. Finally, the last traversal is another breadh-first traversal that generates the flat key
	 * String
	 * 
	 * @param rootNode
	 * @param output
	 * @param lineSeparator
	 */
	private static void multipleTraversalToString(SingleAccessKeyNode rootNode, StringBuffer output,
			String lineSeparator, SingleAccessKeyTree tree2dump) {

		// // first traversal, breadth-first ////
		HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap = new HashMap<SingleAccessKeyNode, Integer>();
		int counter = 1;
		iterativeBreadthFirst(rootNode, nodeBreadthFirstIterationMap, counter);
		// // end first traversal, breadth-first ////

		// // second traversal, depth-first ////
		HashMap<Integer, Integer> nodeChildParentNumberingMap = new HashMap<Integer, Integer>();
		recursiveDepthFirst(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
		// // end second traversal, depth-first ////

		// // third traversal, breadth-first ////
		Queue<SingleAccessKeyNode> queue = new LinkedList<SingleAccessKeyNode>();
		ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<SingleAccessKeyNode>();

		queue.clear();
		visitedNodes.clear();

		counter = 1;
		int currentParentNumber = -1;
		queue.add(rootNode);

		// root node treatment

		counter++;
		// end root node treatment
		visitedNodes.add(rootNode);

		while (!queue.isEmpty()) {
			SingleAccessKeyNode node = queue.remove();
			SingleAccessKeyNode child = null;

			while (Utils.exclusion(node.getChildren(), visitedNodes).size() > 0
					&& (child = (SingleAccessKeyNode) Utils.exclusion(node.getChildren(), visitedNodes)
							.get(0)) != null
			// && child.getCharacter() != null && child.getCharacterState() != null
			) {
				visitedNodes.add(child);

				// / child node treatment

				// displaying the parent node number and the child node character name only once
				if (nodeChildParentNumberingMap.get(new Integer(counter)) != currentParentNumber) {
					currentParentNumber = nodeChildParentNumberingMap.get(new Integer(counter));
					output.append(lineSeparator);
					if (currentParentNumber < 10)
						output.append("   " + currentParentNumber);
					else if (currentParentNumber < 100)
						output.append("  " + currentParentNumber);
					else if (currentParentNumber < 1000)
						output.append(" " + currentParentNumber);
					else
						output.append(currentParentNumber);
					output.append("  " + child.getCharacter().getName() + " = ");
				} else {
					output.append("    ");
					String blankCharacterName = "";
					for (int i = 0; i < child.getCharacter().getName().length(); i++)
						blankCharacterName += " ";
					output.append("  " + blankCharacterName + " = ");
				}

				// displaying the child node character state
				if (child.getCharacterState() instanceof QuantitativeMeasure) {
					output.append(((QuantitativeMeasure) child.getCharacterState()).toStringInterval());
				} else {
					output.append(child.getStringStates());
				}
				output.append(tree2dump.nodeDescriptionAnalysis(child));

				// displaying the child node number if it has children nodes, displaying the taxa otherwise
				if (child.getChildren().size() == 0) {
					output.append(" -> ");
					boolean firstLoop = true;
					for (Taxon taxon : child.getRemainingTaxa()) {
						if (!firstLoop) {
							output.append(", ");
						}
						output.append(taxon.getName());
						firstLoop = false;
					}
				} else {
					output.append(" -> " + counter);
				}

				output.append(lineSeparator);

				queue.add(child);

				counter++;
				// / end child node treatment

			}
		}

		// // end third traversal, breadth-first ////

	}

	// END TXT DUMP, FLAT

	// HTML DUMP, TREE
	/**
	 * get a HTML file containing the key
	 * 
	 * @param String
	 *            , header information
	 * @return File, the html file
	 * @throws IOException
	 */
	public static File dumpHtmlFile(String header, SingleAccessKeyTree tree2dump) throws IOException {

		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File htmlFile = File.createTempFile(Utils.KEY, "." + Utils.HTML, new File(path));

		FileOutputStream fileOutputStream = new FileOutputStream(htmlFile);
		fileOutputStream.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
		BufferedWriter htmlFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
		htmlFileWriter.append(generateHtmlString(header, tree2dump));
		htmlFileWriter.close();

		return htmlFile;
	}

	/**
	 * generates an HTML string that contains the identification key
	 * 
	 * @return String the HTML String
	 */
	private static String generateHtmlString(String header, SingleAccessKeyTree tree2dump) {
		String lineSep = System.getProperty("line.separator");
		StringBuffer slk = new StringBuffer();
		slk.append("<html>" + lineSep);
		slk.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />" + lineSep);
		slk.append("<head>" + lineSep);
		slk.append("<script src='" + Utils.getBundleConfElement("resources.jqueryPath") + "'></script>"
				+ lineSep + "<script type='text/javascript' src='"
				+ Utils.getBundleConfElement("resources.treeviewJsPath") + "'></script>" + lineSep
				+ "<link rel='stylesheet' href='" + Utils.getBundleConfElement("resources.treeviewCssPath")
				+ "' type='text/css' />" + lineSep);

		slk.append("<style type='text/css'>" + lineSep);

		slk.append("body{" + lineSep);
		slk.append("   color:#333;" + lineSep);
		slk.append("   font-family: Verdana, helvetica, arial, sans-serif;" + lineSep);
		slk.append("   font-size: 78%;" + lineSep);
		slk.append("   background: #fff;" + lineSep);
		slk.append("}" + lineSep + lineSep);

		slk.append("#treecontrol  a{");
		slk.append("	color:#333;");
		slk.append("	font-size: 85%;");
		slk.append("}");

		slk.append("#treecontrol  a:hover{");
		slk.append("	color:#777;");
		slk.append("}");

		slk.append(".character{" + lineSep);
		slk.append("   color:#333;" + lineSep);
		slk.append("}" + lineSep + lineSep);

		slk.append(".state{" + lineSep);
		slk.append("   color:#fe8a22;" + lineSep);
		slk.append("}" + lineSep + lineSep);

		slk.append(".taxa{" + lineSep);
		slk.append("   color:#67bb1b;" + lineSep);
		slk.append("   font-style: italic;" + lineSep);
		slk.append("}" + lineSep + lineSep);

		slk.append("a{" + lineSep);
		slk.append("   color:#67bb1b;" + lineSep);
		slk.append("   font-style: italic;" + lineSep);
		slk.append("}" + lineSep + lineSep);

		slk.append("#screenshot{" + lineSep);
		slk.append("	position:absolute;" + lineSep);
		slk.append("	border:1px solid #ccc;" + lineSep);
		slk.append("	background:#333;" + lineSep);
		slk.append("	padding:5px;" + lineSep);
		slk.append("	display:none;" + lineSep);
		slk.append("	color:#fff;" + lineSep);
		slk.append("}" + lineSep);

		slk.append("</style>" + lineSep);

		slk.append("<script>" + lineSep);
		slk.append("this.screenshotPreview = function(){" + lineSep);
		slk.append("	xOffset = -10;" + lineSep);
		slk.append("	yOffset = -50;" + lineSep);
		slk.append("	$(\"a.screenshot\").hover(function(e){" + lineSep);
		slk.append("		this.t = this.title;" + lineSep);
		slk.append("		this.title = \"\";" + lineSep);
		slk.append("		var c = (this.t != \"\") ? \"<br/>\" + this.t : \"\";" + lineSep);
		slk.append("		$(\"body\").append(\"<p id='screenshot'><img src='\"+ this.rel +\"' alt='url preview' width='200px'/>\"+ c +\"</p>\");"
				+ lineSep);
		slk.append("		$(\"#screenshot\")" + lineSep);
		slk.append("			.css(\"top\",(e.pageY - xOffset) + \"px\")" + lineSep);
		slk.append("			.css(\"left\",(e.pageX + yOffset) + \"px\")" + lineSep);
		slk.append("			.fadeIn(\"fast\");" + lineSep);
		slk.append("    }," + lineSep);
		slk.append("	function(){" + lineSep);
		slk.append("		this.title = this.t;" + lineSep);
		slk.append("		$(\"#screenshot\").remove();" + lineSep);
		slk.append("    });" + lineSep);
		slk.append("	$(\"a.screenshot\").mousemove(function(e){" + lineSep);
		slk.append("		$(\"#screenshot\")" + lineSep);
		slk.append("			.css(\"top\",(e.pageY - xOffset) + \"px\")" + lineSep);
		slk.append("			.css(\"left\",(e.pageX + yOffset) + \"px\");" + lineSep);
		slk.append("	});" + lineSep);
		slk.append("};" + lineSep);

		slk.append("  $(document).ready(function(){" + lineSep);
		slk.append("    $('#tree').treeview({" + lineSep);
		slk.append("		collapsed: true," + lineSep);
		slk.append("		unique: false," + lineSep);
		slk.append("		control: \"#treecontrol\"," + lineSep);
		slk.append("		persist: 'location'" + lineSep);
		slk.append("	});" + lineSep);
		slk.append("	screenshotPreview();" + lineSep);
		slk.append(" });" + lineSep);
		slk.append("</script>" + lineSep);

		slk.append("</head>" + lineSep);

		slk.append("<body>" + lineSep);
		slk.append("<div style='margin-left:30px;margin-top:20px;'>" + lineSep);
		slk.append(header.replaceAll(System.getProperty("line.separator"), "<br/>"));

		slk.append("<div id=\"treecontrol\"><a title=\"Collapse the entire tree below\" href=\"#\"> Collapse All</a> | <a title=\"Expand the entire tree below\" href=\"#\"> Expand All</a> | <a title=\"Toggle the tree below, opening closed branches, closing open branches\" href=\"#\">Toggle All</a></div>"
				+ lineSep);

		slk.append("<ul id='tree'>" + lineSep);

		StringBuffer output = new StringBuffer();

		recursiveToHTMLString(tree2dump.getRoot(), output, "", true, 0, 0, tree2dump);

		slk.append(output.toString());

		slk.append("</ul>" + lineSep);
		slk.append("</div>" + lineSep);

		slk.append("</body>");
		slk.append("</html>");

		return slk.toString();

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
	 * @param firstNumbering
	 * @param secondNumbering
	 */
	private static void recursiveToHTMLString(SingleAccessKeyNode node, StringBuffer output,
			String tabulations, boolean displayCharacterName, int firstNumbering, int secondNumbering,
			SingleAccessKeyTree tree2dump) {
		String characterName = null;
		String state = null;
		if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {

			if (displayCharacterName) {
				characterName = node.getCharacter().getName().replaceAll("\\<", "&lt;")
						.replaceAll("\\>", "&gt;");
				characterName = "<span class='character'>" + firstNumbering + ") " + "<b>" + characterName
						+ "</b>" + "</span>";
				output.append(tabulations + "\t<li>" + characterName + "</li>");
			}

			if (node.getCharacterState() instanceof QuantitativeMeasure)
				state = ((QuantitativeMeasure) node.getCharacterState()).toStringInterval();
			else
				state = node.getStringStates();
			state = "<span class='state'>" + firstNumbering + "." + secondNumbering + ") "
					+ state.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;") + "</span>";
			state += "<span class=\"warning\">" + tree2dump.nodeDescriptionAnalysis(node) + "</span>";

			output.append("\n" + tabulations + "\t<li>");

			if (node.hasChild()) {
				output.append("&nbsp;" + state + " (taxa=" + node.getRemainingTaxa().size() + ")");
			} else {
				output.append("&nbsp;" + state + "<span class='taxa'> -> ");
				boolean firstLoop = true;
				for (Taxon taxon : node.getRemainingTaxa()) {
					if (!firstLoop) {
						output.append(", ");
					}
					// create image previous using the first image in the list
					if (taxon.getFirstImage(tree2dump.getDataSet()) != null) {
						output.append("<a href=\"" + taxon.getFirstImage(tree2dump.getDataSet())
								+ "\" class=\"screenshot\" rel=\""
								+ taxon.getFirstImage(tree2dump.getDataSet()) + "\" target=\"_blank\">"
								+ taxon.getName() + "</a>");
					} else {
						output.append(taxon.getName());
					}
					firstLoop = false;
				}
				output.append("</span>");
			}
			if (node.hasChild())
				output.append("<ul>");
			output.append(System.getProperty("line.separator"));
			tabulations = tabulations + "\t";
		}
		firstNumbering++;
		secondNumbering = 0;
		boolean firstLoop = true;
		for (SingleAccessKeyNode childNode : node.getChildren()) {
			secondNumbering++;
			if (firstLoop) {
				recursiveToHTMLString(childNode, output, tabulations, true, firstNumbering, secondNumbering,
						tree2dump);
			} else {
				recursiveToHTMLString(childNode, output, tabulations, false, firstNumbering, secondNumbering,
						tree2dump);
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

	// END HTML DUMP, TREE

	// HTML DUMP, FLAT
	/**
	 * get a HTML file containing the key, in a flat representation
	 * 
	 * @param String
	 *            , header information
	 * @return File, the html file
	 * @throws IOException
	 */
	public static File dumpFlatHtmlFile(String header, SingleAccessKeyTree tree2dump) throws IOException {

		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File htmlFile = File.createTempFile(Utils.KEY, "." + Utils.HTML, new File(path));

		FileOutputStream fileOutputStream = new FileOutputStream(htmlFile);
		fileOutputStream.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
		BufferedWriter htmlFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
		htmlFileWriter.append(generateFlatHtmlString(header, tree2dump));
		htmlFileWriter.close();

		return htmlFile;
	}

	/**
	 * 
	 * generates a flat, HTML-formatted, String representation of a key, in a String object, by calling the
	 * {@link #multipleTraversalToHTMLString} helper method
	 * 
	 * @param header
	 * @return
	 */
	private static String generateFlatHtmlString(String header, SingleAccessKeyTree tree2dump) {
		StringBuffer output = new StringBuffer();
		String lineSep = System.getProperty("line.separator");
		StringBuffer slk = new StringBuffer();
		slk.append("<html>" + lineSep);
		slk.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />" + lineSep);
		slk.append("<head>" + lineSep);
		slk.append("<script src='" + Utils.getBundleConfElement("resources.jqueryPath") + "'></script>"
				+ lineSep + "<script type='text/javascript' src='"
				+ Utils.getBundleConfElement("resources.treeviewJsPath") + "'></script>" + lineSep
				+ "<link rel='stylesheet' href='" + Utils.getBundleConfElement("resources.treeviewCssPath")
				+ "' type='text/css' />" + lineSep);

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
		slk.append("   font-style: italic;" + lineSep);
		slk.append("}" + lineSep + lineSep);

		slk.append(".statesAndTaxa{" + lineSep);
		slk.append("   margin-left: 100px;" + lineSep);
		slk.append("}" + lineSep + lineSep);

		slk.append("a{" + lineSep);
		slk.append("   color:#67bb1b;" + lineSep);
		slk.append("   font-style: italic;" + lineSep);
		slk.append("}" + lineSep + lineSep);

		slk.append("#screenshot{" + lineSep);
		slk.append("	position:absolute;" + lineSep);
		slk.append("	border:1px solid #ccc;" + lineSep);
		slk.append("	background:#333;" + lineSep);
		slk.append("	padding:5px;" + lineSep);
		slk.append("	display:none;" + lineSep);
		slk.append("	color:#fff;" + lineSep);
		slk.append("}" + lineSep + lineSep);
		slk.append("</style>" + lineSep);

		slk.append("<script>" + lineSep);
		slk.append("this.screenshotPreview = function(){" + lineSep);
		slk.append("	xOffset = -10;" + lineSep);
		slk.append("	yOffset = -50;" + lineSep);
		slk.append("	$(\"a.screenshot\").hover(function(e){" + lineSep);
		slk.append("		this.t = this.title;" + lineSep);
		slk.append("		this.title = \"\";" + lineSep);
		slk.append("		var c = (this.t != \"\") ? \"<br/>\" + this.t : \"\";" + lineSep);
		slk.append("		$(\"body\").append(\"<p id='screenshot'><img src='\"+ this.rel +\"' alt='url preview' width='200px'/>\"+ c +\"</p>\");"
				+ lineSep);
		slk.append("		$(\"#screenshot\")" + lineSep);
		slk.append("			.css(\"top\",(e.pageY - xOffset) + \"px\")" + lineSep);
		slk.append("			.css(\"left\",(e.pageX + yOffset) + \"px\")" + lineSep);
		slk.append("			.fadeIn(\"fast\");" + lineSep);
		slk.append("    }," + lineSep);
		slk.append("	function(){" + lineSep);
		slk.append("		this.title = this.t;" + lineSep);
		slk.append("		$(\"#screenshot\").remove();" + lineSep);
		slk.append("    });" + lineSep);
		slk.append("	$(\"a.screenshot\").mousemove(function(e){" + lineSep);
		slk.append("		$(\"#screenshot\")" + lineSep);
		slk.append("			.css(\"top\",(e.pageY - xOffset) + \"px\")" + lineSep);
		slk.append("			.css(\"left\",(e.pageX + yOffset) + \"px\");" + lineSep);
		slk.append("	});" + lineSep);
		slk.append("};" + lineSep);

		slk.append("  $(document).ready(function(){" + lineSep);
		slk.append("	screenshotPreview();" + lineSep);
		slk.append(" });" + lineSep);
		slk.append("</script>" + lineSep);

		slk.append("</head>" + lineSep);

		slk.append("<body>" + lineSep);
		slk.append("<div style='margin-left:30px;margin-top:20px;'>" + lineSep);
		slk.append(header.replaceAll(System.getProperty("line.separator"), "<br/>"));

		multipleTraversalToHTMLString(tree2dump.getRoot(), output, System.getProperty("line.separator"),
				true, tree2dump);

		slk.append(output.toString());

		slk.append("</div>" + lineSep);

		slk.append("</body>");
		slk.append("</html>");

		return slk.toString();
	}

	/**
	 * This methods outputs the {@link #SingleAccesKeyTree} as a flat HTML-formatted String, with mediawiki
	 * hyperlinks. In order to do this, the <tt>SingleAccesKeyTree</tt> is traversed 3 times. The first
	 * traversal is a breadth-first traversal, in order to generate an HashMap (
	 * <tt>nodeBreadthFirstIterationMap</tt>) that associates each node with an arbitrary Integer. The second
	 * traversal is a depth-first traversal, in order to associate (in another HashMap :
	 * <tt>nodeChildParentNumberingMap</tt>), for each node, the node number and the number of its parent
	 * node. Finally, the last traversal is another breadh-first traversal that generates the flat key String
	 * 
	 * @param rootNode
	 * @param output
	 * @param lineSeparator
	 */
	private static void multipleTraversalToHTMLString(SingleAccessKeyNode rootNode, StringBuffer output,
			String lineSeparator, boolean activeLink, SingleAccessKeyTree tree2dump) {

		String marging = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

		// // first traversal, breadth-first ////
		HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap = new HashMap<SingleAccessKeyNode, Integer>();

		int counter = 1;
		iterativeBreadthFirst(rootNode, nodeBreadthFirstIterationMap, counter);

		// // end first traversal, breadth-first ////

		// // second traversal, depth-first ////
		HashMap<Integer, Integer> nodeChildParentNumberingMap = new HashMap<Integer, Integer>();
		recursiveDepthFirst(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
		// // end second traversal, depth-first ////

		// // third traversal, breadth-first ////
		Queue<SingleAccessKeyNode> queue = new LinkedList<SingleAccessKeyNode>();
		ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<SingleAccessKeyNode>();

		counter = 1;
		int currentParentNumber = -1;
		queue.add(rootNode);

		// root node treatment

		counter++;
		// end root node treatment
		visitedNodes.add(rootNode);

		while (!queue.isEmpty()) {
			SingleAccessKeyNode node = queue.remove();
			SingleAccessKeyNode child = null;

			while (Utils.exclusion(node.getChildren(), visitedNodes).size() > 0
					&& (child = (SingleAccessKeyNode) Utils.exclusion(node.getChildren(), visitedNodes)
							.get(0)) != null
			// && child.getCharacter() != null && child.getCharacterState() != null
			) {
				visitedNodes.add(child);

				// / child node treatment

				// displaying the parent node number and the child node character name only once
				if (nodeChildParentNumberingMap.get(new Integer(counter)) != currentParentNumber) {
					currentParentNumber = nodeChildParentNumberingMap.get(new Integer(counter));
					output.append("<br/>" + lineSeparator);
					if (currentParentNumber < 10)
						output.append("   ");
					else if (currentParentNumber < 100)
						output.append("  ");
					else if (currentParentNumber < 1000)
						output.append(" ");

					if (activeLink) {
						output.append("<a name=\"anchor" + currentParentNumber + "\"></a>" + "<strong>"
								+ currentParentNumber + "</strong>");
					} else {
						output.append("<strong>" + currentParentNumber + "</strong>");
					}

					output.append("  <span class=\"character\">"
							+ child.getCharacter().getName().replace(">", "&gt;").replace("<", "&lt;")
							+ ": </span><br/>");

				} else {
					output.append("    ");
					String blankCharacterName = "";
					for (int i = 0; i < child.getCharacter().getName().length(); i++)
						blankCharacterName += " ";
					output.append("  " + blankCharacterName);
				}
				output.append("<span class=\"statesAndTaxa\">");

				// displaying the child node character state
				if (child.getCharacterState() instanceof QuantitativeMeasure) {
					output.append("<span class=\"state\">" + marging
							+ ((QuantitativeMeasure) child.getCharacterState()).toStringInterval()
							+ "</span>");
				} else {
					output.append("<span class=\"state\">" + marging
							+ child.getStringStates().replace(">", "&gt;").replace("<", "&lt;") + "</span>");
				}
				output.append("<span class=\"warning\">" + tree2dump.nodeDescriptionAnalysis(child)
						+ "</span>");

				// displaying the child node number if it has children nodes, displaying the taxa otherwise
				if (child.getChildren().size() == 0) {
					output.append(" => <span class=\"taxa\">");
					boolean firstLoop = true;
					for (Taxon taxon : child.getRemainingTaxa()) {
						if (!firstLoop) {
							output.append(", ");
						}
						// create image previous using the first image in the list
						if (taxon.getFirstImage(tree2dump.getDataSet()) != null) {
							output.append("<a href=\"" + taxon.getFirstImage(tree2dump.getDataSet())
									+ "\" class=\"screenshot\" rel=\""
									+ taxon.getFirstImage(tree2dump.getDataSet()) + "\" target=\"_blank\">"
									+ taxon.getName() + "</a>");
						} else {
							output.append(taxon.getName());
						}
						firstLoop = false;
					}
					output.append("</span>");
				} else {
					if (activeLink) {
						output.append(" => <a href=\"#anchor" + counter + "\">" + counter + "</a>");
					} else {
						output.append(" => " + counter);
					}

				}
				output.append("</span>"); // closes the opening <span class="statesAndTaxa">
				output.append("<br/>" + lineSeparator);

				queue.add(child);

				counter++;
				// / end child node treatment

			}
		}

		// // end third traversal, breadth-first ////

	}

	// END HTML DUMP, FLAT

	// PDF DUMP, TREE
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
	public static File dumpPdfFile(String header, SingleAccessKeyTree tree2dump) throws IOException,
			DocumentException {

		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");
		File pdfFile = File.createTempFile(Utils.KEY, "." + Utils.PDF, new File(path));

		Document pdfDocument = new Document(PageSize.A3, 50, 50, 50, 50);
		PdfWriter.getInstance(pdfDocument, new FileOutputStream(pdfFile));

		pdfDocument.open();

		StyleSheet styles = new StyleSheet();
		styles.loadTagStyle("body", "color", "#333");
		styles.loadTagStyle("body", "background", "#fff");
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
		recursiveToHTMLStringForPdf(tree2dump.getRoot(), output, "", 0, 0, tree2dump);
		output.append("</ul></body></html>");

		htmlWorker.parse(new StringReader(output.toString()));

		pdfDocument.close();
		htmlWorker.close();

		return pdfFile;
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
	private static void recursiveToHTMLStringForPdf(SingleAccessKeyNode node, StringBuffer output,
			String tabulations, int firstNumbering, int secondNumbering, SingleAccessKeyTree tree2dump) {
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
				state = node.getStringStates();
			state = "<span class='state'>" + state.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;")
					+ "</span>";
			state += "<span class=\"warning\">" + tree2dump.nodeDescriptionAnalysis(node) + "</span>";

			if (node.hasChild()) {
				output.append(" | " + state + " (taxa=" + node.getRemainingTaxa().size() + ")");
			} else {
				output.append(" | " + state + "<span class='taxa'> -> ");
				boolean firstLoop = true;
				for (Taxon taxon : node.getRemainingTaxa()) {
					if (!firstLoop) {
						output.append(", ");
					}
					output.append("<i>" + taxon.getName() + "</i>");
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
			recursiveToHTMLStringForPdf(childNode, output, tabulations, firstNumbering, secondNumbering,
					tree2dump);
		}
		if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {

			if (node.hasChild())
				output.append(tabulations + "</span></li></ul>\n");
			else
				output.append(tabulations + "</span></li>\n");
		}
	}

	// END PDF DUMP, TREE

	// PDF DUMP, FLAT
	/**
	 * get a PDF file containing the key, in a flat representation
	 * 
	 * @param String
	 *            , header information
	 * @return File, the pdf file
	 * @throws IOException
	 * @throws COSVisitorException
	 * @throws DocumentException
	 */
	public static File dumpFlatPdfFile(String header, SingleAccessKeyTree tree2dump) throws IOException,
			DocumentException {

		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");
		File pdfFile = File.createTempFile(Utils.KEY, "." + Utils.PDF, new File(path));

		Document pdfDocument = new Document(PageSize.A3, 50, 50, 50, 50);
		PdfWriter.getInstance(pdfDocument, new FileOutputStream(pdfFile));

		pdfDocument.open();

		StyleSheet styles = new StyleSheet();
		styles.loadTagStyle("body", "color", "#333");
		styles.loadTagStyle("body", "background", "#fff");

		styles.loadStyle("character", "color", "#333");
		styles.loadStyle("state", "color", "#fe8a22");
		styles.loadStyle("taxa", "color", "#67bb1b");
		styles.loadStyle("line", "color", "#333");

		HTMLWorker htmlWorker = new HTMLWorker(pdfDocument);
		htmlWorker.setStyleSheet(styles);

		StringBuffer output = new StringBuffer();
		output.append("<html><head></head><body>");
		output.append(header.replaceAll(System.getProperty("line.separator"), "<br/>"));

		multipleTraversalToHTMLString(tree2dump.getRoot(), output, System.getProperty("line.separator"),
				false, tree2dump);

		output.append("</body></html>");

		htmlWorker.parse(new StringReader(output.toString()));

		pdfDocument.close();
		htmlWorker.close();

		return pdfFile;
	}

	// END PDF DUMP, FLAT

	// WIKI DUMP, TREE
	/**
	 * get a wiki file containing the key
	 * 
	 * @param String
	 *            , header information
	 * @return File, the Wikitext file
	 */
	public static File dumpWikiFile(String header, SingleAccessKeyTree tree2dump) throws IOException {
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File wikiFile = File.createTempFile(Utils.KEY, "." + Utils.WIKI, new File(path));

		FileOutputStream fileOutputStream = new FileOutputStream(wikiFile);
		fileOutputStream.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
		BufferedWriter wikiFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));

		if (header != null && !header.equals("")) {
			wikiFileWriter.append("== Info ==");
			wikiFileWriter.newLine();
			wikiFileWriter.append(header.replaceAll(System.getProperty("line.separator"), "<br>"));
			wikiFileWriter.newLine();
		}
		wikiFileWriter.append("== Identification Key==");
		wikiFileWriter.newLine();
		wikiFileWriter.append(" <nowiki>");

		wikiFileWriter.append(generateTreeString(tree2dump));

		wikiFileWriter.append("</nowiki>");
		wikiFileWriter.close();

		return wikiFile;
	}

	// END WIKI DUMP, TREE

	// WIKI DUMP, FLAT
	/**
	 * Generates a File containing a flat wiki-formatted representation of the SingleAccessKeytree, in a flat
	 * representation
	 * 
	 * @param header
	 * @return
	 * @throws IOException
	 */
	public static File dumpFlatWikiFile(String header, SingleAccessKeyTree tree2dump) throws IOException {
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File wikiFile = File.createTempFile(Utils.KEY, "." + Utils.WIKI, new File(path));
		BufferedWriter wikiFlatFileWriter = new BufferedWriter(new FileWriter(wikiFile));

		if (header != null && !header.equals("")) {
			wikiFlatFileWriter.append("== Info ==");
			wikiFlatFileWriter.newLine();
			wikiFlatFileWriter.append(header.replaceAll(System.getProperty("line.separator"), "<br>"));
			wikiFlatFileWriter.newLine();
		}
		wikiFlatFileWriter.append("== Identification Key==");
		wikiFlatFileWriter.newLine();
		// wikiFlatFileWriter.append(" <nowiki>");

		wikiFlatFileWriter.append(generateFlatWikiString(tree2dump));

		// wikiFlatFileWriter.append("</nowiki>");
		wikiFlatFileWriter.close();

		return wikiFile;
	}

	/**
	 * generates a flat, wiki-formatted, String representation of a key, in a String object, by calling the
	 * {@link #multipleTraversalToWikiString} helper method
	 * 
	 * @return
	 */
	private static String generateFlatWikiString(SingleAccessKeyTree tree2dump) {
		StringBuffer output = new StringBuffer();
		multipleTraversalToWikiString(tree2dump.getRoot(), output, System.getProperty("line.separator"),
				tree2dump);
		return output.toString();
	}

	/**
	 * This methods outputs the {@link #SingleAccesKeyTree} as a flat wiki-formatted String, with mediawiki
	 * hyperlinks. In order to do this, the <tt>SingleAccesKeyTree</tt> is traversed 3 times. The first
	 * traversal is a breadth-first traversal, in order to generate an HashMap (
	 * <tt>nodeBreadthFirstIterationMap</tt>) that associates each node with an arbitrary Integer. The second
	 * traversal is a depth-first traversal, in order to associate (in another HashMap :
	 * <tt>nodeChildParentNumberingMap</tt>), for each node, the node number and the number of its parent
	 * node. Finally, the last traversal is another breadh-first traversal that generates the flat key String
	 * 
	 * @param rootNode
	 * @param output
	 * @param lineSeparator
	 */
	private static void multipleTraversalToWikiString(SingleAccessKeyNode rootNode, StringBuffer output,
			String lineSeparator, SingleAccessKeyTree tree2dump) {

		// // first traversal, breadth-first ////
		HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap = new HashMap<SingleAccessKeyNode, Integer>();
		int counter = 1;

		iterativeBreadthFirst(rootNode, nodeBreadthFirstIterationMap, counter);

		// // end first traversal, breadth-first ////

		// // second traversal, depth-first ////
		HashMap<Integer, Integer> nodeChildParentNumberingMap = new HashMap<Integer, Integer>();
		recursiveDepthFirst(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
		// // end second traversal, depth-first ////

		// // third traversal, breadth-first ////
		Queue<SingleAccessKeyNode> queue = new LinkedList<SingleAccessKeyNode>();
		ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<SingleAccessKeyNode>();

		counter = 1;
		int currentParentNumber = -1;
		queue.add(rootNode);

		// root node treatment

		counter++;
		// end root node treatment
		visitedNodes.add(rootNode);

		while (!queue.isEmpty()) {
			SingleAccessKeyNode node = queue.remove();
			SingleAccessKeyNode child = null;

			while (Utils.exclusion(node.getChildren(), visitedNodes).size() > 0
					&& (child = (SingleAccessKeyNode) Utils.exclusion(node.getChildren(), visitedNodes)
							.get(0)) != null
			// && child.getCharacter() != null && child.getCharacterState() != null
			) {
				visitedNodes.add(child);

				// / child node treatment

				// displaying the parent node number and the child node character name only once
				if (nodeChildParentNumberingMap.get(new Integer(counter)) != currentParentNumber) {
					currentParentNumber = nodeChildParentNumberingMap.get(new Integer(counter));
					output.append(lineSeparator);
					if (currentParentNumber < 10)
						output.append("   ");
					else if (currentParentNumber < 100)
						output.append("  ");
					else if (currentParentNumber < 1000)
						output.append(" ");
					output.append("<span id=\"anchor" + currentParentNumber + "\"></span>"
							+ currentParentNumber);

					output.append("  " + child.getCharacter().getName() + " = ");
				} else {
					output.append("    ");
					String blankCharacterName = "";
					for (int i = 0; i < child.getCharacter().getName().length(); i++)
						blankCharacterName += " ";
					output.append("  " + blankCharacterName + " = ");
				}

				// displaying the child node character state
				output.append("<span style=\"color:#fe8a22;\">");// state coloring
				if (child.getCharacterState() instanceof QuantitativeMeasure) {
					output.append(((QuantitativeMeasure) child.getCharacterState()).toStringInterval());
				} else {
					output.append(child.getStringStates());
				}
				output.append("</span>");
				output.append("<span style=\"color: black;\">" + tree2dump.nodeDescriptionAnalysis(child)
						+ "</span>");

				// displaying the child node number if it has children nodes, displaying the taxa otherwise
				output.append(" &#8658; "); // arrow
				if (child.getChildren().size() == 0) {

					boolean firstLoop = true;
					for (Taxon taxon : child.getRemainingTaxa()) {

						if (!firstLoop) {
							output.append(", ");
						}
						output.append("<span style=\"color:#67bb1b;\">"); // taxa coloring
						output.append("''" + taxon.getName() + "''");
						output.append("</span>");
						firstLoop = false;
					}

				} else {
					output.append("[[#anchor" + counter + "|<span style=\"color:#67bb1b;\"><u>" + counter
							+ "</u></span>]]");
				}

				output.append(lineSeparator);

				queue.add(child);

				counter++;
				// / end child node treatment

			}
		}

		// // end third traversal, breadth-first ////

	}

	// END WIKI DUMP, FLAT

	// SPECIES-ID WIKI DUMP, QUESTION/ANSWER
	/**
	 * Generates a File containing a flat wiki-formatted representation of the SingleAccessKeytree, this wiki
	 * representation complies with the wiki format used on species-id.net
	 * 
	 * @param header
	 * @return
	 * @throws IOException
	 */
	public static File dumpFlatSpeciesIDQuestionAnswerWikiFile(String header, SingleAccessKeyTree tree2dump)
			throws IOException {
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File wikiFile = File.createTempFile(Utils.KEY, "." + Utils.WIKI, new File(path));

		FileOutputStream fileOutputStream = new FileOutputStream(wikiFile);
		fileOutputStream.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
		BufferedWriter wikiFlatFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream,
				"UTF-8"));

		if (header != null && !header.equals("")) {
			wikiFlatFileWriter.append("== Info ==");
			wikiFlatFileWriter.newLine();
			wikiFlatFileWriter.append(header.replaceAll(System.getProperty("line.separator"), "<br>"));
			wikiFlatFileWriter.newLine();
		}
		wikiFlatFileWriter.append("== Identification Key==");
		wikiFlatFileWriter.newLine();

		wikiFlatFileWriter.append(generateFlatSpeciesIDQuestionAnswerWikiString(tree2dump));

		wikiFlatFileWriter.close();

		return wikiFile;
	}

	/**
	 * generates a flat, wiki-formatted, String representation of a key that complies with the wiki format
	 * used on <a href="http://species-id.net">species-id.net</a>, in a String object, by calling the
	 * {@link #multipleTraversalToString} helper method
	 * 
	 * @return
	 */
	private static String generateFlatSpeciesIDQuestionAnswerWikiString(SingleAccessKeyTree tree2dump) {

		StringBuffer output = new StringBuffer();
		multipleTraversalToSpeciesIDQuestionAnswerWikiString(tree2dump.getRoot(), output,
				System.getProperty("line.separator"), tree2dump);
		return output.toString();
	}

	/**
	 * This methods outputs the {@link #SingleAccesKeyTree} as a flat wiki-formatted String that complies with
	 * the wiki format used on <a href="http://species-id.net">species-id.net</a>, with mediawiki hyperlinks.
	 * In order to do this, the <tt>SingleAccesKeyTree</tt> is traversed 3 times. The first traversal is a
	 * breadth-first traversal, in order to generate an HashMap ( <tt>nodeBreadthFirstIterationMap</tt>) that
	 * associates each node with an arbitrary Integer. The second traversal is a depth-first traversal, in
	 * order to associate (in another HashMap : <tt>nodeChildParentNumberingMap</tt>), for each node, the node
	 * number and the number of its parent node. Finally, the last traversal is another breadh-first traversal
	 * that generates the flat key String
	 * 
	 * @param rootNode
	 * @param output
	 * @param lineSeparator
	 */
	private static void multipleTraversalToSpeciesIDQuestionAnswerWikiString(SingleAccessKeyNode rootNode,
			StringBuffer output, String lineSeparator, SingleAccessKeyTree tree2dump) {

		// // first traversal, breadth-first ////
		HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap = new HashMap<SingleAccessKeyNode, Integer>();

		int counter = 1;
		iterativeBreadthFirst(rootNode, nodeBreadthFirstIterationMap, counter);

		// // end first traversal, breadth-first ////

		// // second traversal, depth-first ////
		HashMap<Integer, Integer> nodeChildParentNumberingMap = new HashMap<Integer, Integer>();
		recursiveDepthFirst(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
		// // end second traversal, depth-first ////

		// // third traversal, breadth-first ////
		Queue<SingleAccessKeyNode> queue = new LinkedList<SingleAccessKeyNode>();
		ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<SingleAccessKeyNode>();

		counter = 1;
		int currentParentNumber = -1;
		queue.add(rootNode);

		// root node treatment

		counter++;
		// end root node treatment
		visitedNodes.add(rootNode);

		String[] alphabet = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
				"q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };
		int alphabetIndex = 0;

		output.append("{{Key Start|title=" + tree2dump.getLabel() + "}}" + lineSeparator);

		while (!queue.isEmpty()) {
			SingleAccessKeyNode node = queue.remove();
			SingleAccessKeyNode child = null;

			while (Utils.exclusion(node.getChildren(), visitedNodes).size() > 0
					&& (child = (SingleAccessKeyNode) Utils.exclusion(node.getChildren(), visitedNodes)
							.get(0)) != null) {
				visitedNodes.add(child);

				// / child node treatment

				// displaying the parent node number and the child node character name only once
				if (nodeChildParentNumberingMap.get(new Integer(counter)) != currentParentNumber) {
					currentParentNumber = nodeChildParentNumberingMap.get(new Integer(counter));
					output.append(lineSeparator);

					output.append("{{Lead Question |" + currentParentNumber + " | "
							+ child.getCharacter().getName().replace(">", "&gt;").replace("<", "&lt;")
							+ " }}");
					output.append(lineSeparator);
					alphabetIndex = 0;

				}

				// creation of state ID
				String stateID = "";
				try {
					if (alphabetIndex + 1 > alphabet.length) {
						int quotient = (alphabetIndex + 1) / alphabet.length;
						int modulo = (alphabetIndex + 1) % alphabet.length;
						stateID = alphabet[quotient - 1] + alphabet[modulo - 1];
					} else {
						stateID = alphabet[alphabetIndex];
					}
				} catch (Exception e) {
					tree2dump.getUtils().setErrorMessage(
							Utils.getBundleConfElement("message.stateNumberError"), e);
					e.printStackTrace();
				}

				// displaying the child node character state
				output.append("{{Lead|" + currentParentNumber + " " + stateID + "|");
				alphabetIndex++;
				if (child.getCharacterState() instanceof QuantitativeMeasure) {
					output.append(((QuantitativeMeasure) child.getCharacterState()).toStringInterval());
				} else {
					output.append(((State) child.getCharacterState()).getName().replace(">", "&gt;")
							.replace("<", "&lt;"));
				}
				output.append(tree2dump.nodeDescriptionAnalysis(child));
				output.append("|");

				// displaying the child node number if it has children nodes, displaying the taxa otherwise
				if (child.getChildren().size() == 0) {
					output.append("result=");
					boolean firstLoop = true;
					for (Taxon taxon : child.getRemainingTaxa()) {
						if (!firstLoop) {
							output.append(", ");
						}
						output.append(taxon.getName().replace(">", "&gt;").replace("<", "&lt;"));
						firstLoop = false;
					}
				} else {
					output.append(counter);
				}
				output.append("}}"); // closing Lead

				output.append(lineSeparator);

				queue.add(child);

				counter++;
				// / end child node treatment

			}
		}
		output.append("{{Key End}}");
		// // end third traversal, breadth-first ////

	}

	// END SPECIES-ID WIKI DUMP, QUESTION/ANSWER

	// SPECIES-ID WIKI DUMP, STATEMENT
	/**
	 * Generates a File containing a flat wiki-formatted representation of the SingleAccessKeytree, this wiki
	 * representation complies with the wiki format used on species-id.net
	 * 
	 * @param header
	 * @return
	 * @throws IOException
	 */
	public static File dumpFlatSpeciesIDStatementWikiFile(String header, SingleAccessKeyTree tree2dump)
			throws IOException {
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File wikiFile = File.createTempFile(Utils.KEY, "." + Utils.WIKI, new File(path));

		FileOutputStream fileOutputStream = new FileOutputStream(wikiFile);
		fileOutputStream.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
		BufferedWriter wikiFlatFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream,
				"UTF-8"));

		if (header != null && !header.equals("")) {
			wikiFlatFileWriter.append("== Info ==");
			wikiFlatFileWriter.newLine();
			wikiFlatFileWriter.append(header.replaceAll(System.getProperty("line.separator"), "<br>"));
			wikiFlatFileWriter.newLine();
		}
		wikiFlatFileWriter.append("== Identification Key==");
		wikiFlatFileWriter.newLine();

		wikiFlatFileWriter.append(generateFlatSpeciesIDStatementWikiString(tree2dump));

		wikiFlatFileWriter.close();

		return wikiFile;
	}

	/**
	 * generates a flat, wiki-formatted, String representation of a key that complies with the wiki format
	 * used on <a href="http://species-id.net">species-id.net</a>, in a String object, by calling the
	 * {@link #multipleTraversalToString} helper method
	 * 
	 * @return
	 */
	private static String generateFlatSpeciesIDStatementWikiString(SingleAccessKeyTree tree2dump) {

		StringBuffer output = new StringBuffer();
		multipleTraversalToSpeciesIDStatementWikiString(tree2dump.getRoot(), output,
				System.getProperty("line.separator"), tree2dump);
		return output.toString();
	}

	/**
	 * This methods outputs the {@link #SingleAccesKeyTree} as a flat wiki-formatted String that complies with
	 * the wiki format used on <a href="http://species-id.net">species-id.net</a>, with mediawiki hyperlinks.
	 * In order to do this, the <tt>SingleAccesKeyTree</tt> is traversed 3 times. The first traversal is a
	 * breadth-first traversal, in order to generate an HashMap ( <tt>nodeBreadthFirstIterationMap</tt>) that
	 * associates each node with an arbitrary Integer. The second traversal is a depth-first traversal, in
	 * order to associate (in another HashMap : <tt>nodeChildParentNumberingMap</tt>), for each node, the node
	 * number and the number of its parent node. Finally, the last traversal is another breadh-first traversal
	 * that generates the flat key String
	 * 
	 * @param rootNode
	 * @param output
	 * @param lineSeparator
	 */
	private static void multipleTraversalToSpeciesIDStatementWikiString(SingleAccessKeyNode rootNode,
			StringBuffer output, String lineSeparator, SingleAccessKeyTree tree2dump) {

		Queue<SingleAccessKeyNode> queue = new LinkedList<SingleAccessKeyNode>();
		ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<SingleAccessKeyNode>();

		// // first traversal, breadth-first ////
		HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap = new HashMap<SingleAccessKeyNode, Integer>();

		int counter = 1;
		queue.add(rootNode);

		// root node treatment
		nodeBreadthFirstIterationMap.put(rootNode, new Integer(counter));
		counter++;
		// end root node treatment

		visitedNodes.add(rootNode);

		while (!queue.isEmpty()) {
			SingleAccessKeyNode node = queue.remove();
			SingleAccessKeyNode child = null;

			// exclusion(node.getChildren(), visitedNodes) is the list of unvisited children nodes of the
			while (Utils.exclusion(node.getChildren(), visitedNodes).size() > 0
					&& (child = (SingleAccessKeyNode) Utils.exclusion(node.getChildren(), visitedNodes)
							.get(0)) != null) {
				visitedNodes.add(child);

				// / child node treatment
				nodeBreadthFirstIterationMap.put(child, new Integer(counter));
				counter++;

				// / end child node treatment

				queue.add(child);
			}
		}

		// // end first traversal, breadth-first ////

		// // second traversal, depth-first ////
		HashMap<Integer, Integer> nodeChildParentNumberingMap = new HashMap<Integer, Integer>();
		recursiveDepthFirst(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
		// // end second traversal, depth-first ////

		// // third traversal, breadth-first ////
		queue.clear();
		visitedNodes.clear();

		counter = 1;
		int currentParentNumber = -1;
		queue.add(rootNode);

		// root node treatment

		counter++;
		// end root node treatment
		visitedNodes.add(rootNode);

		output.append("{{Key Start|title=" + tree2dump.getLabel() + "}}" + lineSeparator);

		while (!queue.isEmpty()) {
			SingleAccessKeyNode node = queue.remove();
			SingleAccessKeyNode child = null;

			while (Utils.exclusion(node.getChildren(), visitedNodes).size() > 0
					&& (child = (SingleAccessKeyNode) Utils.exclusion(node.getChildren(), visitedNodes)
							.get(0)) != null) {
				visitedNodes.add(child);

				// / child node treatment

				if (nodeChildParentNumberingMap.get(new Integer(counter)) != currentParentNumber) {
					currentParentNumber = nodeChildParentNumberingMap.get(new Integer(counter));
					output.append("{{Lead|" + currentParentNumber + "|");
				} else
					output.append("{{Lead|" + currentParentNumber + "-|");
				// displaying the child node character
				output.append(child.getCharacter().getName() + ":  ");

				// displaying the child node character state
				if (child.getCharacterState() instanceof QuantitativeMeasure) {
					output.append(((QuantitativeMeasure) child.getCharacterState()).toStringInterval());
				} else {
					output.append(((State) child.getCharacterState()).getName().replace(">", "&gt;")
							.replace("<", "&lt;"));
				}
				output.append(tree2dump.nodeDescriptionAnalysis(child));
				output.append("|");

				// displaying the child node number if it has children nodes, displaying the taxa otherwise
				if (child.getChildren().size() == 0) {
					output.append("result=");
					boolean firstLoop = true;
					for (Taxon taxon : child.getRemainingTaxa()) {
						if (!firstLoop) {
							output.append(", ");
						}
						output.append(taxon.getName().replace(">", "&gt;").replace("<", "&lt;"));
						firstLoop = false;
					}
				} else {
					output.append(counter);
				}
				output.append("}}"); // closing Lead

				output.append(lineSeparator);

				queue.add(child);

				counter++;
				// / end child node treatment

			}
		}
		output.append("{{Key End}}");

		// // end third traversal, breadth-first ////

	}

	// END SPECIES-ID WIKI DUMP, STATEMENT

	// DOT DUMP
	/**
	 * get a DOT file containing the key
	 * 
	 * @param header
	 * @return
	 * @throws IOException
	 */
	public static File dumpDotFile(String header, SingleAccessKeyTree tree2dump) throws IOException {
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		header = header.replace(System.getProperty("line.separator"), System.getProperty("line.separator")
				+ "//");
		header = header + System.getProperty("line.separator");
		File dotFile = File.createTempFile("key_", "." + Utils.GV, new File(path));

		FileOutputStream fileOutputStream = new FileOutputStream(dotFile);
		fileOutputStream.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
		BufferedWriter dotFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
		dotFileWriter.append(header);
		dotFileWriter.append("digraph " + dotFile.getName().split("\\.")[0] + " {");
		dotFileWriter.append(generateDotString(tree2dump));
		dotFileWriter.append(System.getProperty("line.separator") + "}");
		dotFileWriter.close();

		return dotFile;
	}

	/**
	 * generates a DOT-formatted String representation of the key
	 * 
	 * @return
	 */
	private static String generateDotString(SingleAccessKeyTree tree2dump) {
		StringBuffer output = new StringBuffer();
		multipleTraversalToDotString(tree2dump.getRoot(), output, System.getProperty("line.separator"),
				tree2dump);
		return output.toString();
	}

	/**
	 * This methods outputs the {@link #SingleAccesKeyTree} as a DOT-formatted String. In order to do this,
	 * the <tt>SingleAccesKeyTree</tt> is traversed 3 times. The first traversal is a breadth-first traversal,
	 * in order to generate an HashMap (<tt>nodeBreadthFirstIterationMap</tt>) that associates each node with
	 * an arbitrary Integer. The second traversal is a depth-first traversal, in order to associate (in
	 * another HashMap : <tt>nodeChildParentNumberingMap</tt>), for each node, the node number and the number
	 * of its parent node. Finally, the last traversal is another breadh-first traversal that generates the
	 * flat key String
	 * 
	 * @param rootNode
	 * @param output
	 * @param lineSeparator
	 */
	private static void multipleTraversalToDotString(SingleAccessKeyNode rootNode, StringBuffer output,
			String lineSeparator, SingleAccessKeyTree tree2dump) {

		// // first traversal, breadth-first ////
		HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap = new HashMap<SingleAccessKeyNode, Integer>();

		int counter = 1;
		iterativeBreadthFirst(rootNode, nodeBreadthFirstIterationMap, counter);

		// // end first traversal, breadth-first ////

		// // second traversal, depth-first ////
		HashMap<Integer, Integer> nodeChildParentNumberingMap = new HashMap<Integer, Integer>();
		recursiveDepthFirst(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
		// // end second traversal, depth-first ////

		// // third traversal, breadth-first ////
		Queue<SingleAccessKeyNode> queue = new LinkedList<SingleAccessKeyNode>();
		ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<SingleAccessKeyNode>();

		counter = 1;
		int currentParentNumber = -1;
		queue.add(rootNode);

		// root node treatment

		counter++;
		// end root node treatment
		visitedNodes.add(rootNode);

		while (!queue.isEmpty()) {
			SingleAccessKeyNode node = queue.remove();
			SingleAccessKeyNode child = null;

			while (Utils.exclusion(node.getChildren(), visitedNodes).size() > 0
					&& (child = (SingleAccessKeyNode) Utils.exclusion(node.getChildren(), visitedNodes)
							.get(0)) != null
			// && child.getCharacter() != null && child.getCharacterState() != null
			) {
				visitedNodes.add(child);

				// / child node treatment

				// displaying the parent node number
				if (nodeChildParentNumberingMap.get(new Integer(counter)) != currentParentNumber) {
					currentParentNumber = nodeChildParentNumberingMap.get(new Integer(counter));
				}
				output.append(lineSeparator);
				output.append(currentParentNumber + " -> ");

				// displaying the child node number
				output.append(counter);

				// displaying the child node character state as a vertex label
				if (child.getCharacterState() instanceof QuantitativeMeasure) {
					output.append(" [label=\""
							+ ((QuantitativeMeasure) child.getCharacterState()).toStringInterval());
				} else {
					output.append(" [label=\"" + child.getStringStates());
				}
				output.append(tree2dump.nodeDescriptionAnalysis(child));
				output.append("\"]");
				output.append(";" + lineSeparator);

				if (child.getChildren().size() == 0) {
					// if the child node has no children nodes, displaying the parent node character and the
					// child node remaining taxa
					output.append(currentParentNumber + " [label=\"" + child.getCharacter().getName()
							+ "\"];");
					output.append(lineSeparator);

					output.append(counter + " [label=\"");
					boolean firstLoop = true;
					for (Taxon taxon : child.getRemainingTaxa()) {
						if (!firstLoop) {
							output.append(", ");
						}
						output.append(taxon.getName());
						firstLoop = false;
					}
					output.append("\",shape=box]");
					output.append(";");
				} else {
					output.append(currentParentNumber + " [label=\"");
					output.append(child.getCharacter().getName() + "\"];");
				}

				output.append(lineSeparator);

				queue.add(child);

				counter++;
				// / end child node treatment

			}
		}

		// // end third traversal, breadth-first ////

	}

	// END DOT DUMP

	// ---------------------- HELPER METHODS ---------------------- //
	/**
	 * Helper method that traverses the SingleAccessKeyTree breadth-first. It is used in multiple traversal
	 * methods in order to generate the nodeBreadthFirstIterationMap HashMap, that associates each node with a
	 * breadth-first incremented number
	 * 
	 * @param rootNode
	 * @param nodeBreadthFirstIterationMap
	 * @param counter
	 */
	private static void iterativeBreadthFirst(SingleAccessKeyNode rootNode,
			HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap, int counter) {
		Queue<SingleAccessKeyNode> queue = new LinkedList<SingleAccessKeyNode>();
		ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<SingleAccessKeyNode>();

		queue.add(rootNode);

		// root node treatment
		nodeBreadthFirstIterationMap.put(rootNode, new Integer(counter));
		counter++;
		// end root node treatment

		visitedNodes.add(rootNode);

		while (!queue.isEmpty()) {
			SingleAccessKeyNode node = queue.remove();
			SingleAccessKeyNode child = null;

			// exclusion(node.getChildren(), visitedNodes) is the list of unvisited children nodes of the
			while (Utils.exclusion(node.getChildren(), visitedNodes).size() > 0
					&& (child = (SingleAccessKeyNode) Utils.exclusion(node.getChildren(), visitedNodes)
							.get(0)) != null) {
				visitedNodes.add(child);

				// / child node treatment
				nodeBreadthFirstIterationMap.put(child, new Integer(counter));
				counter++;

				// / end child node treatment

				queue.add(child);
			}
		}
	}

	/**
	 * Helper method that traverses the SingleAccessKeyTree depth-first. It is used in multipleTraversal
	 * methods in order to generate the nodeChildParentNumberingMap HashMap, that associates a child node
	 * number with the number of its parent node
	 * 
	 * @param node
	 * @param nodeBreadthFirstIterationMap
	 * @param nodeChildParentNumberingMap
	 */
	private static void recursiveDepthFirst(SingleAccessKeyNode node,
			HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap,
			HashMap<Integer, Integer> nodeChildParentNumberingMap) {

		Integer parentNumber = nodeBreadthFirstIterationMap.get(node);
		for (SingleAccessKeyNode childNode : node.getChildren()) {
			Integer childNumber = nodeBreadthFirstIterationMap.get(childNode);
			nodeChildParentNumberingMap.put(childNumber, parentNumber);
			recursiveDepthFirst(childNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
		}
	}

	/**
	 * Helper method that traverses the SingleAccessKeyTree depth-first. It is used in multipleTraversal
	 * methods in order to generate the nodeChildParentNumberingMap HashMap, that associates a child node
	 * number with the number of its parent node, and to generate the rootNodeChildrenIntegerList List, that
	 * contains the node numbers of the children of the root nodes.
	 * 
	 * @param node
	 * @param nodeBreadthFirstIterationMap
	 * @param nodeChildParentNumberingMap
	 * @param rootNodeChildrenIntegerList
	 */
	private static void recursiveDepthFirst(SingleAccessKeyNode node,
			HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap,
			HashMap<Integer, Integer> nodeChildParentNumberingMap, List<Integer> rootNodeChildrenIntegerList) {

		Integer parentNumber = nodeBreadthFirstIterationMap.get(node);
		for (SingleAccessKeyNode childNode : node.getChildren()) {
			Integer childNumber = nodeBreadthFirstIterationMap.get(childNode);
			nodeChildParentNumberingMap.put(childNumber, parentNumber);
			if (parentNumber == 1)
				rootNodeChildrenIntegerList.add(childNumber);

			recursiveDepthFirst(childNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap,
					rootNodeChildrenIntegerList);
		}
	}

	/**
	 * @param htmlString
	 * @return
	 */
	private static String escapeHTMLSpecialCharacters(String htmlString) {
		return htmlString.replace(">", "&gt;").replace("<", "&lt;");
	}

}
