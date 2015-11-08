package fr.lis.ikeyplus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Character of type categorical
 *
 * @author Florian Causse
 */
public class CategoricalCharacter extends Character {

    private List<State> states = null;

    public CategoricalCharacter() {
        this(null);
    }

    public CategoricalCharacter(String name) {
        super();
        this.setName(name);
        this.states = new ArrayList<State>();
    }

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }

    /* (non-Javadoc)
     *
     * @see model.Character#isSupportsCategoricalData() */
    @Override
    public boolean isSupportsCategoricalData() {
        return true;
    }
}