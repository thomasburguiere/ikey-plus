package services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.CodedDescription;
import model.DataSet;
import model.ICharacter;
import model.PolytomousKeyTree;
import model.Taxon;

/**
 * @author Florian Causse
 * @created 18-avr.-2011
 */
public class IdentificationKeyGenerator {

	private PolytomousKeyTree polytomousKeyTree = null; // the Identification
	// Key
	private DataSet dataSet = null; // the knowledge base

	/**
	 * Constructor
	 */
	public IdentificationKeyGenerator() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param polytomousKeyTree
	 * @param dataSet
	 */
	public IdentificationKeyGenerator(PolytomousKeyTree polytomousKeyTree,
			DataSet dataSet) {
		super();
		this.polytomousKeyTree = polytomousKeyTree;
		this.dataSet = dataSet;
	}

	/**
	 * Calcul the discriminant power for all taxa
	 * 
	 * @param characters
	 * @param codedDescriptions
	 * @return Map<ICharacter,Float>, a MAP contening all discriminant power of
	 *         all taxa
	 */
	private Map<ICharacter, Float> charactersScores(
			List<ICharacter> characters,
			Map<Taxon, CodedDescription> codedDescriptions) {
		HashMap<ICharacter, Float> scoreMap = new HashMap<ICharacter, Float>();
		for (ICharacter character : characters) {
			if (character.isSupportsCategoricalData()) {
				scoreMap.put(character, categoricalCharacterScore(character,
						codedDescriptions));
			} else {
				scoreMap.put(character, quantitativeCharacterScore(character,
						codedDescriptions));
			}
		}
		return scoreMap;
	}

	/**
	 * Calcul the discriminant power for categorical character
	 * 
	 * @param character
	 * @param codedDescriptions
	 * @return float, the discriminant power of the categorical character
	 */
	private float categoricalCharacterScore(ICharacter character,
			Map<Taxon, CodedDescription> codedDescriptions) {
		int cpt = 0;
		float score = 0;
		Set<Taxon> alreadyUsed = new HashSet();
		Set<Taxon> taxonKeys1 = codedDescriptions.keySet();
		for (Taxon taxon1 : taxonKeys1) {
			alreadyUsed.add(taxon1);
			Set<Taxon> taxonKeys2 = codedDescriptions.keySet();
			taxonKeys2.removeAll(alreadyUsed);
			for (Taxon taxon2 : taxonKeys2) {
				cpt++;
			}
		}
		return score;
	}

	/**
	 * Calcul the discriminant power for quantitative character
	 * 
	 * @param character
	 * @param codedDescriptions
	 * @return float, the discriminant power of the quantitative character
	 */
	private float quantitativeCharacterScore(ICharacter character,
			Map<Taxon, CodedDescription> codedDescriptions) {
		int cpt = 0;
		float score = 0;
		Set<Taxon> toRemove = new HashSet();
		Set<Taxon> taxonKeys1 = codedDescriptions.keySet();
		for (Taxon taxon1 : taxonKeys1) {
			toRemove.add(taxon1);
			Set<Taxon> taxonKeys2 = codedDescriptions.keySet();
			taxonKeys2.removeAll(toRemove);
			for (Taxon taxon2 : taxonKeys2) {
				cpt++;
			}
		}
		return score;
	}

	public PolytomousKeyTree getPolytomousKeyTree() {
		return polytomousKeyTree;
	}

	public void setPolytomousKeyTree(PolytomousKeyTree polytomousKeyTree) {
		this.polytomousKeyTree = polytomousKeyTree;
	}

	public DataSet getDataSet() {
		return dataSet;
	}

	public void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}

}
