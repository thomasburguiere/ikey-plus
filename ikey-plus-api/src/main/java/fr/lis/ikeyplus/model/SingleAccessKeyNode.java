package fr.lis.ikeyplus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a node of single access key
 *
 * @author Florian Causse
 */
public class SingleAccessKeyNode {

    private ICharacter character = null;
    private Object characterState = null;
    private List<Object> otherCharacterStates = null;
    private List<SingleAccessKeyNode> children = null;
    private String nodeDescription = null;
    private List<Taxon> remainingTaxa = null;

    public SingleAccessKeyNode() throws OutOfMemoryError, Exception {
        this(null, null);
    }

    public SingleAccessKeyNode(final ICharacter character, final Object characterState) throws OutOfMemoryError,
            Exception {
        super();
        this.character = character;
        this.characterState = characterState;
        this.otherCharacterStates = new ArrayList<>();
        this.children = new ArrayList<>();
        this.remainingTaxa = new ArrayList<>();
    }

    public ICharacter getCharacter() {
        return character;
    }

    public void setCharacter(final ICharacter character) {
        this.character = character;
    }

    public Object getCharacterState() {
        return characterState;
    }

    public void setCharacterState(final Object characterState) {
        this.characterState = characterState;
    }

    public List<Object> getOtherCharacterStates() {
        return otherCharacterStates;
    }

    public void setOtherCharacterStates(final List<Object> otherCharacterStates) {
        this.otherCharacterStates = otherCharacterStates;
    }

    public void addOtherCharacterStates(final Object otherCharacterState) {
        this.otherCharacterStates.add(otherCharacterState);
    }

    public String getStringStates() {
        return getStatesToString(" OR ");
    }

    public String getStatesToString(final String separator) {

        final StringBuilder result = new StringBuilder("");
        if (this.getCharacterState() instanceof State) {
            result.append(((State) this.getCharacterState()).getName());
            for (final Object state : this.getOtherCharacterStates()) {
                if (state instanceof State) {
                    result.append(separator).append(((State) state).getName());
                }
            }
        }
        return result.toString();
    }

    public List<State> getStates() {
        if (this.characterState instanceof State) {
            final List<State> states = new ArrayList<>();
            states.add((State) this.getCharacterState());
            for (final Object state : this.getOtherCharacterStates()) {
                if (state instanceof State) {
                    states.add((State) state);
                }
            }
            return states;
        }
        return null;
    }

    public List<SingleAccessKeyNode> getChildren() {
        return children;
    }

    public void setChildren(final List<SingleAccessKeyNode> children) {
        this.children = children;
    }

    public void addChild(final SingleAccessKeyNode singleAccessKeyNode) {
        this.children.add(singleAccessKeyNode);
    }

    public List<Taxon> getRemainingTaxa() {
        return remainingTaxa;
    }

    public void setRemainingTaxa(final List<Taxon> remainingTaxa) {
        this.remainingTaxa = remainingTaxa;
    }

    public boolean hasChild() {
        return children.size() > 0;
    }

    public boolean isEmpty() {
        return character == null || characterState == null;
    }

    public String getNodeDescription() {
        return nodeDescription;
    }

    public void setNodeDescription(final String nodeDescription) {
        this.nodeDescription = nodeDescription;
    }

    public String toString() {
        return character.toString() + " --> " + characterState.toString();
    }

    public boolean isChildrenContainsImages(final DataSet dataSet) {
        for (final SingleAccessKeyNode childNode : this.getChildren()) {
            if (childNode.getCharacter().isSupportsCategoricalData()
                    && ((State) childNode.getCharacterState()).getFirstImageKey() != null
                    && dataSet.getMediaObject(((State) childNode.getCharacterState()).getFirstImageKey()) != null
                    && dataSet.getMediaObject(((State) childNode.getCharacterState()).getFirstImageKey())
                    .startsWith("http")) {
                return true;
            }
        }
        return false;
    }

}
