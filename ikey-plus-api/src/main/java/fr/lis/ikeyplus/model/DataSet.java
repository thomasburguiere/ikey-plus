package fr.lis.ikeyplus.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a knowledge base
 * 
 * @author Florian Causse
 * @created 06-04-2011
 */
public class DataSet {

	private List<ICharacter> characters = null;
	private Map<Taxon, CodedDescription> codedDescriptions = null;
	private String label = null;
	private Map<String, String> mediaObjects = null;

	/**
	 * constructor
	 */
	public DataSet() {
		characters = new ArrayList<ICharacter>();
		codedDescriptions = new LinkedHashMap<Taxon, CodedDescription>();
		mediaObjects = new LinkedHashMap<String, String>();
	}

	/**
	 * get the list of Characters
	 * 
	 * @return List<ICharacter>, the list of Characters
	 */
	public List<ICharacter> getCharacters() {
		return characters;
	}

	/**
	 * set the list of Characters
	 * 
	 * @param List
	 *            <ICharacter>, the list of Characters
	 */
	public void setCharacters(List<ICharacter> characters) {
		this.characters = characters;
	}

	/**
	 * get all description for all taxa
	 * 
	 * @return Map<Taxon, CodedDescription>, all description for all taxa
	 */
	public Map<Taxon, CodedDescription> getCodedDescriptions() {
		return codedDescriptions;
	}

	/**
	 * set all description for all taxa
	 * 
	 * @param Map
	 *            <Taxon, CodedDescription>, all description for all taxa
	 */
	public void setCodedDescriptions(Map<Taxon, CodedDescription> codedDescriptions) {
		this.codedDescriptions = codedDescriptions;
	}

	/**
	 * get the description of one taxon
	 * 
	 * @param Taxon
	 *            , the key
	 * @param CodedDescription
	 *            , the description
	 */
	public CodedDescription getCodedDescription(Taxon taxon) {
		return codedDescriptions.get(taxon);
	}

	/**
	 * add a description for one taxon
	 * 
	 * @param Taxon
	 *            , the key
	 * @param CodedDescription
	 *            , the description
	 */
	public void addCodedDescription(Taxon taxon, CodedDescription codedDescription) {
		codedDescriptions.put(taxon, codedDescription);
	}

	/**
	 * remove a description
	 * 
	 * @param Taxon
	 *            , the key
	 */
	public void removeCodedDescription(Taxon taxon) {
		codedDescriptions.remove(taxon);
	}

	/**
	 * get the list of taxa
	 * 
	 * @return List<Taxon>, the list of taxa
	 */
	public List<Taxon> getTaxa() {
		return new ArrayList<Taxon>(this.codedDescriptions.keySet());
	}

	/**
	 * get the label of the dataset
	 * 
	 * @return String, the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * set the label of the dataset
	 * 
	 * @param String
	 *            , the label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * get a Character by its identifier
	 * 
	 * @param String
	 *            , the character identifier
	 * @return ICharacter, a Character
	 */
	public ICharacter getCharacterById(String id) {
		for (ICharacter character : characters) {
			if (character.getId().equals(id)) {
				return character;
			}
		}
		return null;
	}

	/**
	 * get a State by its identifier
	 * 
	 * @param String
	 *            , state identifier
	 * @return State, a state
	 */
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

	/**
	 * get a character using one of it's state
	 * 
	 * @param State
	 *            , state owned by the character
	 * @return Character, the character corresponding
	 */
	public ICharacter getCharacterByState(State state) {
		for (ICharacter character : characters) {
			if (character instanceof CategoricalCharacter) {
				for (State stateBis : ((CategoricalCharacter) character).getStates()) {
					if (stateBis == state) {
						return character;
					}
				}
			}
		}
		return null;
	}

	/**
	 * get the list of images URL
	 * 
	 * @return Map<String, String>, the list of images URL
	 */
	public Map<String, String> getMediaObjects() {
		return mediaObjects;
	}

	/**
	 * set the list of Image URL
	 * 
	 * @param mediaObjects
	 *            , the list of images URL
	 */
	public void setMediaObjects(Map<String, String> mediaObjects) {
		this.mediaObjects = mediaObjects;
	}

	/**
	 * get the image corresponding to the key
	 * 
	 * @return String, the images URL
	 */
	public String getMediaObject(String key) {
		return mediaObjects.get(key);
	}

	/**
	 * determine if a character is applicable for a taxon
	 * 
	 * @param taxon
	 * @param character
	 * @return boolean, true if the character is is applicable for the current taxon
	 */
	public boolean isApplicable(Taxon taxon, ICharacter character) {
		if (character.getParentCharacter() != null && isApplicable(taxon, character.getParentCharacter())) {
			List<State> inapplicableStates = character.getInapplicableStates();
			List<State> states = (List<State>) this.getCodedDescription(taxon).getCharacterDescription(
					character.getParentCharacter());

			// if the parent character is not described return true
			if (states == null) {
				return true;
			}
			// if one checked state is applicable
			for (int i = 0; i < states.size(); i++) {
				State state = states.get(i);
				if (!inapplicableStates.contains(state)) {
					return true;
				}
			}
			// if one checked state is inapplicable
			for (int i = 0; i < states.size(); i++) {
				State state = states.get(i);
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
	 * @param List
	 *            <ICharacter>
	 * @param character
	 * @return List<ICharacter>, the list of inapplicable character and all its sons
	 */
	public List<ICharacter> getInapplicableCharacters(List<ICharacter> newRemainingCharacters,
			ICharacter selectedCharacter, State state) {

		List<ICharacter> inapplicableCharacter = new ArrayList<ICharacter>();

		for (ICharacter character : newRemainingCharacters) {
			if (character.getInapplicableStates().contains(state)) {
				inapplicableCharacter.add(character);
				inapplicableCharacter.addAll(character.getAllChildren());
			}

		}
		return inapplicableCharacter;
	}

}