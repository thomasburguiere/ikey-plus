package services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import utils.Utils;

import model.CategoricalCharacter;
import model.DataSet;
import model.ICharacter;
import model.QuantitativeCharacter;
import model.QuantitativeMeasure;
import model.SingleAccessKeyNode;
import model.SingleAccessKeyTree;
import model.State;
import model.Taxon;

/**
 * This class is the service generating identification keys
 * 
 * @author Florian Causse
 * @created 18-04-2011
 */
public class IdentificationKeyGenerator {

	// the Identification Key
	private SingleAccessKeyTree singleAccessKeyTree = null;
	// the knowledge base
	private DataSet dataset = null;
	// the maximum number of states per character
	private int maxNbStatesPerCharacter;

	/**
	 * Constructor
	 */
	public IdentificationKeyGenerator() throws Exception {
		this(null, null);
	}

	/**
	 * Constructor
	 * 
	 * @param singleAccessKeyTree
	 * @param dataSet
	 */
	public IdentificationKeyGenerator(SingleAccessKeyTree singleAccessKeyTree, DataSet dataset)
			throws Exception {
		super();
		this.singleAccessKeyTree = singleAccessKeyTree;
		this.dataset = dataset;
	}

	/**
	 * Create the identification key tree
	 */
	public void createIdentificationKey() throws Exception {

		this.singleAccessKeyTree = new SingleAccessKeyTree();
		
		// init maxNumStatesPerCharacter
		this.maxNbStatesPerCharacter = calculateMaxNbStatesPerCharacter();

		// init root node
		SingleAccessKeyNode rootNode = new SingleAccessKeyNode();
		rootNode.setRemainingTaxa(dataset.getTaxa());
		this.singleAccessKeyTree.setRoot(rootNode);

		// calculate next node
		calculateSingleAccessKeyNodeChild(rootNode, dataset.getCharacters(),
				new ArrayList<Taxon>(dataset.getTaxa()));

		// delete useless nodes
		optimizeSingleAccessKeyTree(null, this.singleAccessKeyTree.getRoot());
	}

	/**
	 * Create child Nodes for the SingleAccessKeyTree
	 * 
	 * @param parentNode
	 * @param remainingCharacters
	 * @param remainingTaxa
	 */
	private void calculateSingleAccessKeyNodeChild(SingleAccessKeyNode parentNode,
			List<ICharacter> remainingCharacters, List<Taxon> remainingTaxa) throws Exception {

		if (remainingCharacters.size() > 0 && remainingTaxa.size() > 1) {

			// calculate characters score
			Map<ICharacter, Float> charactersScore = charactersScores(remainingCharacters, remainingTaxa);
			ICharacter selectedCharacter = bestCharacter(charactersScore);
			float selectedScore = charactersScore.get(selectedCharacter);

			/* // display score for each character for (ICharacter character : charactersScore.keySet()) {
			 * System.out.println(character.getName() + ": " + charactersScore.get(character)); }
			 * System.out.println(System.getProperty ("line.separator")+"bestCharacter: " +
			 * selectedCharacter.getName() + System.getProperty("line.separator")); */

			// if the character is categorical
			if (selectedCharacter.isSupportsCategoricalData()) {
				for (State state : ((CategoricalCharacter) selectedCharacter).getStates()) {
					List<Taxon> newRemainingTaxa = getRemainingTaxa(remainingTaxa,
							((CategoricalCharacter) selectedCharacter), state);

					// test if we have to stop the branch or continue
					if (newRemainingTaxa.size() > 0 && selectedScore > 0) {

						// init new node
						SingleAccessKeyNode node = new SingleAccessKeyNode();
						node.setCharacter(selectedCharacter);
						node.setRemainingTaxa(newRemainingTaxa);
						node.setCharacterState(state);

						// put new node as child of parentNode
						parentNode.addChild(node);

						// remove last best character
						List<ICharacter> newRemainingCharacters = new ArrayList<ICharacter>(
								remainingCharacters);
						newRemainingCharacters.remove(selectedCharacter);

						// calculate next node
						calculateSingleAccessKeyNodeChild(node, newRemainingCharacters, newRemainingTaxa);
					}
				}
				// if the character is numerical
			} else {
				List<QuantitativeMeasure> quantitativeMeasures = splitQuantitativeCharacter(
						selectedCharacter, remainingTaxa);

				for (QuantitativeMeasure quantitativeMeasure : quantitativeMeasures) {
					List<Taxon> newRemainingTaxa = getRemainingTaxa(remainingTaxa,
							((QuantitativeCharacter) selectedCharacter), quantitativeMeasure);

					// test if we have to stop the branch or continue
					if (newRemainingTaxa.size() > 0 && selectedScore > 0) {

						// init new node
						SingleAccessKeyNode node = new SingleAccessKeyNode();
						node.setCharacter(selectedCharacter);
						node.setRemainingTaxa(newRemainingTaxa);
						node.setCharacterState(quantitativeMeasure);

						// put new node as child of parentNode
						parentNode.addChild(node);

						// remove last best character
						List<ICharacter> newRemainingCharacters = new ArrayList<ICharacter>(
								remainingCharacters);
						newRemainingCharacters.remove(selectedCharacter);

						// calculate next node
						calculateSingleAccessKeyNodeChild(node, newRemainingCharacters, newRemainingTaxa);
					}
				}
			}
		}
	}

