package IO;

import java.io.IOException;
import java.net.URL;

import model.DataSet;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import utils.SaxErrorHandler;

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

		// set to true the validation of XML using XSD
		saxReader.setFeature("http://xml.org/sax/features/validation", true);
		saxReader.setFeature("http://apache.org/xml/features/validation/schema", true);
		saxReader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", "http://rs.tdwg.org/UBIF/2006/Schema/1.1/SDD.xsd");
		// init the ErrorHandler
		saxReader.setErrorHandler(new SaxErrorHandler());

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
