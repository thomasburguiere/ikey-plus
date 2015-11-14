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
import fr.lis.ikeyplus.utils.IkeyConfig;


/**
 * This class starts the parsing of a SDD file
 *
 * @author Florian Causse
 * @created 18-04-2011
 */

public class SDDSaxParser implements SDDParser {

    // knowledge base (call dataset)

    /**
     * constructor which parses the content of the input file
     */
    @Override
    public DataSet parseDataset(String uri, IkeyConfig utils) throws SAXException, IOException {
        XMLReader saxReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");

        SDDContentHandler handler = new SDDContentHandler(utils);
        saxReader.setContentHandler(handler);

        InputSource is;
        try {
            URL url = new URL(uri);
            is = new InputSource(url.openStream());
        } catch (MalformedURLException e) {
            is = new InputSource(new FileInputStream(new File(uri)));
        }

        saxReader.parse(is);
        return handler.getDataSet();
    }

    /**
     * constructor which parses the content of the input file
     */
    @Override
    public DataSet parseDataset(File inputFile, IkeyConfig conf) throws SAXException, IOException {

        XMLReader saxReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");

        SDDContentHandler handler = new SDDContentHandler(conf);
        saxReader.setContentHandler(handler);

        InputSource is = new InputSource(new FileInputStream(inputFile));

        saxReader.parse(is);
        return handler.getDataSet();
    }

}
