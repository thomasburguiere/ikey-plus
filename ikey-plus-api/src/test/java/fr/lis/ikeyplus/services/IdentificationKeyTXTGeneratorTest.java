package fr.lis.ikeyplus.services;

import com.google.common.collect.Sets;
import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.IO.SingleAccessKeyTreeDumper;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.utils.IkeyConfigBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
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
import static fr.lis.ikeyplus.utils.IkeyConfig.getBundleConfOverridableElement;

/**
 * This class allows to test the TEXT output of IdentificationKeyGenerator service
 *
 * @author Florian Causse
 * @created 18-04-2011
 */
public class IdentificationKeyTXTGeneratorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "src/test/resources/inputFiles/cichorieae.sdd.xml",
            "src/test/resources/inputFiles/Cichorieae-unknownData-fullSDD.xml",
            "src/test/resources/inputFiles/genetta.sdd.xml",
             "src/test/resources/inputFiles/milichia_revision-sdd.xml",
             "src/test/resources/inputFiles/testSDD.xml",
             "src/test/resources/inputFiles/feuillesSDD.xml",
             "src/test/resources/inputFiles/feuillesImagesURL.xml",
             "src/test/resources/inputFiles/smallSDD.xml",
             "src/test/resources/inputFiles/wrongSDD.xml",
             "src/test/resources/inputFiles/pruningSDD.xml",
             "src/test/resources/inputFiles/archaeoSDD.xml"
    })
    public void testIdentificationKeyGenerator(final String testFileUrl) throws Exception {

        // creation of IkeyConfig object (containing options)
        final IkeyConfigBuilder configBuilder = builder();

        configBuilder.verbosity(Sets.newHashSet(HEADER, OTHER, WARNING, STATISTICS));
        configBuilder.scoreMethod(XPER);
        configBuilder.weightContext(COST_EFFECTIVENESS);
        configBuilder.weightType(GLOBAL);
        final var config = configBuilder.build();


        String resultFileName = "";
        long beforeTime = System.currentTimeMillis();

        final StringBuilder header = new StringBuilder();

        // options
        final SDDSaxParser sddSaxParser = new SDDSaxParser(new File(testFileUrl), config);
        // construct header
        header.append(System.lineSeparator()).append(sddSaxParser.getDataset().getLabel()).append(", ").append(getBundleConfOverridableElement("message.createdBy")).append(System.lineSeparator());
        header.append(System.lineSeparator()).append("Options:");
        header.append(System.lineSeparator()).append("sddURL=").append(testFileUrl);
        header.append(System.lineSeparator()).append("fewStatesCharacterFirst=").append(config.isFewStatesCharacterFirst());
        header.append(System.lineSeparator()).append("mergeCharacterStatesIfSameDiscrimination=").append(config.isMergeCharacterStatesIfSameDiscrimination());
        header.append(System.lineSeparator()).append("pruning=").append(config.isPruningEnabled());
        header.append(System.lineSeparator()).append("verbosity=").append(config.getVerbosity());
        header.append(System.lineSeparator()).append("scoreMethod=").append(config.getScoreMethod());
        header.append(System.lineSeparator()).append("weightContext=").append(config.getWeightContext());
        header.append(System.lineSeparator()).append("weightType=").append(config.getWeightType()).append(System.lineSeparator());

        final double parseDuration = (double) (System.currentTimeMillis() - beforeTime) / 1000;
        beforeTime = System.currentTimeMillis();

        final IdentificationKeyGenerator identificationKeyGenerator = new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);
        identificationKeyGenerator.createIdentificationKey();

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
