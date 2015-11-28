package fr.lis.ikeyplus.services;

import fr.lis.ikeyplus.model.*;
import fr.lis.ikeyplus.utils.IkeyConfig;
import fr.lis.ikeyplus.utils.IkeyUtils;

import java.io.Serializable;
import java.util.*;

/**
 * This class is the service generating identification keys
 *
 * @author Florian Causse
 * @created 18-04-2011
 */
public class IdentificationKeyGeneratorImpl implements IdentificationKeyGenerator {

    @Override
    public SingleAccessKeyTree getIdentificationKey(DataSet dataset, IkeyConfig config) throws OutOfMemoryError {
        SingleAccessKeyTree singleAccessKeyTree;
        singleAccessKeyTree = new SingleAccessKeyTree(config);
        singleAccessKeyTree.setDataSet(dataset);

        // init maxNumStatesPerCharacter
        int maxNbStatesPerCharacter = calculateMaxNbStatesPerCharacter(dataset);

        // init root node
        SingleAccessKeyNode rootNode = new SingleAccessKeyNode();
        rootNode.setRemainingTaxa(dataset.getTaxa());
        singleAccessKeyTree.setRoot(rootNode);

        // calculate next node
        calculateSingleAccessKeyNodeChild(rootNode, dataset.getCharacters(),
                new ArrayList<>(dataset.getTaxa()), new ArrayList<ICharacter>(), config, dataset, maxNbStatesPerCharacter);

        // delete useless nodes
        boolean isOptimized = true;
        while (isOptimized) {
            isOptimized = optimizeSingleAccessKeyTree(null, singleAccessKeyTree.getRoot(), false);
        }
        return singleAccessKeyTree;
    }

