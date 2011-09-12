package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
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
	private String label = null;

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
	 * get the label of the key
	 * 
	 * @return String, the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * set the label of the key
	 * 
	 * @param String
	 *            , the label
	 */
	public void setLabel(String label) {
		this.label = label;
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
	private void multipleTraversalToString(SingleAccessKeyNode rootNode, StringBuffer output,
			String lineSeparator) {

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
				output.append(nodeDescriptionAnalysis(child));

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
	private void multipleTraversalToWikiString(SingleAccessKeyNode rootNode, StringBuffer output,
			String lineSeparator) {

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
				output.append("<span style=\"color: black;\">" + nodeDescriptionAnalysis(child) + "</span>");

				// displaying the child node number if it has children nodes, displaying the taxa otherwise
				if (child.getChildren().size() == 0) {
					output.append(" &#8658; "); // arrow
					output.append("<span style=\"color:#67bb1b;\">"); // taxa coloring
					boolean firstLoop = true;
					for (Taxon taxon : child.getRemainingTaxa()) {
						if (!firstLoop) {
							output.append(", ");
						}
						output.append("''" + taxon.getName() + "''");
						firstLoop = false;
					}
					output.append("</span>");
				} else {
					output.append(" &#8658; [[#anchor" + counter + "|" + counter + "]]");
				}

				output.append(lineSeparator);

				queue.add(child);

				counter++;
				// / end child node treatment

			}
		}

		// // end third traversal, breadth-first ////

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
	private void multipleTraversalToSpeciesIDQuestionAnswerWikiString(SingleAccessKeyNode rootNode,
			StringBuffer output, String lineSeparator) {

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

		String[] alphabet = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
				"q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };
		int alphabetIndex = 0;

		output.append("{{Key Start|title=" + this.getLabel() + "}}" + lineSeparator);

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
					Utils.setErrorMessage(Utils.getBundleConfElement("message.stateNumberError"), e);
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
				output.append(nodeDescriptionAnalysis(child));
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
	private void multipleTraversalToSpeciesIDStatementWikiString(SingleAccessKeyNode rootNode,
			StringBuffer output, String lineSeparator) {

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

		output.append("{{Key Start|title=" + this.getLabel() + "}}" + lineSeparator);

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
				output.append(nodeDescriptionAnalysis(child));
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
	private void multipleTraversalToHTMLString(SingleAccessKeyNode rootNode, StringBuffer output,
			String lineSeparator, boolean activeLink) {

		String marging = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
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
				output.append("<span class=\"warning\">" + nodeDescriptionAnalysis(child) + "</span>");

				// displaying the child node number if it has children nodes, displaying the taxa otherwise
				if (child.getChildren().size() == 0) {
					output.append(" => <span class=\"taxa\">");
					boolean firstLoop = true;
					for (Taxon taxon : child.getRemainingTaxa()) {
						if (!firstLoop) {
							output.append(", ");
						}
						output.append(taxon.getName());
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
	private void multipleTraversalToDotString(SingleAccessKeyNode rootNode, StringBuffer output,
			String lineSeparator) {

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
				output.append(nodeDescriptionAnalysis(child));
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

	/**
	 * Helper method that traverses the SingleAccessKeyTree depth-first. It is used in multipleTraversal
	 * methods in order to generate the nodeChildParentNumberingMap HashMap
	 * 
	 * @param node
	 * @param nodeBreadthFirstIterationMap
	 * @param nodeChildParentNumberingMap
	 */
	private void recursiveDepthFirst(SingleAccessKeyNode node,
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
				state = node.getStringStates();
			state = "<span class='state'>" + state.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;")
					+ "</span>";
			state += "<span class=\"warning\">" + nodeDescriptionAnalysis(node) + "</span>";

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
				state = node.getStringStates();
			state = "<span class='state'>" + state.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;")
					+ "</span>";
			state += "<span class=\"warning\">" + nodeDescriptionAnalysis(node) + "</span>";

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
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File txtFile = File.createTempFile(Utils.KEY, "." + Utils.TXT, new File(path));
		BufferedWriter txtFileWriter = new BufferedWriter(new FileWriter(txtFile));
		txtFileWriter.append(header);
		txtFileWriter.append(toString());
		txtFileWriter.close();

		return txtFile;
	}

	/**
	 * get a DOT file containing the key
	 * 
	 * @param header
	 * @return
	 * @throws IOException
	 */
	public File toDotFile(String header) throws IOException {
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		header = header.replace(System.getProperty("line.separator"), System.getProperty("line.separator")
				+ "//");
		header = header + System.getProperty("line.separator");
		File dotFile = File.createTempFile("key_", "." + Utils.GV, new File(path));
		BufferedWriter dotFileWriter = new BufferedWriter(new FileWriter(dotFile));
		dotFileWriter.append(header);
		dotFileWriter.append("digraph " + dotFile.getName().split("\\.")[0] + " {");
		dotFileWriter.append(toDotString());
		dotFileWriter.append(System.getProperty("line.separator") + "}");
		dotFileWriter.close();

		return dotFile;
	}

	/**
	 * generates a flat representation of a key, in a String object, by calling the
	 * {@link #multipleTraversalToString} helper method
	 * 
	 * @return
	 */
	public String toFlatString() {
		StringBuffer output = new StringBuffer();
		multipleTraversalToString(root, output, System.getProperty("line.separator"));
		return output.toString();
	}

	/**
	 * generates a txt file containing the key, in a flat representation
	 * 
	 * @param header
	 *            a String that contains the header
	 * @return a txt File
	 * @throws IOException
	 */
	public File toFlatTxtFile(String header) throws IOException {
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File txtFile = File.createTempFile(Utils.KEY, "." + Utils.TXT, new File(path));
		BufferedWriter txtFileWriter = new BufferedWriter(new FileWriter(txtFile));
		txtFileWriter.append(header);
		txtFileWriter.append(toFlatString());
		txtFileWriter.close();

		return txtFile;
	}

	/**
	 * generates a DOT formatted representation of the key
	 * 
	 * @return
	 */
	public String toDotString() {
		StringBuffer output = new StringBuffer();
		multipleTraversalToDotString(root, output, System.getProperty("line.separator"));
		return output.toString();
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

		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File htmlFile = File.createTempFile(Utils.KEY, "." + Utils.HTML, new File(path));
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
		recursiveToHTMLStringForPdf(root, output, "", 0, 0);
		output.append("</ul></body></html>");

		htmlWorker.parse(new StringReader(output.toString()));

		pdfDocument.close();
		htmlWorker.close();

		return pdfFile;
	}

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
	public File toFlatPdfFile(String header) throws IOException, DocumentException {

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

		multipleTraversalToHTMLString(root, output, System.getProperty("line.separator"), false);

		output.append("</body></html>");

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
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File wikiFile = File.createTempFile(Utils.KEY, "." + Utils.WIKI, new File(path));
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

	/**
	 * Generates a File containing a flat wiki-formatted representation of the SingleAccessKeytree, in a flat
	 * representation
	 * 
	 * @param header
	 * @return
	 * @throws IOException
	 */
	public File toFlatWikiFile(String header) throws IOException {
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File wikiFile = File.createTempFile(Utils.KEY, "." + Utils.WIKI, new File(path));
		BufferedWriter wikiFlatFileWriter = new BufferedWriter(new FileWriter(wikiFile));

		wikiFlatFileWriter.append("== Info ==");
		wikiFlatFileWriter.newLine();
		wikiFlatFileWriter.append(header.replaceAll(System.getProperty("line.separator"), "<br>"));
		wikiFlatFileWriter.newLine();
		wikiFlatFileWriter.append("== Identification Key==");
		wikiFlatFileWriter.newLine();
		// wikiFlatFileWriter.append(" <nowiki>");

		wikiFlatFileWriter.append(toFlatWikiString());

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
	public String toFlatWikiString() {
		StringBuffer output = new StringBuffer();
		multipleTraversalToWikiString(root, output, System.getProperty("line.separator"));
		return output.toString();
	}

	/**
	 * Generates a File containing a flat wiki-formatted representation of the SingleAccessKeytree, this wiki
	 * representation complies with the wiki format used on species-id.net
	 * 
	 * @param header
	 * @return
	 * @throws IOException
	 */
	public File toFlatSpeciesIDQuestionAnswerWikiFile(String header) throws IOException {
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File wikiFile = File.createTempFile(Utils.KEY, "." + Utils.WIKI, new File(path));
		BufferedWriter wikiFlatFileWriter = new BufferedWriter(new FileWriter(wikiFile));

		wikiFlatFileWriter.append("== Info ==");
		wikiFlatFileWriter.newLine();
		wikiFlatFileWriter.append(header.replaceAll(System.getProperty("line.separator"), "<br>"));
		wikiFlatFileWriter.newLine();
		wikiFlatFileWriter.append("== Identification Key==");
		wikiFlatFileWriter.newLine();

		wikiFlatFileWriter.append(toFlatSpeciesIDQuestionAnswerWikiString());

		wikiFlatFileWriter.close();

		return wikiFile;
	}

	/**
	 * Generates a File containing a flat wiki-formatted representation of the SingleAccessKeytree, this wiki
	 * representation complies with the wiki format used on species-id.net
	 * 
	 * @param header
	 * @return
	 * @throws IOException
	 */
	public File toFlatSpeciesIDStatementWikiFile(String header) throws IOException {
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File wikiFile = File.createTempFile(Utils.KEY, "." + Utils.WIKI, new File(path));
		BufferedWriter wikiFlatFileWriter = new BufferedWriter(new FileWriter(wikiFile));

		wikiFlatFileWriter.append("== Info ==");
		wikiFlatFileWriter.newLine();
		wikiFlatFileWriter.append(header.replaceAll(System.getProperty("line.separator"), "<br>"));
		wikiFlatFileWriter.newLine();
		wikiFlatFileWriter.append("== Identification Key==");
		wikiFlatFileWriter.newLine();

		wikiFlatFileWriter.append(toFlatSpeciesIDStatementWikiString());

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
	public String toFlatSpeciesIDQuestionAnswerWikiString() {

		StringBuffer output = new StringBuffer();
		multipleTraversalToSpeciesIDQuestionAnswerWikiString(root, output,
				System.getProperty("line.separator"));
		return output.toString();
	}

	/**
	 * generates a flat, wiki-formatted, String representation of a key that complies with the wiki format
	 * used on <a href="http://species-id.net">species-id.net</a>, in a String object, by calling the
	 * {@link #multipleTraversalToString} helper method
	 * 
	 * @return
	 */
	public String toFlatSpeciesIDStatementWikiString() {

		StringBuffer output = new StringBuffer();
		multipleTraversalToSpeciesIDStatementWikiString(root, output, System.getProperty("line.separator"));
		return output.toString();
	}

	/**
	 * get a HTML file containing the key, in a flat representation
	 * 
	 * @param String
	 *            , header information
	 * @return File, the html file
	 * @throws IOException
	 */
	public File toFlatHtmlFile(String header) throws IOException {

		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File htmlFile = File.createTempFile(Utils.KEY, "." + Utils.HTML, new File(path));
		BufferedWriter htmlFileWriter = new BufferedWriter(new FileWriter(htmlFile));
		htmlFileWriter.append(toFlatHtmlString(header));
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
	public String toFlatHtmlString(String header) {
		StringBuffer output = new StringBuffer();
		String lineSep = System.getProperty("line.separator");
		StringBuffer slk = new StringBuffer();
		slk.append("<html>" + lineSep);
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

		multipleTraversalToHTMLString(root, output, System.getProperty("line.separator"), true);

		slk.append(output.toString());

		slk.append("</div>" + lineSep);

		slk.append("</body>");
		slk.append("</html>");

		return slk.toString();
	}

	/**
	 * Analyses the node description and returns it if it isnot an empty string, and if the verbose level
	 * requires it to be displayed. Returns an empty String otherwise.
	 * 
	 * @param node
	 * @return
	 */
	public String nodeDescriptionAnalysis(SingleAccessKeyNode node) {
		if (node.getNodeDescription() != null && node.getNodeDescription().trim().length() > 0
				&& Utils.verbosity.contains(Utils.WARNINGTAG)) {
			return " (" + node.getNodeDescription() + ")";
		}
		return "";
	}

}
