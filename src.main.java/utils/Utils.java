package utils;

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
	 * @param msg
	 */
	public static void setErrorMessage(String msg) {
		if(errorMessage == null){
			errorMessage = msg;
		}
	}

	/**
	 * Add a message in errorMessage attribute
	 * @param message
	 */
	public static void addErrorMessage(String message) {
		errorMessage = errorMessage + System.getProperty("line.separator") + message;
	}
}
