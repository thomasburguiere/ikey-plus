package fr.lis.ikeyplus.model.key;

import fr.lis.ikeyplus.model.Taxon;
import fr.lis.ikeyplus.model.character.ICharacter;
import fr.lis.ikeyplus.model.description.CharacterState;
import fr.lis.ikeyplus.model.description.State;

import java.util.List;

public final class UndescribedHoldingNode extends CharacterNode{
    private final ICharacter character;
    private final State state;

    public UndescribedHoldingNode(final ICharacter character, final State state) {
        this.character = character;
        this.state = state;
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
    public void setRemainingTaxa(final List<Taxon> taxa) {

    }

    @Override
    public boolean hasChild() {
        return false;
    }

    @Override
    public void addChild(final CharacterNode node) {

    }

    @Override
    public ICharacter getCharacter() {
        return null;
    }

    @Override
    public CharacterState getCharacterState() {
        return null;//state;
    }
}
