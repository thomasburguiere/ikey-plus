package fr.lis.ikeyplus.model;

/**
 * This class represents a Character of type quantitative
 *
 * @author Florian Causse
 */
public class QuantitativeCharacter extends Character {

    String MeasurementUnit = "";

    public QuantitativeCharacter() {
        this(null);
    }

    public QuantitativeCharacter(String name) {
        super();
        this.setName(name);
    }

    public String getMeasurementUnit() {
        return MeasurementUnit;
    }

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