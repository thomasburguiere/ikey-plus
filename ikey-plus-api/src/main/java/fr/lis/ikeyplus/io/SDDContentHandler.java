package fr.lis.ikeyplus.IO;

import fr.lis.ikeyplus.model.CategoricalCharacter;
import fr.lis.ikeyplus.model.CodedDescription;
import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.model.ICharacter;
import fr.lis.ikeyplus.model.QuantitativeCharacter;
import fr.lis.ikeyplus.model.QuantitativeMeasure;
import fr.lis.ikeyplus.model.State;
import fr.lis.ikeyplus.model.Taxon;
import fr.lis.ikeyplus.utils.IkeyConfig;
import fr.lis.ikeyplus.utils.IkeyUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class extends the ContentHandler class, in order to be able to treat each SDD tag and extract data
 *
 * @author Florian Causse
 * @created 18-04-2011
 */
public class SDDContentHandler implements ContentHandler {

    // flag to know if we are in or out a tag
    private boolean inDataset = false;
    private boolean inRepresentation = false;
    private boolean inConceptStates = false;
    private boolean inStates = false;
    private boolean inStateDefinition = false;
    private boolean inStatesReference = false;
    private boolean inNodes = false;
    private boolean inCharNode = false;
    private boolean inDependencyRules = false;
    private boolean inInapplicableIf = false;
    private boolean inOnlyApplicableIf = false;
    private boolean inCodedDescriptions = false;
    private boolean inCodedDescription = false;
    private boolean inScope = false;
    private boolean inSummaryData = false;
    private boolean inCategorical = false;
    private boolean inQuantitative = false;
    private boolean inMeasurementUnit = false;
    private boolean inCategoricalCharacter = false;
    private boolean inQuantitativeCharacter = false;
    private boolean inCharacterTree = false;
    private boolean inCharacters = false;
    private boolean inRatings = false;
    private boolean inMediaObject = false;
    private boolean dataUnavailableFlag = false;

    // to test if the MediaObject is of type image
    private boolean isImageType = false;
    // to test if the Label is the DataSet Label
    private boolean isDataSetLabel = true;
    // to parse only the first dataSet
    private boolean isFirstDataset = true;
    // to parse only the first dataSet
    private boolean isFirstCharacterTree = true;
    // buffer to collect text value
    private StringBuffer buffer = null;
    // kwnoledge base
    private DataSet dataSet = null;
    // IkeyConfig object
    private IkeyConfig config = null;
    // current quantitative character
    private CategoricalCharacter currentCategoricalCharacter = null;
    // current quantitative character
    private QuantitativeCharacter currentQuantitativeCharacter = null;
    // current state
    private State currentState = null;
    // current codedDescription
    private CodedDescription currentCodedDescription = null;
    // current taxon
    private Taxon currentTaxon = null;

    // current CodedDescription Character
    private ICharacter currentCodedDescriptionCharacter = null;
    // current States list for CodedDescription
    private List<State> currentStatesList = null;
    // current QuantitativeMeasure for CodedDescription
    private QuantitativeMeasure currentQuantitativeMeasure = null;
    // current CharacterNode
    private ICharacter currentCharacterNode = null;
    // list of applicable and inapplicable states
    private List<State> currentInapplicableState = null;
    private List<State> currentOnlyApplicableState = null;
    // id of current mediaObject
    private String mediaObjectId = null;
    // id of current mediaObject
    private Map<ICharacter, Integer> ratingsCounter = null;

    /**
     * Default constructor
     */
    public SDDContentHandler(IkeyConfig config) {
        super();
        this.dataSet = new DataSet();
        this.config = config;
        this.ratingsCounter = new HashMap<ICharacter, Integer>();
    }

