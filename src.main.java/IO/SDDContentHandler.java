package IO;

import java.util.ArrayList;
import java.util.List;

import model.CategoricalCharacter;
import model.CodedDescription;
import model.DataSet;
import model.ICharacter;
import model.QuantitativeCharacter;
import model.QuantitativeMeasure;
import model.State;
import model.Taxon;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import utils.Utils;

/**
 * This class extend ContentHandler to be able to treat each SDD tag and extract
 * data
 * 
 * @author Florian Causse
 * @created 18-avr.-2011
 * 
 */
public class SDDContentHandler implements ContentHandler {

	private Locator locator = null;

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

	// to test if the Label is the DataSet Label
	private boolean isDataSetLabel = true;
	// to parse only the first dataset
	private boolean isFirstDataset = true;
	// to parse only the first dataset
	private boolean isFirstCharacterTree = true;
	// buffer to collect text value
	private StringBuffer buffer = null;
	// kwnoledge base
	private DataSet dataset = null;
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
	List<State> currentInapplicableState = null;
	List<State> currentOnlyApplicableState = null;

	/**
	 * Constructor by default
	 */
	public SDDContentHandler() {
		super();
		this.dataset = new DataSet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String nameSpaceURI, String localName,
			String rawName, Attributes attributs) throws SAXException {

		if (isFirstDataset) {

			// if the label is not the dataSet Label
			if (inDataset && !localName.equals("Representation")
					&& !localName.equals("Label")) {
				isDataSetLabel = false;
			}

			// <Dataset>
			if (localName.equals("Dataset")) {
				inDataset = true;
			}

			// <Representation> in <Dataset>
			else if (localName.equals("Representation") && inDataset) {
				inRepresentation = true;
			}

			// <Label> in <Representation> in <Dataset>
			else if (localName.equals("Label") && inRepresentation && inDataset) {
				buffer = new StringBuffer();
			}

			// <ConceptStates>
			else if (localName.equals("ConceptStates")) {
				inConceptStates = true;

			}

			// <StateDefinition> in <ConceptStates>
			else if (localName.equals("StateDefinition") && inConceptStates) {

			}

			// <Characters>
			else if (localName.equals("Characters")) {
				inCharacters = true;
			}

			// <CategoricalCharacter> in <Characters>
			else if (localName.equals("CategoricalCharacter") && inCharacters) {
				inCategoricalCharacter = true;
				currentCategoricalCharacter = new CategoricalCharacter();
				currentCategoricalCharacter.setId(attributs.getValue("id"));
			}

			// <QuantitativeCharacter> in <Characters>
			else if (localName.equals("QuantitativeCharacter") && inCharacters) {
				inQuantitativeCharacter = true;
				currentQuantitativeCharacter = new QuantitativeCharacter();
				currentQuantitativeCharacter.setId(attributs.getValue("id"));
			}

			// <States>
			else if (localName.equals("States")) {
				inStates = true;
			}

			// <StateDefinition> in <States>
			else if (localName.equals("StateDefinition") && inStates) {
				inStateDefinition = true;
				currentState = new State();
				currentState.setId(attributs.getValue("id"));
			}

			// <StateReference> in <States>
			else if (localName.equals("StateReference") && inStates) {
				inStatesReference = true;

			}

			// <ConceptState> in <StateReference>
			else if (localName.equals("ConceptState") && inStatesReference) {

			}

			// <CharacterTree>
			else if (localName.equals("CharacterTree")) {
				if (isFirstCharacterTree) {
					inCharacterTree = true;
				}
			}

			// <Nodes>
			else if (localName.equals("Nodes")) {
				inNodes = true;
			}

			// <CharNode> in <Nodes>
			else if (localName.equals("CharNode") && inNodes) {
				inCharNode = true;
				currentInapplicableState = new ArrayList<State>();
				currentOnlyApplicableState = new ArrayList<State>();
			}

			// <DependencyRules> in <CharNode>
			else if (localName.equals("DependencyRules") && inCharNode) {
				inDependencyRules = true;
			}

			// <InapplicableIf> in <DependencyRules>
			else if (localName.equals("InapplicableIf") && inDependencyRules) {
				inInapplicableIf = true;
			}

			// <OnlyApplicableIf> in <DependencyRules>
			else if (localName.equals("OnlyApplicableIf") && inDependencyRules) {
				inOnlyApplicableIf = true;

			}

			// <State> in <InapplicableIf> in <CharacterTree> && isFirstCharacterTree
			else if (localName.equals("State") && inInapplicableIf && inCharacterTree && isFirstCharacterTree) {
				State state = dataset.getStateById(attributs.getValue("ref"));
				currentInapplicableState.add(state);
			}

			// <State> in <OnlyApplicableIf> in <CharacterTree> && isFirstCharacterTree
			else if (localName.equals("State") && inOnlyApplicableIf && inCharacterTree && isFirstCharacterTree) {
				State state = dataset.getStateById(attributs.getValue("ref"));
				currentOnlyApplicableState.add(state);
			}
			
			// <Character> in <CharNode> in <CharacterTree> && isFirstCharacterTree
			else if (localName.equals("Character") && inCharNode && inCharacterTree && isFirstCharacterTree) {
				currentCharacterNode = dataset.getCharacterById(attributs.getValue("ref"));
			}

			// <CodedDescriptions>
			else if (localName.equals("CodedDescriptions")) {
				inCodedDescriptions = true;
			}

			// <CodedDescription> in <CodedDescriptions>
			else if (localName.equals("CodedDescription")
					&& inCodedDescriptions) {
				inCodedDescription = true;
				currentCodedDescription = new CodedDescription();
				currentCodedDescription.setId(attributs.getValue("id"));
			}

			// <Scope>
			else if (localName.equals("Scope")) {
				inScope = true;
			}

			// <TaxonName> in <Scope>
			else if (localName.equals("TaxonName") && inScope) {

			}

			// <SummaryData>
			else if (localName.equals("SummaryData")) {
				inSummaryData = true;
			}

			// <Categorical> in <SummaryData>
			else if (localName.equals("Categorical") && inSummaryData) {
				inCategorical = true;
				currentCodedDescriptionCharacter = this.dataset
						.getCharacterById(attributs.getValue("ref"));
				currentStatesList = new ArrayList<State>();
			}

			// <State> in <Categorical>
			else if (localName.equals("State") && inCategorical) {
				currentStatesList.add(this.dataset.getStateById(attributs
						.getValue("ref")));
			}

			// <Quantitative> in <SummaryData>
			else if (localName.equals("Quantitative") && inSummaryData) {
				inQuantitative = true;
				currentCodedDescriptionCharacter = this.dataset
						.getCharacterById(attributs.getValue("ref"));
				currentQuantitativeMeasure = new QuantitativeMeasure();
			}

			// <Measure> in <Quantitative>
			else if (localName.equals("Measure") && inQuantitative) {

				if (attributs.getValue("type").equals("Min")) {
					currentQuantitativeMeasure
							.setMin(Utils.convertStringToDouble(attributs
									.getValue("value")));
				} else if (attributs.getValue("type").equals("Max")) {
					currentQuantitativeMeasure
							.setMax(Utils.convertStringToDouble(attributs
									.getValue("value")));
				} else if (attributs.getValue("type").equals("Mean")) {
					currentQuantitativeMeasure
							.setMean(Utils.convertStringToDouble(attributs
									.getValue("value")));
				} else if (attributs.getValue("type").equals("SD")) {
					currentQuantitativeMeasure
							.setSD(Utils.convertStringToDouble(attributs
									.getValue("value")));
				} else if (attributs.getValue("type").equals("UMethLower")) {
					currentQuantitativeMeasure
							.setUMethLower(Utils
									.convertStringToDouble(attributs
											.getValue("value")));
				} else if (attributs.getValue("type").equals("UMethUpper")) {
					currentQuantitativeMeasure
							.setUMethUpper(Utils
									.convertStringToDouble(attributs
											.getValue("value")));
				}
			}

			// <MeasurementUnit>
			else if (localName.equals("MeasurementUnit")) {
				inMeasurementUnit = true;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public void endElement(String nameSpaceURI, String localName, String rawName)
			throws SAXException {

		if (isFirstDataset) {
			// <Dataset>
			if (localName.equals("Dataset")) {
				inDataset = false;
				isFirstDataset = false;
			}

			// <Representation> in <Dataset>
			else if (localName.equals("Representation") && inDataset) {
				inRepresentation = false;
			}

			// <Label> in <Representation> in <Dataset>
			else if (localName.equals("Label") && inRepresentation && inDataset) {
				if (inCodedDescription) {
					currentTaxon = new Taxon();
					currentTaxon.setName(buffer.toString());
				} else if (inCategoricalCharacter && inStateDefinition) {
					currentState.setName(buffer.toString());
				} else if (inCategoricalCharacter) {
					currentCategoricalCharacter.setName(buffer.toString());
				} else if (inQuantitativeCharacter) {
					currentQuantitativeCharacter.setName(buffer.toString());
				} else if (isDataSetLabel) {
					this.dataset.setLabel(buffer.toString());
					isDataSetLabel = false;
				}
			}

			// <ConceptStates>
			else if (localName.equals("ConceptStates")) {
				inConceptStates = false;

			}

			// <StateDefinition> in <ConceptStates>
			else if (localName.equals("StateDefinition") && inConceptStates) {

			}

			// <Characters>
			else if (localName.equals("Characters")) {
				inCharacters = false;
			}

			// <CategoricalCharacter>
			else if (localName.equals("CategoricalCharacter") && inCharacters) {
				inCategoricalCharacter = false;
				this.dataset.getCharacters().add(currentCategoricalCharacter);
				currentCategoricalCharacter = null;
			}

			// <QuantitativeCharacter>
			else if (localName.equals("QuantitativeCharacter") && inCharacters) {
				inQuantitativeCharacter = false;
				this.dataset.getCharacters().add(currentQuantitativeCharacter);
				currentQuantitativeCharacter = null;
			}

			// <States>
			else if (localName.equals("States")) {
				inStates = false;

			}

			// <StateDefinition> in <States>
			else if (localName.equals("StateDefinition") && inStates) {
				inStateDefinition = false;
				currentCategoricalCharacter.getStates().add(currentState);
				currentState = null;
			}

			// <StateReference> in <States>
			else if (localName.equals("StateReference") && inStates) {
				inStatesReference = false;

			}

			// <ConceptState> in <StateReference>
			else if (localName.equals("ConceptState") && inStatesReference) {

			}

			// <CharacterTree>
			else if (localName.equals("CharacterTree")) {
				isFirstCharacterTree = false;
				inCharacterTree = false;
			}

			// <Nodes>
			else if (localName.equals("Nodes")) {
				inNodes = false;
			}

			// <CharNode> in <Nodes>
			else if (localName.equals("CharNode") && inNodes) {
				inCharNode = false;
				if(currentInapplicableState.size() > 0){
					currentCharacterNode.setParentCharacter(dataset.getCharacterByState(currentInapplicableState.get(0)));
					currentCharacterNode.getInapplicableStates().addAll(currentInapplicableState);
				}else if(currentOnlyApplicableState.size() > 0){
					ICharacter character = dataset.getCharacterByState(currentOnlyApplicableState.get(0));
					if(character != null && character instanceof CategoricalCharacter){
						currentCharacterNode.setParentCharacter(character);
						List<State> tempList = new ArrayList<State>(((CategoricalCharacter)character).getStates());
						tempList.removeAll(currentOnlyApplicableState);
						currentCharacterNode.getInapplicableStates().addAll(tempList);
					}
				}
				currentInapplicableState = null;
				currentOnlyApplicableState = null;
				currentCharacterNode = null;
			}

			// <DependencyRules> in <CharNode>
			else if (localName.equals("DependencyRules") && inCharNode) {
				inDependencyRules = false;
			}

			// <InapplicableIf> in <DependencyRules>
			else if (localName.equals("InapplicableIf") && inDependencyRules) {
				inInapplicableIf = false;
			}

			// <OnlyApplicableIf> in <DependencyRules>
			else if (localName.equals("OnlyApplicableIf") && inDependencyRules) {
				inOnlyApplicableIf = false;

			}

			// <State> in <InapplicableIf> in <CharacterTree> && isFirstCharacterTree
			else if (localName.equals("State") && inInapplicableIf && inCharacterTree && isFirstCharacterTree) {
			
			}

			// <State> in <OnlyApplicableIf> in <CharacterTree> && isFirstCharacterTree
			else if (localName.equals("State") && inOnlyApplicableIf && inCharacterTree && isFirstCharacterTree) {
				
			}
			
			// <Character> in <CharNode> in <CharacterTree> && isFirstCharacterTree
			else if (localName.equals("Character") && inCharNode && inCharacterTree && isFirstCharacterTree) {
				
			}

			// <CodedDescriptions>
			else if (localName.equals("CodedDescriptions")) {
				inCodedDescriptions = false;
			}

			// <CodedDescription> in <CodedDescriptions>
			else if (localName.equals("CodedDescription")
					&& inCodedDescriptions) {
				inCodedDescription = false;
				this.dataset.addCodedDescription(currentTaxon,
						currentCodedDescription);
			}

			// <Scope>
			else if (localName.equals("Scope")) {
				inScope = false;
			}

			// <TaxonName> in <Scope>
			else if (localName.equals("TaxonName") && inScope) {

			}

			// <SummaryData>
			else if (localName.equals("SummaryData")) {
				inSummaryData = false;
			}

			// <Categorical> in <SummaryData>
			else if (localName.equals("Categorical") && inSummaryData) {
				inCategorical = false;
				currentCodedDescription.addCharacterDescription(
						currentCodedDescriptionCharacter, currentStatesList);
				currentCodedDescriptionCharacter = null;
				currentStatesList = null;
			}

			// <Quantitative> in <SummaryData>
			else if (localName.equals("Quantitative") && inSummaryData) {
				inQuantitative = false;
				currentCodedDescription.addCharacterDescription(
						currentCodedDescriptionCharacter,
						currentQuantitativeMeasure);
				currentCodedDescriptionCharacter = null;
				currentQuantitativeMeasure = null;
			}

			// <Measure> in <Quantitative>
			else if (localName.equals("Measure") && inQuantitative) {

			}

			// <MeasurementUnit>
			else if (localName.equals("MeasurementUnit")) {
				inMeasurementUnit = false;

			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	@Override
	public void endPrefixMapping(String prefix) throws SAXException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
	 */
	@Override
	public void skippedEntity(String name) throws SAXException {

	}

	
	/**
	 * get the current dataset
	 * @return DataSet, the current dataset
	 */
	public DataSet getDataset() {
		return dataset;
	}

	/**
	 * set the current dataset
	 * @param DataSet, the current dataset
	 */
	public void setDataset(DataSet dataset) {
		this.dataset = dataset;
	}
}