	/**
	 * Delete useless nodes (parentNode and ChildNode have the same nb of taxa)
	 * 
	 * @param parentNode
	 * @param node
	 */
	public void optimizeSingleAccessKeyTree(SingleAccessKeyNode parentNode, SingleAccessKeyNode node)
			throws Exception {

		if (node != null) {
			if (parentNode != null) {
				if (parentNode.getChildren().size() == 1
						&& parentNode.getRemainingTaxa().size() == node.getRemainingTaxa().size()) {
					parentNode.getChildren().addAll(node.getChildren());
					parentNode.getChildren().remove(node);
				}
			}
			for (int i = 0; i < node.getChildren().size(); i++) {
				optimizeSingleAccessKeyTree(node, node.getChildren().get(i));
			}
		}
	}

	/**
	 * @param remaningTaxa
	 * @param character
	 * @param state
	 * @return List<Taxon>, the list of remaining taxa
	 */
	private List<Taxon> getRemainingTaxa(List<Taxon> remainingTaxa, CategoricalCharacter character,
			State state) throws Exception {

		List<Taxon> newRemainingTaxa = new ArrayList<Taxon>();

		// init new remaining taxa list with taxa description matching the
		// current state
		for (Taxon taxon : remainingTaxa) {
			if (dataset.getCodedDescription(taxon).getCharacterDescription(character) == null
					|| ((List<State>) dataset.getCodedDescription(taxon).getCharacterDescription(character))
							.contains(state)) {
				newRemainingTaxa.add(taxon);
			}
		}
		return newRemainingTaxa;
	}

	/**
	 * @param remaningTaxa
	 * @param character
	 * @param quantitativeMeasure
	 * @return List<Taxon>, the list of remaining taxa
	 */
	private List<Taxon> getRemainingTaxa(List<Taxon> remainingTaxa, QuantitativeCharacter character,
			QuantitativeMeasure quantitativeMeasure) throws Exception {

		List<Taxon> newRemainingTaxa = new ArrayList<Taxon>();

		// init new remaining taxa list with taxa description matching the
		// current state
		for (Taxon taxon : remainingTaxa) {
			if (quantitativeMeasure.isInclude(((QuantitativeMeasure) dataset.getCodedDescription(taxon)
					.getCharacterDescription(character)))) {
				newRemainingTaxa.add(taxon);
			}
		}
		return newRemainingTaxa;
	}

	/**
	 * calculate the 2 best intervals for quantitative character node
	 * 
	 * @param character
	 * @return List<QuantitativeMeasure>, the two QuantitativeMeasure for the key
	 */
	private List<QuantitativeMeasure> splitQuantitativeCharacter(ICharacter character,
			List<Taxon> remainingTaxa) throws Exception {

		List<QuantitativeMeasure> quantitativeMeasures = new ArrayList<QuantitativeMeasure>();
		QuantitativeMeasure quantitativeMeasure1 = new QuantitativeMeasure();
		QuantitativeMeasure quantitativeMeasure2 = new QuantitativeMeasure();

		// get the Min and Max values of all remaining taxa
		List<Double> allValues = getAllNumericalValues(character, remainingTaxa);
		Collections.sort(allValues, new Comparator<Double>() {
			@Override
			public int compare(Double val1, Double val2) {
				final int result;
				if (val1 > val2) {
					result = 1;
				} else if (val1 < val2) {
					result = -1;
				} else {
					result = 0;
				}
				return result;
			}
		});
		// determine the best threshold to cut the interval in 2 part
		Double threshold = null;
		Double bestThreshold = null;
		int difference = allValues.size();
		int differenceMin = difference;
		int taxaBefore = 0;
		int taxaAfter = 0;
		for (int i = 0; i < allValues.size() / 2; i++) {
			threshold = allValues.get(i * 2 + 1);
			taxaBefore = 0;
			taxaAfter = 0;
			for (int j = 0; j < allValues.size() / 2; j++) {
				if (allValues.get(j * 2 + 1) <= threshold)
					taxaBefore++;
				if (allValues.get(j * 2) >= threshold)
					taxaAfter++;
			}
			difference = Math.abs(taxaBefore - taxaAfter);
			if (difference < differenceMin) {
				differenceMin = difference;
				bestThreshold = threshold;
			}
		}

		// split the interval in 2 part
		if (allValues.size() > 2 && bestThreshold != null) {
			quantitativeMeasure1.setMin(allValues.get(0));
			quantitativeMeasure1.setMax(new Double(bestThreshold));
			quantitativeMeasure1.setMaxInclude(false);
			quantitativeMeasure2.setMin(new Double(bestThreshold));
			quantitativeMeasure2.setMax(allValues.get(allValues.size() - 1));
		}

		// add the 2 new interval to the list
		quantitativeMeasures.add(quantitativeMeasure1);
		quantitativeMeasures.add(quantitativeMeasure2);

		return quantitativeMeasures;
	}

