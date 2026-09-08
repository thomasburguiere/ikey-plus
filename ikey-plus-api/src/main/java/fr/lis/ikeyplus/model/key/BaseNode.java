package fr.lis.ikeyplus.model.key;

import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.model.Taxon;

import java.util.List;

public sealed interface BaseNode permits CharacterNode, RootNode {

    List<CharacterNode> getChildren();

    List<Taxon> getRemainingTaxa();

    void setRemainingTaxa(List<Taxon> taxa);

    boolean hasChild();

    void addChild(CharacterNode node);

}
