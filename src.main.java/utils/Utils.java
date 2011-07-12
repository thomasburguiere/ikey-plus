package utils;

/**
 * This class allow to manage error messages
 * 
 * @author Florian Causse
 * @created 07-04-2011
 * 
 */
public class Utils {

	public static String errorMessage = null;

	/**
	 * Convert a String value to a Double value
	 * 
	 * @param String
	 *            , the String to convert
	 * @return Double, the Double value
	 */
	public static Double convertStringToDouble(String str) {

		try {
			Double doubleValue = new Double(Double.parseDouble(str));
			return doubleValue;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Init the errorMessage attribute
	 * 
	 * @param msg
	 */
	public static void setErrorMessage(String msg) {
		if (errorMessage == null) {
			errorMessage = msg;
		}
	}
}
