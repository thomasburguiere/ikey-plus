package fr.lis.ikeyplus.services;

import com.google.common.collect.Sets;
import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.utils.IkeyConfigBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static fr.lis.ikeyplus.utils.IkeyConfig.ScoreMethod.XPER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.HEADER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.OTHER;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.STATISTICS;
import static fr.lis.ikeyplus.utils.IkeyConfig.VerbosityLevel.WARNING;
import static fr.lis.ikeyplus.utils.IkeyConfig.WeightContext.COST_EFFECTIVENESS;
import static fr.lis.ikeyplus.utils.IkeyConfig.WeightType.GLOBAL;
import static fr.lis.ikeyplus.utils.IkeyConfig.builder;
import static fr.lis.ikeyplus.utils.IkeyConfig.getBundleConfElement;
import static fr.lis.ikeyplus.utils.IkeyConfig.getBundleConfOverridableElement;
import static fr.lis.ikeyplus.utils.IkeyConfig.setBundleConf;
import static fr.lis.ikeyplus.utils.IkeyConfig.setBundleConfOverridable;

/**
 * This class allows to test the TEXT output of IdentificationKeyGenerator service
 *
 * @author Florian Causse
 * @created 18-04-2011
 */
public class IdentificationKeyTXTGeneratorTest {

    public Logger logger = Logger.getAnonymousLogger();

    @Test
    public void testIdentificationKeyGenerator() throws Exception {

        // creation of IkeyConfig object (containing options)
        final IkeyConfigBuilder configBuilder = builder();

        configBuilder.verbosity(Sets.newHashSet(HEADER, OTHER, WARNING, STATISTICS));
        configBuilder.scoreMethod(XPER);
        configBuilder.weightContext(COST_EFFECTIVENESS);
        configBuilder.weightType(GLOBAL);
        final var config = configBuilder.build();


        // String containing the name of the result file
        String resultFileName = "";
        // define logger
        logger.info("testIdentificationKeyGenerator");
        // define time before parsing SDD file
        long beforeTime = System.currentTimeMillis();

        // define header string
        final StringBuilder header = new StringBuilder();

        final String stringUrl = "src/test/resources/inputFiles/cichorieae.sdd.xml";
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

        final SDDSaxParser sddSaxParser = new SDDSaxParser(new File("src/test/resources/inputFiles/cichorieae.sdd.xml"), config);
        // construct header
        header.append(System.lineSeparator()).append(sddSaxParser.getDataset().getLabel()).append(", ").append(getBundleConfOverridableElement("message.createdBy")).append(System.lineSeparator());
        header.append(System.lineSeparator()).append("Options:");
        header.append(System.lineSeparator()).append("sddURL=").append(stringUrl);
        header.append(System.lineSeparator()).append("fewStatesCharacterFirst=").append(config.isFewStatesCharacterFirst());
        header.append(System.lineSeparator()).append("mergeCharacterStatesIfSameDiscrimination=").append(config.isMergeCharacterStatesIfSameDiscrimination());
        header.append(System.lineSeparator()).append("pruning=").append(config.isPruningEnabled());
        header.append(System.lineSeparator()).append("verbosity=").append(config.getVerbosity());
        header.append(System.lineSeparator()).append("scoreMethod=").append(config.getScoreMethod());
        header.append(System.lineSeparator()).append("weightContext=").append(config.getWeightContext());
        header.append(System.lineSeparator()).append("weightType=").append(config.getWeightType()).append(System.lineSeparator());


        // define parse duration
        final double parseDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;
        // define time before processing key
        beforeTime = System.currentTimeMillis();

        final IdentificationKeyGenerator identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();

        // define creating key duration
        final double keyCreationDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;

        // construct header
        header.append(System.lineSeparator()).append("parseDuration= ").append(parseDuration).append("s");
        header.append(System.lineSeparator()).append("keyCreationDuration= ").append(keyCreationDuration).append("s");
        header.append(System.lineSeparator()).append(System.lineSeparator());

        // create key file
        final SingleAccessKeyTree tree2dump = identificationKeyGenerator.getSingleAccessKeyTree();
        header.append(System.lineSeparator()).append(System.lineSeparator());

        if (!config.getVerbosity().contains(HEADER)) {
            header.setLength(0);
        }
        resultFileName = SingleAccessKeyTreeDumper.dumpTxtFile(
                header.toString(),
                tree2dump,
                config.getVerbosity().contains(VerbosityLevel.STATISTICS),
                "src/test/resources/outputFiles"
        ).getName();
    }
}
