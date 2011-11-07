package IO;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import model.CategoricalCharacter;
import model.CodedDescription;
import model.DataSet;
import model.ICharacter;
import model.QuantitativeMeasure;
import model.State;
import model.Taxon;

import org.junit.Test;

import utils.Utils;

/**
 * This class allow to test the SDDSaxParser
 * 
 * @author Florian Causse
 * @created 18-04-2011
 */
public class SDDSaxParserTest {

	public Logger logger = Logger.getAnonymousLogger();

	@Test
	public void testSDDSaxParser() {

		logger.info("testSDDSaxParser");
		long beforeTime = System.currentTimeMillis();

		// creation of Utils object (containing options)
		Utils utils = new Utils();

		SDDSaxParser sddSaxParser = null;

		try {
			// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/Cichorieae-fullSDD.xml";
			// String stringUrl =
			// "http://www.infosyslab.fr/vibrant/project/test/Cichorieae-unknownData-fullSDD.xml";
			// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/phlebotomes-SDD.xml";
			// String stringUrl =
			// "http://www.infosyslab.fr/vibrant/project/test/milichia_revision-sdd.xml";
			// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/testSDD.xml";
			// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/feuillesSDD.xml";
			// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/feuillesImagesURL.xml";
			// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/smallSDD.xml";
			// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/wrongSDD.xml";
			// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/pruningSDD.xml";
			// String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/archaeoSDD.xml";
			String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/varanusSDD_RatingExample.xml";

			// options
			utils.setFewStatesCharacterFirst(false);
			utils.setMergeCharacterStatesIfSameDiscimination(false);
			utils.setPruning(false);
			utils.setVerbosity("how");
			utils.setScoreMethod(Utils.XPER);
			utils.setWeightContext("CostEffectiveness");

			// test if the URL is valid
			URLConnection urlConnection;
			InputStream httpStream;
			try {
				URL fileURL = new URL(stringUrl);
				// open URL (HTTP query)
				urlConnection = fileURL.openConnection();
				// Open data stream
				httpStream = urlConnection.getInputStream();
			} catch (java.net.MalformedURLException e) {
				utils.setErrorMessage(Utils.getBundleConfElement("message.urlError"), e);
				e.printStackTrace();
			} catch (java.io.IOException e) {
				utils.setErrorMessage(Utils.getBundleConfElement("message.urlError"), e);
				e.printStackTrace();
			}
			sddSaxParser = new SDDSaxParser(stringUrl, utils);
		} catch (Throwable t) {
			utils.setErrorMessage(Utils.getBundleConfElement("message.parsingError"), t);
			t.printStackTrace();
		}

		double parseDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;

		DataSet dataset = sddSaxParser.getDataset();
		if (dataset != null) {
			// DISPLAY THE DATASET
			System.out.println("dataSetLabel : " + dataset.getLabel());
			// CHARACTERS
			System.out.println("characters (" + dataset.getCharacters().size() + ") : ");
			for (ICharacter character : dataset.getCharacters()) {
				if (character instanceof CategoricalCharacter) {
					System.out.println("\t" + character.getName() + "   w=" + character.getWeight());
					for (State state : ((CategoricalCharacter) character).getStates()) {
						System.out.println("\t\t" + state.getName());
						for (String key : state.getMediaObjectKeys()) {
							System.out.println("\t" + key);
						}
					}
				} else {
					System.out.println("\t*N*" + character.getName() + "   w=" + character.getWeight());
				}
			}
			// TAXA AND DESCRIPTION
			System.out.println("taxa (" + dataset.getTaxa().size() + ") : ");
			for (Taxon taxon : dataset.getTaxa()) {
				System.out.println("\t" + taxon.getName());
				for (String key : taxon.getMediaObjectKeys()) {
					System.out.println("\t" + key);
				}
				CodedDescription codedDescription = dataset.getCodedDescription(taxon);

				for (ICharacter character : dataset.getCharacters()) {
					Object characterDescription = codedDescription.getCharacterDescription(character);
					if (characterDescription != null) {
						System.out.println("\t\t" + character.getName());
						if (characterDescription instanceof QuantitativeMeasure) {
							System.out.println("\t\t\t"
									+ ((QuantitativeMeasure) characterDescription).toString());
						} else if (characterDescription instanceof ArrayList<?>) {
							for (State state : (List<State>) characterDescription) {
								System.out.println("\t\t\t" + state.getName());
							}
						}
					}
				}
			}
			// CHARACTER TREE BY PARENTS
			System.out.println("characterTree by parent : ");
			for (ICharacter character : dataset.getCharacters()) {
				if (character.getParentCharacter() != null) {
					System.out.println("\tcharacter->" + character.getName());
					System.out.println("\tparent->" + character.getParentCharacter().getName());
					for (State state : character.getInapplicableStates()) {
						System.out.println("\t\t" + state.getName());
					}
				}
			}
			// CHARACTER TREE BY CHILDREN
			System.out.println("characterTree by children : ");
			for (ICharacter character : dataset.getCharacters()) {
				if (character.getParentCharacter() == null) {
					// display all child characters with its inapplicable states
					displayRecursiveChildren("\t", character);
				}
			}

			// MEDIAOBJECTS
			System.out.println("media objects : ");
			for (String key : dataset.getMediaObjects().keySet()) {
				// display the media object URL
				System.out.println(key + " : " + dataset.getMediaObjects().get(key));

			}
		} else {
			System.out.println("dataset is null !");
		}

		System.out.println(System.getProperty("line.separator") + "parseDuration= " + parseDuration + "s");
	}

	/**
	 * display all the children of character
	 * 
	 * @param tabulations
	 *            , the string tabulation
	 * @param character
	 *            , the parent character
	 */
	private void displayRecursiveChildren(String tabulations, ICharacter character) {
		System.out.println(tabulations + "character->" + character.getName());
		for (State state : character.getInapplicableStates()) {
			System.out.println(tabulations + "\tInappState:" + state.getName());
		}
		for (ICharacter childCharacter : character.getChildCharacters()) {
			displayRecursiveChildren(tabulations + "\t", childCharacter);
		}
	}
}
