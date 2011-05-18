package model;

import java.io.File;

/**
 * @author Florian Causse
 * @created 18-avr.-2011
 */
public class PolytomousKeyTree {

	private PolytomousKeyNode root = null;

	/**
	 * constructor
	 */
	public PolytomousKeyTree() {
		this(null);
	}

	/**
	 * constructor with root node param
	 * 
	 * @param PolytomousKeyNode
	 *            , the root node
	 */
	public PolytomousKeyTree(PolytomousKeyNode root) {
		super();
		this.root = root;
	}

	/**
	 * get the root node
	 * 
	 * @return PolytomousKeyNode, the root node
	 */
	public PolytomousKeyNode getRoot() {
		return root;
	}

	/**
	 * set the root node
	 * 
	 * @param PolytomousKeyNode
	 *            , the root node
	 */
	public void setRoot(PolytomousKeyNode root) {
		this.root = root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return null;
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
