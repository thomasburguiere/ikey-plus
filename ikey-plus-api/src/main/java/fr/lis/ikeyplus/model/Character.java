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
    public float weight = IkeyConfig.DEFAULT_WEIGHT.getIntWeight();
    private ICharacter parentCharacter = null;
    private List<State> inapplicableStates = null;
    private List<ICharacter> childCharacters = null;
    private List<String> mediaObjectKeys = null;

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

    /* (non-Javadoc)
     *
     * @see model.ICharacter#isSupportsCategoricalData() */
    @Override
    public boolean isSupportsCategoricalData() {
        return false;
    }

    /* (non-Javadoc)
     *
     * @see model.ICharacter#getId() */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     *
     * @see model.ICharacter#setId(java.lang.String) */
    @Override
    public void setId(final String id) {
        this.id = id;
    }

    /* (non-Javadoc)
     *
     * @see model.ICharacter#getName() */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     *
     * @see model.ICharacter#setName(java.lang.String) */
    @Override
    public void setName(final String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     *
     * @see model.ICharacter#getweight() */
    @Override
    public float getWeight() {
        return weight;
    }

    /* (non-Javadoc)
     *
     * @see model.ICharacter#setWeight(int) */
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
                && dataset.getMediaObject(mediaObjectKeys.get(0)).startsWith("http")) {
            return dataset.getMediaObject(mediaObjectKeys.get(0));
        }
        return null;
    }

    /* (non-Javadoc)
     *
     * @see model.ICharacter#getInapplicableStates() */
    @Override
    public List<State> getInapplicableStates() {
        return inapplicableStates;
    }

    /* (non-Javadoc)
     *
     * @see model.ICharacter#setInapplicableStates(java.util.List) */
    @Override
    public void setInapplicableStates(final List<State> inapplicableStates) {
        this.inapplicableStates = inapplicableStates;
    }

    /* (non-Javadoc)
     *
     * @see model.ICharacter#getParentCharacter() */
    @Override
    public ICharacter getParentCharacter() {
        return parentCharacter;
    }

    /* (non-Javadoc)
     *
     * @see model.ICharacter#setParentCharacter(model.ICharacter) */
    @Override
    public void setParentCharacter(final ICharacter parentCharacter) {
        this.parentCharacter = parentCharacter;
        this.parentCharacter.getChildCharacters().add(this);
    }

    /* (non-Javadoc)
     *
     * @see model.ICharacter#getChildCharacters() */
    @Override
    public List<ICharacter> getChildCharacters() {
        return childCharacters;
    }

    /* (non-Javadoc)
     *
     * @see model.ICharacter#setChildCharacters(java.util.List) */
    @Override
    public void setChildCharacters(final List<ICharacter> childCharacters) {
        this.childCharacters = childCharacters;
    }

    /* (non-Javadoc)
     *
     * @see model.ICharacter#getAllChildren() */
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

    @Override
    public ICharacter clone() {
        ICharacter newCharacter = null;
        if (isSupportsCategoricalData()) {
            newCharacter = new CategoricalCharacter();
            ((CategoricalCharacter) newCharacter).setStates(((CategoricalCharacter) this).getStates());
        } else {
            newCharacter = new QuantitativeCharacter();
        }
        newCharacter.setChildCharacters(getChildCharacters());
        newCharacter.setId(getId());
        newCharacter.setInapplicableStates(getInapplicableStates());
        newCharacter.setName(getName());
        newCharacter.setParentCharacter(getParentCharacter());
        return newCharacter;
    }

    public String toString() {
        return name;
    }
}
