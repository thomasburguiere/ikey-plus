package fr.lis.ikeyplus.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents a taxon
 *
 * @author Florian Causse
 */
public class Taxon {

    public static final int NB_PATH_IN_KEY = 0;
    public static final int SHORTEST_PATH_IN_KEY = 1;
    public static final int LONGEST_PATH_IN_KEY = 2;
    public static final int AVERAGE_PATH_LENGTH_IN_KEY = 3;
    private static final int SUM_PATH_LENGTHS_IN_KEY = 4;

    private String id;
    private String name = null;
    private List<String> mediaObjectKeys = null;
    private final HashMap<Integer, Float> taxonStatistics;

    public Taxon() {
        this(null);
    }

    public Taxon(String name) {
        super();
        this.name = name;
        mediaObjectKeys = new ArrayList<>();

        // initializing the taxonStatistics
        taxonStatistics = new HashMap<>();
        taxonStatistics.put(NB_PATH_IN_KEY, (float) 0);
        taxonStatistics.put(SHORTEST_PATH_IN_KEY, (float) 0);
        taxonStatistics.put(LONGEST_PATH_IN_KEY, (float) 0);
        taxonStatistics.put(AVERAGE_PATH_LENGTH_IN_KEY, (float) 0);
        taxonStatistics.put(SUM_PATH_LENGTHS_IN_KEY, (float) 0);

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMediaObjectKeys() {
        return mediaObjectKeys;
    }

    public String getFirstImage(DataSet dataSet) {
        if (dataSet != null && mediaObjectKeys != null && !mediaObjectKeys.isEmpty() &&
                dataSet.getMediaObject(mediaObjectKeys.get(0)).startsWith("http")) {
            return dataSet.getMediaObject(mediaObjectKeys.get(0));
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<Integer, Float> getTaxonStatistics() {
        return taxonStatistics;
    }

    public void updatePathStatistics(Float pathLength) {
        float oldNbPath = taxonStatistics.get(NB_PATH_IN_KEY);
        float oldSumPathLength = taxonStatistics.get(SUM_PATH_LENGTHS_IN_KEY);

        float newNbPath = oldNbPath + 1;
        float newSumPathLength = oldSumPathLength + pathLength;
        float newAveragePathLength = newSumPathLength / newNbPath;

        taxonStatistics.put(AVERAGE_PATH_LENGTH_IN_KEY, newAveragePathLength);
        taxonStatistics.put(NB_PATH_IN_KEY, newNbPath);
        taxonStatistics.put(SUM_PATH_LENGTHS_IN_KEY, newSumPathLength);

        if (taxonStatistics.get(SHORTEST_PATH_IN_KEY) == 0) {
            taxonStatistics.put(SHORTEST_PATH_IN_KEY, pathLength);
        } else if (pathLength < taxonStatistics.get(SHORTEST_PATH_IN_KEY)) {
            taxonStatistics.put(SHORTEST_PATH_IN_KEY, pathLength);
        }

        if (taxonStatistics.get(LONGEST_PATH_IN_KEY) == 0) {
            taxonStatistics.put(LONGEST_PATH_IN_KEY, pathLength);
        } else if (pathLength > taxonStatistics.get(LONGEST_PATH_IN_KEY)) {
            taxonStatistics.put(LONGEST_PATH_IN_KEY, pathLength);
        }
    }

}