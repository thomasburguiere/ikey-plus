package IO;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import model.DataSet;
import model.QuantitativeMeasure;
import model.SingleAccessKeyNode;
import model.SingleAccessKeyTree;
import model.State;
import model.Taxon;
import utils.Utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.html.simpleparser.StyleSheet;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * This class generate all outputs format of the key
 * 
 * @author Thomas Burguiere
 * @created 28-09-2011
 */
public abstract class SingleAccessKeyTreeDumper {

	// SDD DUMP
	/**
	 * generate a SDD file containing the key
	 * 
	 * @param String
	 *            , header information
	 * @return File the SDD File
	 * @throws IOException
	 */
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
	 *            the SingleAccessKeyTree which is to be dumped in a SDD-formatted File
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
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String generationDate = dateFormat.format(new Date());
		output.append("<TechnicalMetadata created=\"" + generationDate + "\">" + lineSeparator);
		output.append("<Generator name=\"Identification Key generation WebService\" ");
		output.append("notes=\"This software is developed and distributed by LIS -"
				+ " Laboratoire Informatique et Systématique (LIS) -"
				+ " Université Pierre et Marie Curie - Paris VI - within the ViBRANT project\" version=\"1.1\"/>"
				+ lineSeparator);
		output.append("</TechnicalMetadata>" + lineSeparator);
		output.append("<Dataset xml:lang=\"en\">" + lineSeparator);
		output.append("<Representation>" + lineSeparator);
		output.append("<Label>Identification key</Label>" + lineSeparator);
		output.append("</Representation>" + lineSeparator);

		DataSet originalDataSet = tree2dump.getDataSet();

		output.append("<TaxonNames>" + lineSeparator);
		int taxonIDint = 1;
		String taxonID;
		for (Taxon t : originalDataSet.getTaxa()) {
			taxonID = "t" + taxonIDint;
			taxonIDint++;
			t.setId(taxonID);
			output.append("<TaxonName id=\"" + taxonID + "\">" + lineSeparator);
			output.append("<Representation>" + lineSeparator);
			output.append("<Label>"
					+ t.getName().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
					+ "</Label>" + lineSeparator);
			for (String mediaObjectKey : t.getMediaObjectKeys()) {
				output.append("<MediaObject ref=\"" + mediaObjectKey + "\"/>" + lineSeparator);
			}
			output.append("</Representation>" + lineSeparator);
			output.append("</TaxonName>" + lineSeparator);
		}
		output.append("</TaxonNames>" + lineSeparator);

