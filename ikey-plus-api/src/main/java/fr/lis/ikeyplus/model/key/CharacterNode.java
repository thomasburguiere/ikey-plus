package fr.lis.ikeyplus.model.key;

import fr.lis.ikeyplus.model.character.ICharacter;
import fr.lis.ikeyplus.model.description.CharacterState;

import java.util.List;

public abstract sealed class CharacterNode implements BaseNode permits QuantitativeNode, CategoricalNode {

    private String nodeDescription;

    public abstract ICharacter getCharacter();

    public abstract CharacterState getCharacterState();

    public String getStringStates() {
        return "";
    }

    public String getNodeDescription () {
        return this.nodeDescription;
    }

    public void setNodeDescription(String description) {
        this.nodeDescription = description;
    }
}
