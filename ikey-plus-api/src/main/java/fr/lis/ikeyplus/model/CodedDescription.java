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

    public void setDescription(final Map<ICharacter, Object> description) {
        this.description = description;
    }

    public Map<ICharacter, Integer> getCharacterWeights() {
        return characterWeights;
    }

    public void setCharacterWeights(final Map<ICharacter, Integer> characterWeights) {
        this.characterWeights = characterWeights;
    }

    public Integer getCharacterWeight(final ICharacter character) {
        return characterWeights.get(character);
    }

    public void addCharacterWeight(final ICharacter character, final Integer weight) {
        characterWeights.put(character, weight);
    }

    public void removeCharacterWeight(final ICharacter character) {
        characterWeights.remove(character);
    }

    public Object getCharacterDescription(final ICharacter character) {
        return description.get(character);
    }

    public void addCharacterDescription(final ICharacter character, final Object characterDescription) {
        description.put(character, characterDescription);
    }

    public void removeCharacterDescription(final ICharacter character) {
        description.remove(character);
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }
}