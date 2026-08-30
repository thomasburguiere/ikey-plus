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
public class SDDSaxParser {

    // kwnoledge base (call dataset)
    private DataSet dataset = null;

    /**
     * constructor which parses the content of the input file
     */
    public SDDSaxParser(final String uri, final IkeyConfig utils) throws SAXException, IOException {
        final XMLReader saxReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");

        final SDDContentHandler handler = new SDDContentHandler(utils);
        saxReader.setContentHandler(handler);

        InputSource is = null;
        try {
            final URL url = new URL(uri);
            is = new InputSource(url.openStream());
        } catch (final MalformedURLException e) {
            is = new InputSource(new FileInputStream(new File(uri)));
        }

        saxReader.parse(is);
        setDataset(handler.getDataSet());
    }

    /**
     * constructor which parses the content of the input file
     */
    public SDDSaxParser(final File inputFile, final IkeyConfig conf) throws SAXException, IOException {
        final XMLReader saxReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");

        final SDDContentHandler handler = new SDDContentHandler(conf);
        saxReader.setContentHandler(handler);

        final InputSource is = new InputSource(new FileInputStream(inputFile));

        saxReader.parse(is);
        setDataset(handler.getDataSet());
    }

    public DataSet getDataset() {
        return dataset;
    }

    public void setDataset(final DataSet dataset) {
        this.dataset = dataset;
    }

}
