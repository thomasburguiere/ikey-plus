package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.CategoricalCharacter;
import model.CodedDescription;
import model.DataSet;
import model.ICharacter;
import model.PolytomousKeyTree;
import model.QuantitativeCharacter;
import model.QuantitativeMeasure;
import model.State;
import model.Taxon;

/**
 * @author Florian Causse
 * @created 18-avr.-2011
 */
public class IdentificationKeyGenerator {

	// the Identification Key
	private PolytomousKeyTree polytomousKeyTree = null;
	// the knowledge base
	private DataSet dataset = null;

	/**
	 * Constructor
	 */
	public IdentificationKeyGenerator() {
		this(null, null);
	}

	/**
	 * Constructor
	 * 
	 * @param polytomousKeyTree
	 * @param dataSet
	 */
	public IdentificationKeyGenerator(PolytomousKeyTree polytomousKeyTree,
			DataSet dataset) {
		super();
		this.polytomousKeyTree = polytomousKeyTree;
		this.dataset = dataset;
	}

	/**
	 * create the identification key tree
	 */
	public void createIdentificationKey() {
		// display score for each character
		Map<ICharacter, Float> charactersScore = charactersScores(dataset
				.getCharacters(), dataset.getCodedDescriptions());
		for (ICharacter character : charactersScore.keySet()) {
			System.out.println(character.getName() + ": "
					+ charactersScore.get(character));
		}
		System.out.println("\nbestCharacter: "
				+ bestCharacter(charactersScore).getName());

	}

	/**
	 * Calculate the discriminant power for all taxa
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
				scoreMap.put(character, categoricalCharacterScore(
						(CategoricalCharacter) character, codedDescriptions));
			} else {
				scoreMap.put(character, quantitativeCharacterScore(
						(QuantitativeCharacter) character, codedDescriptions));
			}
		}

		considerChildCharacterScore(scoreMap);
		return scoreMap;
	}

	/**
	 * consider children in the calculate score
	 * 
	 * @param scoreMap
	 */
	private void considerChildCharacterScore(HashMap<ICharacter, Float> scoreMap) {
		for (ICharacter character : scoreMap.keySet()) {
			if (character.getChildCharacters().size() > 0) {
				float max = getMaxChildScore(scoreMap, character);
				if (scoreMap.get(character) < max) {
					scoreMap.put(character, max);
				}

			}
		}
	}

	/**
	 * @param scoreMap
	 * @param character
	 * @return float, the max score of all character child
	 */
	private float getMaxChildScore(HashMap<ICharacter, Float> scoreMap,
			ICharacter character) {
		List<ICharacter> characters = character.getAllChildren();
		float max = 0;
		for (ICharacter childCharacter : characters) {
			if (scoreMap.get(childCharacter) > max) {
				max = scoreMap.get(childCharacter);
			}
		}
		return max;
	}

	/**
	 * @param charactersScore
	 * @return ICharacter, the best character
	 */
	private ICharacter bestCharacter(Map<ICharacter, Float> charactersScore) {

		float bestScore = 0;
		ICharacter bestCharacter = null;

		for (ICharacter character : charactersScore.keySet()) {
			if (charactersScore.get(character) > bestScore) {
				bestScore = charactersScore.get(character);
				bestCharacter = character;
			}
		}
		return bestCharacter;
	}

	/**
	 * Calculate the discriminant power for categorical character
	 * 
	 * @param character
	 * @param codedDescriptions
	 * @return float, the discriminant power of the categorical character
	 */
	private float categoricalCharacterScore(CategoricalCharacter character,
			Map<Taxon, CodedDescription> codedDescriptions) {
		int cpt = 0;
		float score = 0;
		List<Taxon> taxonKeys = new ArrayList(codedDescriptions.keySet());
		for (int i = 0; i < taxonKeys.size() - 1; i++) {
			for (int j = i + 1; j < taxonKeys.size(); j++) {
				if (codedDescriptions.get(taxonKeys.get(i)) != null
						&& codedDescriptions.get(taxonKeys.get(j)) != null) {
					// if the character is applicable for both of these taxa
					if (dataset.isApplicable(taxonKeys.get(i), character)
							&& dataset
									.isApplicable(taxonKeys.get(j), character)) {
						// nb of common states which are absent
						float commonAbsent = 0;
						// nb of common states which are present
						float commonPresent = 0;
						float other = 0;

						List<State> statesList1 = (List<State>) codedDescriptions
								.get(taxonKeys.get(i)).getCharacterDescription(
										character);
						List<State> statesList2 = (List<State>) codedDescriptions
								.get(taxonKeys.get(j)).getCharacterDescription(
										character);

						if (statesList1 != null && statesList2 != null) {
							// search common state
							for (int k = 0; k < character.getStates().size(); k++) {
								State state = character.getStates().get(k);

								if (statesList1.contains(state)) {
									if (statesList2.contains(state)) {
										commonPresent++;
									} else {
										other++;
									}
								} else { // !(statesList2.contains(state))
									if (statesList2.contains(state)) {
										other++;
									} else {
										commonAbsent++;
									}
								}
							}
							// yes or no method (Xper)
							if ((commonPresent == 0) && (other > 0)) {
								score++;
							}
						}
					} else {
						// if the character is applicable for one of these taxa
						cpt--;
					}
				}
				cpt++;
			}
		}
		score = score / cpt;
		// round to 10^-2
		/*
		 * score *= 100; score = (int)(score+.5); score /= 100;
		 */
		return score;
	}

