package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * This class allow to manage all external functionalities : error massage, properties...
 * 
 * @author Florian Causse
 * @created 07-04-2011
 * 
 */
public class Utils {

	public static String errorMessage = null;
	public static File errorMessageFile = null;
	public static String UNKNOWNDATA = "unknownData";

	// properties file
	public static ResourceBundle bundleConf = ResourceBundle.getBundle("conf");
	public static ResourceBundle bundleConfOverridable = ResourceBundle.getBundle("confOverridable");

	// file prefix
	public static String KEY = "key_";
	public static String ERROR = "error_";

	// file extension
	public static String TXT = "txt";
	public static String HTML = "html";
	public static String PDF = "pdf";
	public static String SDD = "sdd";
	public static String WIKI = "wiki";
	public static String DOT = "gv";

	// representation type
	public static String TREE = "tree";
	public static String FLAT = "flat";

	// options
	public static boolean twoStatesCharacterFirst = false;
	public static boolean mergeCharacterStatesIfSameDiscimination = false;
	public static boolean reduceSameConclusionPath = false;
	public static boolean pruning = false;

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
	public static String getBundleConfElement(String key) {
		return Utils.bundleConf.getString(key);
	}
	
	/**
	 * Getter for overridable configuration elements or messages
	 * 
	 * @return String the element corresponding to the key
	 */
	public static String getBundleConfOverridableElement(String key) {
		return Utils.bundleConfOverridable.getString(key);
	}
	
	/**
	 * setter for configuration ResourceBundle
	 * 
	 * @param bundle
	 */
	public static void setBundleConf(ResourceBundle bundleConf) {
		Utils.bundleConf = bundleConf;
	}
	
	/**
	 * setter for overridable configuration ResourceBundle
	 * 
	 * @param bundle
	 */
	public static void setBundleConfOverridable(ResourceBundle bundleConfOverridable) {
		Utils.bundleConfOverridable = bundleConfOverridable;
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
		return Utils.errorMessageFile.getName();
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
		return Utils.errorMessageFile.getName();
	}

	/**
	 * @return String, the url to the error file
	 */
	public static File createErrorFile() {
		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");

		File erroFile = null;
		try {
			erroFile = File.createTempFile(Utils.ERROR, "." + Utils.TXT, new File(path));
			BufferedWriter txtFileWriter;
			txtFileWriter = new BufferedWriter(new FileWriter(erroFile));
			txtFileWriter.append(Utils.errorMessage);
			txtFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return erroFile;
	}

	/**
	 * This method returns the intersection of two Lists
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static List<?> intersection(List<?> list1, List<?> list2) {
		List<Object> list = new ArrayList<Object>();
		for (Object o : list1) {
			if (list2.contains(o))
				list.add(o);
		}
		return list;
	}

	/**
	 * This method returns a list containing the elements of a primary list that do not appear in a list of
	 * excluded elements
	 * 
	 * @param primaryList
	 *            the list which elements are to be retained
	 * @param excludedList
	 *            the list which elements shall not remain in the final list
	 * @return
	 */
	public static List<?> exclusion(List<?> primaryList, List<?> excludedList) {
		List<Object> list = new ArrayList<Object>();
		for (Object o : primaryList) {
			if (!excludedList.contains(o))
				list.add(o);
		}
		return list;
	}

	/**
	 * This method returns the union of two Lists
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static List<?> union(List<?> list1, List<?> list2) {
		Set<Object> set = new HashSet<Object>();
		set.addAll(list1);
		set.addAll(list2);
		return new ArrayList<Object>(set);
	}

}
