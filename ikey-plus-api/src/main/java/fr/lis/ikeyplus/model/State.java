package fr.lis.ikeyplus.model;

import com.google.common.base.Objects;

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
        this.mediaObjectKeys = new ArrayList<>();
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

    public String getFirstImage(DataSet dataSet) {
        if (dataSet != null && mediaObjectKeys != null && !mediaObjectKeys.isEmpty()) {
            if (dataSet.getMediaObject(mediaObjectKeys.get(0)).startsWith("http")) {
                return dataSet.getMediaObject(mediaObjectKeys.get(0));
            }
        }
        return null;
    }

    public String getFirstImageKey() {
        if (!mediaObjectKeys.isEmpty()) {
            return mediaObjectKeys.get(0);
        }
        return null;
    }

    public String toString() {
        return name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        State that = (State) o;

        return Objects.equal(id, that.id) &&
                Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name);
    }
}