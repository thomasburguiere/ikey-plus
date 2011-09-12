package services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.CategoricalCharacter;
import model.DataSet;
import model.ICharacter;
import model.QuantitativeCharacter;
import model.QuantitativeMeasure;
import model.SingleAccessKeyNode;
import model.SingleAccessKeyTree;
import model.State;
import model.Taxon;
import utils.Utils;

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
		this.singleAccessKeyTree.setLabel(dataset.getLabel());

		// init maxNumStatesPerCharacter
		this.maxNbStatesPerCharacter = calculateMaxNbStatesPerCharacter();

		// init root node
		SingleAccessKeyNode rootNode = new SingleAccessKeyNode();
		rootNode.setRemainingTaxa(dataset.getTaxa());
		this.singleAccessKeyTree.setRoot(rootNode);

		// calculate next node
		calculateSingleAccessKeyNodeChild(rootNode, dataset.getCharacters(),
				new ArrayList<Taxon>(dataset.getTaxa()), new ArrayList<ICharacter>());

		// delete useless nodes
		optimizeSingleAccessKeyTree(null, this.singleAccessKeyTree.getRoot());
	}

	/**
	 * Create child Nodes for the SingleAccessKeyTree
	 * 
	 * @param parentNode
	 * @param remainingCharacters
	 * @param remainingTaxa
	 * @param alreadyUsedCharacter
	 *            , the list of numerical characters already used at least one time
	 */
	private void calculateSingleAccessKeyNodeChild(SingleAccessKeyNode parentNode,
			List<ICharacter> remainingCharacters, List<Taxon> remainingTaxa,
			List<ICharacter> alreadyUsedCharacter) throws Exception {

		if (remainingCharacters.size() > 0 && remainingTaxa.size() > 1) {

			// get the list of characters which discriminant power depends on the child character
			List<ICharacter> childDependantCharacters = new ArrayList<ICharacter>();

			// calculate characters score
			Map<ICharacter, Float> charactersScore = charactersScores(remainingCharacters, remainingTaxa,
					childDependantCharacters, alreadyUsedCharacter);
			ICharacter selectedCharacter = bestCharacter(charactersScore);

			// delete characters if score method is not Xper and score = 0
			if (!Utils.scoreMethod.equalsIgnoreCase(Utils.XPER)) {
				for (ICharacter character : charactersScore.keySet()) {
					if (charactersScore.get(character) <= 0) {
						remainingCharacters.removeAll(character.getAllChildren());
						remainingCharacters.remove(character);
					}
				}
			}

			// get not described taxa
			List<Taxon> notDescribedTaxa = null;
			if (selectedCharacter.isSupportsCategoricalData()) {
				notDescribedTaxa = getNotDescribedTaxa(remainingTaxa,
						((CategoricalCharacter) selectedCharacter));
				// delete not described taxa from the remaining taxa list
				remainingTaxa.removeAll(notDescribedTaxa);
			} else {
				notDescribedTaxa = getNotDescribedTaxa(remainingTaxa,
						((QuantitativeCharacter) selectedCharacter));
				// delete not described taxa from the remaining taxa list
				remainingTaxa.removeAll(notDescribedTaxa);
			}

			// display score for each character
			// for (ICharacter character : remainingCharacters) {
			// if (character.isSupportsCategoricalData()) {
			// System.out.println("CC " + character.getName() + ": " + charactersScore.get(character));
			// } else {
			// System.out.println("NN " + character.getName() + ": " + charactersScore.get(character));
			// }
			// }
			// System.out.println(System.getProperty("line.separator") + "bestCharacter: "
			// + selectedCharacter.getName() + System.getProperty("line.separator"));

			// if the character is categorical
			if (selectedCharacter.isSupportsCategoricalData()) {

				// create a child nodes list for mergeCharacterStatesIfSameDiscrimination option
				List<SingleAccessKeyNode> futureChildNodes = new ArrayList<SingleAccessKeyNode>();

				for (State state : ((CategoricalCharacter) selectedCharacter).getStates()) {
					List<Taxon> newRemainingTaxa = getRemainingTaxa(remainingTaxa,
							((CategoricalCharacter) selectedCharacter), state);

					// test if we have to stop the branch or continue
					if (newRemainingTaxa.size() > 0) {

						// init new node
						SingleAccessKeyNode node = new SingleAccessKeyNode();
						node.setCharacter(selectedCharacter);
						node.setRemainingTaxa(newRemainingTaxa);
						node.setCharacterState(state);

						// mergeCharacterStatesIfSameDiscrimination option handling
						if (Utils.mergeCharacterStatesIfSameDiscimination) {
							if (mergeNodesIfSameDiscrimination(futureChildNodes, node)) {
								continue;
							}
						}

						// add the current node to the current child nodes list
						futureChildNodes.add(node);

						// put new node as child of parentNode
						parentNode.addChild(node);

						// create new remaining characters list
						List<ICharacter> newRemainingCharacters = new ArrayList<ICharacter>(
								remainingCharacters);
						// remove last best character from the remaining characters list
						newRemainingCharacters.remove(selectedCharacter);

						// get inapplicable characters
						List<ICharacter> inapplicableCharacters = dataset.getInapplicableCharacters(
								newRemainingCharacters, selectedCharacter, state);
						// remove inapplicable character and its sons from the remaining characters list
						newRemainingCharacters.removeAll(inapplicableCharacters);

						// pruning option handling
						if (Utils.pruning && remainingTaxa.containsAll(newRemainingTaxa)
								&& newRemainingTaxa.containsAll(remainingTaxa)
								&& !childDependantCharacters.contains(selectedCharacter)) {
							node.setNodeDescription(Utils.getBundleConfElement("message.warning.pruning"));
						} else {
							// calculate next node
							calculateSingleAccessKeyNodeChild(node, newRemainingCharacters, newRemainingTaxa,
									new ArrayList<ICharacter>(alreadyUsedCharacter));
						}
					}
				}

				// if the character is numerical
			} else {

				// add the selected character to the already used characters list
				alreadyUsedCharacter.add(selectedCharacter);
				List<QuantitativeMeasure> quantitativeMeasures = splitQuantitativeCharacter(
						selectedCharacter, remainingTaxa);

				for (QuantitativeMeasure quantitativeMeasure : quantitativeMeasures) {
					List<Taxon> newRemainingTaxa = getRemainingTaxa(remainingTaxa,
							((QuantitativeCharacter) selectedCharacter), quantitativeMeasure);

					// test if we have to stop the branch or continue
					if (newRemainingTaxa.size() > 0) {

						// init new node
						SingleAccessKeyNode node = new SingleAccessKeyNode();
						node.setCharacter(selectedCharacter);
						node.setRemainingTaxa(newRemainingTaxa);
						node.setCharacterState(quantitativeMeasure);

						// put new node as child of parentNode
						parentNode.addChild(node);

						// create new remaining characters list, we don't remove the last best character
						List<ICharacter> newRemainingCharacters = new ArrayList<ICharacter>(
								remainingCharacters);

						// pruning option handling
						if (Utils.pruning && remainingTaxa.containsAll(newRemainingTaxa)
								&& newRemainingTaxa.containsAll(remainingTaxa)
								&& !childDependantCharacters.contains(selectedCharacter)) {
							node.setNodeDescription(Utils.getBundleConfElement("message.warning.pruning"));
						} else {
							// if current remaining taxa are similar to parent node remaining taxa
							if (parentNode.getRemainingTaxa().size() == newRemainingTaxa.size()) {
								// remove last best character from the remaining characters list
								newRemainingCharacters.remove(selectedCharacter);
								// calculate next node without selected character
								calculateSingleAccessKeyNodeChild(node, newRemainingCharacters,
										newRemainingTaxa, new ArrayList<ICharacter>(alreadyUsedCharacter));
							} else {
								// calculate next node
								calculateSingleAccessKeyNodeChild(node, newRemainingCharacters,
										newRemainingTaxa, new ArrayList<ICharacter>(alreadyUsedCharacter));
							}
						}
					}
				}
			}

			// if taxa are not described and if verbosity string contains correct tag, create a node "Other (not described)"
			if (Utils.verbosity.contains(Utils.OTHERTAG) && notDescribedTaxa != null && notDescribedTaxa.size() > 0) {
				// init new node
				SingleAccessKeyNode notDescribedNode = new SingleAccessKeyNode();
				notDescribedNode.setCharacter(selectedCharacter);
				notDescribedNode.setRemainingTaxa(notDescribedTaxa);
				notDescribedNode.setCharacterState(new State(Utils.getBundleConfElement("message.notDescribed")));

				// put new node as child of parentNode
				parentNode.addChild(notDescribedNode);
			}
		}
	}

	/**
	 * merge character state if remaining taxa are similar between 2 nodes
	 * 
	 * @param futureChildNodes
	 * @param node
	 * @return true if the current node has been merge with one of future child nodes
	 */
	private boolean mergeNodesIfSameDiscrimination(List<SingleAccessKeyNode> futureChildNodes,
			SingleAccessKeyNode node) {

		for (SingleAccessKeyNode futureChildNode : futureChildNodes) {
			if (node.getRemainingTaxa().size() > 1
					&& futureChildNode.getRemainingTaxa().containsAll(node.getRemainingTaxa())
					|| (futureChildNode.getRemainingTaxa().size() > 1 && node.getRemainingTaxa().containsAll(
							futureChildNode.getRemainingTaxa()))) {
				futureChildNode.addOtherCharacterStates(node.getCharacterState());
				return true;
			}

		}
		return false;
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
	 * @param remaningTaxa
	 * @param character
	 * @return List<Taxon>, the list of not described taxa for categorical character
	 */
	private List<Taxon> getNotDescribedTaxa(List<Taxon> remainingTaxa, CategoricalCharacter character)
			throws Exception {

		List<Taxon> notDescribedTaxa = new ArrayList<Taxon>();

		// init not described taxa list with taxa without description
		for (Taxon taxon : remainingTaxa) {
			if (dataset.getCodedDescription(taxon).getCharacterDescription(character) != null
					&& ((List<State>) dataset.getCodedDescription(taxon).getCharacterDescription(character))
							.size() == 0) {
				notDescribedTaxa.add(taxon);
			}
		}
		return notDescribedTaxa;
	}

	/**
	 * @param remaningTaxa
	 * @param character
	 * @return List<Taxon>, the list of not described taxa for quantitative character
	 */
	private List<Taxon> getNotDescribedTaxa(List<Taxon> remainingTaxa, QuantitativeCharacter character)
			throws Exception {

		List<Taxon> notDescribedTaxa = new ArrayList<Taxon>();

		// init not described taxa list with taxa without description
		for (Taxon taxon : remainingTaxa) {
			if (dataset.getCodedDescription(taxon).getCharacterDescription(character) != null
					&& ((QuantitativeMeasure) dataset.getCodedDescription(taxon).getCharacterDescription(
							character)).isNotSpecified()) {
				notDescribedTaxa.add(taxon);
			}
		}
		return notDescribedTaxa;
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
	 * get all Min and Max for all remaining taxa concerning one quantitative character
	 * 
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
	private Map<ICharacter, Float> charactersScores(List<ICharacter> characters, List<Taxon> remaningTaxa,
			List<ICharacter> childDependantCharacters, List<ICharacter> alreadyUsedCharacter)
			throws Exception {
		LinkedHashMap<ICharacter, Float> scoreMap = new LinkedHashMap<ICharacter, Float>();
		for (ICharacter character : characters) {
			if (character.isSupportsCategoricalData()) {
				scoreMap.put(character,
						categoricalCharacterScore((CategoricalCharacter) character, remaningTaxa));
			} else {
				scoreMap.put(
						character,
						quantitativeCharacterScore((QuantitativeCharacter) character, remaningTaxa,
								alreadyUsedCharacter));
			}
		}

		// take in consideration the score of child character
		considerChildCharacterScore(scoreMap, childDependantCharacters);

		return scoreMap;
	}

	/**
	 * consider children in the calculate score
	 * 
	 * @param scoreMap
	 */
	private void considerChildCharacterScore(HashMap<ICharacter, Float> scoreMap,
			List<ICharacter> childDependantCharacters) throws Exception {
		for (ICharacter character : scoreMap.keySet()) {
			if (character.isSupportsCategoricalData() && character.getChildCharacters().size() > 0) {
				float max = getMaxChildScore(scoreMap, character);
				if (scoreMap.get(character) < max) {
					scoreMap.put(character, max);
					childDependantCharacters.add(character);
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

		float bestScore = -1;
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
	 * @param scoreMethod
	 *            TODO
	 * @param codedDescriptions
	 * @return float, the discriminant power of the categorical character
	 */
	private float categoricalCharacterScore(CategoricalCharacter character, List<Taxon> remainingTaxa)
			throws Exception {
		int cpt = 0;
		float score = 0;
		boolean isAlwaysDescribed = true;

		for (int i = 0; i < remainingTaxa.size() - 1; i++) {
			for (int j = i + 1; j < remainingTaxa.size(); j++) {
				if (dataset.getCodedDescription(remainingTaxa.get(i)) != null
						&& dataset.getCodedDescription(remainingTaxa.get(j)) != null) {
					// if the character is applicable for both of these taxa
					if (dataset.isApplicable(remainingTaxa.get(i), character)
							&& dataset.isApplicable(remainingTaxa.get(j), character)) {

						List<State> statesList1 = (List<State>) dataset.getCodedDescription(
								remainingTaxa.get(i)).getCharacterDescription(character);
						List<State> statesList2 = (List<State>) dataset.getCodedDescription(
								remainingTaxa.get(j)).getCharacterDescription(character);

						// if at least one description is empty for the current character
						if ((statesList1 != null && statesList1.size() == 0)
								|| (statesList2 != null && statesList2.size() == 0)) {
							isAlwaysDescribed = false;
						}

						// if one description is unknown and the other have 0 state checked
						if ((statesList1 == null && statesList2 != null && statesList2.size() == 0)
								|| (statesList2 == null && statesList1 != null && statesList1.size() == 0)) {
							score++;
						} else if (statesList1 != null && statesList2 != null) {

							// nb of common states which are absent
							float commonAbsent = 0;
							// nb of common states which are present
							float commonPresent = 0;
							float other = 0;

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
							score += applyScoreMethod(commonPresent, commonAbsent, other);
						}
						cpt++;
					}
				}
			}
		}

		if (cpt >= 1) {
			score = score / cpt;
		}

		// increasing artificially the score of character containing only described taxa
		if (isAlwaysDescribed && score > 0) {
			score = (float) ((float) score + (float) 2.0);
		}

		// fewStatesCharacterFirst option handling
		if (Utils.fewStatesCharacterFirst && score > 0 && character.getStates().size() >= 2) {
			// increasing artificially score of character with few states
			float coeff = (float) 1
					- ((float) character.getStates().size() / (float) maxNbStatesPerCharacter);
			score = (float) (score + coeff);
		}

		return score;
	}

	/**
	 * Calculate the discriminant power for quantitative character
	 * 
	 * @param character
	 * @param codedDescriptions
	 * @return float, the discriminant power of the quantitative character
	 */
	/**
	 * @param character
	 * @param remainingTaxa
	 * @return
	 * @throws Exception
	 */
	private float quantitativeCharacterScore(QuantitativeCharacter character, List<Taxon> remainingTaxa,
			List<ICharacter> alreadyUsedCharacter) throws Exception {
		int cpt = 0;
		float score = 0;
		boolean isAlwaysDescribed = true;

		List<QuantitativeMeasure> QuantitativeIntervals = splitQuantitativeCharacter(character, remainingTaxa);

		for (int i = 0; i < remainingTaxa.size() - 1; i++) {
			for (int j = i + 1; j < remainingTaxa.size(); j++) {
				if (dataset.getCodedDescription(remainingTaxa.get(i)) != null
						&& dataset.getCodedDescription(remainingTaxa.get(j)) != null) {
					// if the character is applicable for both of these taxa
					if (dataset.isApplicable(remainingTaxa.get(i), character)
							&& dataset.isApplicable(remainingTaxa.get(j), character)) {
						// nb of common states which are absent
						float commonAbsent = 0;
						// nb of common states which are present
						float commonPresent = 0;
						float other = 0;
						QuantitativeMeasure quantitativeMeasure1 = (QuantitativeMeasure) dataset
								.getCodedDescription(remainingTaxa.get(i)).getCharacterDescription(character);
						QuantitativeMeasure quantitativeMeasure2 = (QuantitativeMeasure) dataset
								.getCodedDescription(remainingTaxa.get(j)).getCharacterDescription(character);

						// if at least one description is empty for the current character
						if ((quantitativeMeasure1 != null && quantitativeMeasure1.isNotSpecified())
								|| (quantitativeMeasure2 != null && quantitativeMeasure2.isNotSpecified())) {
							isAlwaysDescribed = false;
						}

						// if one description is unknown and the other have no measure
						if ((quantitativeMeasure1 == null && quantitativeMeasure2 != null && quantitativeMeasure2
								.isNotSpecified())
								|| (quantitativeMeasure2 == null && quantitativeMeasure1 != null && quantitativeMeasure1
										.isNotSpecified())) {
							score++;
							// search common shared values
						} else if (quantitativeMeasure1 != null && quantitativeMeasure2 != null) {

							// if a taxon is described and the other is not, it means that this taxa can be
							// discriminated
							if ((quantitativeMeasure1.isNotSpecified() && !quantitativeMeasure2
									.isNotSpecified())
									|| (quantitativeMeasure2.isNotSpecified() && !quantitativeMeasure1
											.isNotSpecified())) {
								score++;
							} else {

								// search common state
								for (QuantitativeMeasure quantitativeMeasure : QuantitativeIntervals) {
									if (quantitativeMeasure.isInclude(quantitativeMeasure1)) {
										if (quantitativeMeasure.isInclude(quantitativeMeasure2)) {
											commonPresent++;
										} else {
											other++;
										}
									} else {
										if (quantitativeMeasure.isInclude(quantitativeMeasure2)) {
											other++;
										} else {
											commonAbsent++;
										}
									}
								}
								score += applyScoreMethod(commonPresent, commonAbsent, other);
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

		// increasing artificially the score of character containing only described taxa
		if (!alreadyUsedCharacter.contains(character) && isAlwaysDescribed && score > 0) {
			score = (float) ((float) score + (float) 2.0);
		}

		// fewStatesCharacterFirst option handling
		if (Utils.fewStatesCharacterFirst && score > 0) {
			// increasing artificially the score of character with few states
			float coeff = (float) 1 - ((float) 2 / (float) maxNbStatesPerCharacter);
			score = (float) (score + coeff);
		}
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
	 **/
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
	 * This method calculates the maximum number of character per state for the entire dataset
	 * 
	 * @return
	 */
	private int calculateMaxNbStatesPerCharacter() {
		int max = 2;
		for (ICharacter ic : dataset.getCharacters()) {
			if (ic instanceof CategoricalCharacter && ((CategoricalCharacter) ic).getStates() != null
					&& max < ((CategoricalCharacter) ic).getStates().size())
				max = ((CategoricalCharacter) ic).getStates().size();
		}
		return max;
	}

	/**
	 * @param commonPresent
	 * @param commonAbsent
	 * @param other
	 * @return float, the score using the method requested
	 */
	private float applyScoreMethod(float commonPresent, float commonAbsent, float other) {

		float out = 0;

		// Sokal & Michener method
		if (Utils.scoreMethod.trim().equalsIgnoreCase(Utils.SOKALANDMICHENER)) {
			out = 1 - ((commonPresent + commonAbsent) / (commonPresent + commonAbsent + other));
			// round to 10^-3
			out = Utils.roundFloat(out, 3);
		}
		// Jaccard Method
		else if (Utils.scoreMethod.trim().equalsIgnoreCase(Utils.JACCARD)) {
			try {
				// case where description are empty
				out = 1 - (commonPresent / (commonPresent + other));
				// round to 10^-3
				out = Utils.roundFloat(out, 3);
			} catch (ArithmeticException a) {
				out = 0;
			}
		}
		// yes or no method (Xper)
		else {
			if ((commonPresent == 0) && (other > 0)) {
				out = 1;
			} else {
				out = 0;
			}
		}
		return out;
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

	/**
	 * @return int, the max number of states
	 */
	public int getMaxNumStatesPerCharacter() {
		return maxNbStatesPerCharacter;
	}

	/**
	 * @param maxNumStatesPerCharacter
	 */
	public void setMaxNumStatesPerCharacter(int maxNumStatesPerCharacter) {
		this.maxNbStatesPerCharacter = maxNumStatesPerCharacter;
	}

}
