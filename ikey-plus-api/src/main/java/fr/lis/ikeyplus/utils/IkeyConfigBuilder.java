package fr.lis.ikeyplus.utils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

public class IkeyConfigBuilder {
    private IkeyConfig.OutputFormat format = IkeyConfig.OutputFormat.TXT;
    private IkeyConfig.KeyRepresentation representation = IkeyConfig.KeyRepresentation.TREE;
    private boolean fewStatesCharacterFirst = false;
    private boolean mergeCharacterStatesIfSameDiscrimination = false;
    private boolean pruningEnabled = false;
    private final Set<IkeyConfig.VerbosityLevel> verbosity = Sets.newHashSet();
    private IkeyConfig.ScoreMethod scoreMethod = IkeyConfig.ScoreMethod.XPER;
    private IkeyConfig.WeightContext weightContext = IkeyConfig.WeightContext.NO_WEIGHT;
    private IkeyConfig.WeightType weightType = IkeyConfig.WeightType.GLOBAL;

    IkeyConfigBuilder() {
    }

    public IkeyConfigBuilder format(IkeyConfig.OutputFormat format) {
        this.format = format;
        return this;
    }

    public IkeyConfigBuilder representation(IkeyConfig.KeyRepresentation representation) {
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

    public IkeyConfigBuilder scoreMethod(IkeyConfig.ScoreMethod scoreMethod) {
        this.scoreMethod = scoreMethod;
        return this;
    }

    public IkeyConfigBuilder weightContext(IkeyConfig.WeightContext weightContext) {
        this.weightContext = weightContext;
        return this;
    }

    public IkeyConfigBuilder weightType(IkeyConfig.WeightType weightType) {
        this.weightType = weightType;
        return this;
    }

    public IkeyConfigBuilder verbosity(IkeyConfig.VerbosityLevel verbosityLevel) {
        verbosity.add(verbosityLevel);
        return this;
    }

    public IkeyConfigBuilder verbosity(Collection<IkeyConfig.VerbosityLevel> verbosityLevels) {
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
                ImmutableSet.copyOf(verbosity),
                scoreMethod,
                weightContext,
                weightType);
    }

}
