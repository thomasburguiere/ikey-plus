package fr.lis.ikeyplus.model.description;

import fr.lis.ikeyplus.model.DataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a state of categorical character
 *
 * @author Florian Causse
 */
public class State {

    private String id = null;
    private String name;
    private List<String> mediaObjectKeys;

    public State() {
        this(null);
    }

    public State(final String name) {
        this.name = name;
        this.mediaObjectKeys = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public List<String> getMediaObjectKeys() {
        return mediaObjectKeys;
    }

    public void setMediaObjectKeys(final List<String> mediaObjectKeys) {
        this.mediaObjectKeys = mediaObjectKeys;
    }

    public String getFirstImage(final DataSet dataSet) {
        if (dataSet != null && mediaObjectKeys != null && !mediaObjectKeys.isEmpty()) {
            if (dataSet.getMediaObject(mediaObjectKeys.getFirst()).startsWith("http")) {
                return dataSet.getMediaObject(mediaObjectKeys.getFirst());
            }
        }
        return null;
    }

    public String getFirstImageKey() {
        if (!mediaObjectKeys.isEmpty()) {
            return mediaObjectKeys.getFirst();
        }
        return null;
    }

    public String toString() {
        return name;
    }

    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final State that = (State) o;

        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id) + Objects.hashCode(name);
    }
}