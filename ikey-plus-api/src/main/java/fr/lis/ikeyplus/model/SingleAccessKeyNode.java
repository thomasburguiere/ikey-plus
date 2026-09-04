package fr.lis.ikeyplus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a node of single access key
 *
 * @author Florian Causse
 */
public class SingleAccessKeyNode {

    private ICharacter character;
    private Object characterState;
    private List<Object> otherCharacterStates;
    private List<SingleAccessKeyNode> children;
    private String nodeDescription = null;
    private List<Taxon> remainingTaxa;

    public SingleAccessKeyNode() throws OutOfMemoryError, Exception {
        this(null, null);
    }

    public SingleAccessKeyNode(final ICharacter character, final Object characterState) throws OutOfMemoryError {
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
        otherCharacterStates.add(otherCharacterState);
    }

    public String getStringStates() {
        return getStatesToString(" OR ");
    }

    public String getStatesToString(final String separator) {

        final StringBuilder result = new StringBuilder();
        if (characterState instanceof State) {
            result.append(((State) characterState).getName());
            for (final Object state : otherCharacterStates) {
                if (state instanceof State) {
                    result.append(separator).append(((State) state).getName());
                }
            }
        }
        return result.toString();
    }

    public List<State> getStates() {
        if (characterState instanceof State) {
            final List<State> states = new ArrayList<>();
            states.add((State) characterState);
            for (final Object state : otherCharacterStates) {
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
        children.add(singleAccessKeyNode);
    }

    public List<Taxon> getRemainingTaxa() {
        return remainingTaxa;
    }

    public void setRemainingTaxa(final List<Taxon> remainingTaxa) {
        this.remainingTaxa = remainingTaxa;
    }

    public boolean hasChild() {
        return !children.isEmpty();
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
        for (final SingleAccessKeyNode childNode : children) {
            if (childNode.character.isSupportsCategoricalData()
                    && ((State) childNode.characterState).getFirstImageKey() != null
                    && dataSet.getMediaObject(((State) childNode.characterState).getFirstImageKey()) != null
                    && dataSet.getMediaObject(((State) childNode.characterState).getFirstImageKey())
                    .startsWith("http")) {
                return true;
            }
        }
        return false;
    }

}
