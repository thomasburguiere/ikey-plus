package fr.lis.ikeyplus.model.character;

import fr.lis.ikeyplus.model.description.State;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Character of type categorical
 *
 * @author Florian Causse
 */
public class CategoricalCharacter extends Character {

    private List<State> states;

    public CategoricalCharacter() {
        this(null);
    }

    public CategoricalCharacter(final String name) {
        super();
        setName(name);
        this.states = new ArrayList<State>();
    }

    public List<State> getStates() {
        return states;
    }

    public void setStates(final List<State> states) {
        this.states = states;
    }

    @Override
    public boolean isCategorical() {
        return true;
    }

    @Override
    public boolean isQuantitative() {
        return false;
    }
}