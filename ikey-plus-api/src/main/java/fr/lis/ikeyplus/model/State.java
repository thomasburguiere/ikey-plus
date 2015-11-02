package fr.lis.ikeyplus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a state of categorical character
 *
 * @author Florian Causse
 */
public class State {

    private String id = null;
    private String name = null;
    private List<String> mediaObjectKeys = null;

    public State() {
        this(null);
    }

    public State(String name) {
        super();
        this.name = name;
        this.mediaObjectKeys = new ArrayList<String>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String toString() {
        return name;
    }
}