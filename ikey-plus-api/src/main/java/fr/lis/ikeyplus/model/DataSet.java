package fr.lis.ikeyplus.model;

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

    private List<ICharacter> characters = null;
    private Map<Taxon, CodedDescription> codedDescriptions = null;
    private String label = null;
    private Map<String, String> mediaObjects = null;

    public DataSet() {
        characters = new ArrayList<>();
        codedDescriptions = new LinkedHashMap<>();
        mediaObjects = new LinkedHashMap<>();
    }

    public List<ICharacter> getCharacters() {
        return characters;
    }

    public Map<Taxon, CodedDescription> getCodedDescriptions() {
        return codedDescriptions;
    }

    public CodedDescription getCodedDescription(Taxon taxon) {
        return codedDescriptions.get(taxon);
    }

    public void addCodedDescription(Taxon taxon, CodedDescription codedDescription) {
        codedDescriptions.put(taxon, codedDescription);
    }

    public List<Taxon> getTaxa() {
        return new ArrayList<>(codedDescriptions.keySet());
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ICharacter getCharacterById(String id) {
        for (ICharacter character : characters) {
            if (character.getId().equals(id)) {
                return character;
            }
        }
        return null;
    }

    public State getStateById(String id) {
        for (ICharacter character : characters) {
            if (character instanceof CategoricalCharacter) {
                for (State state : ((CategoricalCharacter) character).getStates()) {
                    if (state.getId().equals(id)) {
                        return state;
                    }
                }
            }
        }
        return null;
    }

    public ICharacter getCharacterByState(State state) {
        for (ICharacter character : characters) {
            if (character instanceof CategoricalCharacter) {
                for (State stateBis : ((CategoricalCharacter) character).getStates()) {
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

    public String getMediaObject(String key) {
        return mediaObjects.get(key);
    }

    public boolean isApplicable(Taxon taxon, ICharacter character) {
        if (character.getParentCharacter() != null && isApplicable(taxon, character.getParentCharacter())) {
            List<State> inapplicableStates = character.getInapplicableStates();
            Iterable<State> states = (Iterable<State>) getCodedDescription(taxon).getCharacterDescription(
                    character.getParentCharacter());

            // if the parent character is not described return true
            if (states == null) {
                return true;
            }
            // if one checked state is applicable
            for (State state : states) {
                if (!inapplicableStates.contains(state)) {
                    return true;
                }
            }
            // if one checked state is inapplicable
            for (State state : states) {
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
    public List<ICharacter> getInapplicableCharacters(Iterable<ICharacter> newRemainingCharacters,
                                                      State state) {

        List<ICharacter> inapplicableCharacter = new ArrayList<>();

        for (ICharacter character : newRemainingCharacters) {
            if (character.getInapplicableStates().contains(state)) {
                inapplicableCharacter.add(character);
                inapplicableCharacter.addAll(character.getAllChildren());
            }

        }
        return inapplicableCharacter;
    }

}