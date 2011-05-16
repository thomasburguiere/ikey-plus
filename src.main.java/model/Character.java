package model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Causse
 * @created 11-may.-2011
 */
public class Character implements ICharacter {

	private String name = null;
	private String id = null;
	private ICharacter parentCharacter = null;
	private List<State> inapplicableStates = null;
	public List<ICharacter> childCharacters = null;
	
	/**
	 * constructor by default
	 */
	public Character(){
		this(null);
	}
	
	/**
	 * constructor with name parameter
	 */
	public Character(String name){
		super();
		this.name = name;
		this.inapplicableStates = new ArrayList<State>();
		this.childCharacters = new ArrayList<ICharacter>();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see model.ICharacter#isSupportsCategoricalData()
	 */
	@Override
	public boolean isSupportsCategoricalData() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see model.ICharacter#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see model.ICharacter#setId(java.lang.String)
	 */
	@Override
	public void setId(String id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see model.ICharacter#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see model.ICharacter#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	
	/* (non-Javadoc)
	 * @see model.ICharacter#getInapplicableStates()
	 */
	@Override
	public List<State> getInapplicableStates() {
		return inapplicableStates;
	}

	/* (non-Javadoc)
	 * @see model.ICharacter#setInapplicableStates(java.util.List)
	 */
	@Override
	public void setInapplicableStates(List<State> inapplicableStates) {
		this.inapplicableStates = inapplicableStates;
	}

	/* (non-Javadoc)
	 * @see model.ICharacter#getParentCharacter()
	 */
	@Override
	public ICharacter getParentCharacter() {
		return parentCharacter;
	}

	/* (non-Javadoc)
	 * @see model.ICharacter#setParentCharacter(model.ICharacter)
	 */
	@Override
	public void setParentCharacter(ICharacter parentCharacter) {
		this.parentCharacter = parentCharacter;
		this.parentCharacter.getChildCharacters().add(this);
	}

	/* (non-Javadoc)
	 * @see model.ICharacter#getChildCharacters()
	 */
	@Override
	public List<ICharacter> getChildCharacters() {
		return this.childCharacters;
	}

	/* (non-Javadoc)
	 * @see model.ICharacter#setChildCharacters(java.util.List)
	 */
	@Override
	public void setChildCharacters(List<ICharacter> childCharacters) {
		this.childCharacters = childCharacters;
	}
	
	
}
