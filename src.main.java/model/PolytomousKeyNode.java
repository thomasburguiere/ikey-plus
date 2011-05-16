package model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Causse
 * @created 18-avr.-2011
 */
public class PolytomousKeyNode {

	private ICharacter character = null;
	private Object state = null;
	private List<PolytomousKeyNode> children = new ArrayList<PolytomousKeyNode>();

	/**
	 * constructor
	 */
	public PolytomousKeyNode() {
		super();
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
	public void setChildrens(List<PolytomousKeyNode> children) {
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

}
