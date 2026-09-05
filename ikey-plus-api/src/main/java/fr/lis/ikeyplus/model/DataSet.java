package fr.lis.ikeyplus.model;

import fr.lis.ikeyplus.model.character.CategoricalCharacter;
import fr.lis.ikeyplus.model.character.ICharacter;
import fr.lis.ikeyplus.model.description.CodedDescription;
import fr.lis.ikeyplus.model.description.State;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a knowledge base
 *
 * @author Florian Causse
 */
public class DataSet {

    private List<ICharacter> characters;
    private Map<Taxon, CodedDescription> codedDescriptions;
    private String label = null;
    private Map<String, String> mediaObjects;

    public DataSet() {
        characters = new ArrayList<>();
        codedDescriptions = new LinkedHashMap<>();
        mediaObjects = new LinkedHashMap<>();
    }

    public List<ICharacter> getCharacters() {
        return characters;
    }

    public void setCharacters(final List<ICharacter> characters) {
        this.characters = characters;
    }

    public Map<Taxon, CodedDescription> getCodedDescriptions() {
        return codedDescriptions;
    }

    public void setCodedDescriptions(final Map<Taxon, CodedDescription> codedDescriptions) {
        this.codedDescriptions = codedDescriptions;
    }

    public CodedDescription getCodedDescription(final Taxon taxon) {
        return codedDescriptions.get(taxon);
    }

    public void addCodedDescription(final Taxon taxon, final CodedDescription codedDescription) {
        codedDescriptions.put(taxon, codedDescription);
    }

    public void removeCodedDescription(final Taxon taxon) {
        codedDescriptions.remove(taxon);
    }

    public List<Taxon> getTaxa() {
        return new ArrayList<>(codedDescriptions.keySet());
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public ICharacter getCharacterById(final String id) {
        for (final ICharacter character : characters) {
            if (character.getId().equals(id)) {
                return character;
            }
        }
        return null;
    }

    public State getStateById(final String id) {
        for (final ICharacter character : characters) {
            if (character instanceof CategoricalCharacter) {
                for (final State state : ((CategoricalCharacter) character).getStates()) {
                    if (state.getId().equals(id)) {
                        return state;
                    }
                }
            }
        }
        return null;
    }

    public ICharacter getCharacterByState(final State state) {
        for (final ICharacter character : characters) {
            if (character instanceof CategoricalCharacter) {
                for (final State stateBis : ((CategoricalCharacter) character).getStates()) {
                    if (stateBis.equals(state)) {
                        return character;
                    }
                }
            }
        }
        return null;
    }

    public Map<String, String> getMediaObjects() {
        return mediaObjects;
    }

    public void setMediaObjects(final Map<String, String> mediaObjects) {
        this.mediaObjects = mediaObjects;
    }

    public String getMediaObject(final String key) {
        return mediaObjects.get(key);
    }

    public boolean isApplicable(final Taxon taxon, final ICharacter character) {
        if (character.getParentCharacter() != null && isApplicable(taxon, character.getParentCharacter())) {
            final List<State> inapplicableStates = character.getInapplicableStates();
            final List<State> states = getCodedDescription(taxon).getCategoricalCharacterDescription(
                    (CategoricalCharacter) character.getParentCharacter());

            // if the parent character is not described return true
            if (states == null) {
                return true;
            }
            // if one checked state is applicable
            for (final State state : states) {
                if (!inapplicableStates.contains(state)) {
                    return true;
                }
            }
            // if one checked state is inapplicable
            for (final State state : states) {
                if (inapplicableStates.contains(state)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * determine the list of characters inapplicable for the current state
     *
     * @return List<ICharacter>, the list of inapplicable character and all its sons
     */
    public static List<ICharacter> getInapplicableCharacters(final List<ICharacter> newRemainingCharacters,
                                                             final ICharacter selectedCharacter, final State state) {

        final List<ICharacter> inapplicableCharacter = new ArrayList<>();

        for (final ICharacter character : newRemainingCharacters) {
            if (character.getInapplicableStates().contains(state)) {
                inapplicableCharacter.add(character);
                inapplicableCharacter.addAll(character.getAllChildren());
            }

        }
        return inapplicableCharacter;
    }

}