	/**
	 * Calculate the discriminant power for quantitative character
	 * 
	 * @param character
	 * @param codedDescriptions
	 * @return float, the discriminant power of the quantitative character
	 */
	private float quantitativeCharacterScore(QuantitativeCharacter character,
			Map<Taxon, CodedDescription> codedDescriptions) {
		int cpt = 0;
		float score = 0;
		List<Taxon> taxonKeys = new ArrayList(codedDescriptions.keySet());
		for (int i = 0; i < taxonKeys.size() - 1; i++) {
			for (int j = i + 1; j < taxonKeys.size(); j++) {
				if (codedDescriptions.get(taxonKeys.get(i)) != null
						&& codedDescriptions.get(taxonKeys.get(j)) != null) {
					if (dataset.isApplicable(taxonKeys.get(i), character)
							&& dataset
									.isApplicable(taxonKeys.get(j), character)) {
						// percentage of common values which are shared
						float commonPercentage = 0;

						QuantitativeMeasure quantitativeMeasure1 = (QuantitativeMeasure) codedDescriptions
								.get(taxonKeys.get(i)).getCharacterDescription(
										character);
						QuantitativeMeasure quantitativeMeasure2 = (QuantitativeMeasure) codedDescriptions
								.get(taxonKeys.get(j)).getCharacterDescription(
										character);

						// search common shared values
						if (quantitativeMeasure1 != null
								&& quantitativeMeasure2 != null) {

							if (quantitativeMeasure1.getCalculateMinimum() != null
									&& quantitativeMeasure1
											.getCalculateMaximum() != null
									&& quantitativeMeasure2
											.getCalculateMinimum() != null
									&& quantitativeMeasure2
											.getCalculateMaximum() != null) {

								commonPercentage = calculCommonPercentage(
										quantitativeMeasure1
												.getCalculateMinimum()
												.doubleValue(),
										quantitativeMeasure1
												.getCalculateMaximum()
												.doubleValue(),
										quantitativeMeasure2
												.getCalculateMinimum()
												.doubleValue(),
										quantitativeMeasure2
												.getCalculateMaximum()
												.doubleValue());

								if (commonPercentage <= 0) {
									score++;
								}
							}
						}
					} else {
						// if the character is applicable for one of these taxa
						cpt--;
					}
				}
				cpt++;
			}
		}
		score = score / cpt;
		// round to 10^-2
		/*
		 * score *= 100; score = (int)(score+.5); score /= 100;
		 */
		return score;
	}

	/**
	 * Calculate the common percentage between two interval
	 * 
	 * @param min1
	 * @param max1
	 * @param min2
	 * @param max2
	 * @return float, the common percentage
	 */
	public static float calculCommonPercentage(double min1, double max1,
			double min2, double max2) {
		double minLowerTmp = 0;
		double maxUpperTmp = 0;
		double minUpperTmp = 0;
		double maxLowerTmp = 0;
		float res = 0;

		if (min1 <= min2) {
			minLowerTmp = min1;
			minUpperTmp = min2;
		} else {
			minLowerTmp = min2;
			minUpperTmp = min1;
		}

		if (max1 >= max2) {
			maxUpperTmp = max1;
			maxLowerTmp = max2;
		} else {
			maxUpperTmp = max2;
			maxLowerTmp = max1;
		}

		res = new Double((maxLowerTmp - minUpperTmp)
				/ (maxUpperTmp - minLowerTmp)).floatValue();

		if (res < 0) {
			res = 0;
		}
		return res;
	}

	/**
	 * @return PolytomousKeyTree
	 */
	public PolytomousKeyTree getPolytomousKeyTree() {
		return polytomousKeyTree;
	}

	/**
	 * @param polytomousKeyTree
	 */
	public void setPolytomousKeyTree(PolytomousKeyTree polytomousKeyTree) {
		this.polytomousKeyTree = polytomousKeyTree;
	}

	/**
	 * @return DataSet
	 */
	public DataSet getDataSet() {
		return dataset;
	}

	/**
	 * @param dataSet
	 */
	public void setDataSet(DataSet dataSet) {
		this.dataset = dataSet;
	}

}
