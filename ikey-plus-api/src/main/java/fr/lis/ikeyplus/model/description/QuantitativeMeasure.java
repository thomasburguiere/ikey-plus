package fr.lis.ikeyplus.model.description;

/**
 * This class represents a quantitative measure
 *
 * @author Florian Causse
 */
public class QuantitativeMeasure {

    private Double max = null;
    private Double mean = null;
    private Double min = null;
    private Double sd = null;
    private Double uMethLower = null;
    private Double uMethUpper = null;

    private boolean minInclude = true;
    private boolean maxInclude = true;

    public Double getMax() {
        return max;
    }

    public void setMax(final Double max) {
        this.max = max;
    }

    public Double getMean() {
        return mean;
    }

    public void setMean(final Double mean) {
        this.mean = mean;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(final Double min) {
        this.min = min;
    }

    public Double getSD() {
        return sd;
    }

    public void setSD(final Double sd) {
        this.sd = sd;
    }

    public Double getUMethLower() {
        return uMethLower;
    }

    public void setUMethLower(final Double uMethLower) {
        this.uMethLower = uMethLower;
    }

    public Double getUMethUpper() {
        return uMethUpper;
    }

    public void setUMethUpper(final Double uMethUpper) {
        this.uMethUpper = uMethUpper;
    }

    public boolean isMinInclude() {
        return minInclude;
    }

    public void setMinInclude(final boolean minInclude) {
        this.minInclude = minInclude;
    }

    public boolean isMaxInclude() {
        return maxInclude;
    }

    public void setMaxInclude(final boolean maxInclude) {
        this.maxInclude = maxInclude;
    }

    @Override
    public String toString() {
        return "Min=" + min + "  Max=" + max + "  Mean=" + mean + "  SD=" + sd + "  UMethLower=" + uMethLower
                + "  UMethUpper=" + uMethUpper;
    }

    public String toStringInterval() {
        final String start;
        final String end;

        if (minInclude) {
            start = "[";
        } else {
            start = "]";
        }

        if (maxInclude) {
            end = "]";
        } else {
            end = "[";
        }
        return start + getCalculateMinimum() + ", " + getCalculateMaximum() + end;
    }

    public String toStringInterval(final String unit) {

        if (unit != null && !unit.isEmpty()) {
            return toStringInterval() + " (" + unit + ")";
        }
        return toStringInterval();
    }

    public Double getCalculateMinimum() {
        if (min != null) {
            return min;
        } else if (uMethLower != null) {
            return uMethLower;
        } else if (sd != null && mean != null) {
            return mean - 2 * sd;
        } else {
            return null;
        }
    }

    public Double getCalculateMaximum() {
        if (max != null) {
            return max;
        } else if (uMethUpper != null) {
            return uMethUpper;
        } else if (sd != null && mean != null) {
            return mean + 2 * sd;
        } else {
            return null;
        }
    }

    public boolean isInclude(final QuantitativeMeasure quantitativeMeasure) {

        if (quantitativeMeasure == null) {
            return true;
            // if both taxa are described
        } else if (!isNotSpecified() && !quantitativeMeasure.isNotSpecified()) {
            // if the max value of the current interval is included
            if (maxInclude) {
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

    public boolean isNotSpecified() {
        return getCalculateMinimum() == null || getCalculateMaximum() == null;

    }

}