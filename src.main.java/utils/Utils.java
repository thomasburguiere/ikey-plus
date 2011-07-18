package utils;

import java.util.ResourceBundle;

/**
 * This class allow to manage all external functionalities : error massage, properties...
 * 
 * @author Florian Causse
 * @created 07-04-2011
 * 
 */
public class Utils {

	public static String errorMessage = null;
	public static ResourceBundle bundle = ResourceBundle.getBundle("conf");

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

	/**
	 * getter for configuration elements
	 * 
	 * @return String the element corresponding to the key
	 */
	public static String getBundleElement(String key) {
		return Utils.bundle.getString(key);
	}

}
