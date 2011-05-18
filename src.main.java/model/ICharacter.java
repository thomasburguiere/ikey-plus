package model;

import java.util.List;

/**
 * @author Florian Causse
 * @created 06-avr.-2011
 */
public interface ICharacter {

	public String name = null;
	public String id = null;
	public ICharacter parentCharacter = null;
	public List<State> inapplicableStates = null;
	public List<ICharacter> childCharacters = null;

	public boolean isSupportsCategoricalData();

	/**
	 * getter for name
	 * 
	 * @return String, the name
	 */
	public String getName();

	/**
	 * setter for name
	 * 
	 * @param String
	 *            , the name
	 */
	public void setName(String name);

	/**
	 * @return String, Character identifier
	 */
	public String getId();

	/**
	 * @param String
	 *            , Character identifier
	 */
	public void setId(String id);

	/**
	 * @return List<State>, the list of inapplicable states
	 */
	public List<State> getInapplicableStates();

	/**
	 * @param List
	 *            <State> , the list of inapplicable states
	 */
	public void setInapplicableStates(List<State> inapplicableStates);

	/**
	 * @return ICharacter, the parent character
	 */
	public ICharacter getParentCharacter();

	/**
	 * @param ICharacter
	 *            , the parent character
	 */
	public void setParentCharacter(ICharacter parentCharacter);

	/**
	 * @return List<ICharacter>, the list of child characters
	 */
	public List<ICharacter> getChildCharacters();

	/**
	 * @param List
	 *            <ICharacter>, the list of child characters
	 */
	public void setChildCharacters(List<ICharacter> childCharacters);

	/**
	 * @param List
	 *            <ICharacter>, the list of all child characters
	 */
	public List<ICharacter> getAllChildren();
}