package fr.lis.ikeyplus.model;

import org.immutables.value.Value;
import org.jspecify.annotations.Nullable;

/**
 * This class represents a quantitative measure
 *
 * @author Florian Causse
 */

@Value.Immutable
public interface QuantitativeMeasure {


    @Nullable @Value.Default default Double max() {return null;}
    @Nullable @Value.Default default Double mean() {return null;}
    @Nullable @Value.Default default Double min() {return null;}
    @Nullable @Value.Default default Double sd() {return null;}
    @Nullable @Value.Default default Double uMethLower() {return null;}
    @Nullable @Value.Default default Double uMethUpper() {return null;}
    @Value.Default default boolean minInclude() {return true;}
    @Value.Default default boolean maxInclude() {return true;}

//    @Override
//    default String toString() {
//        return "Min=" + min() + "  Max=" + max() + "  Mean=" + mean() + "  SD=" + sd() + "  UMethLower=" + uMethLower
//                () + "  UMethUpper=" + uMethUpper();
//    }

    default String toStringInterval() {
        final String start;
        final String end;

        if (minInclude()) {
            start = "[";
        } else {
            start = "]";
        }

        if (maxInclude()) {
            end = "]";
        } else {
            end = "[";
        }
        return start + getCalculateMinimum() + ", " + getCalculateMaximum() + end;
    }

    default String toStringInterval(final String unit) {

        if (unit != null && !unit.isEmpty()) {
            return toStringInterval() + " (" + unit + ")";
        }
        return toStringInterval();
    }

    default Double getCalculateMinimum() {
        if (min() != null) {
            return min();
        } else if (uMethLower() != null) {
            return uMethLower();
        } else if (sd() != null && mean() != null) {
            return mean() - 2 * sd();
        } else {
            return null;
        }
    }

    default Double getCalculateMaximum() {
        if (max() != null) {
            return max();
        } else if (uMethUpper() != null) {
            return uMethUpper();
        } else if (sd() != null && mean() != null) {
            return mean() + 2 * sd();
        } else {
            return null;
        }
    }

    default boolean isInclude(final QuantitativeMeasure quantitativeMeasure) {

        if (quantitativeMeasure == null) {
            return true;
            // if both taxa are described
        } else if (!isNotSpecified() && !quantitativeMeasure.isNotSpecified()) {
            // if the max value of the current interval is included
            if (maxInclude()) {
                return (quantitativeMeasure.getCalculateMinimum() >= getCalculateMinimum() && quantitativeMeasure.getCalculateMinimum() <= getCalculateMaximum())
                        || (quantitativeMeasure.getCalculateMaximum() >= getCalculateMinimum() && quantitativeMeasure
                        .getCalculateMaximum() <= getCalculateMaximum());
            } else {
                return (quantitativeMeasure.getCalculateMinimum() >= getCalculateMinimum() && quantitativeMeasure.getCalculateMinimum() < getCalculateMaximum())
                        || (quantitativeMeasure.getCalculateMaximum() >= getCalculateMinimum() && quantitativeMeasure
                        .getCalculateMaximum() < getCalculateMaximum());
            }
        }
        return false;
    }

    default boolean isNotSpecified() {
        return getCalculateMinimum() == null || getCalculateMaximum() == null;

    }

}