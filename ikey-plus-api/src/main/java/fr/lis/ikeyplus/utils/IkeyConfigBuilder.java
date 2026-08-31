package fr.lis.ikeyplus.utils;

import java.util.HashSet;
import java.util.Set;

public class IkeyConfigBuilder {
    private IkeyConfig.OutputFormat format = IkeyConfig.OutputFormat.TXT;
    private IkeyConfig.KeyRepresentation representation = IkeyConfig.KeyRepresentation.TREE;
    private boolean fewStatesCharacterFirst = false;
    private boolean mergeCharacterStatesIfSameDiscrimination = false;
    private boolean pruningEnabled = false;
    private final Set<IkeyConfig.VerbosityLevel> verbosity = new HashSet<>();
    private IkeyConfig.ScoreMethod scoreMethod = IkeyConfig.ScoreMethod.XPER;
    private IkeyConfig.WeightContext weightContext = IkeyConfig.WeightContext.NO_WEIGHT;
    private IkeyConfig.WeightType weightType = IkeyConfig.WeightType.GLOBAL;

    IkeyConfigBuilder(){}

    public IkeyConfigBuilder format(final IkeyConfig.OutputFormat format) {
        this.format = format;
        return this;
    }

    public IkeyConfigBuilder representation(final IkeyConfig.KeyRepresentation representation) {
        this.representation = representation;
        return this;
    }

    public IkeyConfigBuilder fewStatesCharacterFirst() {
        this.fewStatesCharacterFirst = true;
        return this;
    }

    public IkeyConfigBuilder mergeCharacterStatesIfSameDiscrimination() {
        this.mergeCharacterStatesIfSameDiscrimination = true;
        return this;
    }

    public IkeyConfigBuilder enablePruning() {
        this.pruningEnabled = true;
        return this;
    }

    public IkeyConfigBuilder scoreMethod(final IkeyConfig.ScoreMethod scoreMethod) {
        this.scoreMethod = scoreMethod;
        return this;
    }

    public IkeyConfigBuilder weightContext(final IkeyConfig.WeightContext weightContext) {
        this.weightContext = weightContext;
        return this;
    }

    public IkeyConfigBuilder weightType(final IkeyConfig.WeightType weightType) {
        this.weightType = weightType;
        return this;
    }

    public IkeyConfigBuilder verbosity(final IkeyConfig.VerbosityLevel verbosityLevel) {
        verbosity.add(verbosityLevel);
        return this;
    }

    public IkeyConfigBuilder verbosity(final Set<IkeyConfig.VerbosityLevel> verbosityLevels) {
        verbosity.addAll(verbosityLevels);
        return this;
    }

    public IkeyConfig build() {
        return new IkeyConfig(
                format,
                representation,
                fewStatesCharacterFirst,
                mergeCharacterStatesIfSameDiscrimination,
                pruningEnabled,
                verbosity,
                scoreMethod,
                weightContext,
                weightType);
    }

}
