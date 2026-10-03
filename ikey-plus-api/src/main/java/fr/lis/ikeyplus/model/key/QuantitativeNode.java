package fr.lis.ikeyplus.model.key;

import fr.lis.ikeyplus.model.Taxon;
import fr.lis.ikeyplus.model.character.ICharacter;
import fr.lis.ikeyplus.model.character.QuantitativeCharacter;
import fr.lis.ikeyplus.model.description.CharacterState;
import fr.lis.ikeyplus.model.description.QuantitativeMeasure;

import java.util.ArrayList;
import java.util.List;

public final class QuantitativeNode extends CharacterNode {

    private QuantitativeCharacter quantitativeCharacter;
    private QuantitativeMeasure measure;
    private final List<CharacterNode> children;
    private final List<Taxon> remainingTaxa = new ArrayList<>();

    public QuantitativeNode(final QuantitativeCharacter quantitativeCharacter, final QuantitativeMeasure measure) throws OutOfMemoryError, Exception {
//        super.setCharacter(quantitativeCharacter);
        this.quantitativeCharacter = quantitativeCharacter;
        this.measure = measure;
        children = new ArrayList<>();
    }

    @Override
    public ICharacter getCharacter() {
        return quantitativeCharacter;
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
        return children;
    }

    @Override
    public List<Taxon> getRemainingTaxa() {
        return remainingTaxa;
    }

    @Override
    public void setRemainingTaxa(final List<Taxon> taxa) {
        remainingTaxa.clear();
        remainingTaxa.addAll(taxa);
    }

    @Override
    public boolean hasChild() {
        return !children.isEmpty();
    }

    @Override
    public void addChild(final CharacterNode node) {
        this.children.add(node);
    }

    @Override
    public String toString() {
        return "QuantitativeNode{" +
                "character=" + quantitativeCharacter +
                ", measure=" + measure +
                ", children=" + children.size() +
                ", remainingTaxa=" + remainingTaxa.size() +
                '}';
    }
}