    /* (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
     * org.xml.sax.Attributes) */
    @Override
    public void startElement(String nameSpaceURI, String localName, String rawName, Attributes attributes)
            throws SAXException {

        if (isFirstDataset) {

            // if the label is not the dataSet Label
            if (inDataset && !"Representation".equals(localName) && !"Label".equals(localName)) {
                isDataSetLabel = false;
            }

            // <Dataset>
            if ("Dataset".equals(localName)) {
                inDataset = true;
            }

            // <Representation> in <Dataset>
            else if ("Representation".equals(localName) && inDataset) {
                inRepresentation = true;
            }

            // <Label> in <Dataset>
            else if ("Label".equals(localName) && inDataset) {
                buffer = new StringBuffer();
            }

            // <ConceptStates>
            else if ("ConceptStates".equals(localName)) {
                inConceptStates = true;

            }

            // <StateDefinition> in <ConceptStates>
//            else if ("StateDefinition".equals(localName) && inConceptStates) {
//
//            }

            // <Characters>
            else if ("Characters".equals(localName)) {
                inCharacters = true;
            }

            // <CategoricalCharacter> in <Characters>
            else if ("CategoricalCharacter".equals(localName) && inCharacters) {
                inCategoricalCharacter = true;
                currentCategoricalCharacter = new CategoricalCharacter();
                currentCategoricalCharacter.setId(attributes.getValue("id"));
            }

            // <MediaObject> in <Representation> in <CategoricalCharacter>
            else if ("MediaObject".equals(localName) && inRepresentation && inCategoricalCharacter
                    && !inStates) {
                if (attributes.getValue("ref") != null && currentCategoricalCharacter != null) {
                    currentCategoricalCharacter.getMediaObjectKeys().add(attributes.getValue("ref"));
                }
            }

            // <QuantitativeCharacter> in <Characters>
            else if ("QuantitativeCharacter".equals(localName) && inCharacters) {
                inQuantitativeCharacter = true;
                currentQuantitativeCharacter = new QuantitativeCharacter();
                currentQuantitativeCharacter.setId(attributes.getValue("id"));
            }

            // <MediaObject> in <Representation> in <QuantitativeCharacter>
            else if ("MediaObject".equals(localName) && inRepresentation && inQuantitativeCharacter
                    && !inStates) {
                if (attributes.getValue("ref") != null && currentQuantitativeCharacter != null) {
                    currentQuantitativeCharacter.getMediaObjectKeys().add(attributes.getValue("ref"));
                }
            }

            // <States>
            else if ("States".equals(localName)) {
                inStates = true;
            }

            // <StateDefinition> in <States>
            else if ("StateDefinition".equals(localName) && inStates) {
                inStateDefinition = true;
                currentState = new State();
                currentState.setId(attributes.getValue("id"));
            }

            // <MediaObject> in <Representation> in <StateDefinition> in <States>
            else if ("MediaObject".equals(localName) && inStateDefinition && inRepresentation && inStates) {
                if (attributes.getValue("ref") != null && currentState != null) {
                    currentState.getMediaObjectKeys().add(attributes.getValue("ref"));
                }
            }

            // <StateReference> in <States>
            else if ("StateReference".equals(localName) && inStates) {
                inStatesReference = true;

            }

            // <ConceptState> in <StateReference>
            //else if ("ConceptState".equals(localName) && inStatesReference) {

            //}

            // <CharacterTree>
            else if ("CharacterTree".equals(localName)) {
                if (isFirstCharacterTree) {
                    inCharacterTree = true;
                }
            }

            // <Nodes>
            else if ("Nodes".equals(localName)) {
                inNodes = true;
            }

            // <CharNode> in <Nodes>
            else if ("CharNode".equals(localName) && inNodes) {
                inCharNode = true;
                currentInapplicableState = new ArrayList<State>();
                currentOnlyApplicableState = new ArrayList<State>();
            }

            // <DependencyRules> in <CharNode>
            else if ("DependencyRules".equals(localName) && inCharNode) {
                inDependencyRules = true;
            }

            // <InapplicableIf> in <DependencyRules>
            else if ("InapplicableIf".equals(localName) && inDependencyRules) {
                inInapplicableIf = true;
            }

            // <OnlyApplicableIf> in <DependencyRules>
            else if ("OnlyApplicableIf".equals(localName) && inDependencyRules) {
                inOnlyApplicableIf = true;

            }

            // <State> in <InapplicableIf> in <CharacterTree> &&
            // isFirstCharacterTree
            else if ("State".equals(localName) && inInapplicableIf && inCharacterTree && isFirstCharacterTree) {
                State state = dataSet.getStateById(attributes.getValue("ref"));
                currentInapplicableState.add(state);
            }

            // <State> in <OnlyApplicableIf> in <CharacterTree> &&
            // isFirstCharacterTree
            else if ("State".equals(localName) && inOnlyApplicableIf && inCharacterTree
                    && isFirstCharacterTree) {
                State state = dataSet.getStateById(attributes.getValue("ref"));
                currentOnlyApplicableState.add(state);
            }

            // <Character> in <CharNode> in <CharacterTree> &&
            // isFirstCharacterTree
            else if ("Character".equals(localName) && inCharNode && inCharacterTree && isFirstCharacterTree) {
                currentCharacterNode = dataSet.getCharacterById(attributes.getValue("ref"));
            }

            // <CodedDescriptions>
            else if ("CodedDescriptions".equals(localName)) {
                inCodedDescriptions = true;
            }

            // <CodedDescription> in <CodedDescriptions>
            else if ("CodedDescription".equals(localName) && inCodedDescriptions) {
                inCodedDescription = true;
                currentCodedDescription = new CodedDescription();
                currentCodedDescription.setId(attributes.getValue("id"));
                currentTaxon = new Taxon();
            }

            // <MediaObject> in <CodedDescription> in <CodedDescriptions>
            else if ("MediaObject".equals(localName) && inCodedDescription && inCodedDescriptions) {
                if (attributes.getValue("ref") != null && currentTaxon != null) {
                    currentTaxon.getMediaObjectKeys().add(attributes.getValue("ref"));
                }
            }

            // <Scope>
            else if ("Scope".equals(localName)) {
                inScope = true;
            }

            // <TaxonName> in <Scope>
            //else if ("TaxonName".equals(localName) && inScope) {

            //}

            // <SummaryData>
            else if ("SummaryData".equals(localName)) {
                inSummaryData = true;
            }

            // <Categorical> in <SummaryData>
            else if ("Categorical".equals(localName) && inSummaryData) {
                inCategorical = true;
                currentCodedDescriptionCharacter = this.dataSet.getCharacterById(attributes.getValue("ref"));
                currentStatesList = new ArrayList<State>();
            }

            // <State> in <Categorical>
            else if ("State".equals(localName) && inCategorical) {
                if (currentStatesList != null)
                    currentStatesList.add(this.dataSet.getStateById(attributes.getValue("ref")));
            }

            // <Status> in <Categorical>
            else if ("Status".equals(localName) && inCategorical) {
                if (attributes.getValue("code") != null
                        && attributes.getValue("code").equals("DataUnavailable")) {
                    dataUnavailableFlag = true;
                }
            }

            // <Quantitative> in <SummaryData>
            else if ("Quantitative".equals(localName) && inSummaryData) {
                inQuantitative = true;
                currentCodedDescriptionCharacter = this.dataSet.getCharacterById(attributes.getValue("ref"));
                currentQuantitativeMeasure = new QuantitativeMeasure();
            }

            // <Measure> in <Quantitative>
            else if ("Measure".equals(localName) && inQuantitative) {
                if (currentQuantitativeMeasure != null) {
                    if (attributes.getValue("type").equals("Min")) {
                        currentQuantitativeMeasure.setMin(Double.valueOf(attributes
                                .getValue("value")));
                    } else if (attributes.getValue("type").equals("Max")) {
                        currentQuantitativeMeasure.setMax(Double.valueOf(attributes
                                .getValue("value")));
                    } else if (attributes.getValue("type").equals("Mean")) {
                        currentQuantitativeMeasure.setMean(Double.valueOf(attributes
                                .getValue("value")));
                    } else if (attributes.getValue("type").equals("SD")) {
                        currentQuantitativeMeasure.setSD(Double.valueOf(attributes
                                .getValue("value")));
                    } else if (attributes.getValue("type").equals("UMethLower")) {
                        currentQuantitativeMeasure.setUMethLower(Double.valueOf(attributes
                                .getValue("value")));
                    } else if (attributes.getValue("type").equals("UMethUpper")) {
                        currentQuantitativeMeasure.setUMethUpper(Double.valueOf(attributes
                                .getValue("value")));
                    }
                }
            }

            // <Status> in <Quantitative>
            else if ("Status".equals(localName) && inCategorical) {
                if (attributes.getValue("code") != null
                        && attributes.getValue("code").equals("DataUnavailable")) {
                    dataUnavailableFlag = true;
                }
            }

            // <MeasurementUnit>
            else if ("MeasurementUnit".equals(localName)) {
                inMeasurementUnit = true;
            }

            // <Ratings>
            else if ("Ratings".equals(localName)) {
                inRatings = true;
            }

            // <Rating> in <Ratings>
            else if ("Rating".equals(localName) && inRatings) {
                if (attributes.getValue("context").equals(config.getWeightContext().toString())) {
                    int currentRating = IkeyConfig.WeightValue.fromString(attributes.getValue("rating")).getIntWeight();
                    if (currentCodedDescriptionCharacter != null) {
                        if (this.ratingsCounter.get(currentCodedDescriptionCharacter) == null) {
                            this.ratingsCounter.put(currentCodedDescriptionCharacter, 0);
                            currentCodedDescriptionCharacter.setWeight(0);
                        }

                        currentCodedDescriptionCharacter.setWeight(currentCodedDescriptionCharacter
                                .getWeight() + currentRating);
                        this.ratingsCounter.put(currentCodedDescriptionCharacter,
                                this.ratingsCounter.get(currentCodedDescriptionCharacter) + 1);

                        if (config.getWeightType() == IkeyConfig.WeightType.CONTEXTUAL) {
                            currentCodedDescription.addCharacterWeight(currentCodedDescriptionCharacter,
                                    currentRating);
                        }

                    }
                }
            }

            // <MediaObject>
            else if ("MediaObject".equals(localName)) {
                inMediaObject = true;
                if (attributes.getValue("id") != null) {
                    mediaObjectId = attributes.getValue("id");
                } else {
                    mediaObjectId = null;
                }
            }

            // <Type> in <MediaObject>
            else if ("Type".equals(localName) && inMediaObject) {
                buffer = new StringBuffer();
            }

            // <Source> in <MediaObject>
            else if ("Source".equals(localName) && inMediaObject && mediaObjectId != null && isImageType) {
                if (attributes.getValue("href") != null) {
                    dataSet.getMediaObjects().put(mediaObjectId, attributes.getValue("href"));
                }
            }
        }
    }

