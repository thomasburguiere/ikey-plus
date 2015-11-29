package fr.lis.ikeyplus.model;

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

    private final boolean minInclude = true;
    private boolean maxInclude = true;

    public void setMax(Double max) {
        this.max = max;
    }

    public void setMean(Double mean) {
        this.mean = mean;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public void setSD(Double sd) {
        this.sd = sd;
    }

    public void setUMethLower(Double uMethLower) {
        this.uMethLower = uMethLower;
    }

    public void setUMethUpper(Double uMethUpper) {
        this.uMethUpper = uMethUpper;
    }

    public void setMaxInclude(boolean maxInclude) {
        this.maxInclude = maxInclude;
    }

    @Override
    public String toString() {
        return "Min=" + min + "  Max=" + max + "  Mean=" + mean + "  SD=" + sd + "  UMethLower=" + uMethLower
                + "  UMethUpper=" + uMethUpper;
    }

    private String toStringInterval() {
        String start;
        String end;

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

    @SuppressWarnings("LiteralAsArgToStringEquals")
    public String toStringInterval(String unit) {

        if (unit != null && !"".equals(unit)) {
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

    public boolean isInclude(QuantitativeMeasure quantitativeMeasure) {

        if (quantitativeMeasure == null) {
            return true;
            // if both taxa are described
        } else if (!isNotSpecified() && !quantitativeMeasure.isNotSpecified()) {
            // if the max value of the current interval is include
            if (maxInclude) {
                if ((quantitativeMeasure.getCalculateMinimum() >= getCalculateMinimum() && quantitativeMeasure.getCalculateMinimum() <= getCalculateMaximum())
                        || (quantitativeMeasure.getCalculateMaximum() >= getCalculateMinimum() && quantitativeMeasure
                        .getCalculateMaximum() <= getCalculateMaximum())) {
                    return true;
                }
            } else {
                if ((quantitativeMeasure.getCalculateMinimum() >= getCalculateMinimum() && quantitativeMeasure.getCalculateMinimum() < getCalculateMaximum())
                        || (quantitativeMeasure.getCalculateMaximum() >= getCalculateMinimum() && quantitativeMeasure
                        .getCalculateMaximum() < getCalculateMaximum())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isNotSpecified() {
        return getCalculateMinimum() == null || getCalculateMaximum() == null;

    }

}