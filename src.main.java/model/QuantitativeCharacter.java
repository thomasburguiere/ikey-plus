package model;

/**
 * This class represents a Character of type quantitative
 * 
 * @author Florian Causse
 * @created 06-04-2011
 */
public class QuantitativeCharacter extends Character {

	String MeasurementUnit = "";

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

	/**
	 * set the measurement unit
	 * 
	 * @return string, the measurement unit
	 */
	public String getMeasurementUnit() {
		return MeasurementUnit;
	}

	/**
	 * set the measurement unit
	 * 
	 * @param measurementUnit
	 */
	public void setMeasurementUnit(String measurementUnit) {
		MeasurementUnit = measurementUnit;
	}

	/* (non-Javadoc)
	 * 
	 * @see model.Character#isSupportsCategoricalData() */
	@Override
	public boolean isSupportsCategoricalData() {
		return false;
	}
}