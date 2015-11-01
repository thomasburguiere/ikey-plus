package fr.lis.ikeyplus.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents the description of a taxon, i.e. the association of characters, and their
 * character-states for this taxon
 *
 * @author Florian Causse
 * @created 06-04-2011
 */
public class CodedDescription {

    private String id = null;
    private Map<ICharacter, Object> description = null;
    private Map<ICharacter, Integer> characterWeights = null;

    /**
     * the constructor by default
     */
    public CodedDescription() {
        this.description = new LinkedHashMap<ICharacter, Object>();
        this.characterWeights = new LinkedHashMap<ICharacter, Integer>();
    }

    /**
     * get the description (all Characters)
     *
     * @return Map<ICharacter, Object>
     */
    public Map<ICharacter, Object> getDescription() {
        return description;
    }

    /**
     * set the description of all the characters
     *
     * @param Map <ICharacter, Object>
     */
    public void setDescription(Map<ICharacter, Object> description) {
        this.description = description;
    }

    /**
     * get all the character weights
     *
     * @return Map<ICharacter, Integer>
     */
    public Map<ICharacter, Integer> getCharacterWeights() {
        return characterWeights;
    }

    /**
     * set all the character weights
     *
     * @param Map <ICharacter, Integer>
     */
    public void setCharacterWeights(Map<ICharacter, Integer> characterWeights) {
        this.characterWeights = characterWeights;
    }

    /**
     * get a character weight
     *
     * @param ICharacter
     * @return Integer
     */
    public Integer getCharacterWeight(ICharacter character) {
        return characterWeights.get(character);
    }

    /**
     * add a character weight
     *
     * @param ICharacter
     * @param Integer
     */
    public void addCharacterWeight(ICharacter character, Integer weight) {
        characterWeights.put(character, weight);
    }

    /**
     * remove a character weight
     *
     * @param ICharacter character
     */
    public void removeCharacterWeight(ICharacter character) {
        characterWeights.remove(character);
    }

    /**
     * get the description for one character
     *
     * @param ICharacter , the key
     * @return Object, the character description
     */
    public Object getCharacterDescription(ICharacter character) {
        return description.get(character);
    }

    /**
     * add the description for one character
     *
     * @param ICharacter , the key
     * @param Object     , the description concerning the character
     */
    public void addCharacterDescription(ICharacter character, Object characterDescription) {
        description.put(character, characterDescription);
    }

    /**
     * remove the description for one character
     *
     * @param ICharacter , the key
     */
    public void removeCharacterDescription(ICharacter character) {
        description.remove(character);
    }

    /**
     * get the identifier
     *
     * @return String, CodedDescription identifier
     */
    public String getId() {
        return id;
    }

    /**
     * set the identifier
     *
     * @param String , CodedDescription identifier
     */
    public void setId(String id) {
        this.id = id;
    }
}