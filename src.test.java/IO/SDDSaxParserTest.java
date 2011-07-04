package IO;

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

public class SDDSaxParserTest {

	public Logger logger = Logger.getAnonymousLogger();

	@Test
	public void testSDDSaxParser() {
		logger.info("testSDDSaxParser");
		long beforeTime = System.currentTimeMillis();
		
		SDDSaxParser sddSaxParser = null;
		
		try {
			//sddSaxParser = new SDDSaxParser("http://www.infosyslab.fr/vibrant/project/test/milichia_revision-sdd.xml");
			//sddSaxParser = new SDDSaxParser("http://www.infosyslab.fr/vibrant/project/test/testSDD.xml");
			//sddSaxParser = new SDDSaxParser("http://www.infosyslab.fr/vibrant/project/test/Cichorieae-fullSDD.xml");
			sddSaxParser = new SDDSaxParser("http://www.infosyslab.fr/vibrant/project/test/feuillesSDD.xml");
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		double parseDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;

		DataSet dataset = sddSaxParser.getDataset();
		if (dataset != null) {
			// DISPLAY THE DATASET
			System.out.println("dataSetLabel : " + dataset.getLabel());
			// CHARACTERS
			System.out.println("characters (" + dataset.getCharacters().size()
					+ ") : ");
			for (ICharacter character : dataset.getCharacters()) {

				if (character instanceof CategoricalCharacter) {
					System.out.println("\t" + character.getName());
					for (State state : ((CategoricalCharacter) character)
							.getStates()) {
						System.out.println("\t\t" + state.getName());
					}
				} else {
					System.out.println("\t*N*" + character.getName());
				}
			}
			// TAXA AND DESCRIPTION
			System.out.println("taxa (" + dataset.getTaxa().size() + ") : ");
			for (Taxon taxon : dataset.getTaxa()) {
				System.out.println("\t" + taxon.getName());
				CodedDescription codedDescription = dataset
						.getCodedDescription(taxon);

				for (ICharacter character : dataset.getCharacters()) {
					Object characterDescription = codedDescription
							.getCharacterDescription(character);
					if (characterDescription != null) {
						System.out.println("\t\t" + character.getName());
						if (characterDescription instanceof QuantitativeMeasure) {
							System.out
									.println("\t\t\t"
											+ ((QuantitativeMeasure) characterDescription)
													.toString());
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
					System.out.println("\tparent->"
							+ character.getParentCharacter().getName());
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
		} else {
			System.out.println("dataset is null !");
		}
		
		System.out.println(System.getProperty("line.separator")+"parseDuration= " + parseDuration + "s");
	}

	/**
	 * display all the children of character
	 * 
	 * @param tabulations
	 *            , the string tabulation
	 * @param character
	 *            , the parent character
	 */
	private void displayRecursiveChildren(String tabulations,
			ICharacter character) {
		System.out.println(tabulations + "character->" + character.getName());
		for (State state : character.getInapplicableStates()) {
			System.out.println(tabulations + "\tInappState:" + state.getName());
		}
		for (ICharacter childCharacter : character.getChildCharacters()) {
			displayRecursiveChildren(tabulations + "\t", childCharacter);
		}
	}
}
