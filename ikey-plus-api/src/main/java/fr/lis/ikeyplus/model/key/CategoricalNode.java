package fr.lis.ikeyplus.model.key;

import fr.lis.ikeyplus.model.character.CategoricalCharacter;
import fr.lis.ikeyplus.model.character.ICharacter;
import fr.lis.ikeyplus.model.description.State;

import java.util.ArrayList;
import java.util.List;

public class CategoricalNode extends SingleAccessKeyNode {

    private final CategoricalCharacter character;
    private final State selectedState;
    private final List<State> otherCharacterStates = new ArrayList<>();

    public CategoricalNode(final CategoricalCharacter character, final State selectedState) throws OutOfMemoryError, Exception {
        super(character, selectedState);
        this.character = character;
        this.selectedState = selectedState;
    }

    public State getSelectedState() {
        return selectedState;
    }

    @Override
    public ICharacter getCharacter() {
        return character;
    }

    @Override
    public String getStringStates() {
        return getStatesToString(" OR ");
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
}
