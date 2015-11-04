package fr.lis.ikeyplus.IO;

import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.model.QuantitativeCharacter;
import fr.lis.ikeyplus.model.QuantitativeMeasure;
import fr.lis.ikeyplus.model.SingleAccessKeyNode;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.model.State;
import fr.lis.ikeyplus.model.Taxon;
import fr.lis.ikeyplus.utils.IkeyConfig;
import fr.lis.ikeyplus.utils.IkeyUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
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

/**
 * This static class generates all outputs format of any SingleAccessKeyTree object
 *
 * @author Thomas Burguiere
 */
public abstract class SingleAccessKeyTreeDumper {

    // SDD DUMP

    public static File dumpSddFile(SingleAccessKeyTree tree2dump) throws IOException {
        String path = IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.prefix")
                + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder");

        File sddFile = File.createTempFile(IkeyUtils.KEY, "." + IkeyConfig.OutputFormat.SDD, new File(path));

        FileOutputStream fileOutputStream = new FileOutputStream(sddFile);
        fileOutputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        BufferedWriter sddFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
        sddFileWriter.append(generateSddString(tree2dump));
        sddFileWriter.close();

        return sddFile;
    }

    private static String generateSddString(SingleAccessKeyTree tree2dump) {
        StringBuffer output = new StringBuffer();
        String lineSeparator = System.getProperty("line.separator");

        output.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(lineSeparator);
        output.append("<Datasets xmlns=\"http://rs.tdwg.org/UBIF/2006/\" ");
        output.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + "xsi:schemaLocation=\"http://rs.tdwg.org/UBIF/2006/ " + "http://rs.tdwg.org/UBIF/2006/Schema/1.1/SDD.xsd\">").append(lineSeparator);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String generationDate = dateFormat.format(new Date());
        output.append("<TechnicalMetadata created=\"").append(generationDate).append("\">").append(lineSeparator);
        output.append("<Generator name=\"Identification Key generation WebService\" ");
        output.append("notes=\"This software is developed and distributed by LIS -" + " Laboratoire Informatique et Systématique (LIS) -" + " Université Pierre et Marie Curie - Paris VI - within the ViBRANT project\" version=\"1.1\"/>").append(lineSeparator);
        output.append("</TechnicalMetadata>").append(lineSeparator);
        output.append("<Dataset xml:lang=\"en\">").append(lineSeparator);
        output.append("<Representation>").append(lineSeparator);
        output.append("<Label>Identification key</Label>").append(lineSeparator);
        output.append("</Representation>").append(lineSeparator);

        DataSet originalDataSet = tree2dump.getDataSet();

        output.append("<TaxonNames>").append(lineSeparator);
        int taxonIDint = 1;
        String taxonID;
        for (Taxon t : originalDataSet.getTaxa()) {
            taxonID = "t" + taxonIDint;
            taxonIDint++;
            t.setId(taxonID);
            output.append("<TaxonName id=\"").append(taxonID).append("\">").append(lineSeparator);
            output.append("<Representation>").append(lineSeparator);
            output.append("<Label>").append(t.getName().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")).append("</Label>").append(lineSeparator);
            for (String mediaObjectKey : t.getMediaObjectKeys()) {
                output.append("<MediaObject ref=\"").append(mediaObjectKey).append("\"/>").append(lineSeparator);
            }
            output.append("</Representation>").append(lineSeparator);
            output.append("</TaxonName>").append(lineSeparator);
        }
        output.append("</TaxonNames>").append(lineSeparator);

        output.append("<IdentificationKeys>").append(lineSeparator);
        multipleTraversalToSddString(tree2dump.getRoot(), output, lineSeparator, tree2dump);
        output.append("</IdentificationKeys>").append(lineSeparator);

        if (originalDataSet.getMediaObjects().keySet().size() > 0) {
            output.append("<MediaObjects>").append(lineSeparator);

            // creation of mediaObjects
            for (String mediaObjectKey : originalDataSet.getMediaObjects().keySet()) {
                output.append("<MediaObject id=\"").append(mediaObjectKey).append("\">").append(lineSeparator);
                output.append("<Representation>").append(lineSeparator);
                output.append("<Label>");
                output.append(mediaObjectKey);
                output.append("</Label>").append(lineSeparator);
                output.append("</Representation>").append(lineSeparator);
                output.append("<Type>Image</Type>").append(lineSeparator);
                output.append("<Source href=\"").append(originalDataSet.getMediaObject(mediaObjectKey)).append("\"/>").append(lineSeparator);
                output.append("</MediaObject>").append(lineSeparator);
            }

            output.append("</MediaObjects>").append(lineSeparator);
        }
        output.append("</Dataset>").append(lineSeparator);
        output.append("</Datasets>");
        return output.toString();
    }

    /**
     * This methods outputs the {@link SingleAccessKeyTree} as a flat SDD-formatted String that complies with
     * the SDD format . In order to do this, the <tt>SingleAccesKeyTree</tt> is traversed 3 times. The first
     * traversal is a breadth-first traversal, in order to generate an HashMap (
     * <tt>nodeBreadthFirstIterationMap</tt>) that associates each node with an arbitrary Integer. The second
     * traversal is a depth-first traversal, in order to associate (in another HashMap :
     * <tt>nodeChildParentNumberingMap</tt>), for each node, the node number and the number of its parent
     * node. Finally, the last traversal is another breadh-first traversal that generates the flat key String
     */
    private static void multipleTraversalToSddString(SingleAccessKeyNode rootNode, StringBuffer output,
                                                     String lineSeparator, SingleAccessKeyTree tree2dump) {

        // // FIRST TRAVERSAL, breadth-first ////
        HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap = new HashMap<>();
        int counter = 1;
        iterativeBreadthFirst(rootNode, nodeBreadthFirstIterationMap, counter);
        // // END FIRST TRAVERSAL, breadth-first ////

        // // SECOND TRAVERSAL, depth-first ////
        HashMap<Integer, Integer> nodeChildParentNumberingMap = new HashMap<>();
        List<Integer> rootNodeChildrenIntegerList = new ArrayList<>();
        recursiveDepthFirstIntegerIndex(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap,
                rootNodeChildrenIntegerList);
        // // END SECOND TRAVERSAL, depth-first ////

        // // THIRD TRAVERSAL, breadth-first ////
        Queue<SingleAccessKeyNode> queue = new LinkedList<>();
        ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<>();
        counter = 1;
        int currentParentNumber = -1;
        queue.add(rootNode);

        counter++;
        // end root node treatment
        visitedNodes.add(rootNode);
        output.append("<IdentificationKey>").append(lineSeparator);
        output.append("<Representation>").append(lineSeparator);
        output.append("<Label>").append(tree2dump.getLabel()).append("</Label>").append(lineSeparator);
        output.append("</Representation>").append(lineSeparator);

        StringBuilder mediaObjectsTags = new StringBuilder();
        while (!queue.isEmpty()) {
            SingleAccessKeyNode node = queue.remove();
            SingleAccessKeyNode child;

            while (IkeyUtils.exclusion(node.getChildren(), visitedNodes).size() > 0
                    && (child = (SingleAccessKeyNode) IkeyUtils.exclusion(node.getChildren(), visitedNodes)
                    .get(0)) != null) {
                visitedNodes.add(child);

                // / child node treatment

                if (nodeChildParentNumberingMap.get(Integer.valueOf(counter)) != currentParentNumber) {
                    currentParentNumber = nodeChildParentNumberingMap.get(Integer.valueOf(counter));
                }

                if (counter == 2) {// first child node of the root node
                    output.append("<Question>").append(lineSeparator);
                    output.append("<Text>").append(escapeHTMLSpecialCharacters(child.getCharacter().getName())).append("</Text>").append(lineSeparator);
                    output.append("</Question>").append(lineSeparator);
                    output.append("<Leads>").append(lineSeparator);
                }

                // initiate the mediaObject Tags
                mediaObjectsTags.setLength(0);
                if (child.getCharacter().isSupportsCategoricalData()) {
                    for (String mediaObjectKey : ((State) child.getCharacterState()).getMediaObjectKeys()) {
                        mediaObjectsTags.append("<MediaObject ref=\"").append(mediaObjectKey).append("\"/>").append(lineSeparator);
                    }
                }

                // other child nodes of the root node
                if (rootNodeChildrenIntegerList.contains(Integer.valueOf(counter))) {
                    if (child.hasChild()) {
                        output.append("<Lead id=\"lead").append(counter - 1).append("\">").append(lineSeparator);
                        output.append("<Statement>").append(child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")
                                .replace("&", "&amp;"));
                        output.append("</Statement>").append(lineSeparator);
                        output.append(mediaObjectsTags.toString());
                        output.append("<Question>").append(lineSeparator);
                        output.append("<Text>").append(child.getChildren().get(0).getCharacter().getName().replace(">", "&gt;")
                                .replace("<", "&lt;").replace("&", "&amp;")).append("</Text>").append(lineSeparator);
                        output.append("</Question>").append(lineSeparator);
                        output.append("</Lead>").append(lineSeparator);
                    } else {

                        // output.append("<Lead>" + lineSeparator);
                        // output.append("<Statement>"
                        // + child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")
                        // .replace("&", "&amp;") + lineSeparator);
                        // output.append("</Statement>");
                        // output.append(mediaObjectsTags);
                        // for (Taxon t : child.getRemainingTaxa()) {
                        // output.append("<TaxonName ref=\"" + t.getId() + "\"/>" + lineSeparator);
                        // break;
                        // /* taxonCounter++; output.append("<Label>");
                        // * output.append(t.getName().replace(">", "&gt;").replace("<", "&lt;")
                        // * .replace("&", "&amp;")); output.append("</Label>" + lineSeparator);
                        // * output.append("</TaxonName>" + lineSeparator); */
                        // }
                        // output.append("</Lead>" + lineSeparator);

                        output.append("<Lead id=\"nil0\">").append(lineSeparator);
                        output.append("<Statement>nil</Statement>").append(lineSeparator);
                        output.append(mediaObjectsTags.toString());
                        output.append("</Lead>").append(lineSeparator);

                        for (Taxon t : child.getRemainingTaxa()) {
                            output.append("<Lead>").append(lineSeparator);
                            output.append("<Parent ref=\"nil0\" />").append(lineSeparator);
                            output.append("<Statement>").append(child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")
                                    .replace("&", "&amp;"));
                            output.append("</Statement>").append(lineSeparator);
                            output.append("<TaxonName ref=\"").append(t.getId()).append("\"/>").append(lineSeparator);
                            output.append("</Lead>").append(lineSeparator);
                        }

                    }
                } else {
                    if (child.hasChild()) {
                        output.append("<Lead id=\"lead").append(counter - 1).append("\">").append(lineSeparator);
                        output.append("<Parent ref=\"lead").append(currentParentNumber - 1).append("\"/>").append(lineSeparator);
                        output.append("<Statement>").append(child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")
                                .replace("&", "&amp;"));
                        output.append("</Statement>").append(lineSeparator);
                        output.append(mediaObjectsTags.toString());
                        output.append("<Question>").append(lineSeparator);
                        output.append("<Text>").append(child.getChildren().get(0).getCharacter().getName().replace(">", "&gt;")
                                .replace("<", "&lt;").replace("&", "&amp;")).append("</Text>").append(lineSeparator);
                        output.append("</Question>").append(lineSeparator);
                        output.append("</Lead>").append(lineSeparator);

                    } else {
                        // output.append("<Lead>" + lineSeparator);
                        // output.append("<Parent ref=\"lead" + (currentParentNumber - 1) + "\"/>"
                        // + lineSeparator);
                        // output.append("<Statement>"
                        // + child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")
                        // .replace("&", "&amp;"));
                        // output.append("</Statement>" + lineSeparator);
                        // output.append(mediaObjectsTags);
                        // for (Taxon t : child.getRemainingTaxa()) {
                        // output.append("<TaxonName ref=\"" + t.getId() + "\"/>" + lineSeparator);
                        // break;
                        // /* taxonCounter++; output.append("<Label>");
                        // * output.append(t.getName().replace(">", "&gt;").replace("<", "&lt;")
                        // * .replace("&", "&amp;")); output.append("</Label>" + lineSeparator);
                        // * output.append("</TaxonName>" + lineSeparator); */
                        // }
                        // output.append("</Lead>" + lineSeparator);

                        output.append("<Lead id=\"nil").append(counter - 1).append("\">").append(lineSeparator);
                        output.append("<Parent ref=\"lead").append(currentParentNumber - 1).append("\"/>").append(lineSeparator);
                        output.append("<Statement>nil</Statement>").append(lineSeparator);
                        output.append(mediaObjectsTags);
                        output.append("</Lead>").append(lineSeparator);

                        for (Taxon t : child.getRemainingTaxa()) {
                            output.append("<Lead>").append(lineSeparator);
                            output.append("<Parent ref=\"nil").append(counter - 1).append("\" />").append(lineSeparator);
                            output.append("<Statement>").append(child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")
                                    .replace("&", "&amp;"));
                            output.append("</Statement>").append(lineSeparator);
                            output.append("<TaxonName ref=\"").append(t.getId()).append("\"/>").append(lineSeparator);
                            output.append("</Lead>").append(lineSeparator);
                        }
                    }
                }

                queue.add(child);

                counter++;
                // / end child node treatment

            }
        }
        output.append("</Leads>").append(lineSeparator);
        output.append("</IdentificationKey>").append(lineSeparator);
        // // end third traversal, breadth-first ////

    }

