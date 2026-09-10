package fr.lis.ikeyplus.model.key;

import fr.lis.ikeyplus.model.Taxon;
import fr.lis.ikeyplus.model.character.CategoricalCharacter;
import fr.lis.ikeyplus.model.character.ICharacter;
import fr.lis.ikeyplus.model.description.CharacterState;
import fr.lis.ikeyplus.model.description.State;

import java.util.ArrayList;
import java.util.List;

public final class CategoricalNode extends CharacterNode {

    private final CategoricalCharacter character;
    private final State selectedState;
    private final List<State> otherCharacterStates = new ArrayList<>();
    private final List<Taxon> remainingTaxa = new ArrayList<>();
    private String nodeDescription = null;
    private final List<CharacterNode> children;

    public CategoricalNode(final CategoricalCharacter character, final State selectedState) throws OutOfMemoryError, Exception {
//        super(character, selectedState);
        this.character = character;
        this.selectedState = selectedState;
        children = new ArrayList<>();
    }

    public State getSelectedState() {
        return selectedState;
    }

    @Override
    public ICharacter getCharacter() {
        return character;
    }


    @Override
    public CharacterState getCharacterState() {
        return selectedState;
    }

    @Override
    public String getStringStates() {
        return getStatesToString(" OR ");
    }

    @Override
    public void setNodeDescription(final String description) {
        nodeDescription = description;
    }

    public String getStatesToString(final String separator) {

        final StringBuilder result = new StringBuilder();
        result.append(selectedState.getName());
        for (final State state : otherCharacterStates) {
            result.append(separator).append(state.getName());
        }
        return result.toString();
    }

    public void addOtherCharacterStates(final State otherCharacterState) {
        otherCharacterStates.add(otherCharacterState);
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
        return "CategoricalNode{" +
                "character=" + character +
                ", selectedState=" + selectedState +
                ", otherCharacterStates=" + otherCharacterStates +
                ", remainingTaxa=" + remainingTaxa.size() +
                ", nodeDescription='" + nodeDescription + '\'' +
                ", children=" + children.size() +
                '}';
    }
}
