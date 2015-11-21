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

    private Map<ICharacter, Object> description = null;
    private Map<ICharacter, Integer> characterWeights = null;

    public CodedDescription() {
        this.description = new LinkedHashMap<>();
        this.characterWeights = new LinkedHashMap<>();
    }

    public Map<ICharacter, Integer> getCharacterWeights() {
        return characterWeights;
    }

    public Integer getCharacterWeight(ICharacter character) {
        return characterWeights.get(character);
    }

    public void addCharacterWeight(ICharacter character, Integer weight) {
        characterWeights.put(character, weight);
    }

    public Object getCharacterDescription(ICharacter character) {
        return description.get(character);
    }

    public void addCharacterDescription(ICharacter character, Object characterDescription) {
        description.put(character, characterDescription);
    }

}