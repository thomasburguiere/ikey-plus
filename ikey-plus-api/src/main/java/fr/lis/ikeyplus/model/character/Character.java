package fr.lis.ikeyplus.model.character;

import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.model.State;
import fr.lis.ikeyplus.utils.IkeyConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Character
 *
 * @author Florian Causse
 */
public abstract class Character implements ICharacter {

    private String name;
    private String id = null;
    private float weight = IkeyConfig.DEFAULT_WEIGHT.getIntWeight();
    private ICharacter parentCharacter = null;
    private List<State> inapplicableStates;
    private List<ICharacter> childCharacters;
    private List<String> mediaObjectKeys;

    public Character() {
        this(null);
    }

    public Character(final String name) {
        super();
        this.name = name;
        this.inapplicableStates = new ArrayList<>();
        this.childCharacters = new ArrayList<>();
        this.mediaObjectKeys = new ArrayList<>();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public float getWeight() {
        return weight;
    }

    @Override
    public void setWeight(final float weight) {
        this.weight = weight;

    }

    @Override
    public List<String> getMediaObjectKeys() {
        return mediaObjectKeys;
    }

    @Override
    public void setMediaObjectKeys(final List<String> mediaObjectKey) {
        this.mediaObjectKeys = mediaObjectKey;
    }

    @Override
    public String getFirstImage(final DataSet dataset) {
        if (dataset != null && mediaObjectKeys != null && !mediaObjectKeys.isEmpty()
                && dataset.getMediaObject(mediaObjectKeys.getFirst()).startsWith("http")) {
            return dataset.getMediaObject(mediaObjectKeys.getFirst());
        }
        return null;
    }

    @Override
    public List<State> getInapplicableStates() {
        return inapplicableStates;
    }

    @Override
    public void setInapplicableStates(final List<State> inapplicableStates) {
        this.inapplicableStates = inapplicableStates;
    }

    @Override
    public ICharacter getParentCharacter() {
        return parentCharacter;
    }

    @Override
    public void setParentCharacter(final ICharacter parentCharacter) {
        this.parentCharacter = parentCharacter;
        this.parentCharacter.getChildCharacters().add(this);
    }

    @Override
    public List<ICharacter> getChildCharacters() {
        return childCharacters;
    }

    @Override
    public void setChildCharacters(final List<ICharacter> childCharacters) {
        this.childCharacters = childCharacters;
    }

    @Override
    public List<ICharacter> getAllChildren() {
        final List<ICharacter> allChildrenCharacter = new ArrayList<>();
        addChildrenToList(allChildrenCharacter, this);
        return allChildrenCharacter;
    }

    private void addChildrenToList(final List<ICharacter> allChildrenCharacter, final ICharacter character) {
        for (final ICharacter childCharacter : character.getChildCharacters()) {
            allChildrenCharacter.add(childCharacter);
            addChildrenToList(allChildrenCharacter, childCharacter);
        }
    }

    public String toString() {
        return name;
    }
}
