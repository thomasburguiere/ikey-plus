package fr.lis.ikeyplus.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.utils.IkeyConfig;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author Florian Causse
 * @created 18-04-2011
 */
public class SDDSaxParser {

    // knowledge base (call dataset)
    private DataSet dataset;

    public SDDSaxParser(final InputStream in, final IkeyConfig config) throws SAXException, IOException, ParserConfigurationException {
        final var readerAndHandler = initXmlReaderAndSddContentHandler(config);

        final InputSource is = new InputSource(in);
        readerAndHandler.reader.parse(is);

        dataset = readerAndHandler.handler.getDataSet();
    }

    private record ReaderAndHandler(XMLReader reader, SDDContentHandler handler) {
    }

    private static ReaderAndHandler initXmlReaderAndSddContentHandler(final IkeyConfig ikeyConfig) throws SAXException, ParserConfigurationException {

        final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);

        final XMLReader xmlReader = saxParserFactory.newSAXParser().getXMLReader();

        final SDDContentHandler handler = new SDDContentHandler(ikeyConfig);
        xmlReader.setContentHandler(handler);

        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        return new ReaderAndHandler(xmlReader, handler);
    }

    public DataSet getDataset() {
        return dataset;
    }

    public void setDataset(final DataSet dataset) {
        this.dataset = dataset;
    }

}
