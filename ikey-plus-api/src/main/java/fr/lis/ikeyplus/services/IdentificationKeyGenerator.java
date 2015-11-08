package fr.lis.ikeyplus.services;

import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.utils.IkeyConfig;

public interface IdentificationKeyGenerator {
    SingleAccessKeyTree getIdentificationKey(DataSet dataset, IkeyConfig config) throws OutOfMemoryError;
}