		output.append("<IdentificationKeys>" + lineSeparator);
		multipleTraversalToSddString(tree2dump.getRoot(), output, lineSeparator, tree2dump);
		output.append("</IdentificationKeys>" + lineSeparator);
		output.append("<MediaObjects>" + lineSeparator);
		for (Taxon t : originalDataSet.getTaxa()) {
			for (String mediaObjectKey : t.getMediaObjectKeys()) {
				output.append("<MediaObject id=\"" + mediaObjectKey + "\">" + lineSeparator);
				output.append("<Representation>" + lineSeparator);
				output.append("<Label>");
				output.append(mediaObjectKey);
				output.append("</Label>" + lineSeparator);
				output.append("</Representation>" + lineSeparator);
				output.append("<Type>Image</Type>" + lineSeparator);
				output.append("<Source href=\"" + originalDataSet.getMediaObject(mediaObjectKey) + "\"/>"
						+ lineSeparator);
				output.append("</MediaObject>" + lineSeparator);
			}
		}
		output.append("</MediaObjects>" + lineSeparator);
		output.append("</Dataset>" + lineSeparator);
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
			String lineSeparator, SingleAccessKeyTree tree2dump) {

		// // FIRST TRAVERSAL, breadth-first ////
		HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap = new HashMap<SingleAccessKeyNode, Integer>();
		int counter = 1;
		iterativeBreadthFirst(rootNode, nodeBreadthFirstIterationMap, counter);
		// // END FIRST TRAVERSAL, breadth-first ////

		// // SECOND TRAVERSAL, depth-first ////
		HashMap<Integer, Integer> nodeChildParentNumberingMap = new HashMap<Integer, Integer>();
		List<Integer> rootNodeChildrenIntegerList = new ArrayList<Integer>();
		recursiveDepthFirstIntegerIndex(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap,
				rootNodeChildrenIntegerList);
		// // END SECOND TRAVERSAL, depth-first ////

		// // THIRD TRAVERSAL, breadth-first ////
		Queue<SingleAccessKeyNode> queue = new LinkedList<SingleAccessKeyNode>();
		ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<SingleAccessKeyNode>();
		counter = 1;
		int currentParentNumber = -1;
		queue.add(rootNode);

		counter++;
		// end root node treatment
		visitedNodes.add(rootNode);
		output.append("<IdentificationKey>" + lineSeparator);
		output.append("<Representation>" + lineSeparator);
		output.append("<Label>" + tree2dump.getLabel() + "</Label>" + lineSeparator);
		output.append("</Representation>" + lineSeparator);

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
					output.append("<Text>" + escapeHTMLSpecialCharacters(child.getCharacter().getName())
							+ "</Text>" + lineSeparator);
					output.append("</Question>" + lineSeparator);
					output.append("<Leads>" + lineSeparator);
				}
				// other child nodes of the root node
				if (rootNodeChildrenIntegerList.contains(new Integer(counter))) {
					if (child.hasChild()) {
						output.append("<Lead id=\"lead" + (counter - 1) + "\">" + lineSeparator);
						output.append("<Statement>"
								+ child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")
										.replace("&", "&amp;"));
						output.append("</Statement>" + lineSeparator);
						output.append("<Question>" + lineSeparator);
						output.append("<Text>"
								+ child.getChildren().get(0).getCharacter().getName().replace(">", "&gt;")
										.replace("<", "&lt;").replace("&", "&amp;") + "</Text>"
								+ lineSeparator);
						output.append("</Question>" + lineSeparator);
						output.append("</Lead>" + lineSeparator);
					} else {

						output.append("<Lead>" + lineSeparator);
						output.append("<Statement>"
								+ child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")
										.replace("&", "&amp;") + lineSeparator);
						output.append("</Statement>");
						for (Taxon t : child.getRemainingTaxa()) {
							output.append("<TaxonName ref=\"" + t.getId() + "\"/>" + lineSeparator);
							break;
							/* taxonCounter++; output.append("<Label>");
							 * output.append(t.getName().replace(">", "&gt;").replace("<", "&lt;")
							 * .replace("&", "&amp;")); output.append("</Label>" + lineSeparator);
							 * output.append("</TaxonName>" + lineSeparator); */
						}
						output.append("</Lead>" + lineSeparator);
					}
				} else {
					if (child.hasChild()) {
						output.append("<Lead id=\"lead" + (counter - 1) + "\">" + lineSeparator);
						output.append("<Parent ref=\"lead" + (currentParentNumber - 1) + "\"/>"
								+ lineSeparator);
						output.append("<Statement>"
								+ child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")
										.replace("&", "&amp;"));
						output.append("</Statement>" + lineSeparator);
						output.append("<Question>" + lineSeparator);
						output.append("<Text>"
								+ child.getChildren().get(0).getCharacter().getName().replace(">", "&gt;")
										.replace("<", "&lt;").replace("&", "&amp;") + "</Text>"
								+ lineSeparator);
						output.append("</Question>" + lineSeparator);
						output.append("</Lead>" + lineSeparator);

					} else {
						output.append("<Lead>" + lineSeparator);
						output.append("<Parent ref=\"lead" + (currentParentNumber - 1) + "\"/>"
								+ lineSeparator);
						output.append("<Statement>"
								+ child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")
										.replace("&", "&amp;"));
						output.append("</Statement>" + lineSeparator);
						for (Taxon t : child.getRemainingTaxa()) {
							output.append("<TaxonName ref=\"" + t.getId() + "\"/>" + lineSeparator);
							break;
							/* taxonCounter++; output.append("<Label>");
							 * output.append(t.getName().replace(">", "&gt;").replace("<", "&lt;")
							 * .replace("&", "&amp;")); output.append("</Label>" + lineSeparator);
							 * output.append("</TaxonName>" + lineSeparator); */
						}
						output.append("</Lead>" + lineSeparator);
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
		iterativeBreadthFirstSkipChildlessNodes(rootNode, nodeBreadthFirstIterationMap, counter);
		// // end first traversal, breadth-first ////

		// // second traversal, depth-first ////
		HashMap<SingleAccessKeyNode, Integer> nodeChildParentNumberingMap = new HashMap<SingleAccessKeyNode, Integer>();
		recursiveDepthFirstNodeIndex(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
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
				if (nodeChildParentNumberingMap.get(child) != currentParentNumber) {
					currentParentNumber = nodeChildParentNumberingMap.get(child);
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
				if (child.hasChild())
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

		// slk.append("<div id=\"treecontrol\"><a title=\"Collapse the entire tree below\" href=\"#\">Collapse All</a> | <a title=\"Expand the entire tree below\" href=\"#\">Expand All</a> | <a title=\"Toggle the tree below, opening closed branches, closing open branches\" href=\"#\">Toggle All</a></div>"
		// + lineSep);
		slk.append("<div><a style=\"color:#444;\" title=\"Collapse the entire tree below\" href=\"#\" onClick=\"window.location.href=window.location.href\">Collapse All</a></div><br/>"
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
		iterativeBreadthFirstSkipChildlessNodes(rootNode, nodeBreadthFirstIterationMap, counter);

		// // end first traversal, breadth-first ////

		// // second traversal, depth-first ////
		HashMap<SingleAccessKeyNode, Integer> nodeChildParentNumberingMap = new HashMap<SingleAccessKeyNode, Integer>();
		recursiveDepthFirstNodeIndex(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
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
				if (nodeChildParentNumberingMap.get(child) != currentParentNumber) {
					currentParentNumber = nodeChildParentNumberingMap.get(child);
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
				if (child.hasChild())
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
	 */
	public static File dumpPdfFile(String header, SingleAccessKeyTree tree2dump) throws IOException {

		try {
			String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
					+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");
			File pdfFile;

			pdfFile = File.createTempFile(Utils.KEY, "." + Utils.PDF, new File(path));

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

		} catch (IOException e) {
			tree2dump.getUtils().setErrorMessage(Utils.getBundleConfElement("message.creatingFileError"), e);
			e.printStackTrace();
		} catch (DocumentException e) {
			tree2dump.getUtils().setErrorMessage(Utils.getBundleConfElement("message.creatingFileError"), e);
			e.printStackTrace();
		}

		return null;
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
	 */
	public static File dumpFlatPdfFile(String header, SingleAccessKeyTree tree2dump) throws IOException {

		try {
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

		} catch (IOException e) {
			tree2dump.getUtils().setErrorMessage(Utils.getBundleConfElement("message.creatingFileError"), e);
			e.printStackTrace();
		} catch (DocumentException e) {
			tree2dump.getUtils().setErrorMessage(Utils.getBundleConfElement("message.creatingFileError"), e);
			e.printStackTrace();
		}

		return null;
	}

	// END PDF DUMP, FLAT

	// WIKI DUMP, TREE
	/**
	 * get a wiki file containing the key
	 * 
	 * @param String
	 *            , header information
	 * @return File, the Wikitext file
	 * @throws IOException
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

		wikiFileWriter.append(generateTreeWiki(tree2dump));

		wikiFileWriter.close();

		return wikiFile;
	}

	private static String generateTreeWiki(SingleAccessKeyTree tree2dump) {
		StringBuffer output = new StringBuffer();
		recursiveToWiki(tree2dump.getRoot(), output, "", 0, 0, tree2dump);
		return output.toString();
	}

	/**
	 * recursively method to be abbe to display Wiki representation of this SingleAccessKeyTree
	 * 
	 * @param node
	 * @param output
	 * @param tabulations
	 * @param firstNumbering
	 * @param secondNumbering
	 */
	private static void recursiveToWiki(SingleAccessKeyNode node, StringBuffer output, String tabulations,
			int firstNumbering, int secondNumbering, SingleAccessKeyTree tree2dump) {

		if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {
			if (node.getCharacterState() instanceof QuantitativeMeasure) {
				output.append(tabulations + firstNumbering + "." + secondNumbering + ") "
						+ "<span style=\"color:#333\">" + node.getCharacter().getName() + "</span> | "
						+ "<span style=\"color:#fe8a22\">"
						+ ((QuantitativeMeasure) node.getCharacterState()).toStringInterval() + "</span>");
			} else {
				output.append(tabulations + firstNumbering + "." + secondNumbering + ") "
						+ "<span style=\"color:#333\">" + node.getCharacter().getName() + "</span> | "
						+ "<span style=\"color:#fe8a22\">" + node.getStringStates() + "</span>");
			}
			output.append(tree2dump.nodeDescriptionAnalysis(node));
			if (node.getChildren().size() == 0) {
				output.append(" -> ");
				boolean firstLoop = true;
				for (Taxon taxon : node.getRemainingTaxa()) {
					if (!firstLoop) {
						output.append(", ");
					}
					output.append("<span style=\"color:#67bb1b\">" + taxon.getName() + "</span>");
					firstLoop = false;
				}
			} else {
				output.append(" (taxa=" + node.getRemainingTaxa().size() + ")");
			}
			output.append(System.getProperty("line.separator"));
			tabulations = tabulations + ":";
		}
		firstNumbering++;
		secondNumbering = 0;
		for (SingleAccessKeyNode childNode : node.getChildren()) {
			secondNumbering++;
			recursiveToWiki(childNode, output, tabulations, firstNumbering, secondNumbering, tree2dump);
		}
	}

	// END WIKI DUMP, TREE

	// WIKI DUMP, FLAT
	/**
	 * Generates a File containing a flat wiki-formatted representation of the SingleAccessKeytree, in a flat
	 * representation
	 * 
	 * @param header
	 * @return File, the output flat wiki file
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

		iterativeBreadthFirstSkipChildlessNodes(rootNode, nodeBreadthFirstIterationMap, counter);

		// // end first traversal, breadth-first ////

		// // second traversal, depth-first ////
		HashMap<SingleAccessKeyNode, Integer> nodeChildParentNumberingMap = new HashMap<SingleAccessKeyNode, Integer>();
		recursiveDepthFirstNodeIndex(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
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
							.get(0)) != null) {
				visitedNodes.add(child);

				// / child node treatment

				// displaying the parent node number and the child node character name only once
				if (nodeChildParentNumberingMap.get(child) != currentParentNumber) {
					currentParentNumber = nodeChildParentNumberingMap.get(child);
					output.append(lineSeparator);
					output.append("<span id=\"anchor" + currentParentNumber + "\"></span>"
							+ currentParentNumber);

					output.append("  " + child.getCharacter().getName());
					output.append(lineSeparator);
					output.append("::::::= ");
				} else {
					output.append("::::::= ");
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

				if (child.hasChild())
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
	 * @return File, the output FlatSpeciesIDQuestionAnswerWiki file
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
		iterativeBreadthFirstSkipChildlessNodes(rootNode, nodeBreadthFirstIterationMap, counter);

		// // end first traversal, breadth-first ////

		// // second traversal, depth-first ////
		HashMap<SingleAccessKeyNode, Integer> nodeChildParentNumberingMap = new HashMap<SingleAccessKeyNode, Integer>();
		recursiveDepthFirstNodeIndex(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
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
				if (nodeChildParentNumberingMap.get(child) != currentParentNumber) {
					currentParentNumber = nodeChildParentNumberingMap.get(child);
					output.append(lineSeparator);

					output.append("{{Lead Question |"
							+ currentParentNumber
							+ " | "
							+ child.getCharacter().getName().replace(">", "&gt;").replace("<", "&lt;")
									.replace(">", "&gt;").replace("=", "&#61;") + " }}");
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
							.replace("=", "&#61;").replace("<", "&lt;"));
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
						output.append(taxon.getName().replace(">", "&gt;").replace("<", "&lt;")
								.replace("=", "&#61;"));
						firstLoop = false;
					}
				} else {
					output.append(counter);
				}
				output.append("}}"); // closing Lead

				output.append(lineSeparator);

				queue.add(child);

				if (child.hasChild())
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
	 * @return File, the output FlatSpeciesIDStatementWiki file
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
		iterativeBreadthFirstSkipChildlessNodes(rootNode, nodeBreadthFirstIterationMap, counter);

		// // end first traversal, breadth-first ////

		// // second traversal, depth-first ////
		HashMap<SingleAccessKeyNode, Integer> nodeChildParentNumberingMap = new HashMap<SingleAccessKeyNode, Integer>();
		recursiveDepthFirstNodeIndex(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
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

				if (nodeChildParentNumberingMap.get(child) != currentParentNumber) {
					currentParentNumber = nodeChildParentNumberingMap.get(child);
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
							.replace("<", "&lt;").replace("=", "&#61;"));
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
						output.append(taxon.getName().replace(">", "&gt;").replace("<", "&lt;")
								.replace("=", "&#61;"));
						firstLoop = false;
					}
				} else {
					output.append(counter);
				}
				output.append("}}"); // closing Lead

				output.append(lineSeparator);

				queue.add(child);
				if (child.getChildren().size() > 0)
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
	 * @return File, the output dot file
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
		recursiveDepthFirstIntegerIndex(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
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

	// ZIP DUMP
	/**
	 * get a ZIP file containing all the key output formats
	 * 
	 * @param header
	 * @return File, the output zip file
	 * @throws IOException
	 */
	public static File dumpZipFile(String header, SingleAccessKeyTree tree2dump) throws IOException {

		// create all output formats
		File sddFile = dumpSddFile(header, tree2dump);
		File txtFile = dumpTxtFile(header, tree2dump);
		File flatTxtFile = dumpFlatTxtFile(header, tree2dump);
		File htmlFile = dumpHtmlFile(header, tree2dump);
		File flatHtmlFile = dumpFlatHtmlFile(header, tree2dump);
		File pdfFile = dumpPdfFile(header, tree2dump);
		File flatPdfFile = dumpFlatPdfFile(header, tree2dump);
		File wikiFile = dumpWikiFile(header, tree2dump);
		File flatWikiFile = dumpFlatWikiFile(header, tree2dump);
		File flatSpeciesIDQuestionAnswerWikiFile = dumpFlatSpeciesIDQuestionAnswerWikiFile(header, tree2dump);
		File flatSpeciesIDStatementWikiFile = dumpFlatSpeciesIDStatementWikiFile(header, tree2dump);
		File dotFile = dumpDotFile(header, tree2dump);

		// add all output file to the files list
		List<File> filesList = new ArrayList<File>();
		filesList.add(sddFile);
		filesList.add(txtFile);
		filesList.add(flatTxtFile);
		filesList.add(htmlFile);
		filesList.add(flatHtmlFile);
		filesList.add(pdfFile);
		filesList.add(flatPdfFile);
		filesList.add(wikiFile);
		filesList.add(flatWikiFile);
		filesList.add(flatSpeciesIDQuestionAnswerWikiFile);
		filesList.add(flatSpeciesIDStatementWikiFile);
		filesList.add(dotFile);

		String label = "";
		if (tree2dump.getLabel() != null) {
			label = tree2dump.getLabel() + "-";
		}
		// create a map matching file to file path
		Map<File, String> correspondingFilePath = new HashMap<File, String>();
		correspondingFilePath.put(sddFile, label + "key" + System.getProperty("file.separator") + "flat"
				+ System.getProperty("file.separator") + sddFile.getName());
		correspondingFilePath.put(txtFile, label + "key" + System.getProperty("file.separator") + "tree"
				+ System.getProperty("file.separator") + txtFile.getName());
		correspondingFilePath.put(flatTxtFile, label + "key" + System.getProperty("file.separator") + "flat"
				+ System.getProperty("file.separator") + flatTxtFile.getName());
		correspondingFilePath.put(htmlFile, label + "key" + System.getProperty("file.separator") + "tree"
				+ System.getProperty("file.separator") + htmlFile.getName());
		correspondingFilePath.put(flatHtmlFile, label + "key" + System.getProperty("file.separator") + "flat"
				+ System.getProperty("file.separator") + flatHtmlFile.getName());
		correspondingFilePath.put(pdfFile, label + "key" + System.getProperty("file.separator") + "tree"
				+ System.getProperty("file.separator") + pdfFile.getName());
		correspondingFilePath.put(flatPdfFile, label + "key" + System.getProperty("file.separator") + "flat"
				+ System.getProperty("file.separator") + flatPdfFile.getName());
		correspondingFilePath.put(wikiFile, label + "key" + System.getProperty("file.separator") + "tree"
				+ System.getProperty("file.separator") + wikiFile.getName());
		correspondingFilePath.put(flatWikiFile, label + "key" + System.getProperty("file.separator") + "flat"
				+ System.getProperty("file.separator") + flatWikiFile.getName());
		correspondingFilePath.put(
				flatSpeciesIDQuestionAnswerWikiFile,
				label + "key" + System.getProperty("file.separator") + "flat"
						+ System.getProperty("file.separator")
						+ flatSpeciesIDQuestionAnswerWikiFile.getName());
		correspondingFilePath.put(
				flatSpeciesIDStatementWikiFile,
				label + "key" + System.getProperty("file.separator") + "flat"
						+ System.getProperty("file.separator") + flatSpeciesIDStatementWikiFile.getName());
		correspondingFilePath.put(dotFile, label + "key" + System.getProperty("file.separator") + "tree"
				+ System.getProperty("file.separator") + dotFile.getName());

		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File zipFile = File.createTempFile(Utils.KEY, "." + Utils.ZIP, new File(path));

		try {
			// create the writing flow
			FileOutputStream dest = new FileOutputStream(zipFile);

			// calculate the checksum : Adler32 (faster) or CRC32
			CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());

			// create the writing buffer
			BufferedOutputStream buff = new BufferedOutputStream(checksum);

			// create the zip writing flow
			ZipOutputStream out = new ZipOutputStream(buff);

			// specify the uncompress method
			out.setMethod(ZipOutputStream.DEFLATED);

			// specify the compress quality
			out.setLevel(Deflater.BEST_COMPRESSION);

			// Temporary buffer
			byte data[] = new byte[Utils.BUFFER];

			// for each file of the list
			for (File file : filesList) {

				// create the reading flow
				FileInputStream fi = new FileInputStream(file);

				// creation of a read buffer of the stream
				BufferedInputStream buffi = new BufferedInputStream(fi, Utils.BUFFER);

				// create input for this Zip file
				ZipEntry entry = new ZipEntry(Utils.unAccent(correspondingFilePath.get(file)));

				// add this entry in the flow of writing the Zip archive
				out.putNextEntry(entry);

				// writing the package file BUFFER bytes in the flow Writing
				int count;
				while ((count = buffi.read(data, 0, Utils.BUFFER)) != -1) {
					out.write(data, 0, count);
				}

				// close the current entry
				out.closeEntry();

				// close the flow of reading
				buffi.close();
			}
			// close the flow of writing
			out.close();
			buff.close();
			checksum.close();
			dest.close();

		} catch (Exception e) {
			tree2dump.getUtils().setErrorMessage(Utils.getBundleConfElement("message.creatingFileError"), e);
			e.printStackTrace();
		}

		return zipFile;
	}

	// END ZIP DUMP

	// ---------------------- HELPER METHODS ---------------------- //
	/**
	 * Helper method that traverses the SingleAccessKeyTree breadth-first. It is used in multiple traversal
	 * methods in order to generate the nodeBreadthFirstIterationMap HashMap, that associates each node with a
	 * breadth-first incremented number (only if the traversed node has at least 1 child node)
	 * 
	 * @param rootNode
	 * @param nodeBreadthFirstIterationMap
	 * @param counter
	 */
	private static void iterativeBreadthFirstSkipChildlessNodes(SingleAccessKeyNode rootNode,
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

				if (child.hasChild()) {
					// / child node treatment
					nodeBreadthFirstIterationMap.put(child, new Integer(counter));
					counter++;
				}

				// / end child node treatment

				queue.add(child);
			}
		}
	}

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
	private static void recursiveDepthFirstIntegerIndex(SingleAccessKeyNode node,
			HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap,
			HashMap<Integer, Integer> nodeChildParentNumberingMap) {

		Integer parentNumber = nodeBreadthFirstIterationMap.get(node);
		for (SingleAccessKeyNode childNode : node.getChildren()) {
			Integer childNumber = nodeBreadthFirstIterationMap.get(childNode);
			nodeChildParentNumberingMap.put(childNumber, parentNumber);
			recursiveDepthFirstIntegerIndex(childNode, nodeBreadthFirstIterationMap,
					nodeChildParentNumberingMap);
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
	private static void recursiveDepthFirstNodeIndex(SingleAccessKeyNode node,
			HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap,
			HashMap<SingleAccessKeyNode, Integer> nodeChildParentNumberingMap) {

		Integer parentNumber = nodeBreadthFirstIterationMap.get(node);
		for (SingleAccessKeyNode childNode : node.getChildren()) {
			nodeChildParentNumberingMap.put(childNode, parentNumber);
			recursiveDepthFirstNodeIndex(childNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
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
	private static void recursiveDepthFirstIntegerIndex(SingleAccessKeyNode node,
			HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap,
			HashMap<Integer, Integer> nodeChildParentNumberingMap, List<Integer> rootNodeChildrenIntegerList) {

		Integer parentNumber = nodeBreadthFirstIterationMap.get(node);
		for (SingleAccessKeyNode childNode : node.getChildren()) {
			Integer childNumber = nodeBreadthFirstIterationMap.get(childNode);
			nodeChildParentNumberingMap.put(childNumber, parentNumber);
			if (parentNumber == 1)
				rootNodeChildrenIntegerList.add(childNumber);

			recursiveDepthFirstIntegerIndex(childNode, nodeBreadthFirstIterationMap,
					nodeChildParentNumberingMap, rootNodeChildrenIntegerList);
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
	private static void recursiveDepthFirstNodeIndex(SingleAccessKeyNode node,
			HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap,
			HashMap<SingleAccessKeyNode, Integer> nodeChildParentNumberingMap,
			List<SingleAccessKeyNode> rootNodeChildrenIntegerList) {

		Integer parentNumber = nodeBreadthFirstIterationMap.get(node);
		for (SingleAccessKeyNode childNode : node.getChildren()) {
			nodeChildParentNumberingMap.put(childNode, parentNumber);
			if (parentNumber == 1)
				rootNodeChildrenIntegerList.add(childNode);

			recursiveDepthFirstNodeIndex(childNode, nodeBreadthFirstIterationMap,
					nodeChildParentNumberingMap, rootNodeChildrenIntegerList);
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
