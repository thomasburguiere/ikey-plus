package fr.lis.ikeyplus.model.description;

import fr.lis.ikeyplus.model.character.CategoricalCharacter;
import fr.lis.ikeyplus.model.character.ICharacter;
import fr.lis.ikeyplus.model.character.QuantitativeCharacter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the description of a taxon, i.e. the association of characters, and their
 * character-states for this taxon
 *
 * @author Florian Causse
 */
public class CodedDescription {

    private String id = null;
    private final Map<CategoricalCharacter, List<State>> categoricalDescription;
    private final Map<QuantitativeCharacter, QuantitativeMeasure> quantitativeDescription;
    private final Map<ICharacter, Boolean> unknownData;
    private Map<ICharacter, Integer> characterWeights;

    public CodedDescription() {
        this.categoricalDescription = new LinkedHashMap<>();
        this.quantitativeDescription = new LinkedHashMap<>();
        this.unknownData = new LinkedHashMap<>();
        this.characterWeights = new LinkedHashMap<>();
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

    public boolean existsDescription(final ICharacter character) {
        return categoricalDescription.get(character) != null || quantitativeDescription.get(character) != null;
    }

    public void addCategoricalCharacterDescription(final CategoricalCharacter character, final List<State> characterDescription) {
        categoricalDescription.put(character, characterDescription);
    }

    public void addQuantitativeCharacterDescription(final QuantitativeCharacter character, final QuantitativeMeasure characterDescription) {
        quantitativeDescription.put(character, characterDescription);
    }

    public List<State> getCategoricalCharacterDescription(final CategoricalCharacter character) {
        return categoricalDescription.get(character);
    }

    public QuantitativeMeasure getQuantitativeCharacterDescription(final QuantitativeCharacter character) {
        return quantitativeDescription.get(character);
    }

    public void setUnknownDescription(final ICharacter character) {
        unknownData.put(character, true);
    }

    public boolean isUnknownDescription(final ICharacter character) {
        final var res = unknownData.get(character);
        return res != null && res;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }
}