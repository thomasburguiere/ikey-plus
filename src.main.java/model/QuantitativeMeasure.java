package model;

/**
 * This class represents a quantitative measure
 * 
 * @author Florian Causse
 * @created 06-04-2011
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

	/**
	 * constructor
	 */
	public QuantitativeMeasure() {

	}

	/**
	 * get the maximum value
	 * 
	 * @return Double, the maximum value
	 */
	public Double getMax() {
		return max;
	}

	/**
	 * set the maximum value
	 * 
	 * @param Double
	 *            , the maximum value
	 */
	public void setMax(Double max) {
		this.max = max;
	}

	/**
	 * get the mean value
	 * 
	 * @return Double, the mean value
	 */
	public Double getMean() {
		return mean;
	}

	/**
	 * set the mean value
	 * 
	 * @param Double
	 *            , the mean value
	 */
	public void setMean(Double mean) {
		this.mean = mean;
	}

	/**
	 * get the minimum value
	 * 
	 * @return Double, the minimum value
	 */
	public Double getMin() {
		return min;
	}

	/**
	 * set the minimum value
	 * 
	 * @param Double
	 *            , the minimum value
	 */
	public void setMin(Double min) {
		this.min = min;
	}

	/**
	 * get the standard deviation value
	 * 
	 * @return Double, the standard deviation value
	 */
	public Double getSD() {
		return sd;
	}

	/**
	 * set the standard deviation value
	 * 
	 * @param Double
	 *            , the standard deviation value
	 */
	public void setSD(Double sd) {
		this.sd = sd;
	}

	/**
	 * get the normal lower value
	 * 
	 * @return Double, the normal lower value
	 */
	public Double getUMethLower() {
		return uMethLower;
	}

	/**
	 * set the normal lower value
	 * 
	 * @param Double
	 *            , the normal lower value
	 */
	public void setUMethLower(Double uMethLower) {
		this.uMethLower = uMethLower;
	}

	/**
	 * get the normal upper value
	 * 
	 * @return Double, the normal upper value
	 */
	public Double getUMethUpper() {
		return uMethUpper;
	}

	/**
	 * set the normal upper value
	 * 
	 * @param Double
	 *            , the normal upper value
	 */
	public void setUMethUpper(Double uMethUpper) {
		this.uMethUpper = uMethUpper;
	}

	/**
	 * @return boolean, true if Min value is include in the interval
	 */
	public boolean isMinInclude() {
		return minInclude;
	}

	/**
	 * @param minInclud
	 */
	public void setMinInclude(boolean minInclude) {
		this.minInclude = minInclude;
	}

	/**
	 * @return boolean, true if Max value is include in the interval
	 */
	public boolean isMaxInclude() {
		return maxInclude;
	}

	/**
	 * @param maxInclud
	 */
	public void setMaxInclude(boolean maxInclude) {
		this.maxInclude = maxInclude;
	}

	/**
	 * get the string representation
	 * 
	 * @return String, the string representation
	 */
	@Override
	public String toString() {
		return "Min=" + min + "  Max=" + max + "  Mean=" + mean + "  SD=" + sd + "  UMethLower=" + uMethLower
				+ "  UMethUpper=" + uMethUpper;
	}

	/**
	 * get the string representation as interval
	 * 
	 * @return String, the string representation
	 */
	public String toStringInterval() {
		String start = null;
		String end = null;

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

	/**
	 * calculate the minimum value
	 * 
	 * @return Double, the minimum value
	 */
	public Double getCalculateMinimum() {
		if (min != null) {
			return min;
		} else if (uMethLower != null) {
			return uMethLower;
		} else if (sd != null && mean != null) {
			return new Double(mean.doubleValue() - 2 * sd.doubleValue());
		} else {
			return null;
		}
	}

	/**
	 * calculate the maximum value
	 * 
	 * @return Double, the maximum value
	 */
	public Double getCalculateMaximum() {
		if (max != null) {
			return new Double(max.doubleValue());
		} else if (uMethUpper != null) {
			return new Double(uMethUpper.doubleValue());
		} else if (sd != null && mean != null) {
			return new Double(mean.doubleValue() + 2 * sd.doubleValue());
		} else {
			return null;
		}
	}

	/**
	 * @param quantitativeMeasure
	 * @return boolean, true if quantitativeMeasure is include in the current quantitativeMeasure
	 */
	public boolean isInclude(QuantitativeMeasure quantitativeMeasure) {
		if (quantitativeMeasure == null
				|| quantitativeMeasure.getCalculateMinimum() == null
				|| quantitativeMeasure.getCalculateMaximum() == null
				|| this.getCalculateMinimum() == null
				|| this.getCalculateMaximum() == null
				|| (quantitativeMeasure.getCalculateMinimum().doubleValue() >= this.getCalculateMinimum()
						.doubleValue() && quantitativeMeasure.getCalculateMinimum().doubleValue() <= this
						.getCalculateMaximum().doubleValue())
				|| (quantitativeMeasure.getCalculateMaximum().doubleValue() >= this.getCalculateMinimum()
						.doubleValue() && quantitativeMeasure.getCalculateMaximum().doubleValue() <= this
						.getCalculateMaximum().doubleValue())) {
			return true;
		}
		return false;
	}

}