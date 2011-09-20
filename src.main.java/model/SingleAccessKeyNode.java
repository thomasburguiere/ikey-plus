package model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a node of single access key
 * 
 * @author Florian Causse
 * @created 18-04-2011
 */
public class SingleAccessKeyNode {

	private ICharacter character = null;
	private Object characterState = null;
	private List<Object> otherCharacterStates = null;
	private List<SingleAccessKeyNode> children = null;
	private String nodeDescription = null;
	private List<Taxon> remainingTaxa = null;

	/**
	 * constructor
	 */
	public SingleAccessKeyNode() {
		this(null, null);
	}

	/**
	 * @param ICharacter
	 *            , a character
	 * @param Object
	 *            , the description concerning the current character -> List<State> or QuantitativeMeasure
	 */
	public SingleAccessKeyNode(ICharacter character, Object characterState) {
		super();
		this.character = character;
		this.characterState = characterState;
		this.otherCharacterStates = new ArrayList<Object>();
		this.children = new ArrayList<SingleAccessKeyNode>();
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
	public Object getCharacterState() {
		return characterState;
	}

	/**
	 * set the description assign to the node
	 * 
	 * @param Object
	 *            , the description concerning the node
	 */
	public void setCharacterState(Object characterState) {
		this.characterState = characterState;
	}

	/**
	 * get the other states of the node
	 * 
	 * @return Object, the description concerning the node
	 */
	public List<Object> getOtherCharacterStates() {
		return otherCharacterStates;
	}

	/**
	 * set the other states of the node
	 * 
	 * @param Object
	 *            , the description concerning the node
	 */
	public void setOtherCharacterStates(List<Object> otherCharacterStates) {
		this.otherCharacterStates = otherCharacterStates;
	}

	/**
	 * add other state to the node
	 * 
	 * @param Object
	 *            , the description concerning the node
	 */
	public void addOtherCharacterStates(Object otherCharacterState) {
		this.otherCharacterStates.add(otherCharacterState);
	}

	/**
	 * get states as String using default separator
	 * 
	 * @return String, the list of states
	 */
	public String getStringStates() {
		return getStatesToString(" OR ");
	}

	/**
	 * get states as String using new separator
	 * 
	 * @return String, the list of states
	 */
	public String getStatesToString(String separator) {

		String result = "";
		if (this.getCharacterState() instanceof State) {
			result = ((State) this.getCharacterState()).getName();
			for (Object state : this.getOtherCharacterStates()) {
				if (state instanceof State) {
					result = result + separator + ((State) state).getName();
				}
			}
		}
		return result;
	}

	/**
	 * get all children
	 * 
	 * @return List<SingleAccessKeyNode>, all child nodes
	 */
	public List<SingleAccessKeyNode> getChildren() {
		return children;
	}

	/**
	 * set all children
	 * 
	 * @param List
	 *            <SingleAccessKeyNode>, all child nodes
	 */
	public void setChildren(List<SingleAccessKeyNode> children) {
		this.children = children;
	}

	/**
	 * add one child
	 * 
	 * @param SingleAccessKeyNode
	 *            , a node
	 */
	public void addChild(SingleAccessKeyNode singleAccessKeyNode) {
		this.children.add(singleAccessKeyNode);
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

	/**
	 * @return true if this SingleAccessKeyNode's child list size is greater than 0
	 */
	public boolean hasChild() {
		return children.size() > 0;
	}

	/**
	 * @return true if this SingleAccessKeynode's character or characterState is null
	 */
	public boolean isEmpty() {
		return character == null || characterState == null;
	}

	/**
	 * @return String, the message to display for this node
	 */
	public String getNodeDescription() {
		return nodeDescription;
	}

	/**
	 * @param nodeDescription
	 */
	public void setNodeDescription(String nodeDescription) {
		this.nodeDescription = nodeDescription;
	}

	public String toString() {
		return character.toString() + " --> " + characterState.toString();
	}

}