    // END SDD DUMP

    // TXT DUMP, TREE

    public static File dumpTxtFile(String header, SingleAccessKeyTree tree2dump, boolean showStatistics, final String generatedFilesFolder)
            throws IOException {

        if (!new File(generatedFilesFolder).exists()) {
            new File(generatedFilesFolder).mkdirs();
        }
        File txtFile = File.createTempFile(IkeyUtils.KEY, "." + IkeyConfig.OutputFormat.TXT, new File(generatedFilesFolder));

        FileOutputStream fileOutputStream = new FileOutputStream(txtFile);
        fileOutputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        BufferedWriter txtFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));

        header = "Generated by IKey+, Laboratoire Informatique et Systematique, UMR 7205, MNHN Paris"
                + System.getProperty("line.separator") + header;

        txtFileWriter.append(header);
        txtFileWriter.append(generateTreeString(tree2dump));

        if (showStatistics) {
            tree2dump.gatherTaxonPathStatistics();
            txtFileWriter.append(outputTaxonPathStatisticsString(tree2dump));
        }
        txtFileWriter.close();

        return txtFile;
    }

    private static String generateTreeString(SingleAccessKeyTree tree2dump) {
        StringBuffer output = new StringBuffer();
        recursiveToString(tree2dump.getRoot(), output, System.getProperty("line.separator"), 0, 0, tree2dump);

        return output.toString();
    }

    /**
     * This method recursively traverses the SingleAccessKeyTree depth-first, in order to generate a character
     * string that contains a tree-oriented representation of the key
     */
    private static void recursiveToString(SingleAccessKeyNode node, StringBuffer output, String tabulations,
                                          int firstNumbering, int secondNumbering, SingleAccessKeyTree tree2dump) {

        if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {
            if (node.getCharacterState() instanceof QuantitativeMeasure) {
                output.append(tabulations).append(firstNumbering).append(".").append(secondNumbering).append(") ").append(node.getCharacter().getName()).append(" | ").append(((QuantitativeMeasure) node.getCharacterState())
                        .toStringInterval(((QuantitativeCharacter) node.getCharacter())
                                .getMeasurementUnit()));
            } else {
                output.append(tabulations).append(firstNumbering).append(".").append(secondNumbering).append(") ").append(node.getCharacter().getName()).append(" | ").append(node.getStringStates());
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
                output.append(" (items=").append(node.getRemainingTaxa().size()).append(")");
            }
            tabulations = tabulations + "\t";
        }
        firstNumbering++;
        secondNumbering = 0;
        if (node != null) {
            for (SingleAccessKeyNode childNode : node.getChildren()) {
                secondNumbering++;
                recursiveToString(childNode, output, tabulations, firstNumbering, secondNumbering, tree2dump);
            }
        }
    }

    // END TXT DUMP, TREE

    // TXT DUMP, FLAT

    public static File dumpFlatTxtFile(String header, SingleAccessKeyTree tree2dump, boolean showStatistics, final String generatedFilesFolder)
            throws IOException {

        File txtFile = File.createTempFile(IkeyUtils.KEY, "." + IkeyConfig.OutputFormat.TXT, new File(generatedFilesFolder));

        FileOutputStream fileOutputStream = new FileOutputStream(txtFile);
        fileOutputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        BufferedWriter txtFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));

        header = "Generated by IKey+, Laboratoire Informatique et Systematique, UMR 7205, MNHN Paris"
                + System.getProperty("line.separator") + header;

        txtFileWriter.append(header);
        txtFileWriter.append(generateFlatString(tree2dump));

        if (showStatistics) {
            tree2dump.gatherTaxonPathStatistics();
            txtFileWriter.append(outputTaxonPathStatisticsString(tree2dump));
        }

        txtFileWriter.close();

        return txtFile;
    }

    /**
     * generates a flat representation of a key, in a String object, by calling the
     * {@link #multipleTraversalToString} helper method
     */
    private static String generateFlatString(SingleAccessKeyTree tree2dump) {
        StringBuffer output = new StringBuffer();
        multipleTraversalToString(tree2dump.getRoot(), output, System.getProperty("line.separator"),
                tree2dump);

        return output.toString();
    }

    /**
     * This methods outputs the {@link SingleAccessKeyTree} as a flat character String. In order to do this,
     * the <tt>SingleAccesKeyTree</tt> is traversed 3 times. The first traversal is a breadth-first traversal,
     * in order to generate an HashMap (<tt>nodeBreadthFirstIterationMap</tt>) that associates each node with
     * an arbitrary Integer. The second traversal is a depth-first traversal, in order to associate (in
     * another HashMap : <tt>nodeChildParentNumberingMap</tt>), for each node, the node number and the number
     * of its parent node. Finally, the last traversal is another breadh-first traversal that generates the
     * flat key String
     */
    private static void multipleTraversalToString(SingleAccessKeyNode rootNode, StringBuffer output,
                                                  String lineSeparator, SingleAccessKeyTree tree2dump) {

        // // first traversal, breadth-first ////
        HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap = new HashMap<>();
        int counter = 1;
        iterativeBreadthFirstSkipChildlessNodes(rootNode, nodeBreadthFirstIterationMap, counter);
        // // end first traversal, breadth-first ////

        // // second traversal, depth-first ////
        HashMap<SingleAccessKeyNode, Integer> nodeChildParentNumberingMap = new HashMap<>();
        recursiveDepthFirstNodeIndex(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
        // // end second traversal, depth-first ////

        // // third traversal, breadth-first ////
        Queue<SingleAccessKeyNode> queue = new LinkedList<>();
        ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<>();

        queue.clear();
        visitedNodes.clear();

        counter = 1;
        int currentParentNumber = -1;
        queue.add(rootNode);

        // root node treatment

        counter++;
        // end root node treatment
        visitedNodes.add(rootNode);

        StringBuilder blankCharacterName = new StringBuilder();
        while (!queue.isEmpty()) {
            SingleAccessKeyNode node = queue.remove();
            SingleAccessKeyNode child;

            while (IkeyUtils.exclusion(node.getChildren(), visitedNodes).size() > 0
                    && (child = (SingleAccessKeyNode) IkeyUtils.exclusion(node.getChildren(), visitedNodes)
                    .get(0)) != null
                // && child.getCharacter() != null && child.getCharacterState() != null
                    ) {
                visitedNodes.add(child);

                // / child node treatment

                // displaying the parent node number and the child node character name only once
                if (nodeChildParentNumberingMap.get(child) != currentParentNumber) {
                    currentParentNumber = nodeChildParentNumberingMap.get(child);
                    output.append(lineSeparator);
                    if (currentParentNumber < 10) {
                        output.append("   ").append(currentParentNumber);
                    } else if (currentParentNumber < 100) {
                        output.append("  ").append(currentParentNumber);
                    } else if (currentParentNumber < 1000) {
                        output.append(" ").append(currentParentNumber);
                    } else {
                        output.append(currentParentNumber);
                    }
                    output.append("  ").append(child.getCharacter().getName()).append(" = ");
                } else {
                    output.append("    ");
                    blankCharacterName .setLength(0);
                    for (int i = 0; i < child.getCharacter().getName().length(); i++) {
                        blankCharacterName.append(" ");
                    }
                    output.append("  ").append(blankCharacterName).append(" = ");
                }

                // displaying the child node character state
                if (child.getCharacterState() instanceof QuantitativeMeasure) {
                    output.append(((QuantitativeMeasure) child.getCharacterState())
                            .toStringInterval(((QuantitativeCharacter) child.getCharacter())
                                    .getMeasurementUnit()));
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
                    output.append(" -> ").append(counter);
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

    public static File dumpHtmlFile(String header, SingleAccessKeyTree tree2dump, boolean showStatistics, String generatedFilesFolder)
            throws IOException {

        File htmlFile = File.createTempFile(IkeyUtils.KEY, "." + IkeyConfig.OutputFormat.HTML, new File(generatedFilesFolder));

        FileOutputStream fileOutputStream = new FileOutputStream(htmlFile);
        fileOutputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        BufferedWriter htmlFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));

        header = "Generated by IKey+, Laboratoire Informatique et Systematique, UMR 7205, MNHN Paris"
                + header;

        htmlFileWriter.append(generateHtmlString(header, tree2dump, showStatistics));
        htmlFileWriter.close();

        return htmlFile;
    }

    private static String generateHtmlString(String header, SingleAccessKeyTree tree2dump,
                                             boolean showStatistics) throws IOException {
        String lineSep = System.getProperty("line.separator");
        StringBuilder slk = new StringBuilder();

        slk.append("<html>").append(lineSep);
        slk.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />").append(lineSep);
        slk.append("<meta name=\'generator\' content=\'Generated by IKey+\' />").append(lineSep);
        slk.append("<meta name=\'author\' content=\'Laboratoire Informatique et Systematique, UMR 7205, MNHN Paris\' />").append(lineSep);
        slk.append("<head>").append(lineSep);
        slk.append("<script src='").append(IkeyConfig.getBundleConfElement("resources.jqueryPath")).append("'></script>").append(lineSep).append("<script type='text/javascript' src='").append(IkeyConfig.getBundleConfElement("resources.treeviewJsPath")).append("'></script>").append(lineSep).append("<link rel='stylesheet' href='").append(IkeyConfig.getBundleConfElement("resources.treeviewCssPath")).append("' type='text/css' />").append(lineSep);

        slk.append("<style type='text/css'>").append(lineSep);

        InputStream cssInputStream = SingleAccessKeyTreeDumper.class.getResourceAsStream(IkeyConfig
                .getBundleConfElement("resources.CSSName"));
        if (cssInputStream != null) {
            BufferedInputStream bin = new BufferedInputStream(cssInputStream);

            // create a byte array
            byte[] contents = new byte[1024];

            int bytesRead;
            String strFileContents;

            while ((bytesRead = bin.read(contents)) != -1) {

                strFileContents = new String(contents, 0, bytesRead);
                slk.append(strFileContents);
            }
        }

        slk.append("</style>").append(lineSep);

        slk.append("<script>").append(lineSep);

        InputStream javascriptInputStream = SingleAccessKeyTreeDumper.class.getResourceAsStream(IkeyConfig
                .getBundleConfElement("resources.JSName"));
        if (javascriptInputStream != null) {
            BufferedInputStream bin = new BufferedInputStream(javascriptInputStream);

            // create a byte array
            byte[] contents = new byte[1024];

            int bytesRead;
            String strFileContents;

            while ((bytesRead = bin.read(contents)) != -1) {

                strFileContents = new String(contents, 0, bytesRead);
                slk.append(strFileContents);
            }
        }

        slk.append("</script>").append(lineSep);

        slk.append("</head>").append(lineSep);

        slk.append("<body onLoad=\'initTree();\' >").append(lineSep);
        slk.append("<div style='margin-left:30px;margin-top:20px;'>").append(lineSep);
        slk.append(header.replaceAll(System.getProperty("line.separator"), "<br/>"));

        slk.append("<div id=\"treecontrol\"><a title=\"Collapse the entire tree below\" href=\"#\" onClick=\"window.location.href=window.location.href\">Collapse All</a> | <a title=\"Expand the entire tree below\" href=\"#\">Expand All</a></div>").append(lineSep);
        // slk.append("<div><a style=\"color:#444;\" title=\"Collapse the entire tree below\" href=\"#\" onClick=\"window.location.href=window.location.href\">Collapse All</a></div><br/>"
        // + lineSep);

        slk.append("<ul id='tree'>").append(lineSep);

        StringBuilder output = new StringBuilder();

        recursiveToHTMLString(tree2dump.getRoot(), null, output, "", true, 0, 0, tree2dump);

        slk.append(output.toString());

        slk.append("</ul>").append(lineSep);
        slk.append("</div>").append(lineSep);

        if (showStatistics) {
            tree2dump.gatherTaxonPathStatistics();
            slk.append(outputTaxonPathStatisticsHTML(tree2dump));
        }

        slk.append("</body>");
        slk.append("</html>");

        return slk.toString();
    }

    /**
     * recursively traverses (depth-first) the SingleAccessKeyTree, and returns an HTML representation of this
     * SingleAccessKeyTree in an unordered list (&lt;ul&gt;)
     */
    private static void recursiveToHTMLString(SingleAccessKeyNode node, SingleAccessKeyNode parentNode,
                                              StringBuilder output, String tabulations, boolean displayCharacterName, int firstNumbering,
                                              int secondNumbering, SingleAccessKeyTree tree2dump) {
        StringBuilder characterName = new StringBuilder("");
        StringBuilder state = new StringBuilder("");
        if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {

            if (displayCharacterName) {
                String characterNameContent = node.getCharacter().getName().replaceAll("\\<", "&lt;")
                        .replaceAll("\\>", "&gt;");
                characterName.append("<span class='character'>").append(firstNumbering).append(") ").
                        append("<b>").append(characterNameContent).append("</b>").append("</span>");

                // create link to display states images
                String htmlImageLink = "";
                if (parentNode.isChildrenContainsImages(tree2dump.getDataSet())) {
                    StringBuilder javascriptStateNameTab = new StringBuilder("new Array(");
                    StringBuilder javascriptUrlImageTab = new StringBuilder("new Array(");
                    boolean firstLoop = true;
                    for (SingleAccessKeyNode childNode : parentNode.getChildren()) {
                        if (childNode.getCharacter().isSupportsCategoricalData()) {
                            if (!firstLoop) {
                                javascriptStateNameTab.append(", ");
                                javascriptUrlImageTab.append(", ");
                            }
                            javascriptStateNameTab.append("\"").append(((State) childNode.getCharacterState()).getName().replaceAll("\"", "")
                                    .replaceAll("'", " ")).append("\"");
                            javascriptUrlImageTab.append("\"").append(((State) childNode.getCharacterState()).getFirstImage(tree2dump
                                    .getDataSet())).append("\"");
                            firstLoop = false;
                        }
                    }
                    javascriptStateNameTab.append(")");
                    javascriptUrlImageTab.append(")");

                    htmlImageLink = " <a class='stateImageLink' onClick='newStateImagesWindowTree(\""
                            + node.getCharacter().getName().replaceAll("\"", "").replaceAll("'", " ")
                            + "\", " + javascriptStateNameTab + ", " + javascriptUrlImageTab
                            + ");' >(<strong>?</strong>)</a>";
                }

                output.append(tabulations).append("\t<li>").append(characterName).append(htmlImageLink).append("</li>");
            }

            if (node.getCharacterState() instanceof QuantitativeMeasure) {
                state.append(((QuantitativeMeasure) node.getCharacterState())
                        .toStringInterval(((QuantitativeCharacter) node.getCharacter()).getMeasurementUnit()));
            } else {
                state.append(node.getStringStates());
            }
            String regexed = state.toString().replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;");
            state.setLength(0);
            state.append("<span class='state'>").append(firstNumbering).append(".").append(secondNumbering).append(") ").append(regexed).append("</span>")
                    .append("<span class=\"warning\">").append(tree2dump.nodeDescriptionAnalysis(node)).append("</span>");

            output.append("\n").append(tabulations).append("\t<li>");

            if (node.hasChild()) {
                output.append("&nbsp;").append(state).append(" (items=").append(node.getRemainingTaxa().size()).append(")");
            } else {
                output.append("&nbsp;").append(state).append("<span class='taxa'> -> ");
                boolean firstLoop = true;
                for (Taxon taxon : node.getRemainingTaxa()) {
                    if (!firstLoop) {
                        output.append(", ");
                    }
                    // create image previous using the first image in the list
                    if (taxon.getFirstImage(tree2dump.getDataSet()) != null) {
                        output.append("<a href=\"").append(taxon.getFirstImage(tree2dump.getDataSet())).append("\" class=\"screenshot\" rel=\"").append(taxon.getFirstImage(tree2dump.getDataSet())).append("\" target=\"_blank\">").append(taxon.getName()).append("</a>");
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
        if (node != null) {
            for (SingleAccessKeyNode childNode : node.getChildren()) {
                secondNumbering++;
                if (firstLoop) {
                    recursiveToHTMLString(childNode, node, output, tabulations, true, firstNumbering,
                            secondNumbering, tree2dump);
                } else {
                    recursiveToHTMLString(childNode, node, output, tabulations, false, firstNumbering,
                            secondNumbering, tree2dump);
                }
                firstLoop = false;
            }
            if (node.getCharacter() != null && node.getCharacterState() != null) {

                if (node.hasChild())
                    output.append(tabulations).append("</li></ul>\n");
                else
                    output.append(tabulations).append("</li>\n");
            }
        }
    }

    // END HTML DUMP, TREE

    // HTML DUMP, FLAT

    public static File dumpFlatHtmlFile(String header, SingleAccessKeyTree tree2dump, boolean showStatistics, String generatedFilesFolder)
            throws IOException {

        File htmlFile = File.createTempFile(IkeyUtils.KEY, "." + IkeyConfig.OutputFormat.HTML, new File(generatedFilesFolder));

        FileOutputStream fileOutputStream = new FileOutputStream(htmlFile);
        fileOutputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        BufferedWriter htmlFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));

        header = "Generated by IKey+, Laboratoire Informatique et Systematique, UMR 7205, MNHN Paris"
                + System.getProperty("line.separator") + header;

        htmlFileWriter.append(generateFlatHtmlString(header, tree2dump, showStatistics));
        htmlFileWriter.close();

        return htmlFile;
    }

    /**
     * generates an HTML file that contains the key, in a flat, interactive representation (i.e. only one node
     * of the key is displayed, and the end-user can navigate through each node of the key), by calling
     * {@link #generateInteractiveHtmlString}
     */
    public static File dumpInteractiveHtmlFile(String header, SingleAccessKeyTree tree2dump,
                                               boolean showStatistics, String generatedFilesFolder) throws IOException {

        File htmlFile = File.createTempFile(IkeyUtils.KEY, "." + IkeyConfig.OutputFormat.HTML, new File(generatedFilesFolder));
        FileOutputStream fileOutputStream = new FileOutputStream(htmlFile);
        fileOutputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        BufferedWriter htmlFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
        htmlFileWriter.append(generateInteractiveHtmlString(header, tree2dump, showStatistics));
        htmlFileWriter.close();

        return htmlFile;

    }

    private static String generateFlatHtmlString(String header, SingleAccessKeyTree tree2dump,
                                                 boolean showStatistics) throws IOException {

        StringBuffer output = new StringBuffer();
        String lineSep = System.getProperty("line.separator");
        StringBuilder slk = new StringBuilder();
        slk.append("<html>").append(lineSep);
        slk.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />").append(lineSep);
        slk.append("<meta name=\'generator\' content=\'Generated by IKey+\' />").append(lineSep);
        slk.append("<meta name=\'author\' content=\'Laboratoire Informatique et Systematique, UMR 7205, MNHN Paris\' />").append(lineSep);
        slk.append("<head>").append(lineSep);
        slk.append("<script src='").append(IkeyConfig.getBundleConfElement("resources.jqueryPath")).append("'></script>").append(lineSep);

        slk.append("<style type='text/css'>").append(lineSep);

        InputStream cssInputStream = SingleAccessKeyTreeDumper.class.getResourceAsStream(IkeyConfig
                .getBundleConfElement("resources.CSSName"));
        if (cssInputStream != null) {
            BufferedInputStream bin = new BufferedInputStream(cssInputStream);

            // create a byte array
            byte[] contents = new byte[1024];

            int bytesRead;
            String strFileContents;

            while ((bytesRead = bin.read(contents)) != -1) {

                strFileContents = new String(contents, 0, bytesRead);
                slk.append(strFileContents);
            }
        }

        slk.append("</style>").append(lineSep);

        slk.append("<script>").append(lineSep);

        InputStream javascriptInputStream = SingleAccessKeyTreeDumper.class.getResourceAsStream(IkeyConfig
                .getBundleConfElement("resources.JSName"));
        if (javascriptInputStream != null) {
            BufferedInputStream bin = new BufferedInputStream(javascriptInputStream);

            // create a byte array
            byte[] contents = new byte[1024];

            int bytesRead;
            String strFileContents;

            while ((bytesRead = bin.read(contents)) != -1) {

                strFileContents = new String(contents, 0, bytesRead);
                slk.append(strFileContents);
            }
        }

        slk.append("</script>").append(lineSep);

        slk.append("</head>").append(lineSep);

        slk.append("<body>").append(lineSep);
        slk.append("<div style='margin-left:30px;margin-top:20px;'>").append(lineSep);
        slk.append(header.replaceAll(System.getProperty("line.separator"), "<br/>"));

        multipleTraversalToHTMLString(tree2dump.getRoot(), output, System.getProperty("line.separator"),
                true, tree2dump);

        slk.append(output.toString());

        slk.append("</div>").append(lineSep);

        if (showStatistics) {
            tree2dump.gatherTaxonPathStatistics();
            slk.append(outputTaxonPathStatisticsHTML(tree2dump));
        }

        slk.append("</body>");
        slk.append("</html>");

        return slk.toString();
    }

    private static String generateInteractiveHtmlString(String header, SingleAccessKeyTree tree2dump,
                                                        boolean showStatistics) throws IOException {

        StringBuffer output = new StringBuffer();
        String lineSep = System.getProperty("line.separator");
        StringBuilder slk = new StringBuilder();
        slk.append("<html>").append(lineSep);
        slk.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />").append(lineSep);
        slk.append("<script type=\"text/javascript\">").append(lineSep);
        slk.append("if(screen.width<640 && screen.height < 500) {").append(lineSep);
        slk.append("  document.write('<meta name = \"viewport\" content = \"width = 250\">') ;").append(lineSep);
        slk.append("}").append(lineSep);
        slk.append("</script>");

        slk.append("<head>").append(lineSep);
        slk.append("<script src='").append(IkeyConfig.getBundleConfElement("resources.jqueryPath")).append("'></script>").append(lineSep);

        slk.append("<style type='text/css'>").append(lineSep);

        InputStream cssInputStream = SingleAccessKeyTreeDumper.class.getResourceAsStream(IkeyConfig
                .getBundleConfElement("resources.CSSName"));
        if (cssInputStream != null) {
            BufferedInputStream bin = new BufferedInputStream(cssInputStream);

            // create a byte array
            byte[] contents = new byte[1024];

            int bytesRead;
            String strFileContents;

            while ((bytesRead = bin.read(contents)) != -1) {

                strFileContents = new String(contents, 0, bytesRead);
                slk.append(strFileContents);
            }
        }

        slk.append("</style>").append(lineSep);

        slk.append("<script>").append(lineSep);

        InputStream javascriptInputStream = SingleAccessKeyTreeDumper.class.getResourceAsStream(IkeyConfig
                .getBundleConfElement("resources.JSName"));
        if (javascriptInputStream != null) {
            BufferedInputStream bin = new BufferedInputStream(javascriptInputStream);

            // create a byte array
            byte[] contents = new byte[1024];

            int bytesRead;
            String strFileContents;

            while ((bytesRead = bin.read(contents)) != -1) {

                strFileContents = new String(contents, 0, bytesRead);
                slk.append(strFileContents);
            }
        }

        slk.append("</script>").append(lineSep);

        slk.append("</head>").append(lineSep);

        slk.append("<body onLoad=\'initViewNodes();\'>").append(lineSep);

        slk.append("<div id=\"keyWait\" style='margin-left:30px;margin-top:20px;' >").append(lineSep);
        slk.append("Generating Key, please wait...");
        slk.append("</div>").append(lineSep);

        slk.append("<div id=\"keyBody\" style='visibility: hidden; margin-left:30px;margin-top:20px;'>").append(lineSep);
        slk.append(header.replaceAll(System.getProperty("line.separator"), "<br/>"));

        slk.append("<input type=\'button\' value=\'Previous Step\' onClick=\'goToPreviousViewNode();\' />");
        slk.append("<input type=\'button\' value=\'RESET\' onClick=\'goToFirstViewNode();\' />");

        slk.append("<br/>");

        multipleTraversalToInteractiveHTMLString(tree2dump.getRoot(), output,
                System.getProperty("line.separator"), true, tree2dump);

        slk.append(output.toString());

        slk.append("</div>").append(lineSep);

        if (showStatistics) {
            tree2dump.gatherTaxonPathStatistics();
            slk.append(outputTaxonPathStatisticsHTML(tree2dump));
        }
        slk.append("</body>");
        slk.append("</html>");

        return slk.toString();
    }

    /**
     * This methods outputs the {@link SingleAccessKeyTree} as a flat HTML-formatted String. In order to do
     * this, the <tt>SingleAccesKeyTree</tt> is traversed 3 times. The first traversal is a breadth-first
     * traversal, in order to generate an HashMap ( <tt>nodeBreadthFirstIterationMap</tt>) that associates
     * each node with an arbitrary Integer. The second traversal is a depth-first traversal, in order to
     * associate (in another HashMap : <tt>nodeChildParentNumberingMap</tt>), for each node, the node number
     * and the number of its parent node. Finally, the last traversal is another breadh-first traversal that
     * generates the flat key String
     */
    private static void multipleTraversalToHTMLString(SingleAccessKeyNode rootNode, StringBuffer output,
                                                      String lineSeparator, boolean activeLink, SingleAccessKeyTree tree2dump) {

        String marging = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp"
                + ";&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

        // // first traversal, breadth-first ////
        HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap = new HashMap<>();

        int counter = 1;
        iterativeBreadthFirstSkipChildlessNodes(rootNode, nodeBreadthFirstIterationMap, counter);

        // // end first traversal, breadth-first ////

        // // second traversal, depth-first ////
        HashMap<SingleAccessKeyNode, Integer> nodeChildParentNumberingMap = new HashMap<>();
        recursiveDepthFirstNodeIndex(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
        // // end second traversal, depth-first ////

        // // third traversal, breadth-first ////
        Queue<SingleAccessKeyNode> queue = new LinkedList<>();
        ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<>();

        counter = 1;
        int currentParentNumber = -1;
        queue.add(rootNode);

        // root node treatment

        counter++;
        // end root node treatment
        visitedNodes.add(rootNode);

        while (!queue.isEmpty()) {
            SingleAccessKeyNode node = queue.remove();
            SingleAccessKeyNode child;

            while (IkeyUtils.exclusion(node.getChildren(), visitedNodes).size() > 0
                    && (child = (SingleAccessKeyNode) IkeyUtils.exclusion(node.getChildren(), visitedNodes)
                    .get(0)) != null
                // && child.getCharacter() != null && child.getCharacterState() != null
                    ) {
                visitedNodes.add(child);

                // / child node treatment

                // displaying the parent node number and the child node character name only once
                if (nodeChildParentNumberingMap.get(child) != currentParentNumber) {
                    currentParentNumber = nodeChildParentNumberingMap.get(child);
                    output.append("<br/>").append(lineSeparator);
                    if (currentParentNumber < 10)
                        output.append("   ");
                    else if (currentParentNumber < 100)
                        output.append("  ");
                    else if (currentParentNumber < 1000)
                        output.append(" ");

                    // close the previous opening <span class="viewNode"> if this is not the first one
                    if (currentParentNumber > 1)
                        output.append(lineSeparator).append("</span>");
                    output.append("<span class=\"viewNode\" id=\"viewNode").append(currentParentNumber).append("\">");

                    if (activeLink) {
                        output.append("<a name=\"anchor").append(currentParentNumber).append("\"></a>");
                    }
                    output.append("<strong>").append(currentParentNumber).append("</strong>");

                    String htmlImageLink = "";
                    if (node.isChildrenContainsImages(tree2dump.getDataSet())) {
                        htmlImageLink = "<a class='stateImageLink' onClick='newStateImagesWindow("
                                + currentParentNumber + ");' >(<strong>?</strong>)</a>";
                    }
                    output.append("  <span class=\"character\">").append(child.getCharacter().getName().replace(">", "&gt;").replace("<", "&lt;")).append(" </span>").append(htmlImageLink).append(":<br/>");

                } else {
                    output.append("    ");
                    String blankCharacterName = "";
                    for (int i = 0; i < child.getCharacter().getName().length(); i++)
                        blankCharacterName += " ";
                    output.append("  ").append(blankCharacterName);
                }
                output.append("<span class=\"statesAndTaxa\">");

                String mediaKey = "";
                // displaying the child node character state
                if (child.getCharacterState() instanceof QuantitativeMeasure) {
                    output.append("<span class=\"state\"" + "\">").append(marging).append(((QuantitativeMeasure) child.getCharacterState())
                            .toStringInterval(((QuantitativeCharacter) child.getCharacter())
                                    .getMeasurementUnit())).append("</span>");
                } else {
                    mediaKey = ((State) child.getCharacterState()).getFirstImageKey();
                    output.append("<span class=\"state\" id=\"state_").append(mediaKey).append("\" >").append(marging).append(child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")).append("</span>");

                }
                output.append("<span class=\"warning\">").append(tree2dump.nodeDescriptionAnalysis(child)).append("</span>");

                // displaying the child node number if it has children nodes, displaying the taxa otherwise
                if (child.getChildren().size() == 0) {
                    output.append(" =&gt; <span class=\"taxa\">");
                    boolean firstLoop = true;
                    for (Taxon taxon : child.getRemainingTaxa()) {
                        if (!firstLoop) {
                            output.append(", ");
                        }
                        // create image previous using the first image in the list
                        if (taxon.getFirstImage(tree2dump.getDataSet()) != null) {
                            output.append("<a href=\"").append(taxon.getFirstImage(tree2dump.getDataSet())).append("\" class=\"screenshot\" rel=\"").append(taxon.getFirstImage(tree2dump.getDataSet())).append("\" target=\"_blank\">").append(taxon.getName()).append("</a>");
                        } else {
                            output.append(taxon.getName());
                        }
                        firstLoop = false;
                    }
                    output.append("</span>");
                } else {
                    if (activeLink) {
                        output.append(" =&gt; <a href=\"#anchor").append(counter).append("\">").append(counter).append("</a>");
                    } else {
                        output.append(" =&gt; ").append(counter);
                    }

                }
                output.append("</span>"); // closes the opening <span class="statesAndTaxa">
                if (child.getCharacter().isSupportsCategoricalData()) {
                    output.append("<span class=\"stateImageURL\" id=\"stateImageURL_").append(mediaKey).append("\">");
                    output.append(((State) child.getCharacterState()).getFirstImage(tree2dump.getDataSet()) != null ? ((State) child
                            .getCharacterState()).getFirstImage(tree2dump.getDataSet()) : "");
                    output.append("</span>");
                }
                output.append("<br/>").append(lineSeparator);

                queue.add(child);
                if (child.hasChild())
                    counter++;
                // / end child node treatment

            }
        }

        // // end third traversal, breadth-first ////

    }

    /**
     * This methods outputs the {@link SingleAccessKeyTree} as a flat, interactive HTML-formatted String. In
     * order to do this, the <tt>SingleAccesKeyTree</tt> is traversed 3 times. The first traversal is a
     * breadth-first traversal, in order to generate an HashMap ( <tt>nodeBreadthFirstIterationMap</tt>) that
     * associates each node with an arbitrary Integer. The second traversal is a depth-first traversal, in
     * order to associate (in another HashMap : <tt>nodeChildParentNumberingMap</tt>), for each node, the node
     * number and the number of its parent node. Finally, the last traversal is another breadh-first traversal
     * that generates the flat key String
     */
    private static void multipleTraversalToInteractiveHTMLString(SingleAccessKeyNode rootNode,
                                                                 StringBuffer output, String lineSeparator, boolean activeLink, SingleAccessKeyTree tree2dump) {

        String marging = "<br/>&nbsp;&nbsp;&nbsp;";

        // // first traversal, breadth-first ////
        HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap = new HashMap<>();

        int counter = 1;
        iterativeBreadthFirstSkipChildlessNodes(rootNode, nodeBreadthFirstIterationMap, counter);

        // // end first traversal, breadth-first ////

        // // second traversal, depth-first ////
        HashMap<SingleAccessKeyNode, Integer> nodeChildParentNumberingMap = new HashMap<>();
        recursiveDepthFirstNodeIndex(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
        // // end second traversal, depth-first ////

        // // third traversal, breadth-first ////
        Queue<SingleAccessKeyNode> queue = new LinkedList<>();
        ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<>();

        counter = 1;
        int currentParentNumber = -1;
        queue.add(rootNode);

        // root node treatment

        counter++;
        // end root node treatment
        visitedNodes.add(rootNode);

        while (!queue.isEmpty()) {
            SingleAccessKeyNode node = queue.remove();
            SingleAccessKeyNode child;

            while (IkeyUtils.exclusion(node.getChildren(), visitedNodes).size() > 0
                    && (child = (SingleAccessKeyNode) IkeyUtils.exclusion(node.getChildren(), visitedNodes)
                    .get(0)) != null
                // && child.getCharacter() != null && child.getCharacterState() != null
                    ) {
                visitedNodes.add(child);

                // / child node treatment

                // displaying the parent node number and the child node character name only once
                if (nodeChildParentNumberingMap.get(child) != currentParentNumber) {
                    currentParentNumber = nodeChildParentNumberingMap.get(child);
                    output.append("<br/>").append(lineSeparator);
                    if (currentParentNumber < 10) {
                        output.append("   ");
                    } else if (currentParentNumber < 100) {
                        output.append("  ");
                    } else if (currentParentNumber < 1000) {
                        output.append(" ");
                    }

                    // close the previous opening <span class="viewNode"> if this is not the first one
                    if (currentParentNumber > 1)
                        output.append(lineSeparator).append("</span>");
                    output.append("<span class=\"viewNode\" id=\"viewNode").append(currentParentNumber).append("\">");

                    if (activeLink) {
                        output.append("<a name=\"anchor").append(currentParentNumber).append("\"></a>");
                    }
                    output.append("<strong>").append(currentParentNumber).append("</strong>");

                    String htmlImageLink = "";
                    // if (node.isChildrenContainsImages(tree2dump.getDataSet())) {
                    // htmlImageLink = "<a class='stateImageLink' onClick='newStateImagesWindow("
                    // + currentParentNumber + ");' >(<strong>?</strong>)</a>";
                    // }
                    output.append("  <span class=\"character\">").append(child.getCharacter().getName().replace(">", "&gt;").replace("<", "&lt;")).append(" </span>").append(htmlImageLink).append(":<br/>");

                } else {
                    output.append("    ");
                    String blankCharacterName = "";
                    for (int i = 0; i < child.getCharacter().getName().length(); i++)
                        blankCharacterName += " ";
                    output.append("  ").append(blankCharacterName);
                }
                output.append("<span class=\"statesAndTaxa\">");

                String mediaKey = "";
                // displaying the child node character state
                if (child.getCharacterState() instanceof QuantitativeMeasure) {
                    output.append("<span class=\"state\"" + "\">").append(marging).append(((QuantitativeMeasure) child.getCharacterState())
                            .toStringInterval(((QuantitativeCharacter) child.getCharacter())
                                    .getMeasurementUnit())).append("</span>");
                } else {
                    mediaKey = ((State) child.getCharacterState()).getFirstImageKey();
                    output.append("<span class=\"state\" id=\"state_").append(mediaKey).append("\" >").append(marging).append(child.getStringStates().replace(">", "&gt;").replace("<", "&lt;")).append("</span>");

                }
                output.append("<span class=\"warning\">").append(tree2dump.nodeDescriptionAnalysis(child)).append("</span>");

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
                            output.append("<a href=\"").append(taxon.getFirstImage(tree2dump.getDataSet())).append("\" class=\"screenshot\" rel=\"").append(taxon.getFirstImage(tree2dump.getDataSet())).append("\" target=\"_blank\">").append(taxon.getName()).append("</a>");
                        } else {
                            output.append(taxon.getName());
                        }
                        firstLoop = false;
                    }
                    output.append("</span>");
                } else {
                    if (activeLink) {
                        output.append(" => <input class=\"nextNodeButton\" type=\"button\" value=\"next step\" onClick=\'goToViewNode(").append(counter).append(")\' />");
                    } else {
                        output.append(" => ").append(counter);
                    }

                }
                output.append("</span>"); // closes the opening <span class="statesAndTaxa">
                if (child.getCharacter().isSupportsCategoricalData()) {
                    output.append("<br/><span class=\"stateImageURLandContainer\" id=\"stateImageURLandContainer").append(counter).append("\" >");
                    output.append("<span class=\"stateImageURL\" id=\"stateImageURL_").append(mediaKey).append("\">");
                    output.append(((State) child.getCharacterState()).getFirstImage(tree2dump.getDataSet()) != null ? ((State) child
                            .getCharacterState()).getFirstImage(tree2dump.getDataSet()) : "");
                    output.append("</span>");
                    output.append("<br/><span class=\"stateImageContainer\" id=\"stateImageContainer").append(counter).append("\" ></span>").append(lineSeparator);
                    output.append("</span>"); // closes the opening <span class="stateImageURLandContainer">
                }
                output.append("<br/>").append(lineSeparator);

                queue.add(child);
                if (child.hasChild())
                    counter++;
                // / end child node treatment

            }
        }

        // // end third traversal, breadth-first ////

    }


    // WIKI DUMP, TREE

    public static File dumpWikiFile(String header, SingleAccessKeyTree tree2dump, boolean showStatistics, final String generatedFilesFolder)
            throws IOException {

        File wikiFile = File.createTempFile(IkeyUtils.KEY, "." + IkeyConfig.OutputFormat.WIKI, new File(generatedFilesFolder));

        FileOutputStream fileOutputStream = new FileOutputStream(wikiFile);
        fileOutputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        BufferedWriter wikiFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));

        if (header != null && !header.equals("")) {
            wikiFileWriter.append("== Info ==");
            wikiFileWriter.newLine();
            wikiFileWriter.append(header.replaceAll(System.getProperty("line.separator"), "<br>"));
            wikiFileWriter.newLine();
        }
        wikiFileWriter.append("== Identification Key==");
        wikiFileWriter.newLine();

        wikiFileWriter.append(generateTreeWiki(tree2dump, showStatistics));

        wikiFileWriter.close();

        return wikiFile;
    }

    private static String generateTreeWiki(SingleAccessKeyTree tree2dump, boolean showStatistics) {
        StringBuffer output = new StringBuffer();
        recursiveToWiki(tree2dump.getRoot(), output, "", 0, 0, tree2dump);
        if (showStatistics) {
            tree2dump.gatherTaxonPathStatistics();
            output.append(outputTaxonPathStatisticsWiki(tree2dump));
        }
        return output.toString();
    }

    private static void recursiveToWiki(SingleAccessKeyNode node, StringBuffer output, String tabulations,
                                        int firstNumbering, int secondNumbering, SingleAccessKeyTree tree2dump) {

        if (node != null && node.getCharacter() != null && node.getCharacterState() != null) {
            if (node.getCharacterState() instanceof QuantitativeMeasure) {
                output.append(tabulations).append(firstNumbering).append(".").append(secondNumbering).append(") ").append("<span style=\"color:#333\">").append(node.getCharacter().getName()).append("</span> | ").append("<span style=\"color:#fe8a22\">").append(((QuantitativeMeasure) node.getCharacterState())
                        .toStringInterval(((QuantitativeCharacter) node.getCharacter())
                                .getMeasurementUnit())).append("</span>");
            } else {
                output.append(tabulations).append(firstNumbering).append(".").append(secondNumbering).append(") ").append("<span style=\"color:#333\">").append(node.getCharacter().getName()).append("</span> | ").append("<span style=\"color:#fe8a22\">").append(node.getStringStates()).append("</span>");
            }
            output.append(tree2dump.nodeDescriptionAnalysis(node));
            if (node.getChildren().size() == 0) {
                output.append(" -> ");
                boolean firstLoop = true;
                for (Taxon taxon : node.getRemainingTaxa()) {
                    if (!firstLoop) {
                        output.append(", ");
                    }
                    output.append("<span style=\"color:#67bb1b\">").append(taxon.getName()).append("</span>");
                    firstLoop = false;
                }
            } else {
                output.append(" (items=").append(node.getRemainingTaxa().size()).append(")");
            }
            output.append(System.getProperty("line.separator"));
            tabulations = tabulations + ":";
        }
        firstNumbering++;
        secondNumbering = 0;
        if (node != null) {
            for (SingleAccessKeyNode childNode : node.getChildren()) {
                secondNumbering++;
                recursiveToWiki(childNode, output, tabulations, firstNumbering, secondNumbering, tree2dump);
            }
        }
    }

    // END WIKI DUMP, TREE

    // WIKI DUMP, FLAT

    public static File dumpFlatWikiFile(String header, SingleAccessKeyTree tree2dump, boolean showStatistics, final String generatedFilesFolder)
            throws IOException {

        File wikiFile = File.createTempFile(IkeyUtils.KEY, "." + IkeyConfig.OutputFormat.WIKI, new File(generatedFilesFolder));
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

        if (showStatistics) {
            tree2dump.gatherTaxonPathStatistics();
            wikiFlatFileWriter.append(outputTaxonPathStatisticsWiki(tree2dump));
        }

        // wikiFlatFileWriter.append("</nowiki>");
        wikiFlatFileWriter.close();

        return wikiFile;
    }

    /**
     * generates a flat, wiki-formatted, String representation of a key, in a String object, by calling the
     * {@link #multipleTraversalToWikiString} helper method
     */
    private static String generateFlatWikiString(SingleAccessKeyTree tree2dump) {
        StringBuffer output = new StringBuffer();
        multipleTraversalToWikiString(tree2dump.getRoot(), output, System.getProperty("line.separator"),
                tree2dump);

        return output.toString();
    }

    /**
     * This methods outputs the {@link SingleAccessKeyTree} as a flat wiki-formatted String, with mediawiki
     * hyperlinks. In order to do this, the <tt>SingleAccesKeyTree</tt> is traversed 3 times. The first
     * traversal is a breadth-first traversal, in order to generate an HashMap (
     * <tt>nodeBreadthFirstIterationMap</tt>) that associates each node with an arbitrary Integer. The second
     * traversal is a depth-first traversal, in order to associate (in another HashMap :
     * <tt>nodeChildParentNumberingMap</tt>), for each node, the node number and the number of its parent
     * node. Finally, the last traversal is another breadh-first traversal that generates the flat key String
     */
    private static void multipleTraversalToWikiString(SingleAccessKeyNode rootNode, StringBuffer output,
                                                      String lineSeparator, SingleAccessKeyTree tree2dump) {

        // // first traversal, breadth-first ////
        HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap = new HashMap<>();
        int counter = 1;

        iterativeBreadthFirstSkipChildlessNodes(rootNode, nodeBreadthFirstIterationMap, counter);

        // // end first traversal, breadth-first ////

        // // second traversal, depth-first ////
        HashMap<SingleAccessKeyNode, Integer> nodeChildParentNumberingMap = new HashMap<>();
        recursiveDepthFirstNodeIndex(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
        // // end second traversal, depth-first ////

        // // third traversal, breadth-first ////
        Queue<SingleAccessKeyNode> queue = new LinkedList<>();
        ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<>();

        counter = 1;
        int currentParentNumber = -1;
        queue.add(rootNode);

        // root node treatment

        counter++;
        // end root node treatment
        visitedNodes.add(rootNode);

        while (!queue.isEmpty()) {
            SingleAccessKeyNode node = queue.remove();
            SingleAccessKeyNode child;

            while (IkeyUtils.exclusion(node.getChildren(), visitedNodes).size() > 0
                    && (child = (SingleAccessKeyNode) IkeyUtils.exclusion(node.getChildren(), visitedNodes)
                    .get(0)) != null) {
                visitedNodes.add(child);

                // / child node treatment

                // displaying the parent node number and the child node character name only once
                if (nodeChildParentNumberingMap.get(child) != currentParentNumber) {
                    currentParentNumber = nodeChildParentNumberingMap.get(child);
                    output.append(lineSeparator);
                    output.append("<span id=\"anchor").append(currentParentNumber).append("\"></span>").append(currentParentNumber);

                    output.append("  ").append(child.getCharacter().getName());
                    output.append(lineSeparator);
                    output.append("::::::= ");
                } else {
                    output.append("::::::= ");
                }

                // displaying the child node character state
                output.append("<span style=\"color:#fe8a22;\">");// state coloring
                if (child.getCharacterState() instanceof QuantitativeMeasure) {
                    output.append(((QuantitativeMeasure) child.getCharacterState())
                            .toStringInterval(((QuantitativeCharacter) child.getCharacter())
                                    .getMeasurementUnit()));
                } else {
                    output.append(child.getStringStates());
                }
                output.append("</span>");
                output.append("<span style=\"color: black;\">").append(tree2dump.nodeDescriptionAnalysis(child)).append("</span>");

                // displaying the child node number if it has children nodes, displaying the taxa otherwise
                output.append(" &#8658; "); // arrow
                if (child.getChildren().size() == 0) {

                    boolean firstLoop = true;
                    for (Taxon taxon : child.getRemainingTaxa()) {

                        if (!firstLoop) {
                            output.append(", ");
                        }
                        output.append("<span style=\"color:#67bb1b;\">"); // taxa coloring
                        output.append("''").append(taxon.getName()).append("''");
                        output.append("</span>");
                        firstLoop = false;
                    }

                } else {
                    output.append("[[#anchor").append(counter).append("|<span style=\"color:#67bb1b;\"><u>").append(counter).append("</u></span>]]");
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

    // DOT DUMP

    /**
     * generates a DOT file (viewable with <a href="http://www.graphviz.org">graphviz</a>) containing the key,
     * by calling {@link #generateDotString}
     */
    public static File dumpDotFile(String header, SingleAccessKeyTree tree2dump, String generatedFilesFolder) throws IOException {

        header = header.replace(System.getProperty("line.separator"), System.getProperty("line.separator")
                + "//");
        header = header + System.getProperty("line.separator");
        File dotFile = File.createTempFile("key_", "." + IkeyUtils.GV, new File(generatedFilesFolder));

        FileOutputStream fileOutputStream = new FileOutputStream(dotFile);
        fileOutputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        BufferedWriter dotFileWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
        dotFileWriter.append(header);
        dotFileWriter.append("digraph ").append(dotFile.getName().split("\\.")[0]).append(" {");
        dotFileWriter.append(generateDotString(tree2dump));
        dotFileWriter.append(System.getProperty("line.separator")).append("}");
        dotFileWriter.close();

        return dotFile;
    }

    /**
     * generates a DOT-formatted String representation of the key, by calling
     * {@link #multipleTraversalToDotString}
     */
    private static String generateDotString(SingleAccessKeyTree tree2dump) {
        StringBuffer output = new StringBuffer();
        multipleTraversalToDotString(tree2dump.getRoot(), output, System.getProperty("line.separator"),
                tree2dump);
        return output.toString();
    }

    /**
     * This methods outputs the {@link SingleAccessKeyTree} as a DOT-formatted String. In order to do this,
     * the <tt>SingleAccesKeyTree</tt> is traversed 3 times. The first traversal is a breadth-first traversal,
     * in order to generate an HashMap (<tt>nodeBreadthFirstIterationMap</tt>) that associates each node with
     * an arbitrary Integer. The second traversal is a depth-first traversal, in order to associate (in
     * another HashMap : <tt>nodeChildParentNumberingMap</tt>), for each node, the node number and the number
     * of its parent node. Finally, the last traversal is another breadh-first traversal that generates the
     * flat key String
     */
    private static void multipleTraversalToDotString(SingleAccessKeyNode rootNode, StringBuffer output,
                                                     String lineSeparator, SingleAccessKeyTree tree2dump) {

        // // first traversal, breadth-first ////
        HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap = new HashMap<>();

        int counter = 1;
        iterativeBreadthFirst(rootNode, nodeBreadthFirstIterationMap, counter);

        // // end first traversal, breadth-first ////

        // // second traversal, depth-first ////
        HashMap<Integer, Integer> nodeChildParentNumberingMap = new HashMap<>();
        recursiveDepthFirstIntegerIndex(rootNode, nodeBreadthFirstIterationMap, nodeChildParentNumberingMap);
        // // end second traversal, depth-first ////

        // // third traversal, breadth-first ////
        Queue<SingleAccessKeyNode> queue = new LinkedList<>();
        ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<>();

        counter = 1;
        int currentParentNumber = -1;
        queue.add(rootNode);

        // root node treatment

        counter++;
        // end root node treatment
        visitedNodes.add(rootNode);

        while (!queue.isEmpty()) {
            SingleAccessKeyNode node = queue.remove();
            SingleAccessKeyNode child;

            while (IkeyUtils.exclusion(node.getChildren(), visitedNodes).size() > 0
                    && (child = (SingleAccessKeyNode) IkeyUtils.exclusion(node.getChildren(), visitedNodes)
                    .get(0)) != null
                // && child.getCharacter() != null && child.getCharacterState() != null
                    ) {
                visitedNodes.add(child);

                // / child node treatment

                // displaying the parent node number
                if (nodeChildParentNumberingMap.get(Integer.valueOf(counter)) != currentParentNumber) {
                    currentParentNumber = nodeChildParentNumberingMap.get(Integer.valueOf(counter));
                }
                output.append(lineSeparator);
                output.append(currentParentNumber).append(" -> ");

                // displaying the child node number
                output.append(counter);

                // displaying the child node character state as a vertex label
                if (child.getCharacterState() instanceof QuantitativeMeasure) {
                    output.append(" [label=\"").append(((QuantitativeMeasure) child.getCharacterState())
                            .toStringInterval(((QuantitativeCharacter) child.getCharacter())
                                    .getMeasurementUnit()));
                } else {
                    output.append(" [label=\"").append(child.getStringStates());
                }
                output.append(tree2dump.nodeDescriptionAnalysis(child));
                output.append("\"]");
                output.append(";").append(lineSeparator);

                if (child.getChildren().size() == 0) {
                    // if the child node has no children nodes, displaying the parent node character and the
                    // child node remaining taxa
                    output.append(currentParentNumber).append(" [label=\"").append(child.getCharacter().getName()).append("\"];");
                    output.append(lineSeparator);

                    output.append(counter).append(" [label=\"");
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
                    output.append(currentParentNumber).append(" [label=\"");
                    output.append(child.getCharacter().getName()).append("\"];");
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
    public static File dumpZipFile(String header, SingleAccessKeyTree tree2dump, boolean showStatistics, String generatedFilesFolder)
            throws IOException {

        // create all output formats
        File sddFile = dumpSddFile(tree2dump);
        File txtFile = dumpTxtFile(header, tree2dump, showStatistics, generatedFilesFolder);
        File flatTxtFile = dumpFlatTxtFile(header, tree2dump, showStatistics, generatedFilesFolder);
        File htmlFile = dumpHtmlFile(header, tree2dump, showStatistics, generatedFilesFolder);
        File flatHtmlFile = dumpFlatHtmlFile(header, tree2dump, showStatistics, generatedFilesFolder);
        File interactiveHtmlFile = dumpInteractiveHtmlFile(header, tree2dump, showStatistics, generatedFilesFolder);
        File wikiFile = dumpWikiFile(header, tree2dump, showStatistics, generatedFilesFolder);
        File flatWikiFile = dumpFlatWikiFile(header, tree2dump, showStatistics, generatedFilesFolder);
        File dotFile = dumpDotFile(header, tree2dump, generatedFilesFolder);

        // add all output file to the files list
        List<File> filesList = new ArrayList<>();
        filesList.add(sddFile);
        filesList.add(txtFile);
        filesList.add(flatTxtFile);
        filesList.add(htmlFile);
        filesList.add(flatHtmlFile);
        filesList.add(interactiveHtmlFile);
        filesList.add(wikiFile);
        filesList.add(flatWikiFile);
        filesList.add(dotFile);

        String label = "";
        if (tree2dump.getLabel() != null) {
            label = tree2dump.getLabel() + "-";
        }
        // create a map matching file to file path
        Map<File, String> correspondingFilePath = new HashMap<>();
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
        correspondingFilePath.put(interactiveHtmlFile, label + "key" + System.getProperty("file.separator")
                + "flat" + System.getProperty("file.separator") + interactiveHtmlFile.getName());
        correspondingFilePath.put(wikiFile, label + "key" + System.getProperty("file.separator") + "tree"
                + System.getProperty("file.separator") + wikiFile.getName());
        correspondingFilePath.put(flatWikiFile, label + "key" + System.getProperty("file.separator") + "flat"
                + System.getProperty("file.separator") + flatWikiFile.getName());
        correspondingFilePath.put(dotFile, label + "key" + System.getProperty("file.separator") + "tree"
                + System.getProperty("file.separator") + dotFile.getName());

        String path = IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.prefix")
                + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder");

        File zipFile = File.createTempFile(IkeyUtils.KEY, "." + IkeyConfig.OutputFormat.ZIP, new File(path));

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
            byte data[] = new byte[IkeyUtils.BUFFER];

            // for each file of the list
            for (File file : filesList) {

                // create the reading flow
                FileInputStream fi = new FileInputStream(file);

                // creation of a read buffer of the stream
                BufferedInputStream buffi = new BufferedInputStream(fi, IkeyUtils.BUFFER);

                // create input for this Zip file
                ZipEntry entry = new ZipEntry(IkeyUtils.unAccent(correspondingFilePath.get(file)));

                // add this entry in the flow of writing the Zip archive
                out.putNextEntry(entry);

                // writing the package file BUFFER bytes in the flow Writing
                int count;
                while ((count = buffi.read(data, 0, IkeyUtils.BUFFER)) != -1) {
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
            tree2dump.getConfig().setErrorMessage(IkeyConfig.getBundleConfElement("message.creatingFileError"), e);
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
     */
    private static void iterativeBreadthFirstSkipChildlessNodes(SingleAccessKeyNode rootNode,
                                                                HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap, int counter) {
        Queue<SingleAccessKeyNode> queue = new LinkedList<>();
        ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<>();

        queue.add(rootNode);

        // root node treatment
        nodeBreadthFirstIterationMap.put(rootNode, counter);
        counter++;
        // end root node treatment

        visitedNodes.add(rootNode);

        while (!queue.isEmpty()) {
            SingleAccessKeyNode node = queue.remove();
            SingleAccessKeyNode child;

            // exclusion(node.getChildren(), visitedNodes) is the list of unvisited children nodes of the
            while (IkeyUtils.exclusion(node.getChildren(), visitedNodes).size() > 0
                    && (child = (SingleAccessKeyNode) IkeyUtils.exclusion(node.getChildren(), visitedNodes)
                    .get(0)) != null) {
                visitedNodes.add(child);

                if (child.hasChild()) {
                    // / child node treatment
                    nodeBreadthFirstIterationMap.put(child, counter);
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
     */
    private static void iterativeBreadthFirst(SingleAccessKeyNode rootNode,
                                              HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap, int counter) {
        Queue<SingleAccessKeyNode> queue = new LinkedList<>();
        ArrayList<SingleAccessKeyNode> visitedNodes = new ArrayList<>();

        queue.add(rootNode);

        // root node treatment
        nodeBreadthFirstIterationMap.put(rootNode, counter);
        counter++;
        // end root node treatment

        visitedNodes.add(rootNode);

        while (!queue.isEmpty()) {
            SingleAccessKeyNode node = queue.remove();
            SingleAccessKeyNode child;

            // exclusion(node.getChildren(), visitedNodes) is the list of unvisited children nodes of the
            while (IkeyUtils.exclusion(node.getChildren(), visitedNodes).size() > 0
                    && (child = (SingleAccessKeyNode) IkeyUtils.exclusion(node.getChildren(), visitedNodes)
                    .get(0)) != null) {
                visitedNodes.add(child);

                // / child node treatment
                nodeBreadthFirstIterationMap.put(child, counter);
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
     */
    private static void recursiveDepthFirstIntegerIndex(SingleAccessKeyNode node,
                                                        HashMap<SingleAccessKeyNode, Integer> nodeBreadthFirstIterationMap,
                                                        HashMap<Integer, Integer> nodeChildParentNumberingMap, List<Integer> rootNodeChildrenIntegerList) {

        Integer parentNumber = nodeBreadthFirstIterationMap.get(node);
        for (SingleAccessKeyNode childNode : node.getChildren()) {
            Integer childNumber = nodeBreadthFirstIterationMap.get(childNode);
            nodeChildParentNumberingMap.put(childNumber, parentNumber);
            if (parentNumber == 1) {
                rootNodeChildrenIntegerList.add(childNumber);
            }

            recursiveDepthFirstIntegerIndex(childNode, nodeBreadthFirstIterationMap,
                    nodeChildParentNumberingMap, rootNodeChildrenIntegerList);
        }
    }

    /**
     * Helper method that receives a character string, escapes special HTML character contained in that
     * character String, and returns it
     */
    private static String escapeHTMLSpecialCharacters(String htmlString) {
        return htmlString.replace(">", "&gt;").replace("<", "&lt;").replace("&", "&amp;");
    }

    /**
     * Output method that loops over the list of taxa contained in the Dataset, and outputs basic path
     * statistics for each Taxon, in a plain-text representation
     */
    private static String outputTaxonPathStatisticsString(SingleAccessKeyTree tree2dump) {
        String lineSeparator = System.getProperty("line.separator");
        StringBuilder output = new StringBuilder(0);

        DataSet ds = tree2dump.getDataSet();
        float sumNbPath = 0;
        float sumMinPathLength = 0;
        float sumAvgPathLength = 0;
        float sumMaxPathLength = 0;
        int c = 0;
        output.append(lineSeparator).append(lineSeparator).append(lineSeparator).append("STATISTICS").append(lineSeparator);
        output.append("Taxon\tnumber of paths leading to taxon\t");
        output.append("length of the shortest path leading to taxon\t");
        output.append("average length of paths leading to taxon\t");
        output.append("length of the longest path leading to taxon\t");
        output.append(lineSeparator);
        for (Taxon t : ds.getTaxa()) {

            output.append(t.getName()).append("\t").append(t.getTaxonStatistics().get(Taxon.NB_PATH_IN_KEY).intValue()).append("\t").append(t.getTaxonStatistics().get(Taxon.SHORTEST_PATH_IN_KEY).intValue()).append("\t").append(IkeyUtils.roundFloat(t.getTaxonStatistics().get(Taxon.AVERAGE_PATHLENGTH_IN_KEY), 3)).append("\t").append(t.getTaxonStatistics().get(Taxon.LONGEST_PATH_IN_KEY).intValue());

            if (t.getTaxonStatistics().get(Taxon.NB_PATH_IN_KEY) > 0) {
                sumNbPath += t.getTaxonStatistics().get(Taxon.NB_PATH_IN_KEY);
                sumMinPathLength += t.getTaxonStatistics().get(Taxon.SHORTEST_PATH_IN_KEY);
                sumMaxPathLength += t.getTaxonStatistics().get(Taxon.LONGEST_PATH_IN_KEY);
                sumAvgPathLength += t.getTaxonStatistics().get(Taxon.AVERAGE_PATHLENGTH_IN_KEY);
                c++;
                output.append(lineSeparator);
            }
        }

        // round all average values
        float averageNbPath = IkeyUtils.roundFloat((sumNbPath / (float) c), 3);
        float averageMinPath = IkeyUtils.roundFloat((sumMinPathLength / (float) c), 3);
        float averageAvgPath = IkeyUtils.roundFloat((sumAvgPathLength / (float) c), 3);
        float averageMaxPath = IkeyUtils.roundFloat((sumMaxPathLength / (float) c), 3);

        output.append("AVERAGE\t").append(averageNbPath).append("\t").append(averageMinPath).append("\t").append(averageAvgPath).append("\t").append(averageMaxPath);
        output.append(lineSeparator);

        return output.toString();
    }

    /**
     * Output method that loops over the list of taxa contained in the Dataset, and outputs basic path
     * statistics for each Taxon, in an HTML representation
     */
    private static String outputTaxonPathStatisticsHTML(SingleAccessKeyTree tree2dump) {
        String lineSeparator = System.getProperty("line.separator");
        StringBuilder output = new StringBuilder(0);
        DataSet ds = tree2dump.getDataSet();

        output.append("<div style=\"margin-left: 30px;word-wrap: break-word;\" id=\"statistics\">").append(lineSeparator);
        output.append("<br/><br/><strong>STATISTICS</strong>").append(lineSeparator);
        output.append("<table class=\"statisticsTable\">").append(lineSeparator);

        float sumNbPath = 0;
        float sumMinPathLength = 0;
        float sumAvgPathLength = 0;
        float sumMaxPathLength = 0;
        int c = 0;
        int i = 0;
        output.append("<tr>").append(lineSeparator);
        output.append("<td>Taxon</td>");
        output.append("<td width=\"100px;\">Number of paths leading to taxon</td>");
        output.append("<td width=\"100px;\">Length of the shortest path leading to taxon</td>");
        output.append("<td width=\"100px;\">Average length of paths leading to taxon</td>");
        output.append("<td width=\"100px;\">Length of the longest path leading to taxon</td>");
        output.append("</tr>").append(lineSeparator);

        for (Taxon t : ds.getTaxa()) {

            if (i % 2 != 0) {
                output.append("<tr class=\"paire\">").append(lineSeparator);
            } else {
                output.append("<tr>").append(lineSeparator);
            }
            output.append("<td>").append(escapeHTMLSpecialCharacters(t.getName())).append("</td><td>").append(t.getTaxonStatistics().get(Taxon.NB_PATH_IN_KEY).intValue()).append("</td><td>").append(t.getTaxonStatistics().get(Taxon.SHORTEST_PATH_IN_KEY).intValue()).append("</td><td>").append(IkeyUtils.roundFloat(t.getTaxonStatistics().get(Taxon.AVERAGE_PATHLENGTH_IN_KEY), 3)).append("</td><td>").append(t.getTaxonStatistics().get(Taxon.LONGEST_PATH_IN_KEY).intValue()).append("</td>");

            if (t.getTaxonStatistics().get(Taxon.NB_PATH_IN_KEY) > 0) {
                sumNbPath += t.getTaxonStatistics().get(Taxon.NB_PATH_IN_KEY);
                sumMinPathLength += t.getTaxonStatistics().get(Taxon.SHORTEST_PATH_IN_KEY);
                sumMaxPathLength += t.getTaxonStatistics().get(Taxon.LONGEST_PATH_IN_KEY);
                sumAvgPathLength += t.getTaxonStatistics().get(Taxon.AVERAGE_PATHLENGTH_IN_KEY);
                c++;
            }
            i++;
            output.append("</tr>").append(lineSeparator);
        }

        // round all average values
        float averageNbPath = IkeyUtils.roundFloat((sumNbPath / (float) c), 3);
        float averageMinPath = IkeyUtils.roundFloat((sumMinPathLength / (float) c), 3);
        float averageAvgPath = IkeyUtils.roundFloat((sumAvgPathLength / (float) c), 3);
        float averageMaxPath = IkeyUtils.roundFloat((sumMaxPathLength / (float) c), 3);

        output.append("<tr><td>AVERAGE</td><td>").append(averageNbPath).append("</td><td>").append(averageMinPath).append("</td><td>").append(averageAvgPath).append("</td><td>").append(averageMaxPath).append("</td></tr>");
        output.append(lineSeparator);

        output.append("</table>").append(lineSeparator);
        output.append("</div>").append(lineSeparator);
        return output.toString();
    }

    /**
     * Output method that loops over the list of taxa contained in the Dataset, and outputs basic path
     * statistics for each Taxon, in a wiki representation
     */
    private static String outputTaxonPathStatisticsWiki(SingleAccessKeyTree tree2dump) {
        String lineSeparator = System.getProperty("line.separator");
        StringBuilder output = new StringBuilder(0);
        DataSet ds = tree2dump.getDataSet();

        float sumNbPath = 0;
        float sumMinPathLength = 0;
        float sumAvgPathLength = 0;
        float sumMaxPathLength = 0;
        int c = 0;
        output.append(lineSeparator).append(lineSeparator).append("== STATISTICS == ").append(lineSeparator);

        output.append("{|align=\"center\" style=\"text-align:center;\"").append(lineSeparator);
        output.append("!Taxon").append(lineSeparator);
        output.append("!Number of paths leading to taxon").append(lineSeparator);
        output.append("!Length of the shortest path leading to taxon").append(lineSeparator);
        output.append("!Average length of paths leading to taxon").append(lineSeparator);
        output.append("!Length of the longest path leading to taxon").append(lineSeparator);
        output.append("|-").append(lineSeparator);

        for (Taxon t : ds.getTaxa()) {
            output.append("|align=\"left\"|").append(t.getName()).append(lineSeparator).append("|").append(t.getTaxonStatistics().get(Taxon.NB_PATH_IN_KEY).intValue()).append(lineSeparator).append("|").append(t.getTaxonStatistics().get(Taxon.SHORTEST_PATH_IN_KEY).intValue()).append(lineSeparator).append("|").append(IkeyUtils.roundFloat(t.getTaxonStatistics().get(Taxon.AVERAGE_PATHLENGTH_IN_KEY), 3)).append(lineSeparator).append("|").append(t.getTaxonStatistics().get(Taxon.LONGEST_PATH_IN_KEY).intValue()).append(lineSeparator).append("|-").append(lineSeparator);

            if (t.getTaxonStatistics().get(Taxon.NB_PATH_IN_KEY) > 0) {
                sumNbPath += t.getTaxonStatistics().get(Taxon.NB_PATH_IN_KEY);
                sumMinPathLength += t.getTaxonStatistics().get(Taxon.SHORTEST_PATH_IN_KEY);
                sumMaxPathLength += t.getTaxonStatistics().get(Taxon.LONGEST_PATH_IN_KEY);
                sumAvgPathLength += t.getTaxonStatistics().get(Taxon.AVERAGE_PATHLENGTH_IN_KEY);
                c++;
            }
        }

        // round all average values
        float averageNbPath = IkeyUtils.roundFloat((sumNbPath / (float) c), 3);
        float averageMinPath = IkeyUtils.roundFloat((sumMinPathLength / (float) c), 3);
        float averageAvgPath = IkeyUtils.roundFloat((sumAvgPathLength / (float) c), 3);
        float averageMaxPath = IkeyUtils.roundFloat((sumMaxPathLength / (float) c), 3);

        output.append("!align=\"left\"|AVERAGE").append(lineSeparator).append("|").append(averageNbPath).append(lineSeparator).append("|").append(averageMinPath).append(lineSeparator).append("|").append(averageAvgPath).append(lineSeparator).append("|").append(averageMaxPath).append(lineSeparator).append("|}");
        output.append(lineSeparator);

        return output.toString();
    }


}