	/**
	 * @param character
	 * @param remainingTaxa
	 * @return List<Double>, the list of Min and Max values of all remaining taxa
	 */
	private List<Double> getAllNumericalValues(ICharacter character, List<Taxon> remainingTaxa)
			throws Exception {

		List<Double> allValues = new ArrayList<Double>();

		for (Taxon taxon : remainingTaxa) {
			if (dataset.getCodedDescription(taxon).getCharacterDescription(character) != null
					&& dataset.getCodedDescription(taxon).getCharacterDescription(character) instanceof QuantitativeMeasure) {

				Double minTmp = ((QuantitativeMeasure) dataset.getCodedDescription(taxon)
						.getCharacterDescription(character)).getCalculateMinimum();
				Double maxTmp = ((QuantitativeMeasure) dataset.getCodedDescription(taxon)
						.getCharacterDescription(character)).getCalculateMaximum();
				if (minTmp != null)
					allValues.add(minTmp);
				if (maxTmp != null)
					allValues.add(maxTmp);
			}
		}
		return allValues;
	}

	/**
	 * Calculate the discriminant power for all taxa
	 * 
	 * @param characters
	 * @param codedDescriptions
	 * @return Map<ICharacter,Float>, a MAP contening all discriminant power of all taxa
	 */
	private Map<ICharacter, Float> charactersScores(List<ICharacter> characters, List<Taxon> remaningTaxa)
			throws Exception {
		LinkedHashMap<ICharacter, Float> scoreMap = new LinkedHashMap<ICharacter, Float>();
		for (ICharacter character : characters) {
			if (character.isSupportsCategoricalData()) {
				scoreMap.put(character,
						categoricalCharacterScore((CategoricalCharacter) character, remaningTaxa));
			} else {
				scoreMap.put(character,
						quantitativeCharacterScore((QuantitativeCharacter) character, remaningTaxa));
			}
		}
		// take in consideration the score of child character
		considerChildCharacterScore(scoreMap);

		return scoreMap;
	}

