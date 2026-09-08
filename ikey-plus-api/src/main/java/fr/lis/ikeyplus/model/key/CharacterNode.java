package fr.lis.ikeyplus.model.key;

import fr.lis.ikeyplus.model.character.ICharacter;
import fr.lis.ikeyplus.model.description.CharacterState;

import java.util.List;

public sealed interface CharacterNode extends BaseNode permits QuantitativeNode, CategoricalNode {

    ICharacter getCharacter();

//    void setCharacter(ICharacter character);

    CharacterState getCharacterState();

//    void setCharacterState(CharacterState characterState);

    default String getStringStates() {
        return "";
    }

    void setNodeDescription(String description);
}
