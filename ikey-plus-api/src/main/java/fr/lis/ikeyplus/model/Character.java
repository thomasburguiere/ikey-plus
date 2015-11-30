package fr.lis.ikeyplus.model;

import fr.lis.ikeyplus.utils.IkeyConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Character
 *
 * @author Florian Causse
 */
public class Character implements ICharacter {

    private String name = null;
    private String id = null;
    private float weight = IkeyConfig.DEFAULT_WEIGHT.getIntWeight();
    private ICharacter parentCharacter = null;
    private List<State> inapplicableStates = null;
    private List<ICharacter> childCharacters = null;
    private List<String> mediaObjectKeys = null;

    public Character() {
        this.name = null;
        this.inapplicableStates = new ArrayList<>();
        this.childCharacters = new ArrayList<>();
        this.mediaObjectKeys = new ArrayList<>();
    }

    @Override
    public boolean isSupportsCategoricalData() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public float getWeight() {
        return weight;
    }

    @Override
    public void setWeight(float weight) {
        this.weight = weight;

    }

    @Override
    public List<String> getMediaObjectKeys() {
        return mediaObjectKeys;
    }

    @Override
    public List<State> getInapplicableStates() {
        return inapplicableStates;
    }

    @Override
    public ICharacter getParentCharacter() {
        return parentCharacter;
    }

    @Override
    public void setParentCharacter(ICharacter parentCharacter) {
        this.parentCharacter = parentCharacter;
        this.parentCharacter.getChildCharacters().add(this);
    }

    @Override
    public List<ICharacter> getChildCharacters() {
        return childCharacters;
    }

    @Override
    public List<ICharacter> getAllChildren() {
        List<ICharacter> allChildrenCharacter = new ArrayList<>();
        addChildrenToList(allChildrenCharacter, this);
        return allChildrenCharacter;
    }

    private void addChildrenToList(List<ICharacter> allChildrenCharacter, ICharacter character) {
        for (ICharacter childCharacter : character.getChildCharacters()) {
            allChildrenCharacter.add(childCharacter);
            addChildrenToList(allChildrenCharacter, childCharacter);
        }
    }

    public String toString() {
        return name;
    }
}