    private void calculateSingleAccessKeyNodeChild(SingleAccessKeyNode parentNode,
                                                   List<ICharacter> remainingCharacters,
                                                   List<Taxon> remainingTaxa,
                                                   List<ICharacter> alreadyUsedCharacter,
                                                   IkeyConfig config,
                                                   DataSet dataset,
                                                   int maxNbStatesPerCharacter) {

        if (remainingCharacters.size() > 0 && remainingTaxa.size() > 1) {

            // get the list of characters which discriminant power depends on the child character
            List<ICharacter> childDependantCharacters = new ArrayList<>();

            // calculate characters score
            Map<ICharacter, Float> charactersScore = charactersScores(remainingCharacters, remainingTaxa,
                    childDependantCharacters, alreadyUsedCharacter, dataset, config, maxNbStatesPerCharacter);
            ICharacter selectedCharacter = bestCharacter(charactersScore, remainingTaxa, config, dataset);

            // delete characters if score method is not Xper and score = 0
            if (config.getScoreMethod() != IkeyConfig.ScoreMethod.XPER) {
                for (Map.Entry<ICharacter, Float> entry : charactersScore.entrySet()) {
                    if (entry.getValue() <= 0) {
                        remainingCharacters.removeAll(entry.getKey().getAllChildren());
                        remainingCharacters.remove(entry.getKey());
                    }
                }
            }

            // get not described taxa
            List<Taxon> notDescribedTaxa;
            if (selectedCharacter.isSupportsCategoricalData()) {
                notDescribedTaxa = getNotDescribedTaxa(remainingTaxa,
                        ((CategoricalCharacter) selectedCharacter), dataset);
                // delete not described taxa from the remaining taxa list
                remainingTaxa.removeAll(notDescribedTaxa);
            } else {
                notDescribedTaxa = getNotDescribedTaxa(remainingTaxa,
                        ((QuantitativeCharacter) selectedCharacter), dataset);
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
                List<SingleAccessKeyNode> futureChildNodes = new ArrayList<>();

                for (State state : ((CategoricalCharacter) selectedCharacter).getStates()) {
                    List<Taxon> newRemainingTaxa = getRemainingTaxa(remainingTaxa,
                            ((CategoricalCharacter) selectedCharacter), state, dataset);

                    // test if we have to stop the branch or continue
                    if (newRemainingTaxa.size() > 0) {

                        // init new node
                        SingleAccessKeyNode node = new SingleAccessKeyNode();
                        node.setCharacter(selectedCharacter);
                        node.setRemainingTaxa(newRemainingTaxa);
                        node.setCharacterState(state);

                        // mergeCharacterStatesIfSameDiscrimination option handling
                        if (config.isMergeCharacterStatesIfSameDiscrimination()
                                && mergeNodesIfSameDiscrimination(futureChildNodes, node)) {
                            continue;
                        }

                        // add the current node to the current child nodes list
                        futureChildNodes.add(node);

                        // put new node as child of parentNode
                        parentNode.addChild(node);

                        // create new remaining characters list
                        List<ICharacter> newRemainingCharacters = new ArrayList<>(
                                remainingCharacters);
                        // remove last best character from the remaining characters list
                        newRemainingCharacters.remove(selectedCharacter);

                        // get inapplicable characters
                        List<ICharacter> inapplicableCharacters = dataset.getInapplicableCharacters(
                                newRemainingCharacters, state);
                        // remove inapplicable character and its sons from the remaining characters list
                        newRemainingCharacters.removeAll(inapplicableCharacters);

                        // pruning option handling
                        if (config.isPruningEnabled() && remainingTaxa.containsAll(newRemainingTaxa)
                                && newRemainingTaxa.containsAll(remainingTaxa)
                                && !childDependantCharacters.contains(selectedCharacter)) {
                            node.setNodeDescription(IkeyConfig.getBundleConfElement("message.warning.pruning"));
                        } else {
                            // calculate next node
                            calculateSingleAccessKeyNodeChild(node, newRemainingCharacters, newRemainingTaxa,
                                    new ArrayList<>(alreadyUsedCharacter), config, dataset, maxNbStatesPerCharacter);
                        }
                    }
                }

                // if the character is numerical
            } else {

                // add the selected character to the already used characters list
                alreadyUsedCharacter.add(selectedCharacter);
                List<QuantitativeMeasure> quantitativeMeasures = splitQuantitativeCharacter(
                        selectedCharacter, remainingTaxa, dataset);

                for (QuantitativeMeasure quantitativeMeasure : quantitativeMeasures) {
                    List<Taxon> newRemainingTaxa = getRemainingTaxa(remainingTaxa,
                            ((QuantitativeCharacter) selectedCharacter), quantitativeMeasure, dataset);

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
                        List<ICharacter> newRemainingCharacters = new ArrayList<>(
                                remainingCharacters);

                        // pruning option handling
                        if (config.isPruningEnabled() && remainingTaxa.containsAll(newRemainingTaxa)
                                && newRemainingTaxa.containsAll(remainingTaxa)
                                && !childDependantCharacters.contains(selectedCharacter)) {
                            node.setNodeDescription(IkeyConfig.getBundleConfElement("message.warning.pruning"));
                        } else {
                            // if current remaining taxa are similar to parent node remaining taxa
                            if (parentNode.getRemainingTaxa().size() == newRemainingTaxa.size()) {
                                // remove last best character from the remaining characters list
                                newRemainingCharacters.remove(selectedCharacter);
                                // calculate next node without selected character
                                calculateSingleAccessKeyNodeChild(node, newRemainingCharacters,
                                        newRemainingTaxa, new ArrayList<>(alreadyUsedCharacter), config, dataset, maxNbStatesPerCharacter);
                            } else {
                                // calculate next node
                                calculateSingleAccessKeyNodeChild(node, newRemainingCharacters,
                                        newRemainingTaxa, new ArrayList<>(alreadyUsedCharacter), config, dataset, maxNbStatesPerCharacter);
                            }
                        }
                    }
                }
            }

            // if taxa are not described and if verbosity string contains correct tag, create a node
            // "Other (not described)"
            if (config.getVerbosity().contains(IkeyConfig.VerbosityLevel.OTHER) && notDescribedTaxa.size() > 0) {
                // init new node
                SingleAccessKeyNode notDescribedNode = new SingleAccessKeyNode();
                notDescribedNode.setCharacter(selectedCharacter);
                notDescribedNode.setRemainingTaxa(notDescribedTaxa);
                notDescribedNode.setCharacterState(new State(IkeyConfig
                        .getBundleConfElement("message.notDescribed")));

                // put new node as child of parentNode
                parentNode.addChild(notDescribedNode);
            }
        }
    }

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

    private boolean optimizeSingleAccessKeyTree(SingleAccessKeyNode parentNode,
                                                SingleAccessKeyNode node,
                                                boolean isOptimized) {

        if (node != null) {
            if (parentNode != null
                    && parentNode.getChildren().size() == 1
                    && parentNode.getRemainingTaxa().size() == node.getRemainingTaxa().size()) {
                parentNode.getChildren().addAll(node.getChildren());
                parentNode.getChildren().remove(node);
                isOptimized = true;
            }
            for (int i = 0; i < node.getChildren().size(); i++) {
                isOptimized = optimizeSingleAccessKeyTree(node, node.getChildren().get(i), isOptimized);
            }
        }
        return isOptimized;
    }

    private List<Taxon> getRemainingTaxa(List<Taxon> remainingTaxa,
                                         CategoricalCharacter character,
                                         State state,
                                         DataSet dataset) {

        List<Taxon> newRemainingTaxa = new ArrayList<>();

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

    private List<Taxon> getRemainingTaxa(List<Taxon> remainingTaxa,
                                         QuantitativeCharacter character,
                                         QuantitativeMeasure quantitativeMeasure,
                                         DataSet dataset) {

        List<Taxon> newRemainingTaxa = new ArrayList<>();

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

    private List<Taxon> getNotDescribedTaxa(List<Taxon> remainingTaxa,
                                            CategoricalCharacter character,
                                            DataSet dataset) {

        List<Taxon> notDescribedTaxa = new ArrayList<>();

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

    private List<Taxon> getNotDescribedTaxa(List<Taxon> remainingTaxa,
                                            QuantitativeCharacter character,
                                            DataSet dataset) {

        List<Taxon> notDescribedTaxa = new ArrayList<>();

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

    private static class DoubleComparator implements Comparator<Double>, Serializable {

        private static final long serialVersionUID = -3053308031245292806L;

        @Override
        public int compare(Double val1, Double val2) {
            return Double.compare(val1, val2);
        }
    }

    private List<QuantitativeMeasure> splitQuantitativeCharacter(ICharacter character,
                                                                 List<Taxon> remainingTaxa,
                                                                 DataSet dataset) {

        List<QuantitativeMeasure> quantitativeMeasures = new ArrayList<>();
        QuantitativeMeasure quantitativeMeasure1 = new QuantitativeMeasure();
        QuantitativeMeasure quantitativeMeasure2 = new QuantitativeMeasure();

        // get the Min and Max values of all remaining taxa
        List<Double> allValues = getAllNumericalValues(character, remainingTaxa, dataset);
        Collections.sort(allValues, new DoubleComparator());
        // determine the best threshold to cut the interval in 2 part
        Double threshold;
        Double bestThreshold = null;
        int difference = allValues.size();
        int differenceMin = difference;
        int taxaBefore;
        int taxaAfter;
        for (int i = 0; i < allValues.size() / 2; i++) {
            threshold = allValues.get(i * 2 + 1);
            taxaBefore = 0;
            taxaAfter = 0;
            for (int j = 0; j < allValues.size() / 2; j++) {
                if (allValues.get(j * 2 + 1) <= threshold) {
                    taxaBefore++;
                }
                if (allValues.get(j * 2) >= threshold) {
                    taxaAfter++;
                }
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
            quantitativeMeasure1.setMax(bestThreshold);
            quantitativeMeasure1.setMaxInclude(false);
            quantitativeMeasure2.setMin(bestThreshold);
            quantitativeMeasure2.setMax(allValues.get(allValues.size() - 1));
        }

        // add the 2 new interval to the list
        quantitativeMeasures.add(quantitativeMeasure1);
        quantitativeMeasures.add(quantitativeMeasure2);

        return quantitativeMeasures;
    }

    private List<Double> getAllNumericalValues(ICharacter character, List<Taxon> remainingTaxa, DataSet dataset) {

        List<Double> allValues = new ArrayList<>();

        for (Taxon taxon : remainingTaxa) {
            if (dataset.getCodedDescription(taxon).getCharacterDescription(character) != null
                    && dataset.getCodedDescription(taxon).getCharacterDescription(character) instanceof QuantitativeMeasure) {

                Double minTmp = ((QuantitativeMeasure) dataset.getCodedDescription(taxon)
                        .getCharacterDescription(character)).getCalculateMinimum();
                Double maxTmp = ((QuantitativeMeasure) dataset.getCodedDescription(taxon)
                        .getCharacterDescription(character)).getCalculateMaximum();
                if (minTmp != null) {
                    allValues.add(minTmp);
                }
                if (maxTmp != null) {
                    allValues.add(maxTmp);
                }
            }
        }
        return allValues;
    }

    private Map<ICharacter, Float> charactersScores(List<ICharacter> characters,
                                                    List<Taxon> remaningTaxa,
                                                    List<ICharacter> childDependantCharacters,
                                                    List<ICharacter> alreadyUsedCharacter,
                                                    DataSet dataset,
                                                    IkeyConfig config,
                                                    int maxNbStatesPerCharacter) {
        HashMap<ICharacter, Float> scoreMap = new LinkedHashMap<>();
        for (ICharacter character : characters) {
            if (character.isSupportsCategoricalData()) {
                scoreMap.put(character,
                        categoricalCharacterScore((CategoricalCharacter) character, remaningTaxa, dataset, config, maxNbStatesPerCharacter));
            } else {
                scoreMap.put(
                        character,
                        quantitativeCharacterScore((QuantitativeCharacter) character, remaningTaxa,
                                alreadyUsedCharacter, dataset, config, maxNbStatesPerCharacter));
            }
        }

        // take in consideration the score of child character
        considerChildCharacterScore(scoreMap, childDependantCharacters);

        return scoreMap;
    }

    private void considerChildCharacterScore(HashMap<ICharacter, Float> scoreMap,
                                             List<ICharacter> childDependantCharacters) {
        for (Map.Entry<ICharacter, Float> characterScoreEntry : scoreMap.entrySet()) {
            final ICharacter character = characterScoreEntry.getKey();
            final Float score = characterScoreEntry.getValue();

            if (character.isSupportsCategoricalData() && character.getChildCharacters().size() > 0) {
                float max = getMaxChildScore(scoreMap, character);
                if (score < max) {
                    scoreMap.put(character, max);
                    childDependantCharacters.add(character);
                }
            }
        }
    }

    private float getMaxChildScore(HashMap<ICharacter, Float> scoreMap, ICharacter character) {
        List<ICharacter> characters = character.getAllChildren();
        float max = -1;
        if (character.getParentCharacter() != null
                && scoreMap.keySet().contains(character.getParentCharacter())) {
            max = -1;
        } else {
            for (ICharacter childCharacter : characters) {
                if (scoreMap.get(childCharacter) != null) {
                    if (max == -1) {
                        max = scoreMap.get(childCharacter);
                    }
                    if (scoreMap.get(childCharacter) >= max) {
                        // init max score with child score + 0.0001 (to ensure that
                        // the parent score will be better)
                        max = (float) (scoreMap.get(childCharacter) + 0.0001);
                    }
                }
            }
        }
        return max;
    }

    /**
     * Returns the character with the best score.</br></br> Character weight takes precedence over
     * discriminant power.</br> By default, global character weights are used, but if
     * useContextualCharacterWeights is set to <b> <tt>true</tt></b>, contextual character weights are used,
     * <i>i.e.</i> for a given character, the weight applied may vary depending on the taxon considered.</br>
     * If no weight are detected in the SDD file, all the characters are initialized with the same weight (3)
     */
    private ICharacter bestCharacter(Map<ICharacter, Float> charactersScore,
                                     List<Taxon> remainingTaxa,
                                     IkeyConfig config,
                                     DataSet dataset) {

        float bestScore = -1;
        ICharacter bestCharacter = null;

        if (config.getWeightType() == IkeyConfig.WeightType.CONTEXTUAL) {
            float bestWeight = -1;

            for (Map.Entry<ICharacter, Float> characterScoreEntry : charactersScore.entrySet()) {
                final ICharacter character = characterScoreEntry.getKey();
                final Float score = characterScoreEntry.getValue();

                int nWeights = 0;
                float weightsSum = 0;
                float averageWeight = 0;
                for (Taxon taxon : remainingTaxa) {

                    CodedDescription currentCodedDescription = dataset.getCodedDescription(taxon);
                    if (currentCodedDescription.getCharacterWeights().containsKey(character)) {
                        nWeights++;
                        weightsSum += currentCodedDescription.getCharacterWeight(character);
                    } else {
                        nWeights++;
                        weightsSum += IkeyConfig.DEFAULT_WEIGHT.getIntWeight();
                    }

                }
                if (nWeights > 0) {
                    averageWeight = (weightsSum / nWeights);
                }

                if (averageWeight > bestWeight) {
                    bestCharacter = character;
                    bestWeight = averageWeight;
                    bestScore = score;
                } else if (Objects.equals(averageWeight, bestWeight) && score >= bestScore) {
                    bestScore = score;
                    bestCharacter = character;
                }
            }

            charactersScore.remove(bestCharacter);

            // if the set of scores contains at least one score similar to the best score
            if (charactersScore.containsValue(bestScore) && bestCharacter.isSupportsCategoricalData()) {
                int lessTaxaNumber = getTaxaNumberForAllStates((CategoricalCharacter) bestCharacter,
                        remainingTaxa, dataset);

                for (Map.Entry<ICharacter, Float> characterScoreEntry : charactersScore.entrySet()) {
                    final ICharacter character = characterScoreEntry.getKey();
                    final Float score = characterScoreEntry.getValue();

                    for (Taxon taxon : remainingTaxa) {
                        CodedDescription currentCodedDescription = dataset.getCodedDescription(taxon);
                        if (currentCodedDescription.getCharacterWeight(character) != null
                                && currentCodedDescription.getCharacterWeight(character) == Float.valueOf(bestWeight).intValue()
                                && score == bestScore
                                && character.isSupportsCategoricalData()) {
                            // get the number of taxa of all child nodes of the current CategoricalCharacter
                            int currentTaxaNumber = getTaxaNumberForAllStates(
                                    (CategoricalCharacter) character, remainingTaxa, dataset);
                            // if the current taxa number is lower than the less taxa number
                            if (currentTaxaNumber < lessTaxaNumber) {
                                bestScore = score;
                                bestCharacter = character;
                                lessTaxaNumber = currentTaxaNumber;
                            }
                        }
                    }
                }

            }

        } else {

            float bestWeight = -1;

            for (Map.Entry<ICharacter, Float> characterScoreEntry : charactersScore.entrySet()) {
                final ICharacter character = characterScoreEntry.getKey();
                final Float score = characterScoreEntry.getValue();
                // if the current character weight is better than the bestWeight
                if (character.getWeight() > bestWeight) {
                    bestCharacter = character;
                    bestWeight = character.getWeight();
                    bestScore = score;
                    // if the current character weight is equal to the bestWeight and the current character
                    // score
                    // is better than the best score
                } else if (character.getWeight() == bestWeight && score >= bestScore) {
                    bestScore = score;
                    bestCharacter = character;
                }

            }

            charactersScore.remove(bestCharacter);

            // if the set of scores contains at least one score similar to the best score
            if (charactersScore.containsValue(bestScore) && bestCharacter.isSupportsCategoricalData()) {
                int lessTaxaNumber = getTaxaNumberForAllStates((CategoricalCharacter) bestCharacter,
                        remainingTaxa, dataset);

                for (Map.Entry<ICharacter, Float> characterScoreEntry : charactersScore.entrySet()) {
                    final ICharacter character = characterScoreEntry.getKey();
                    final Float score = characterScoreEntry.getValue();

                    if (character.getWeight() == bestWeight && score == bestScore
                            && character.isSupportsCategoricalData()) {
                        // get the number of taxa of all child nodes of the current CategoricalCharacter
                        int currentTaxaNumber = getTaxaNumberForAllStates((CategoricalCharacter) character,
                                remainingTaxa, dataset);
                        // if the current taxa number is lower than the less taxa number
                        if (currentTaxaNumber < lessTaxaNumber) {
                            bestScore = score;
                            bestCharacter = character;
                            lessTaxaNumber = currentTaxaNumber;
                        }
                    }
                }
            }
        }

        return bestCharacter;
    }

    private int getTaxaNumberForAllStates(CategoricalCharacter character, List<Taxon> remainingTaxa, DataSet dataset) {
        int taxaNumber = 0;
        for (Taxon taxon : remainingTaxa) {
            if (dataset.getCodedDescription(taxon).getCharacterDescription(character) != null) {
                taxaNumber += ((List<State>) dataset.getCodedDescription(taxon).getCharacterDescription(
                        character)).size();
            }
        }
        return taxaNumber;
    }

    private float categoricalCharacterScore(CategoricalCharacter character,
                                            List<Taxon> remainingTaxa,
                                            DataSet dataset,
                                            IkeyConfig config,
                                            int maxNbStatesPerCharacter) {
        int cpt = 0;
        float score = 0;
        boolean isAlwaysDescribed = true;

        for (int i = 0; i < remainingTaxa.size() - 1; i++) {
            for (int j = i + 1; j < remainingTaxa.size(); j++) {
                if (dataset.getCodedDescription(remainingTaxa.get(i)) != null
                        && dataset.getCodedDescription(remainingTaxa.get(j)) != null
                        && dataset.isApplicable(remainingTaxa.get(i), character)
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
                        score += applyScoreMethod(commonPresent, commonAbsent, other, config);
                    }
                    cpt++;
                }
            }
        }

        if (cpt >= 1) {
            score = score / cpt;
        }

        // increasing artificially the score of character containing only described taxa
        if (isAlwaysDescribed && score > 0) {
            score = score + (float) 2.0;
        }
        // fewStatesCharacterFirst option handling
        if (config.isFewStatesCharacterFirst() && score > 0 && character.getStates().size() >= 2) {
            // increasing artificially score of character with few states
            float coeff = (float) 1
                    - ((float) character.getStates().size() / (float) maxNbStatesPerCharacter);
            score = score + coeff;
        }

        return score;
    }

    private float quantitativeCharacterScore(QuantitativeCharacter character,
                                             List<Taxon> remainingTaxa,
                                             List<ICharacter> alreadyUsedCharacter,
                                             DataSet dataset,
                                             IkeyConfig config,
                                             int maxNbStatesPerCharacter) {
        int cpt = 0;
        float score = 0;
        boolean isAlwaysDescribed = true;

        List<QuantitativeMeasure> QuantitativeIntervals = splitQuantitativeCharacter(character, remainingTaxa, dataset);

        for (int i = 0; i < remainingTaxa.size() - 1; i++) {
            for (int j = i + 1; j < remainingTaxa.size(); j++) {
                if (dataset.getCodedDescription(remainingTaxa.get(i)) != null
                        && dataset.getCodedDescription(remainingTaxa.get(j)) != null
                        // if the character is applicable for both of these taxa
                        && dataset.isApplicable(remainingTaxa.get(i), character)
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
                            score += applyScoreMethod(commonPresent, commonAbsent, other, config);
                        }
                    }
                    cpt++;
                }
            }
        }


        if (cpt >= 1) {
            score = score / cpt;
        }

        // increasing artificially the score of character containing only described taxa
        if (!alreadyUsedCharacter.contains(character) && isAlwaysDescribed && score > 0) {
            score = score + (float) 2.0;
        }

        // fewStatesCharacterFirst option handling
        if (config.isFewStatesCharacterFirst() && score > 0) {
            // increasing artificially the score of character with few states
            float coeff = (float) 1 - ((float) 2 / (float) maxNbStatesPerCharacter);
            score = score + coeff;
        }
        return score;
    }

    private int calculateMaxNbStatesPerCharacter(DataSet dataset) {
        int max = 2;
        for (ICharacter ic : dataset.getCharacters()) {
            if (ic instanceof CategoricalCharacter && ((CategoricalCharacter) ic).getStates() != null
                    && max < ((CategoricalCharacter) ic).getStates().size()) {
                max = ((CategoricalCharacter) ic).getStates().size();
            }
        }
        return max;
    }

    private float applyScoreMethod(float commonPresent, float commonAbsent, float other, IkeyConfig config) {

        float out;

        // Sokal & Michener method
        if (config.getScoreMethod() == IkeyConfig.ScoreMethod.SOKAL_AND_MICHENER) {
            out = 1 - ((commonPresent + commonAbsent) / (commonPresent + commonAbsent + other));
            // round to 10^-3
            out = IkeyUtils.roundFloat(out, 3);
        }
        // Jaccard Method
        else if (config.getScoreMethod() == IkeyConfig.ScoreMethod.JACCARD) {
            try {
                // case where description are empty
                out = 1 - (commonPresent / (commonPresent + other));
                // round to 10^-3
                out = IkeyUtils.roundFloat(out, 3);
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

}
