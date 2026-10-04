package fr.lis.ikeyplus.model.key;

import fr.lis.ikeyplus.model.character.ICharacter;
import fr.lis.ikeyplus.model.description.CharacterState;

public abstract sealed class CharacterNode implements BaseNode permits CategoricalNode, UndescribedHoldingNode, QuantitativeNode {

    private String nodeDescription;

    public abstract ICharacter getCharacter();

    public abstract CharacterState getCharacterState();

    public String getStringStates() {
        return "";
    }

    public String getNodeDescription () {
        return nodeDescription;
    }

    public void setNodeDescription(final String description) {
        this.nodeDescription = description;
    }
}
