package model;

/**
 * @author Florian Causse
 * @created 06-avr.-2011
 */
public class Taxon {

	private String name = null;

	/**
	 * constructor
	 */
	public Taxon() {
		this(null);
	}

	/**
	 * constructor with name param
	 * 
	 * @param String
	 *            , the name
	 */
	public Taxon(String name) {
		super();
		this.name = name;
	}

	/**
	 * get the taxon name
	 * 
	 * @return String, the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * set the taxon name
	 * 
	 * @param String
	 *            , the name
	 */
	public void setName(String name) {
		this.name = name;
	}

}