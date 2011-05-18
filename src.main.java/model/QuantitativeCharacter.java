package model;

/**
 * @author Florian Causse
 * @created 06-avr.-2011
 */
public class QuantitativeCharacter extends Character {

	/**
	 * constructor by default
	 */
	public QuantitativeCharacter() {
		this(null);
	}

	/**
	 * constructor with name parameter
	 */
	public QuantitativeCharacter(String name) {
		super();
		this.setName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see model.Character#isSupportsCategoricalData()
	 */
	@Override
	public boolean isSupportsCategoricalData() {
		return false;
	}
}