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

    public SingleAccessKeyNode() throws OutOfMemoryError {
        this(null, null);
    }

    public SingleAccessKeyNode(ICharacter character, Object characterState) throws OutOfMemoryError {
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

    public void setCharacter(ICharacter character) {
        this.character = character;
    }

    public Object getCharacterState() {
        return characterState;
    }

    public void setCharacterState(Object characterState) {
        this.characterState = characterState;
    }

    public List<Object> getOtherCharacterStates() {
        return otherCharacterStates;
    }

    public void setOtherCharacterStates(List<Object> otherCharacterStates) {
        this.otherCharacterStates = otherCharacterStates;
    }

    public void addOtherCharacterStates(Object otherCharacterState) {
        this.otherCharacterStates.add(otherCharacterState);
    }

    public String getStringStates() {
        return getStatesToString(" OR ");
    }

    public String getNodeLabel(){
        final StringBuilder output = new StringBuilder();
        if (children.size() == 0) {
            output.append(" -> ");
            output.append(getNodeTaxaLabel());
        } else {
            output.append(" (items=").append(remainingTaxa.size()).append(")");
        }
        return output.toString();
    }

    public String getNodeTaxaLabel() {
        boolean firstLoop = true;
        final StringBuilder output = new StringBuilder();
        for (Taxon taxon : remainingTaxa) {
            if (!firstLoop) {
                output.append(", ");
            }
            output.append(taxon.getName());
            firstLoop = false;
        }
        return output.toString();
    }

    public String getStatesToString(String separator) {

        StringBuilder result = new StringBuilder("");
        if (characterState instanceof State) {
            result.append(((State) characterState).getName());
            for (Object state : otherCharacterStates) {
                if (state instanceof State) {
                    result.append(separator).append(((State) state).getName());
                }
            }
        }
        return result.toString();
    }

    public List<State> getStates() {
        if (this.characterState instanceof State) {
            List<State> states = new ArrayList<>();
            states.add((State) characterState);
            for (Object state : otherCharacterStates) {
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

    public void setChildren(List<SingleAccessKeyNode> children) {
        this.children = children;
    }

    public void addChild(SingleAccessKeyNode singleAccessKeyNode) {
        this.children.add(singleAccessKeyNode);
    }

    public List<Taxon> getRemainingTaxa() {
        return remainingTaxa;
    }

    public void setRemainingTaxa(List<Taxon> remainingTaxa) {
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

    public void setNodeDescription(String nodeDescription) {
        this.nodeDescription = nodeDescription;
    }

    public String toString() {
        return character.toString() + " --> " + characterState.toString();
    }

    public boolean isChildrenContainsImages(DataSet dataSet) {
        for (SingleAccessKeyNode childNode : children) {
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
