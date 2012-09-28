package fr.lis.ikeyplus.model;

import java.util.ArrayList;
import java.util.List;

import fr.lis.ikeyplus.utils.Utils;

/**
 * This class represents a Character
 * 
 * @author Florian Causse
 * @created 11-05-2011
 */
public class Character implements ICharacter {

	private String name = null;
	private String id = null;
	public float weight = Utils.DEFAULT_WEIGHT;
	private ICharacter parentCharacter = null;
	private List<State> inapplicableStates = null;
	private List<ICharacter> childCharacters = null;
	private List<String> mediaObjectKeys = null;

	/**
	 * constructor by default
	 */
	public Character() {
		this(null);
	}

	/**
	 * constructor with name parameter
	 */
	public Character(String name) {
		super();
		this.name = name;
		this.inapplicableStates = new ArrayList<State>();
		this.childCharacters = new ArrayList<ICharacter>();
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
	public void setId(String id) {
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
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * 
	 * @see model.ICharacter#getweight() */
	@Override
	public float getWeight() {
		return this.weight;
	}

	/* (non-Javadoc)
	 * 
	 * @see model.ICharacter#setWeight(int) */
	@Override
	public void setWeight(float weight) {
		this.weight = weight;

	}

	/**
	 * @return the mediaObjectKey
	 */
	public List<String> getMediaObjectKeys() {
		return mediaObjectKeys;
	}

	/**
	 * @param mediaObjectKey
	 *            the mediaObjectKey to set
	 */
	public void setMediaObjectKeys(List<String> mediaObjectKey) {
		this.mediaObjectKeys = mediaObjectKey;
	}

	public String getFirstImage(DataSet dataset) {
		if (dataset != null && mediaObjectKeys != null && mediaObjectKeys.size() > 0) {
			if (dataset.getMediaObject(mediaObjectKeys.get(0)).startsWith("http")) {
				return dataset.getMediaObject(mediaObjectKeys.get(0));
			}
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
	public void setInapplicableStates(List<State> inapplicableStates) {
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
	public void setParentCharacter(ICharacter parentCharacter) {
		this.parentCharacter = parentCharacter;
		this.parentCharacter.getChildCharacters().add(this);
	}

	/* (non-Javadoc)
	 * 
	 * @see model.ICharacter#getChildCharacters() */
	@Override
	public List<ICharacter> getChildCharacters() {
		return this.childCharacters;
	}

	/* (non-Javadoc)
	 * 
	 * @see model.ICharacter#setChildCharacters(java.util.List) */
	@Override
	public void setChildCharacters(List<ICharacter> childCharacters) {
		this.childCharacters = childCharacters;
	}

	/* (non-Javadoc)
	 * 
	 * @see model.ICharacter#getAllChildren() */
	@Override
	public List<ICharacter> getAllChildren() {
		List<ICharacter> allChildrenCharacter = new ArrayList<ICharacter>();
		addChildrenToList(allChildrenCharacter, this);
		return allChildrenCharacter;
	}

	/**
	 * add all the child character in a list
	 * 
	 * @param allChildrenCharacter
	 *            , the list of character
	 * @param character
	 *            , the current character
	 */
	private void addChildrenToList(List<ICharacter> allChildrenCharacter, ICharacter character) {
		for (ICharacter childCharacter : character.getChildCharacters()) {
			allChildrenCharacter.add(childCharacter);
			addChildrenToList(allChildrenCharacter, childCharacter);
		}
	}

	/**
	 * clone the character
	 * 
	 * @return ICharacter, the cloned character
	 */
	public ICharacter clone() {
		ICharacter newCharacter = null;
		if (this.isSupportsCategoricalData()) {
			newCharacter = new CategoricalCharacter();
			((CategoricalCharacter) newCharacter).setStates(((CategoricalCharacter) this).getStates());
		} else {
			newCharacter = new QuantitativeCharacter();
		}
		newCharacter.setChildCharacters(this.getChildCharacters());
		newCharacter.setId(this.getId());
		newCharacter.setInapplicableStates(this.getInapplicableStates());
		newCharacter.setName(this.getName());
		newCharacter.setParentCharacter(this.getParentCharacter());
		return newCharacter;
	}

	/**
	 * get the string value characterizing the current character
	 * 
	 * @return String, the character name
	 */
	public String toString() {
		return this.name;
	}
}
