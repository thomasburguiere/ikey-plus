package fr.lis.ikeyplus.model;

import org.immutables.value.Value;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * This class represents a state of categorical character
 *
 * @author Florian Causse
 */
@Value.Immutable
public abstract class State {

    @Nullable
    @Value.Default
    public String id() {return null;}
    public abstract String name();
    public abstract List<String> mediaObjectKeys();

    public String getFirstImage(final DataSet dataSet) {
        if (dataSet != null && mediaObjectKeys() != null && !mediaObjectKeys().isEmpty()) {
            if (dataSet.getMediaObject(mediaObjectKeys().getFirst()).startsWith("http")) {
                return dataSet.getMediaObject(mediaObjectKeys().getFirst());
            }
        }
        return null;
    }

    public String getFirstImageKey() {
        if (!mediaObjectKeys().isEmpty()) {
            return mediaObjectKeys().getFirst();
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id()) + Objects.hashCode(name());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final State that)) {
            return false;
        }

        return Objects.equals(id(), that.id()) &&
            Objects.equals(name(), that.name());
    }
}