	/**
	 * consider children in the calculate score
	 * 
	 * @param scoreMap
	 */
	private void considerChildCharacterScore(HashMap<ICharacter, Float> scoreMap) throws Exception {
		for (ICharacter character : scoreMap.keySet()) {
			if (character.isSupportsCategoricalData() && character.getChildCharacters().size() > 0) {
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
	private float getMaxChildScore(HashMap<ICharacter, Float> scoreMap, ICharacter character)
			throws Exception {
		List<ICharacter> characters = character.getAllChildren();
		float max = -1;
		for (ICharacter childCharacter : characters) {
			if (scoreMap.get(childCharacter) != null) {
				if (max == -1)
					max = scoreMap.get(childCharacter);
				if (scoreMap.get(childCharacter) >= max) {
					// init max score with child score + 0.0001 (to be sure
					// parent
					// score will be better)
					max = (float) (scoreMap.get(childCharacter) + 0.0001);
				}
			}
		}
		return max;
	}

	/**
	 * @param charactersScore
	 * @return ICharacter, the best character
	 */
	private ICharacter bestCharacter(Map<ICharacter, Float> charactersScore) throws Exception {

		float bestScore = 0;
		ICharacter bestCharacter = null;

		for (ICharacter character : charactersScore.keySet()) {
			if (charactersScore.get(character) >= bestScore) {
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
	private float categoricalCharacterScore(CategoricalCharacter character, List<Taxon> remaningTaxa)
			throws Exception {
		int cpt = 0;
		float score = 0;

		for (int i = 0; i < remaningTaxa.size() - 1; i++) {
			for (int j = i + 1; j < remaningTaxa.size(); j++) {
				if (dataset.getCodedDescription(remaningTaxa.get(i)) != null
						&& dataset.getCodedDescription(remaningTaxa.get(j)) != null) {
					// if the character is applicable for both of these taxa
					if (dataset.isApplicable(remaningTaxa.get(i), character)
							&& dataset.isApplicable(remaningTaxa.get(j), character)) {
						// nb of common states which are absent
						float commonAbsent = 0;
						// nb of common states which are present
						float commonPresent = 0;
						float other = 0;
						List<State> statesList1 = (List<State>) dataset.getCodedDescription(
								remaningTaxa.get(i)).getCharacterDescription(character);
						List<State> statesList2 = (List<State>) dataset.getCodedDescription(
								remaningTaxa.get(j)).getCharacterDescription(character);
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
									// !(statesList2.contains(state))
								} else {
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
						cpt++;
					}
				}
			}
		}
		if (cpt >= 1) {
			score = score / cpt;
		}

		// managing of twoStatesCharacterFirst option
		if (Utils.twoStatesCharacterFirst) {
			// artificially increasing score of character with 2 states
			float coeff = (float) 1 - (character.getStates().size() / maxNbStatesPerCharacter);
			score = (float) (score+coeff);
		}

		// round to 10^-2
		/* score *= 100; score = (int)(score+.5); score /= 100; */
		return score;
	}

	/**
	 * This method calculates the maximum number of character per state for the entire dataset
	 * 
	 * @return
	 */
	private int calculateMaxNbStatesPerCharacter() {
		int max = 2;
		for (ICharacter ic : dataset.getCharacters()) {
			if (ic instanceof CategoricalCharacter && max < ((CategoricalCharacter) ic).getStates().size())
				max = ((CategoricalCharacter) ic).getStates().size();
		}
		return max;
	}

	/**
	 * Calculate the discriminant power for quantitative character
	 * 
	 * @param character
	 * @param codedDescriptions
	 * @return float, the discriminant power of the quantitative character
	 */
	private float quantitativeCharacterScore(QuantitativeCharacter character, List<Taxon> remaningTaxa)
			throws Exception {
		int cpt = 0;
		float score = 0;
		for (int i = 0; i < remaningTaxa.size() - 1; i++) {
			for (int j = i + 1; j < remaningTaxa.size(); j++) {
				if (dataset.getCodedDescription(remaningTaxa.get(i)) != null
						&& dataset.getCodedDescription(remaningTaxa.get(j)) != null) {
					if (dataset.isApplicable(remaningTaxa.get(i), character)
							&& dataset.isApplicable(remaningTaxa.get(j), character)) {

						QuantitativeMeasure quantitativeMeasure1 = (QuantitativeMeasure) dataset
								.getCodedDescription(remaningTaxa.get(i)).getCharacterDescription(character);
						QuantitativeMeasure quantitativeMeasure2 = (QuantitativeMeasure) dataset
								.getCodedDescription(remaningTaxa.get(j)).getCharacterDescription(character);

						// percentage of common values which are shared
						float commonPercentage = 0;

						// search common shared values
						if (quantitativeMeasure1 != null && quantitativeMeasure2 != null) {

							if (quantitativeMeasure1.getCalculateMinimum() != null
									&& quantitativeMeasure1.getCalculateMaximum() != null
									&& quantitativeMeasure2.getCalculateMinimum() != null
									&& quantitativeMeasure2.getCalculateMaximum() != null) {

								commonPercentage = calculCommonPercentage(quantitativeMeasure1
										.getCalculateMinimum().doubleValue(), quantitativeMeasure1
										.getCalculateMaximum().doubleValue(), quantitativeMeasure2
										.getCalculateMinimum().doubleValue(), quantitativeMeasure2
										.getCalculateMaximum().doubleValue());

								if (commonPercentage == 0) {
									score++;
								}
							}
						}
						cpt++;
					}
				}
			}
		}
		if (cpt >= 1) {
			score = score / cpt;
		}
		
		// managing of twoStatesCharacterFirst option
		if (Utils.twoStatesCharacterFirst) {
			// artificially increasing score of character with 2 states
			float coeff = (float) 1 - (2 / maxNbStatesPerCharacter);
			score = (float) (score+coeff);
		}
		
		// round to 10^-2
		/* score *= 100; score = (int)(score+.5); score /= 100; */
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
	public static float calculCommonPercentage(double min1, double max1, double min2, double max2)
			throws Exception {
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

		res = new Double((maxLowerTmp - minUpperTmp) / (maxUpperTmp - minLowerTmp)).floatValue();

		if (res < 0) {
			res = 0;
		}
		return res;
	}

	/**
	 * @return SingleAccessKeyTree
	 */
	public SingleAccessKeyTree getSingleAccessKeyTree() {
		return singleAccessKeyTree;
	}

	/**
	 * @param singleAccessKeyTree
	 */
	public void setSingleAccessKeyTree(SingleAccessKeyTree singleAccessKeyTree) {
		this.singleAccessKeyTree = singleAccessKeyTree;
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

	public int getMaxNumStatesPerCharacter() {
		return maxNbStatesPerCharacter;
	}

	public void setMaxNumStatesPerCharacter(int maxNumStatesPerCharacter) {
		this.maxNbStatesPerCharacter = maxNumStatesPerCharacter;
	}

}
