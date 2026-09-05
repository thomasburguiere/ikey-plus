package fr.lis.ikeyplus.model.key;

import fr.lis.ikeyplus.model.character.QuantitativeCharacter;
import fr.lis.ikeyplus.model.description.CharacterState;
import fr.lis.ikeyplus.model.description.QuantitativeMeasure;

public class QuantitativeNode extends SingleAccessKeyNode {

    private QuantitativeCharacter quantitativeCharacter;
    private QuantitativeMeasure measure;

    public QuantitativeNode(final QuantitativeCharacter quantitativeCharacter, final QuantitativeMeasure measure) throws OutOfMemoryError, Exception {
        super.setCharacter(quantitativeCharacter);
        this.quantitativeCharacter = quantitativeCharacter;
        this.measure = measure;
    }

    @Override
    public CharacterState getCharacterState() {
        return measure;
    }

    public QuantitativeCharacter getQuantitativeCharacter() {
        return quantitativeCharacter;
    }

    public QuantitativeMeasure getMeasure() {
        return measure;
    }
}
