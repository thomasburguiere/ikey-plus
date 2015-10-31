package fr.lis.ikeyplus.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.utils.Utils;

/**
 * This class starts the parsing of a SDD file
 * 
 * @author Florian Causse
 * @created 18-04-2011
 */
public class SDDSaxParser {

	// kwnoledge base (call dataset)
	private DataSet dataset = null;

	/**
	 * constructor which parses the content of the input file
	 */
	public SDDSaxParser(String uri, Utils utils) throws SAXException, IOException {
		XMLReader saxReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");

		SDDContentHandler handler = new SDDContentHandler(utils);
		saxReader.setContentHandler(handler);

		InputSource is = null;
		try {
			URL url = new URL(uri);
			is = new InputSource(url.openStream());
		} catch (MalformedURLException e) {
			is = new InputSource(new FileInputStream(new File(uri)));
		}

		saxReader.parse(is);
		this.setDataset(handler.getDataSet());
	}

	/**
	 * constructor which parses the content of the input file
	 */
    public SDDSaxParser(File inputFile, Utils conf) throws SAXException, IOException {
        XMLReader saxReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");

        SDDContentHandler handler = new SDDContentHandler(conf);
        saxReader.setContentHandler(handler);

        InputSource is = new InputSource(new FileInputStream(inputFile));

        saxReader.parse(is);
        this.setDataset(handler.getDataSet());
    }

	public DataSet getDataset() {
		return dataset;
	}

	public void setDataset(DataSet dataset) {
		this.dataset = dataset;
	}

}
