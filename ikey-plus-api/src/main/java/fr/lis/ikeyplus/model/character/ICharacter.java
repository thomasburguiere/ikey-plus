package fr.lis.ikeyplus.model.character;

import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.model.State;

import java.util.List;

/**
 * Interface of Character
 *
 * @author Florian Causse
 * @created 06-04-2011
 */
public interface ICharacter {

    boolean isCategorical();

    String getName();

    void setName(String name);

    String getId();

    void setId(String id);

    float getWeight();

    void setWeight(float weight);

    List<State> getInapplicableStates();

    void setInapplicableStates(List<State> inapplicableStates);

    ICharacter getParentCharacter();

    void setParentCharacter(ICharacter parentCharacter);

    List<ICharacter> getChildCharacters();

    void setChildCharacters(List<ICharacter> childCharacters);

    List<ICharacter> getAllChildren();

    List<String> getMediaObjectKeys();

    void setMediaObjectKeys(List<String> mediaObjects);

    String getFirstImage(DataSet dataset);

    boolean isQuantitative();
}