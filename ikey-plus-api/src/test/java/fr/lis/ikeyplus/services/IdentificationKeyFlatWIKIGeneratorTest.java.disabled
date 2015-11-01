package fr.lis.ikeyplus.services;

import com.google.common.collect.Sets;
import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.utils.IkeyConfig;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.HEADER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.OTHER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.STATISTIC;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.WARNING;
/**
 * This class allow to test the flat WIKI output of IdentificationKeyGenerator service
 *
 * @author Thomas Burguiere
 * @created 18-07-2011
 */
@Ignore
public class IdentificationKeyFlatWIKIGeneratorTest {

    public Logger logger = Logger.getAnonymousLogger();

    @Test
    public void testIdentificationKeyGenerator() {

        // creation of IkeyConfig object (containing options)
        IkeyConfig utils = new IkeyConfig();

        // set the confTest ResourceBundle
        IkeyConfig.setBundleConfOverridable(ResourceBundle.getBundle("fr.lis.ikeyplus.confTest"));
        IkeyConfig.setBundleConf(ResourceBundle.getBundle("fr.lis.ikeyplus.confTest"));

        // String containing the name of the result file
        String resultFileName = "";
        try {
            // define logger
            logger.info("testIdentificationKeyGenerator");
            // define time before parsing SDD file
            long beforeTime = System.currentTimeMillis();

            // define header string
            StringBuffer header = new StringBuffer();

            SDDSaxParser sddSaxParser = null;
            try {
                String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/cichorieae.sdd.xml";
                // String stringUrl =
                // "http://www.infosyslab.fr/vibrant/project/test/Cichorieae-unknownData-fullSDD.xml";
                // String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/phlebotomes-SDD.xml";
                // String stringUrl =
                // "http://www.infosyslab.fr/vibrant/project/test/milichia_revision-sdd.xml";
                // String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/testSDD.xml";
                // String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/feuillesSDD.xml";
                // String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/feuillesImagesURL.xml";
                // String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/smallSDD.xml";
                // String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/wrongSDD.xml";
                // String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/pruningSDD.xml";
                // String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/archaeoSDD.xml";
                // String stringUrl =
                // "http://www.infosyslab.fr/vibrant/project/test/varanusSDD_RatingExample.xml";
                // String stringUrl =
                // "http://www.infosyslab.fr/vibrant/project/test/varanusSDD_RatingExample3_contextual.xml";
                // String stringUrl = "http://www.infosyslab.fr/vibrant/project/test/bambousSDD.xml";

                // options
                utils.setFewStatesCharacterFirst(false);
                utils.setMergeCharacterStatesIfSameDiscrimination(false);
                utils.setPruning(false);
                utils.setVerbosity(Sets.newHashSet(HEADER, OTHER, WARNING, STATISTIC));
                utils.setScoreMethod(IkeyConfig.ScoreMethod.XPER);
                utils.setWeightContext(IkeyConfig.WeightContext.COST_EFFECTIVENESS);
                utils.setWeightType(IkeyConfig.WeightType.GLOBAL);

                // test if the URL is valid
                URLConnection urlConnection;
                try {
                    URL fileURL = new URL(stringUrl);
                    // open URL (HTTP query)
                    urlConnection = fileURL.openConnection();
                    // Open data stream
                    urlConnection.getInputStream();
                } catch (java.net.MalformedURLException e) {
                    utils.setErrorMessage(IkeyConfig.getBundleConfElement("message.urlError"), e);
                    e.printStackTrace();
                } catch (java.io.IOException e) {
                    utils.setErrorMessage(IkeyConfig.getBundleConfElement("message.urlError"), e);
                    e.printStackTrace();
                }
                sddSaxParser = new SDDSaxParser(stringUrl, utils);
                // construct header
                header.append(System.getProperty("line.separator") + sddSaxParser.getDataset().getLabel()
                        + ", " + IkeyConfig.getBundleConfOverridableElement("message.createdBy")
                        + System.getProperty("line.separator"));
                header.append(System.getProperty("line.separator") + "Options:");
                header.append(System.getProperty("line.separator") + "sddURL=" + stringUrl);
                header.append(System.getProperty("line.separator") + "fewStatesCharacterFirst="
                        + utils.isFewStatesCharacterFirst());
                header.append(System.getProperty("line.separator")
                        + "mergeCharacterStatesIfSameDiscrimination="
                        + utils.isMergeCharacterStatesIfSameDiscrimination());
                header.append(System.getProperty("line.separator") + "pruning=" + utils.isPruning());
                header.append(System.getProperty("line.separator") + "verbosity=" + utils.getVerbosity());
                header.append(System.getProperty("line.separator") + "scoreMethod=" + utils.getScoreMethod());
                header.append(System.getProperty("line.separator") + "weightContext="
                        + utils.getWeightContext());
                header.append(System.getProperty("line.separator") + "weightType=" + utils.getWeightType()
                        + System.getProperty("line.separator"));

            } catch (Throwable t) {
                utils.setErrorMessage(IkeyConfig.getBundleConfElement("message.parsingError"), t);
                t.printStackTrace();
            }

            // define parse duration
            double parseDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;
            // define time before processing key
            beforeTime = System.currentTimeMillis();

            IdentificationKeyGenerator identificationKeyGenerator = null;
            try {
                identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), utils);
                identificationKeyGenerator.createIdentificationKey();
            } catch (Throwable t) {
                if (t instanceof OutOfMemoryError) {
                    utils.setErrorMessage(IkeyConfig.getBundleConfElement("message.memoryError"), t);
                } else {
                    utils.setErrorMessage(IkeyConfig.getBundleConfElement("message.creatingKeyError"), t);
                }
                t.printStackTrace();
            }

            // define creating key duration
            double keyCreationDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;

            // construct header
            header.append(System.getProperty("line.separator") + "parseDuration= " + parseDuration + "s");
            header.append(System.getProperty("line.separator") + "keyCreationDuration= "
                    + keyCreationDuration + "s");

            // create key file
            try {
                SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
                header.append(System.getProperty("line.separator") + System.getProperty("line.separator"));

                if (!utils.getVerbosity().contains(IkeyConfig.VerbosityLevel.HEADER)) {
                    header.setLength(0);
                }
                resultFileName = SingleAccessKeyTreeDumper.dumpFlatWikiFile(header.toString(), tree2dump,
                        utils.getVerbosity().contains(IkeyConfig.VerbosityLevel.STATISTIC)).getName();

            } catch (IOException e) {
                utils.setErrorMessage(IkeyConfig.getBundleConfElement("message.creatingFileError"), e);
                e.printStackTrace();
            }
        } catch (Throwable t) {
            utils.setErrorMessage(IkeyConfig.getBundleConfElement("message.error"), t);
            t.printStackTrace();
        }

        // if error exist use error file as result file
        if (utils.getErrorMessageFile() != null) {
            resultFileName = utils.getErrorMessageFile().getName();
        }

        // display the URL of file result
        System.out.println(resultFileName);
    }
}
