package utils;

/**
 * This class allow to manage error messages
 * 
 * @author Florian Causse
 * @created 07-jul.-2011
 * 
 */
public class IdentificationKeyErrorMessage {

	public IdentificationKeyErrorMessage(String msg) {
		Utils.setErrorMessage(msg);
	}

	public IdentificationKeyErrorMessage(String msg, Throwable t) {
		Utils.setErrorMessage(msg + ": " + t.getMessage());
	}
}
