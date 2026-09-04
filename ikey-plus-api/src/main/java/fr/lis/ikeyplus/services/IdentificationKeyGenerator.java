package fr.lis.ikeyplus.services;

import fr.lis.ikeyplus.model.character.CategoricalCharacter;
import fr.lis.ikeyplus.model.CodedDescription;
import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.model.character.ICharacter;
import fr.lis.ikeyplus.model.character.QuantitativeCharacter;
import fr.lis.ikeyplus.model.QuantitativeMeasure;
import fr.lis.ikeyplus.model.SingleAccessKeyNode;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.model.State;
import fr.lis.ikeyplus.model.Taxon;
import fr.lis.ikeyplus.utils.IkeyConfig;
import fr.lis.ikeyplus.utils.IkeyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private final DataSet dataset;
    // the config object (containing options)
    private final IkeyConfig config;
    // the maximum number of states per character
    private int maxNbStatesPerCharacter;

    public IdentificationKeyGenerator(final DataSet dataset, final IkeyConfig config) {
        this.dataset = dataset;
        this.config = config;
    }

    public void createIdentificationKey() throws OutOfMemoryError, Exception {

        this.singleAccessKeyTree = new SingleAccessKeyTree(config);
        singleAccessKeyTree.setDataSet(dataset);

        // init maxNumStatesPerCharacter
        this.maxNbStatesPerCharacter = calculateMaxNbStatesPerCharacter();

        // init root node
        final SingleAccessKeyNode rootNode = new SingleAccessKeyNode();
        rootNode.setRemainingTaxa(dataset.getTaxa());
        singleAccessKeyTree.setRoot(rootNode);

        // calculate next node
        calculateSingleAccessKeyNodeChild(rootNode, dataset.getCharacters(),
                new ArrayList<>(dataset.getTaxa()), new ArrayList<>());

        // delete useless nodes
        boolean isOptimized = true;
        while (isOptimized) {
            isOptimized = optimizeSingleAccessKeyTree(null, singleAccessKeyTree.getRoot(), false);
        }
    }

    private void calculateSingleAccessKeyNodeChild(
            final SingleAccessKeyNode parentNode,
            final List<ICharacter> remainingCharacters,
            final List<Taxon> remainingTaxa,
            final List<ICharacter> alreadyUsedCharacter
    ) throws Exception {

        if (!remainingCharacters.isEmpty() && remainingTaxa.size() > 1) {

            // get the list of characters which discriminant power depends on the child character
            final List<ICharacter> childDependantCharacters = new ArrayList<>();

            // calculate characters score
            final Map<ICharacter, Float> charactersScore = charactersScores(remainingCharacters, remainingTaxa,
                    childDependantCharacters, alreadyUsedCharacter);
            final ICharacter selectedCharacter = bestCharacter(charactersScore, remainingTaxa);

            // delete characters if score method is not Xper and score = 0
            if (config.getScoreMethod() != IkeyConfig.ScoreMethod.XPER) {
                for (final Map.Entry<ICharacter, Float> entry : charactersScore.entrySet()) {
                    if (entry.getValue() <= 0) {
                        remainingCharacters.removeAll(entry.getKey().getAllChildren());
                        remainingCharacters.remove(entry.getKey());
                    }
                }
            }

            // get not described taxa
            final List<Taxon> notDescribedTaxa;
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
                final List<SingleAccessKeyNode> futureChildNodes = new ArrayList<>();

                final CategoricalCharacter catCharCast = (CategoricalCharacter) selectedCharacter;
                for (final State state : catCharCast.getStates()) {
                    final List<Taxon> newRemainingTaxa = getRemainingTaxa(remainingTaxa,
                            catCharCast, state);

                    // test if we have to stop the branch or continue
                    if (!newRemainingTaxa.isEmpty()) {

                        // init new node
                        final SingleAccessKeyNode node = new SingleAccessKeyNode();
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
                        final List<ICharacter> newRemainingCharacters = new ArrayList<>(
                                remainingCharacters);
                        // remove last best character from the remaining characters list
                        newRemainingCharacters.remove(selectedCharacter);

                        // get inapplicable characters
                        final List<ICharacter> inapplicableCharacters = DataSet.getInapplicableCharacters(
                                newRemainingCharacters, selectedCharacter, state);
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
                                    new ArrayList<>(alreadyUsedCharacter));
                        }
                    }
                }

                // if the character is numerical
            } else {

                // add the selected character to the already used characters list
                alreadyUsedCharacter.add(selectedCharacter);
                final QuantitativeCharacter quantCharCast = (QuantitativeCharacter) selectedCharacter;
                final List<QuantitativeMeasure> quantitativeMeasures = splitQuantitativeCharacter(
                        quantCharCast, remainingTaxa);

                for (final QuantitativeMeasure quantitativeMeasure : quantitativeMeasures) {
                    final List<Taxon> newRemainingTaxa = getRemainingTaxa(remainingTaxa,
                            quantCharCast, quantitativeMeasure);

                    // test if we have to stop the branch or continue
                    if (!newRemainingTaxa.isEmpty()) {

                        // init new node
                        final SingleAccessKeyNode node = new SingleAccessKeyNode();
                        node.setCharacter(selectedCharacter);
                        node.setRemainingTaxa(newRemainingTaxa);
                        node.setCharacterState(quantitativeMeasure);

                        // put new node as child of parentNode
                        parentNode.addChild(node);

                        // create new remaining characters list, we don't remove the last best character
                        final List<ICharacter> newRemainingCharacters = new ArrayList<>(
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
                                        newRemainingTaxa, new ArrayList<>(alreadyUsedCharacter));
                            } else {
                                // calculate next node
                                calculateSingleAccessKeyNodeChild(node, newRemainingCharacters,
                                        newRemainingTaxa, new ArrayList<>(alreadyUsedCharacter));
                            }
                        }
                    }
                }
            }

            // if taxa are not described and if verbosity string contains correct tag, create a node
            // "Other (not described)"
            if (config.getVerbosity().contains(IkeyConfig.VerbosityLevel.OTHER) && !notDescribedTaxa.isEmpty()) {
                // init new node
                final SingleAccessKeyNode notDescribedNode = new SingleAccessKeyNode();
                notDescribedNode.setCharacter(selectedCharacter);
                notDescribedNode.setRemainingTaxa(notDescribedTaxa);
                notDescribedNode.setCharacterState(new State(IkeyConfig
                        .getBundleConfElement("message.notDescribed")));

                // put new node as child of parentNode
                parentNode.addChild(notDescribedNode);
            }
        }
    }

    private boolean mergeNodesIfSameDiscrimination(
            final List<SingleAccessKeyNode> futureChildNodes,
            final SingleAccessKeyNode node
    ) {

        for (final SingleAccessKeyNode futureChildNode : futureChildNodes) {
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

    public boolean optimizeSingleAccessKeyTree(
            final SingleAccessKeyNode parentNode,
            final SingleAccessKeyNode node,
            boolean isOptimized
    ) {

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

    private List<Taxon> getRemainingTaxa(
            final List<Taxon> remainingTaxa,
            final CategoricalCharacter character,
            final State state
    ) {

        final List<Taxon> newRemainingTaxa = new ArrayList<>();

        // init new remaining taxa list with taxa description matching the
        // current state
        for (final Taxon taxon : remainingTaxa) {
            if (dataset.getCodedDescription(taxon).isUnknownDescription(character)
                    || dataset.getCodedDescription(taxon).getCategoricalCharacterDescription(character)
                    .contains(state)) {
                newRemainingTaxa.add(taxon);
            }
        }
        return newRemainingTaxa;
    }

    private List<Taxon> getRemainingTaxa(
            final List<Taxon> remainingTaxa,
            final QuantitativeCharacter character,
            final QuantitativeMeasure quantitativeMeasure
    ) {

        final List<Taxon> newRemainingTaxa = new ArrayList<>();

        // init new remaining taxa list with taxa description matching the
        // current state
        for (final Taxon taxon : remainingTaxa) {
            if (quantitativeMeasure.isInclude(dataset.getCodedDescription(taxon)
                    .getQuantitativeCharacterDescription(character))) {
                newRemainingTaxa.add(taxon);
            }
        }
        return newRemainingTaxa;
    }

    private List<Taxon> getNotDescribedTaxa(final List<Taxon> remainingTaxa, final CategoricalCharacter character) {

        final List<Taxon> notDescribedTaxa = new ArrayList<>();

        // init not described taxa list with taxa without description
        for (final Taxon taxon : remainingTaxa) {
            if (!dataset.getCodedDescription(taxon).isUnknownDescription(character)
                    && dataset.getCodedDescription(taxon).getCategoricalCharacterDescription(character).isEmpty()) {
                notDescribedTaxa.add(taxon);
            }
        }
        return notDescribedTaxa;
    }

    private List<Taxon> getNotDescribedTaxa(final List<Taxon> remainingTaxa, final QuantitativeCharacter character) {

        final List<Taxon> notDescribedTaxa = new ArrayList<>();

        // init not described taxa list with taxa without description
        for (final Taxon taxon : remainingTaxa) {
            if (!dataset.getCodedDescription(taxon).isUnknownDescription(character)
                    && dataset.getCodedDescription(taxon).getQuantitativeCharacterDescription(
                    character).isNotSpecified()) {
                notDescribedTaxa.add(taxon);
            }
        }
        return notDescribedTaxa;
    }

    private List<QuantitativeMeasure> splitQuantitativeCharacter(
            final QuantitativeCharacter character,
            final List<Taxon> remainingTaxa
    ) throws Exception {

        final List<QuantitativeMeasure> quantitativeMeasures = new ArrayList<>();
        final QuantitativeMeasure quantitativeMeasure1 = new QuantitativeMeasure();
        final QuantitativeMeasure quantitativeMeasure2 = new QuantitativeMeasure();

        // get the Min and Max values of all remaining taxa
        final List<Double> allValues = getAllNumericalValues(character, remainingTaxa);
        allValues.sort(Double::compare);
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
            quantitativeMeasure1.setMin(allValues.getFirst());
            quantitativeMeasure1.setMax(bestThreshold);
            quantitativeMeasure1.setMaxInclude(false);
            quantitativeMeasure2.setMin(bestThreshold);
            quantitativeMeasure2.setMax(allValues.getLast());
        }

        // add the 2 new interval to the list
        quantitativeMeasures.add(quantitativeMeasure1);
        quantitativeMeasures.add(quantitativeMeasure2);

        return quantitativeMeasures;
    }

    private List<Double> getAllNumericalValues(final QuantitativeCharacter character, final List<Taxon> remainingTaxa) {

        final List<Double> allValues = new ArrayList<>();

        for (final Taxon taxon : remainingTaxa) {
            if (!dataset.getCodedDescription(taxon).isUnknownDescription(character)
                    && dataset.getCodedDescription(taxon).getQuantitativeCharacterDescription(character) != null) {

                final Double minTmp = dataset.getCodedDescription(taxon)
                        .getQuantitativeCharacterDescription(character).getCalculateMinimum();
                final Double maxTmp = dataset.getCodedDescription(taxon)
                        .getQuantitativeCharacterDescription(character).getCalculateMaximum();
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

    private Map<ICharacter, Float> charactersScores(
            final List<ICharacter> characters,
            final List<Taxon> remaningTaxa,
            final List<ICharacter> childDependantCharacters,
            final List<ICharacter> alreadyUsedCharacter
    )
            throws Exception {
        final HashMap<ICharacter, Float> scoreMap = new LinkedHashMap<>();
        for (final ICharacter character : characters) {
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

    private void considerChildCharacterScore(
            final HashMap<ICharacter, Float> scoreMap,
            final List<ICharacter> childDependantCharacters
    ) {
        for (final Map.Entry<ICharacter, Float> entry : scoreMap.entrySet()) {
            final ICharacter character = entry.getKey();
            if (character.isSupportsCategoricalData() && !character.getChildCharacters().isEmpty()) {
                final float max = getMaxChildScore(scoreMap, character);
                if (entry.getValue() < max) {
                    scoreMap.put(character, max);
                    childDependantCharacters.add(character);
                }
            }
        }
    }

    private float getMaxChildScore(final HashMap<ICharacter, Float> scoreMap, final ICharacter character) {
        final List<ICharacter> characters = character.getAllChildren();
        float max = -1;
        if (character.getParentCharacter() != null
                && scoreMap.containsKey(character.getParentCharacter())) {
            max = -1;
        } else {
            for (final ICharacter childCharacter : characters) {
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
    private ICharacter bestCharacter(final Map<ICharacter, Float> charactersScore, final List<Taxon> remainingTaxa) {

        float bestScore = -1;
        ICharacter bestCharacter = null;

        if (config.getWeightType() == IkeyConfig.WeightType.CONTEXTUAL) {
            float bestWeight = -1;

            for (final Map.Entry<ICharacter, Float> entry : charactersScore.entrySet()) {
                final ICharacter character = entry.getKey();

                if (charactersScore.containsKey(character)) {
                    int nWeights = 0;
                    float weightsSum = 0;
                    float averageWeight = 0;
                    for (final Taxon taxon : remainingTaxa) {

                        final CodedDescription currentCodedDescription = dataset.getCodedDescription(taxon);
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
                        bestScore = entry.getValue();
                    } else if (averageWeight == bestWeight && entry.getValue() >= bestScore) {
                        bestScore = entry.getValue();
                        bestCharacter = character;
                    }
                }

            }

            charactersScore.remove(bestCharacter);

            // if the set of scores contains at least one score similar to the best score
            if (charactersScore.containsValue(bestScore) && bestCharacter.isSupportsCategoricalData()) {
                int lessTaxaNumber = getTaxaNumberForAllStates((CategoricalCharacter) bestCharacter,
                        remainingTaxa);

                for (final Map.Entry<ICharacter, Float> entry : charactersScore.entrySet()) {
                    final ICharacter character = entry.getKey();
                    for (final Taxon taxon : remainingTaxa) {
                        final CodedDescription currentCodedDescription = dataset.getCodedDescription(taxon);
                        if (currentCodedDescription.getCharacterWeight(character) != null
                                && currentCodedDescription.getCharacterWeight(character) == bestWeight
                                && entry.getValue() == bestScore
                                && character.isSupportsCategoricalData()) {
                            // get the number of taxa of all child nodes of the current CategoricalCharacter
                            final int currentTaxaNumber = getTaxaNumberForAllStates(
                                    (CategoricalCharacter) character, remainingTaxa);
                            // if the current taxa number is lower than the less taxa number
                            if (currentTaxaNumber < lessTaxaNumber) {
                                bestScore = entry.getValue();
                                bestCharacter = character;
                                lessTaxaNumber = currentTaxaNumber;
                            }
                        }
                    }
                }

            }

        } else {

            float bestWeight = -1;
            for (final Map.Entry<ICharacter, Float> entry : charactersScore.entrySet()) {
                final ICharacter character = entry.getKey();

                // if the current character weight is better than the bestWeight
                if (character.getWeight() > bestWeight) {
                    bestCharacter = character;
                    bestWeight = character.getWeight();
                    bestScore = entry.getValue();
                    // if the current character weight is equal to the bestWeight and the current character
                    // score
                    // is better than the best score
                } else if (character.getWeight() == bestWeight && entry.getValue() >= bestScore) {
                    bestScore = entry.getValue();
                    bestCharacter = character;
                }

            }

            charactersScore.remove(bestCharacter);

            // if the set of scores contains at least one score similar to the best score
            if (charactersScore.containsValue(bestScore) && bestCharacter.isSupportsCategoricalData()) {
                int lessTaxaNumber = getTaxaNumberForAllStates((CategoricalCharacter) bestCharacter,
                        remainingTaxa);
                for (final Map.Entry<ICharacter, Float> entry : charactersScore.entrySet()) {
                    final ICharacter character = entry.getKey();
                    if (character.getWeight() == bestWeight && entry.getValue() == bestScore
                            && character.isSupportsCategoricalData()) {
                        // get the number of taxa of all child nodes of the current CategoricalCharacter
                        final int currentTaxaNumber = getTaxaNumberForAllStates((CategoricalCharacter) character,
                                remainingTaxa);
                        // if the current taxa number is lower than the less taxa number
                        if (currentTaxaNumber < lessTaxaNumber) {
                            bestScore = entry.getValue();
                            bestCharacter = character;
                            lessTaxaNumber = currentTaxaNumber;
                        }
                    }
                }
            }
        }

        return bestCharacter;
    }

    private int getTaxaNumberForAllStates(final CategoricalCharacter character, final List<Taxon> remainingTaxa) {
        int taxaNumber = 0;
        for (final Taxon taxon : remainingTaxa) {
            if (!dataset.getCodedDescription(taxon).isUnknownDescription(character)) {
                taxaNumber += dataset.getCodedDescription(taxon).getCategoricalCharacterDescription(
                        character).size();
            }
        }
        return taxaNumber;
    }

    private float categoricalCharacterScore(final CategoricalCharacter character, final List<Taxon> remainingTaxa) {
        int cpt = 0;
        float score = 0;
        boolean isAlwaysDescribed = true;

        for (int i = 0; i < remainingTaxa.size() - 1; i++) {
            for (int j = i + 1; j < remainingTaxa.size(); j++) {
                if (dataset.getCodedDescription(remainingTaxa.get(i)) != null
                        && dataset.getCodedDescription(remainingTaxa.get(j)) != null
                        && dataset.isApplicable(remainingTaxa.get(i), character)
                        && dataset.isApplicable(remainingTaxa.get(j), character)) {

                    final boolean desc1Unknown = dataset.getCodedDescription(remainingTaxa.get(i))
                            .isUnknownDescription(character);
                    final boolean desc2Unknown = dataset.getCodedDescription(remainingTaxa.get(j))
                            .isUnknownDescription(character);

                    final List<State> statesList1 = dataset.getCodedDescription(
                            remainingTaxa.get(i)).getCategoricalCharacterDescription(character);
                    final List<State> statesList2 = dataset.getCodedDescription(
                            remainingTaxa.get(j)).getCategoricalCharacterDescription(character);

                    // if at least one description is empty for the current character
                    if ((!desc1Unknown && statesList1.isEmpty())
                            || (!desc2Unknown && statesList2.isEmpty())) {
                        isAlwaysDescribed = false;
                    }

                    // if one description is unknown and the other have 0 state checked
                    if ((desc1Unknown && !desc2Unknown && statesList2.isEmpty())
                            || (desc2Unknown && !desc1Unknown && statesList1.isEmpty())) {
                        score++;
                    } else if (!desc1Unknown && !desc2Unknown) {

                        // nb of common states which are absent
                        float commonAbsent = 0;
                        // nb of common states which are present
                        float commonPresent = 0;
                        float other = 0;

                        // search common state
                        for (int k = 0; k < character.getStates().size(); k++) {
                            final State state = character.getStates().get(k);

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

        if (cpt >= 1) {
            score = score / cpt;
        }

        // increasing artificially the score of character containing only described taxa
        if (isAlwaysDescribed && score > 0) {
            score = score + (float) 2.0;
        }

        // fewStatesCharacterFirst option handling
        if (config.isFewStatesCharacterFirst() && score > 0 && character.getStates().

                size()

                >= 2) {
            // increasing artificially score of character with few states
            final float coeff = (float) 1
                    - ((float) character.getStates().size() / (float) maxNbStatesPerCharacter);
            score = score + coeff;
        }

        return score;
    }

    private float quantitativeCharacterScore(
            final QuantitativeCharacter character,
            final List<Taxon> remainingTaxa,
            final List<ICharacter> alreadyUsedCharacter
    ) throws Exception {
        int cpt = 0;
        float score = 0;
        boolean isAlwaysDescribed = true;

        final List<QuantitativeMeasure> QuantitativeIntervals = splitQuantitativeCharacter(character, remainingTaxa);

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

                    final boolean desc1Unknown = dataset.getCodedDescription(remainingTaxa.get(i))
                            .isUnknownDescription(character);
                    final boolean desc2Unknown = dataset.getCodedDescription(remainingTaxa.get(j))
                            .isUnknownDescription(character);
                    final QuantitativeMeasure quantitativeMeasure1 = dataset
                            .getCodedDescription(remainingTaxa.get(i))
                            .getQuantitativeCharacterDescription(character);
                    final QuantitativeMeasure quantitativeMeasure2 = dataset
                            .getCodedDescription(remainingTaxa.get(j))
                            .getQuantitativeCharacterDescription(character);

                    // if at least one description is empty for the current character
                    if ((!desc1Unknown && quantitativeMeasure1.isNotSpecified())
                            || (!desc2Unknown && quantitativeMeasure2.isNotSpecified())) {
                        isAlwaysDescribed = false;
                    }

                    // if one description is unknown and the other have no measure
                    if ((desc1Unknown && !desc2Unknown && quantitativeMeasure2.isNotSpecified())
                            || (desc2Unknown && !desc1Unknown && quantitativeMeasure1.isNotSpecified())
                    ) {
                        score++;
                        // search common shared values
                    } else if (!desc1Unknown && !desc2Unknown) {

                        // if a taxon is described and the other is not, it means that this taxa can be
                        // discriminated
                        if ((quantitativeMeasure1.isNotSpecified() && !quantitativeMeasure2
                                .isNotSpecified())
                                || (quantitativeMeasure2.isNotSpecified() && !quantitativeMeasure1
                                .isNotSpecified())) {
                            score++;
                        } else {

                            // search common state
                            for (final QuantitativeMeasure quantitativeMeasure : QuantitativeIntervals) {
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
            final float coeff = (float) 1 - ((float) 2 / (float) maxNbStatesPerCharacter);
            score = score + coeff;
        }
        return score;
    }

    public static float calculateCommonPercentage(final double min1, final double max1, final double min2, final double max2) {
        final double minLowerTmp;
        final double maxUpperTmp;
        final double minUpperTmp;
        final double maxLowerTmp;
        float res;

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

        res = Double.valueOf((maxLowerTmp - minUpperTmp) / (maxUpperTmp - minLowerTmp)).floatValue();

        if (res < 0) {
            res = 0;
        }
        return res;
    }

    private int calculateMaxNbStatesPerCharacter() {
        int max = 2;
        for (final ICharacter ic : dataset.getCharacters()) {
            if (ic instanceof CategoricalCharacter && ((CategoricalCharacter) ic).getStates() != null
                    && max < ((CategoricalCharacter) ic).getStates().size()) {
                max = ((CategoricalCharacter) ic).getStates().size();
            }
        }
        return max;
    }

    private float applyScoreMethod(final float commonPresent, final float commonAbsent, final float other) {

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
            } catch (final ArithmeticException a) {
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

    public SingleAccessKeyTree getSingleAccessKeyTree() {
        return singleAccessKeyTree;
    }

    public DataSet getDataSet() {
        return dataset;
    }

    public int getMaxNumStatesPerCharacter() {
        return maxNbStatesPerCharacter;
    }

    public void setMaxNumStatesPerCharacter(final int maxNumStatesPerCharacter) {
        this.maxNbStatesPerCharacter = maxNumStatesPerCharacter;
    }

}
