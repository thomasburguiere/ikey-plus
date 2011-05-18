package IO;

import java.io.IOException;
import java.net.URL;

import model.DataSet;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class allow to launch the parsing of SDD file
 * 
 * @author Florian Causse
 * @created 18-avr.-2011
 */
public class SDDSaxParser {

	// kwnoledge base (call dataset)
	private DataSet dataset = null;

	/**
	 * constructor executing the parse method
	 */
	public SDDSaxParser(String uri) throws SAXException, IOException {
		XMLReader saxReader = XMLReaderFactory
				.createXMLReader("org.apache.xerces.parsers.SAXParser");

		SDDContentHandler handler = new SDDContentHandler();
		saxReader.setContentHandler(handler);

		URL url = new URL(uri);
		InputSource is = null;
		is = new InputSource(url.openStream());

		saxReader.parse(is);
		this.setDataset(handler.getDataset());
	}

	/**
	 * get the current dataset
	 * 
	 * @return DataSet, the current dataset
	 */
	public DataSet getDataset() {
		return dataset;
	}

	/**
	 * set the current dataset
	 * 
	 * @param DataSet
	 *            , the current dataset
	 */
	public void setDataset(DataSet dataset) {
		this.dataset = dataset;
	}

}
