package fr.lis.ikeyplus.model.character;

/**
 * This class represents a Character of type quantitative
 *
 * @author Florian Causse
 */
public class QuantitativeCharacter extends Character {

    private String measurementUnit = "";

    public QuantitativeCharacter() {
        this(null);
    }

    public QuantitativeCharacter(final String name) {
        super();
        setName(name);
    }

    public String getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(final String measurementUnit) {
        this.measurementUnit = measurementUnit;
    }

    /* (non-Javadoc)
     *
     * @see model.Character#isSupportsCategoricalData() */
    @Override
    public boolean isSupportsCategoricalData() {
        return false;
    }
}