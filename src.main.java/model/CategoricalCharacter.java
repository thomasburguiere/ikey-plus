package model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Character of type categorical
 * 
 * @author Florian Causse
 * @created 06-04-2011
 */
public class CategoricalCharacter extends Character {

	private List<State> states = null;

	/**
	 * constructor by default
	 */
	public CategoricalCharacter() {
		this(null);
	}

	/**
	 * constructor with name parameter
	 */
	public CategoricalCharacter(String name) {
		super();
		this.setName(name);
		this.states = new ArrayList<State>();
	}

	/**
	 * getter for states
	 * 
	 * @return List<State>, a list of character states
	 */
	public List<State> getStates() {
		return states;
	}

	/**
	 * setter for states
	 * 
	 * @param List
	 *            <State>
	 */
	public void setStates(List<State> states) {
		this.states = states;
	}

	/* (non-Javadoc)
	 * 
	 * @see model.Character#isSupportsCategoricalData() */
	@Override
	public boolean isSupportsCategoricalData() {
		return true;
	}
}