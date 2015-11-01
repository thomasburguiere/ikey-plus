package fr.lis.ikeyplus.model;

import java.util.List;

/**
 * Interface of Character
 *
 * @author Florian Causse
 * @created 06-04-2011
 */
public interface ICharacter {

    public String name = null;
    public String id = null;
    public float weight = 3;
    public ICharacter parentCharacter = null;
    public List<State> inapplicableStates = null;
    public List<ICharacter> childCharacters = null;
    public List<String> mediaObjectKeys = null;

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
     * @param String , the name
     */
    public void setName(String name);

    /**
     * getter for ID
     *
     * @return String, Character identifier
     */
    public String getId();

    /**
     * setter for ID
     *
     * @param String , Character identifier
     */
    public void setId(String id);

    /**
     * getter for weight
     *
     * @return int, Character weight
     */
    public float getWeight();

    /**
     * setter for weight
     *
     * @param int , Character weight
     */
    public void setWeight(float weight);

    /**
     * get the inapplicable states
     *
     * @return List<State>, the list of inapplicable states
     */
    public List<State> getInapplicableStates();

    /**
     * set the inapplicable states
     *
     * @param List <State> , the list of inapplicable states
     */
    public void setInapplicableStates(List<State> inapplicableStates);

    /**
     * get the parent character
     *
     * @return ICharacter, the parent character
     */
    public ICharacter getParentCharacter();

    /**
     * set the parent character
     *
     * @param ICharacter , the parent character
     */
    public void setParentCharacter(ICharacter parentCharacter);

    /**
     * get all child characters
     *
     * @return List<ICharacter>, the list of child characters
     */
    public List<ICharacter> getChildCharacters();

    /**
     * set the child characters
     *
     * @param List <ICharacter>, the list of child characters
     */
    public void setChildCharacters(List<ICharacter> childCharacters);

    /**
     * get all children in the hierarchies
     *
     * @param List <ICharacter>, the list of all child characters
     */
    public List<ICharacter> getAllChildren();

    /**
     * @return
     */
    public List<String> getMediaObjectKeys();

    /**
     * @param mediaObjects
     */
    public void setMediaObjectKeys(List<String> mediaObjects);

    /**
     * @param dataset
     * @return
     */
    public String getFirstImage(DataSet dataset);
}