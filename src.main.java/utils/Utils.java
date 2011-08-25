package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	public static String errorMessageFile = null;
	public static ResourceBundle bundle = ResourceBundle.getBundle("conf");
	public static String TXT = "txt";
	public static String HTML = "html";
	public static String PDF = "pdf";
	public static String SDD = "sdd";
	public static String WIKI = "wiki";
	public static String ERROR = "error";

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
	 * Getter for configuration elements or messages
	 * 
	 * @return String the element corresponding to the key
	 */
	public static String getBundleElement(String key) {
		return Utils.bundle.getString(key);
	}

	/**
	 * @param msg
	 * @return String, the error file name
	 */
	public static String setErrorMessage(String msg) {
		if (Utils.errorMessage == null) {
			Utils.errorMessage = msg;
			Utils.errorMessageFile = createErrorFile();
		}
		return Utils.errorMessageFile;
	}

	/**
	 * @param msg
	 *            , the readable message
	 * @param t
	 *            , the exception
	 * @return String, the error file name
	 */
	public static String setErrorMessage(String msg, Throwable t) {
		if (Utils.errorMessage == null) {
			Utils.errorMessage = msg + ": " + t.getMessage();
			Utils.errorMessageFile = createErrorFile();
		}
		return Utils.errorMessageFile;
	}

	/**
	 * @return String, the url to the error file
	 */
	public static String createErrorFile() {
		String path = Utils.getBundleElement("generatedKeyFiles.prefix")
				+ Utils.getBundleElement("generatedKeyFiles.folder");

		File erroFile = null;
		try {
			erroFile = File.createTempFile("key_", "." + Utils.ERROR, new File(path));
			BufferedWriter txtFileWriter;
			txtFileWriter = new BufferedWriter(new FileWriter(erroFile));
			txtFileWriter.append(Utils.errorMessage);
			txtFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return erroFile.getName();
	}

	/**
	 * setter for ResourceBundle
	 * 
	 * @param bundle
	 */
	public static void setBundle(ResourceBundle bundle) {
		Utils.bundle = bundle;
	}

}
