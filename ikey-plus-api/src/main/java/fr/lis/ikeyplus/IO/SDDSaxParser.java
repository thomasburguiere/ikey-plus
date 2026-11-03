package fr.lis.ikeyplus.IO;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.utils.IkeyConfig;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author Florian Causse
 * @created 18-04-2011
 */
public class SDDSaxParser {

    private final SAXParserFactory saxParserFactory;

    public SDDSaxParser() {
        saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
    }

    private record ReaderAndHandler(XMLReader reader, SDDContentHandler handler) {
    }

    public DataSet parseDataset(final InputStream in, final IkeyConfig config) throws SAXException, IOException, ParserConfigurationException {
        final var readerAndHandler = initXmlReaderAndSddContentHandler(config);

        final InputSource is = new InputSource(in);
        readerAndHandler.reader.parse(is);

        return readerAndHandler.handler.getDataSet();
    }

    private ReaderAndHandler initXmlReaderAndSddContentHandler(final IkeyConfig ikeyConfig) throws SAXException, ParserConfigurationException {

        final XMLReader xmlReader = saxParserFactory.newSAXParser().getXMLReader();

        final SDDContentHandler handler = new SDDContentHandler(ikeyConfig);
        xmlReader.setContentHandler(handler);

        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        return new ReaderAndHandler(xmlReader, handler);
    }
}
