package fr.lis.ikeyplus.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents the description of a taxon, i.e. the association of characters, and their
 * character-states for this taxon
 *
 * @author Florian Causse
 */
public class CodedDescription {

    private String id = null;
    private Map<ICharacter, Object> description = null;
    private Map<ICharacter, Integer> characterWeights = null;

    public CodedDescription() {
        this.description = new LinkedHashMap<>();
        this.characterWeights = new LinkedHashMap<>();
    }

    public Map<ICharacter, Object> getDescription() {
        return description;
    }

    public void setDescription(Map<ICharacter, Object> description) {
        this.description = description;
    }

    public Map<ICharacter, Integer> getCharacterWeights() {
        return characterWeights;
    }

    public void setCharacterWeights(Map<ICharacter, Integer> characterWeights) {
        this.characterWeights = characterWeights;
    }

    public Integer getCharacterWeight(ICharacter character) {
        return characterWeights.get(character);
    }

    public void addCharacterWeight(ICharacter character, Integer weight) {
        characterWeights.put(character, weight);
    }

    public void removeCharacterWeight(ICharacter character) {
        characterWeights.remove(character);
    }

    public Object getCharacterDescription(ICharacter character) {
        return description.get(character);
    }

    public void addCharacterDescription(ICharacter character, Object characterDescription) {
        description.put(character, characterDescription);
    }

    public void removeCharacterDescription(ICharacter character) {
        description.remove(character);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}