    /* (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String) */
    @Override
    public void endElement(String nameSpaceURI, String localName, String rawName) throws SAXException {

        if (isFirstDataset) {
            // <Dataset>
            if ("Dataset".equals(localName)) {
                inDataset = false;
                isFirstDataset = false;

                // null description will be considered as unknown data. Empty states list or
                // QuantitativeMeasure
                // will be considered as not specified (not described).
                for (Taxon taxon : this.dataSet.getCodedDescriptions().keySet()) {
                    for (ICharacter character : this.dataSet.getCharacters()) {
                        if (this.dataSet.getCodedDescriptions().get(taxon).getCharacterDescription(character) == null) {
                            if (character.isSupportsCategoricalData()) {
                                this.dataSet.getCodedDescriptions().get(taxon)
                                        .addCharacterDescription(character, new ArrayList<State>());
                            } else {
                                this.dataSet.getCodedDescriptions().get(taxon)
                                        .addCharacterDescription(character, new QuantitativeMeasure());
                            }
                            // put to null Unknown data
                        } else if (this.dataSet.getCodedDescriptions().get(taxon)
                                .getCharacterDescription(character) instanceof String
                                && ((String) this.dataSet.getCodedDescriptions().get(taxon)
                                .getCharacterDescription(character)).equals(IkeyUtils.UNKNOWN_DATA)) {
                            this.dataSet.getCodedDescriptions().get(taxon)
                                    .addCharacterDescription(character, null);
                        }
                    }
                }

                // update (average) the weight for all characters if the parameter
                // useContextualCharacterWeights is not enabled
                for (ICharacter character : this.dataSet.getCharacters()) {
                    if (this.ratingsCounter.get(character) != null) {
                        character.setWeight((float) (character.getWeight())
                                / (float) (this.ratingsCounter.get(character)));
                    }
                }

            }

            // <Representation> in <Dataset>
            else if ("Representation".equals(localName) && inDataset) {
                inRepresentation = false;
            }

            // <Label> in <Representation> in <Dataset>
            else if ("Label".equals(localName) && inRepresentation && inDataset) {
                if (inCodedDescription) {
                    currentTaxon.setName(buffer.toString());
                } else if (inCategoricalCharacter && inStateDefinition) {
                    currentState.setName(buffer.toString());
                } else if (inCategoricalCharacter) {
                    currentCategoricalCharacter.setName(buffer.toString());
                } else if (inQuantitativeCharacter) {
                    currentQuantitativeCharacter.setName(buffer.toString());
                } else if (isDataSetLabel) {
                    this.dataSet.setLabel(buffer.toString());
                    isDataSetLabel = false;
                }
            }

            // <Label> in <MeasurementUnit> in <Dataset>
            else if ("Label".equals(localName) && inMeasurementUnit && inDataset) {
                if (currentQuantitativeCharacter != null) {
                    currentQuantitativeCharacter.setMeasurementUnit(buffer.toString());
                }
            }

            // <ConceptStates>
            else if ("ConceptStates".equals(localName)) {
                inConceptStates = false;

            }

            // <StateDefinition> in <ConceptStates>
            //else if ("StateDefinition".equals(localName) && inConceptStates) {

            //}

            // <Characters>
            else if ("Characters".equals(localName)) {
                inCharacters = false;
            }

            // <CategoricalCharacter>
            else if ("CategoricalCharacter".equals(localName) && inCharacters) {
                inCategoricalCharacter = false;
                this.dataSet.getCharacters().add(currentCategoricalCharacter);
                currentCategoricalCharacter = null;
            }

            // <QuantitativeCharacter>
            else if ("QuantitativeCharacter".equals(localName) && inCharacters) {
                inQuantitativeCharacter = false;
                this.dataSet.getCharacters().add(currentQuantitativeCharacter);
                currentQuantitativeCharacter = null;
            }

            // <States>
            else if ("States".equals(localName)) {
                inStates = false;

            }

            // <StateDefinition> in <States>
            else if ("StateDefinition".equals(localName) && inStates) {
                inStateDefinition = false;
                currentCategoricalCharacter.getStates().add(currentState);
                currentState = null;
            }

            // <StateReference> in <States>
            else if ("StateReference".equals(localName) && inStates) {
                inStatesReference = false;

            }

            // <ConceptState> in <StateReference>
            //else if ("ConceptState".equals(localName) && inStatesReference) {

            //}

            // <CharacterTree>
            else if ("CharacterTree".equals(localName)) {
                isFirstCharacterTree = false;
                inCharacterTree = false;
            }

            // <Nodes>
            else if ("Nodes".equals(localName)) {
                inNodes = false;
            }

            // <CharNode> in <Nodes>
            else if ("CharNode".equals(localName) && inNodes) {
                inCharNode = false;
                if (currentInapplicableState.size() > 0) {
                    currentCharacterNode.setParentCharacter(dataSet
                            .getCharacterByState(currentInapplicableState.get(0)));
                    currentCharacterNode.getInapplicableStates().addAll(currentInapplicableState);
                } else if (currentOnlyApplicableState.size() > 0) {
                    ICharacter character = dataSet.getCharacterByState(currentOnlyApplicableState.get(0));
                    if (character != null && character instanceof CategoricalCharacter) {
                        currentCharacterNode.setParentCharacter(character);
                        List<State> tempList = new ArrayList<State>(
                                ((CategoricalCharacter) character).getStates());
                        tempList.removeAll(currentOnlyApplicableState);
                        currentCharacterNode.getInapplicableStates().addAll(tempList);
                    }
                }
                currentInapplicableState = null;
                currentOnlyApplicableState = null;
                currentCharacterNode = null;
            }

            // <DependencyRules> in <CharNode>
            else if ("DependencyRules".equals(localName) && inCharNode) {
                inDependencyRules = false;
            }

            // <InapplicableIf> in <DependencyRules>
            else if ("InapplicableIf".equals(localName) && inDependencyRules) {
                inInapplicableIf = false;
            }

            // <OnlyApplicableIf> in <DependencyRules>
            else if ("OnlyApplicableIf".equals(localName) && inDependencyRules) {
                inOnlyApplicableIf = false;

            }

            // <State> in <InapplicableIf> in <CharacterTree> &&
            // isFirstCharacterTree
            //else if ("State".equals(localName) && inInapplicableIf && inCharacterTree && isFirstCharacterTree) {

            //}

            // <State> in <OnlyApplicableIf> in <CharacterTree> &&
            // isFirstCharacterTree
            //else if ("State".equals(localName) && inOnlyApplicableIf && inCharacterTree       && isFirstCharacterTree) {

            //}

            // <Character> in <CharNode> in <CharacterTree> &&
            // isFirstCharacterTree
            // else if ("Character".equals(localName) && inCharNode && inCharacterTree && isFirstCharacterTree) {

            //}

            // <CodedDescriptions>
            else if ("CodedDescriptions".equals(localName)) {
                inCodedDescriptions = false;
            }

            // <CodedDescription> in <CodedDescriptions>
            else if ("CodedDescription".equals(localName) && inCodedDescriptions) {
                inCodedDescription = false;
                this.dataSet.addCodedDescription(currentTaxon, currentCodedDescription);
                currentTaxon = null;
            }

            // <Scope>
            else if ("Scope".equals(localName)) {
                inScope = false;
            }

            // <TaxonName> in <Scope>
            //else if ("TaxonName".equals(localName) && inScope) {

            // }

            // <SummaryData>
            else if ("SummaryData".equals(localName)) {
                inSummaryData = false;
            }

            // <Categorical> in <SummaryData>
            else if ("Categorical".equals(localName) && inSummaryData) {
                inCategorical = false;
                if (dataUnavailableFlag) {
                    currentCodedDescription.addCharacterDescription(currentCodedDescriptionCharacter,
                            IkeyUtils.UNKNOWN_DATA);
                } else {
                    currentCodedDescription.addCharacterDescription(currentCodedDescriptionCharacter,
                            currentStatesList);
                }
                currentCodedDescriptionCharacter = null;
                currentStatesList = null;
                dataUnavailableFlag = false;
            }

            // <Quantitative> in <SummaryData>
            else if ("Quantitative".equals(localName) && inSummaryData) {
                inQuantitative = false;
                if (dataUnavailableFlag) {
                    currentCodedDescription.addCharacterDescription(currentCodedDescriptionCharacter,
                            IkeyUtils.UNKNOWN_DATA);
                } else {
                    currentCodedDescription.addCharacterDescription(currentCodedDescriptionCharacter,
                            currentQuantitativeMeasure);
                }
                currentCodedDescriptionCharacter = null;
                currentQuantitativeMeasure = null;
                dataUnavailableFlag = false;
            }

            // <Measure> in <Quantitative>
            //else if ("Measure".equals(localName) && inQuantitative) {

            //}

            // <MeasurementUnit>
            else if ("MeasurementUnit".equals(localName)) {
                inMeasurementUnit = false;

            }

            // <Ratings>
            else if ("Ratings".equals(localName)) {
                inRatings = false;
            }

            // <MediaObject>
            else if ("MediaObject".equals(localName)) {
                inMediaObject = false;
                isImageType = false;
                mediaObjectId = null;
            }

            // <Type> in <MediaObject>
            else if ("Type".equals(localName) && inMediaObject && mediaObjectId != null) {
                if (buffer != null && buffer.toString().equalsIgnoreCase("image")) {
                    isImageType = true;
                } else {
                    isImageType = false;
                }
            }
        }
    }

    /* (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#endDocument() */
    @Override
    public void endDocument() throws SAXException {

    }

    /* (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String) */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {

    }

    /* (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator) */
    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;

    }

    /* (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#startDocument() */
    @Override
    public void startDocument() throws SAXException {

    }

    /* (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String) */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {

    }

    /* (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#characters(char[], int, int) */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String data = new String(ch, start, length);

        data = data.replaceAll("\t", " "); // replace all the \t character by
        // space. It corresponds to the xml
        // indentation characters.
        data = data.replaceAll("\n", " ");
        data = data.replaceAll("[\\s]+", " ");
        if (this.buffer != null) {
            this.buffer.append(data);
        }
    }

    /* (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int) */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

    }

    /* (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String) */
    @Override
    public void processingInstruction(String target, String data) throws SAXException {

    }

    /* (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String) */
    @Override
    public void skippedEntity(String name) throws SAXException {

    }

    /**
     * get the current dataSet
     *
     * @return DataSet, the current dataSet
     */
    public DataSet getDataSet() {
        return dataSet;
    }

    /**
     * set the current dataSet
     *
     * @param DataSet , the current dataSet
     */
    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }
}
