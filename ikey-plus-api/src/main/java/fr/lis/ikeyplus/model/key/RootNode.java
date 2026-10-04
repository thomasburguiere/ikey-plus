package fr.lis.ikeyplus.model.key;

import fr.lis.ikeyplus.model.Taxon;

import java.util.ArrayList;
import java.util.List;

public final class RootNode implements BaseNode {

    private final List<CharacterNode> children;
    private List<Taxon> remainingTaxa;

    public RootNode(){
        this(new ArrayList<>());
    }

    public RootNode(final List<CharacterNode> children) {
        this.children = children;
        this.remainingTaxa = new ArrayList<>();
    }

    @Override
    public List<CharacterNode> getChildren() {
        return this.children;
    }

    @Override
    public List<Taxon> getRemainingTaxa() {
        return remainingTaxa;
    }

    @Override
    public void setRemainingTaxa(List<Taxon> taxa) {
        this.remainingTaxa = taxa;
    }

    @Override
    public boolean hasChild() {
        return false;
    }

    @Override
    public void addChild(CharacterNode node) {
        this.children.add(node);
    }

    @Override
    public String toString() {
        return "RootNode{" +
                "children=" + children.size() +
                ", remainingTaxa=" + remainingTaxa.size() +
                '}';
    }
}
