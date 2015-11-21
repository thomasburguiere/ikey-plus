package fr.lis.ikeyplus.model;

import java.util.List;

/**
 * Interface of Character
 *
 * @author Florian Causse
 * @created 06-04-2011
 */
public interface ICharacter {

    String name = null;
    String id = null;
    float weight = 3;

    boolean isSupportsCategoricalData();

    String getName();

    void setName(String name);

    String getId();

    void setId(String id);

    float getWeight();

    void setWeight(float weight);

    List<State> getInapplicableStates();

    ICharacter getParentCharacter();

    void setParentCharacter(ICharacter parentCharacter);

    List<ICharacter> getChildCharacters();

    List<ICharacter> getAllChildren();

    List<String> getMediaObjectKeys();

}