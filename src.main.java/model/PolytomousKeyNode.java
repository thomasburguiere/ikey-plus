package model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Causse
 * @created 18-avr.-2011
 */
public class PolytomousKeyNode {

	private ICharacter character = null;
	private ICharacter parentCharacter = null;
	private Object state = null;
	private List<PolytomousKeyNode> children = null;
	private List<Taxon> remainingTaxa = null;

	/**
	 * constructor
	 */
	public PolytomousKeyNode() {
		this(null, null);
	}

	/**
	 * @param ICharacter
	 *            , a character
	 * @param Object
	 *            , the description concerning the current character ->
	 *            List<State> or QuantitativeMeasure
	 */
	public PolytomousKeyNode(ICharacter character, Object state) {
		super();
		this.character = character;
		this.state = state;
		this.children = new ArrayList<PolytomousKeyNode>();
		this.remainingTaxa = new ArrayList<Taxon>();
	}

	/**
	 * get the character assign to the node
	 * 
	 * @return ICharacter, a character
	 */
	public ICharacter getCharacter() {
		return character;
	}

	/**
	 * set the character assign to the node
	 * 
	 * @param ICharacter
	 *            , a character
	 */
	public void setCharacter(ICharacter character) {
		this.character = character;
	}

	/**
	 * get the description assign to the Node
	 * 
	 * @return Object, the description concerning the node
	 */
	public Object getState() {
		return state;
	}

	/**
	 * set the description assign to the node
	 * 
	 * @param Object
	 *            , the description concerning the node
	 */
	public void setState(Object state) {
		this.state = state;
	}

	/**
	 * get all children
	 * 
	 * @return List<PolytomousKeyNode>, all child nodes
	 */
	public List<PolytomousKeyNode> getChildren() {
		return children;
	}

	/**
	 * set all children
	 * 
	 * @param List
	 *            <PolytomousKeyNode>, all child nodes
	 */
	public void setChildren(List<PolytomousKeyNode> children) {
		this.children = children;
	}

	/**
	 * add one child
	 * 
	 * @param PolytomousKeyNode
	 *            , a node
	 */
	public void addChild(PolytomousKeyNode polytomousKeyNode) {
		this.children.add(polytomousKeyNode);
	}

	/**
	 * get the parent character
	 * 
	 * @return ICharacter
	 */
	public ICharacter getParentCharacter() {
		return parentCharacter;
	}

	/**
	 * set the parent character
	 * 
	 * @param parentCharacter
	 */
	public void setParentCharacter(ICharacter parentCharacter) {
		this.parentCharacter = parentCharacter;
	}

	/**
	 * get the remaining taxa
	 * 
	 * @return List<Taxon>
	 */
	public List<Taxon> getRemainingTaxa() {
		return remainingTaxa;
	}

	/**
	 * set the remaining taxa
	 * 
	 * @param remainingTaxa
	 */
	public void setRemainingTaxa(List<Taxon> remainingTaxa) {
		this.remainingTaxa = remainingTaxa;
	}
}
