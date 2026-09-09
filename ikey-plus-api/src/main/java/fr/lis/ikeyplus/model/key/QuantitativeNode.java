package fr.lis.ikeyplus.model.key;

import fr.lis.ikeyplus.model.Taxon;
import fr.lis.ikeyplus.model.character.ICharacter;
import fr.lis.ikeyplus.model.character.QuantitativeCharacter;
import fr.lis.ikeyplus.model.description.CharacterState;
import fr.lis.ikeyplus.model.description.QuantitativeMeasure;

import java.util.List;

public final class QuantitativeNode extends CharacterNode {

    private QuantitativeCharacter quantitativeCharacter;
    private QuantitativeMeasure measure;

    public QuantitativeNode(final QuantitativeCharacter quantitativeCharacter, final QuantitativeMeasure measure) throws OutOfMemoryError, Exception {
//        super.setCharacter(quantitativeCharacter);
        this.quantitativeCharacter = quantitativeCharacter;
        this.measure = measure;
    }

    @Override
    public ICharacter getCharacter() {
        return null;
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

    @Override
    public List<CharacterNode> getChildren() {
        return List.of();
    }

    @Override
    public List<Taxon> getRemainingTaxa() {
        return List.of();
    }

    @Override
    public void setRemainingTaxa(List<Taxon> taxa) {

    }

    @Override
    public boolean hasChild() {
        return false;
    }

    @Override
    public void addChild(CharacterNode node) {

    }
}
