package fr.lis.ikeyplus.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents a taxon
 *
 * @author Florian Causse
 * @created 06-04-2011
 */
public class Taxon {

    public static final int NB_PATH_IN_KEY = 0;
    public static final int SHORTEST_PATH_IN_KEY = 1;
    public static final int LONGEST_PATH_IN_KEY = 2;
    public static final int AVERAGE_PATHLENGTH_IN_KEY = 3;
    public static final int SUM_PATHLENGTHS_IN_KEY = 4;

    private String id;
    private String name = null;
    private List<String> mediaObjectKeys = null;
    private HashMap<Integer, Float> pathStatistics;

    public Taxon() {
        this(null);
    }

    public Taxon(String name) {
        super();
        this.name = name;
        mediaObjectKeys = new ArrayList<>();

        // initializing the taxonStatistics
        pathStatistics = new HashMap<>();
        pathStatistics.put(NB_PATH_IN_KEY, (float) 0);
        pathStatistics.put(SHORTEST_PATH_IN_KEY, (float) 0);
        pathStatistics.put(LONGEST_PATH_IN_KEY, (float) 0);
        pathStatistics.put(AVERAGE_PATHLENGTH_IN_KEY, (float) 0);
        pathStatistics.put(SUM_PATHLENGTHS_IN_KEY, (float) 0);

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

    public void setMediaObjectKeys(List<String> mediaObjectKeys) {
        this.mediaObjectKeys = mediaObjectKeys;
    }

    public String getFirstImage(DataSet dataSet) {
        if (dataSet != null && mediaObjectKeys != null && mediaObjectKeys.size() > 0) {
            if (dataSet.getMediaObject(mediaObjectKeys.get(0)).startsWith("http")) {
                return dataSet.getMediaObject(mediaObjectKeys.get(0));
            }
        }
        return null;
    }

    public String getFirstImageKey() {
        if (mediaObjectKeys.size() > 0) {
            return mediaObjectKeys.get(0);
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
        return pathStatistics;
    }

    public void setTaxonStatistics(HashMap<Integer, Float> taxonStatistics) {
        this.pathStatistics = taxonStatistics;
    }

    public void updatePathStatistics(Float pathLength) {
        float oldNbPath = pathStatistics.get(NB_PATH_IN_KEY);
        float oldSumPathLength = pathStatistics.get(SUM_PATHLENGTHS_IN_KEY);

        float newNbPath = oldNbPath + 1;
        float newSumPathLength = oldSumPathLength + pathLength;
        float newAveragePathLength = newSumPathLength / newNbPath;

        pathStatistics.put(AVERAGE_PATHLENGTH_IN_KEY, newAveragePathLength);
        pathStatistics.put(NB_PATH_IN_KEY, newNbPath);
        pathStatistics.put(SUM_PATHLENGTHS_IN_KEY, newSumPathLength);

        if (pathStatistics.get(SHORTEST_PATH_IN_KEY) == 0) {
            pathStatistics.put(SHORTEST_PATH_IN_KEY, pathLength);
        } else if (pathLength < pathStatistics.get(SHORTEST_PATH_IN_KEY)) {
            pathStatistics.put(SHORTEST_PATH_IN_KEY, pathLength);
        }

        if (pathStatistics.get(LONGEST_PATH_IN_KEY) == 0) {
            pathStatistics.put(LONGEST_PATH_IN_KEY, pathLength);
        } else if (pathLength > pathStatistics.get(LONGEST_PATH_IN_KEY)) {
            pathStatistics.put(LONGEST_PATH_IN_KEY, pathLength);
        }
    }

}