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

    private boolean minInclude = true;
    private boolean maxInclude = true;

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getMean() {
        return mean;
    }

    public void setMean(Double mean) {
        this.mean = mean;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getSD() {
        return sd;
    }

    public void setSD(Double sd) {
        this.sd = sd;
    }

    public Double getUMethLower() {
        return uMethLower;
    }

    public void setUMethLower(Double uMethLower) {
        this.uMethLower = uMethLower;
    }

    public Double getUMethUpper() {
        return uMethUpper;
    }

    public void setUMethUpper(Double uMethUpper) {
        this.uMethUpper = uMethUpper;
    }

    public boolean isMinInclude() {
        return minInclude;
    }

    public void setMinInclude(boolean minInclude) {
        this.minInclude = minInclude;
    }

    public boolean isMaxInclude() {
        return maxInclude;
    }

    public void setMaxInclude(boolean maxInclude) {
        this.maxInclude = maxInclude;
    }

    @Override
    public String toString() {
        return "Min=" + min + "  Max=" + max + "  Mean=" + mean + "  SD=" + sd + "  UMethLower=" + uMethLower
                + "  UMethUpper=" + uMethUpper;
    }

    public String toStringInterval() {
        String start;
        String end;

        if (isMinInclude()) {
            start = "[";
        } else {
            start = "]";
        }

        if (isMaxInclude()) {
            end = "]";
        } else {
            end = "[";
        }
        return start + this.getCalculateMinimum() + ", " + this.getCalculateMaximum() + end;
    }

    public String toStringInterval(String unit) {

        if (unit != null && !unit.equals("")) {
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
        } else if (!this.isNotSpecified() && !quantitativeMeasure.isNotSpecified()) {
            // if the max value of the current interval is include
            if (this.maxInclude) {
                if ((quantitativeMeasure.getCalculateMinimum() >= this.getCalculateMinimum() && quantitativeMeasure.getCalculateMinimum() <= this
                        .getCalculateMaximum())
                        || (quantitativeMeasure.getCalculateMaximum() >= this
                        .getCalculateMinimum() && quantitativeMeasure
                        .getCalculateMaximum() <= this.getCalculateMaximum())) {
                    return true;
                }
            } else {
                if ((quantitativeMeasure.getCalculateMinimum() >= this.getCalculateMinimum() && quantitativeMeasure.getCalculateMinimum() < this
                        .getCalculateMaximum())
                        || (quantitativeMeasure.getCalculateMaximum() >= this
                        .getCalculateMinimum() && quantitativeMeasure
                        .getCalculateMaximum() < this.getCalculateMaximum())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isNotSpecified() {
        return this.getCalculateMinimum() == null || this.getCalculateMaximum() == null;

    }

}