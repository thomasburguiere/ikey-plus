package fr.lis.ikeyplus.utils;

import java.util.HashSet;
import java.util.Set;

import static fr.lis.ikeyplus.utils.IkeyConfig.*;

public class IkeyConfigBuilder {
    private OutputFormat format = OutputFormat.TXT;
    private KeyRepresentation representation = KeyRepresentation.TREE;
    private boolean fewStatesCharacterFirst = false;
    private boolean mergeCharacterStatesIfSameDiscrimination = false;
    private boolean pruningEnabled = false;
    private final Set<VerbosityLevel> verbosity = new HashSet<>();
    private ScoreMethod scoreMethod = ScoreMethod.XPER;
    private WeightContext weightContext = WeightContext.NO_WEIGHT;
    private WeightType weightType = WeightType.GLOBAL;

    IkeyConfigBuilder(){}

    public IkeyConfigBuilder format(final OutputFormat format) {
        this.format = format;
        return this;
    }

    public IkeyConfigBuilder representation(final KeyRepresentation representation) {
        this.representation = representation;
        return this;
    }

    public IkeyConfigBuilder fewStatesCharacterFirst() {
        this.fewStatesCharacterFirst = true;
        return this;
    }

    public IkeyConfigBuilder fewStatesCharacterFirst(final boolean value) {
        this.fewStatesCharacterFirst = value;
        return this;
    }

    public IkeyConfigBuilder mergeCharacterStatesIfSameDiscrimination() {
        this.mergeCharacterStatesIfSameDiscrimination = true;
        return this;
    }

    public IkeyConfigBuilder mergeCharacterStatesIfSameDiscrimination(final boolean value) {
        this.mergeCharacterStatesIfSameDiscrimination = value;
        return this;
    }

    public IkeyConfigBuilder enablePruning() {
        this.pruningEnabled = true;
        return this;
    }
    public IkeyConfigBuilder enablePruning(final boolean value) {
        this.pruningEnabled = value;
        return this;
    }

    public IkeyConfigBuilder scoreMethod(final ScoreMethod scoreMethod) {
        this.scoreMethod = scoreMethod;
        return this;
    }

    public IkeyConfigBuilder weightContext(final WeightContext weightContext) {
        this.weightContext = weightContext;
        return this;
    }

    public IkeyConfigBuilder weightType(final WeightType weightType) {
        this.weightType = weightType;
        return this;
    }

    public IkeyConfigBuilder verbosity(final VerbosityLevel verbosityLevel) {
        verbosity.add(verbosityLevel);
        return this;
    }

    public IkeyConfigBuilder verbosity(final Set<VerbosityLevel> verbosityLevels) {
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
