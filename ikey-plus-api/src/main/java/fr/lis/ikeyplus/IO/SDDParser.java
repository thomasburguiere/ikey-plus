package fr.lis.ikeyplus.IO;

import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.utils.IkeyConfig;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

public interface SDDParser {
    DataSet parseDataset(String uri, IkeyConfig utils) throws SAXException, IOException;

    DataSet parseDataset(File inputFile, IkeyConfig conf) throws SAXException, IOException;
}
