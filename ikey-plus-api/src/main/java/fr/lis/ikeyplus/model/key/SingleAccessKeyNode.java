package fr.lis.ikeyplus.model.key;

import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.model.Taxon;
import fr.lis.ikeyplus.model.character.ICharacter;
import fr.lis.ikeyplus.model.description.CharacterState;
import fr.lis.ikeyplus.model.description.State;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a node of single access key
 *
 * @author Florian Causse
 */
public class SingleAccessKeyNode {

    private ICharacter character;
    private CharacterState characterState;
    private List<CharacterState> otherCharacterStates;
    private List<SingleAccessKeyNode> children;
    private String nodeDescription = null;
    private List<Taxon> remainingTaxa;

    public SingleAccessKeyNode() throws OutOfMemoryError, Exception {
        this(null, null);
    }

    public SingleAccessKeyNode(final ICharacter character, final CharacterState characterState) throws OutOfMemoryError {
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

    public CharacterState getCharacterState() {
        return characterState;
    }

    public void setCharacterState(final CharacterState characterState) {
        this.characterState = characterState;
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
            if (childNode instanceof final CategoricalNode catNode
                    && catNode.getSelectedState().getFirstImageKey() != null
                    && dataSet.getMediaObject(catNode.getSelectedState().getFirstImageKey()) != null
                    && dataSet.getMediaObject(catNode.getSelectedState().getFirstImageKey())
                    .startsWith("http")) {
                return true;
            }
        }
        return false;
    }

    public String getStringStates() {
        return "";
    }

}
