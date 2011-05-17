package model;

/**
 * @author Florian Causse
 * @created 06-avr.-2011
 */
public class State {

	private String id = null;
	private String name = null;

	/**
	 * constructor
	 */
	public State() {
		this(null);
	}

	/**
	 * constructor with name param
	 * 
	 * @param String
	 *            , the name
	 */
	public State(String name) {
		super();
		this.name = name;
	}

	/**
	 * get the name
	 * 
	 * @return String, the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * set the name
	 * 
	 * @param String
	 *            , the name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * get the state identifier
	 * 
	 * @return String , the state identifier
	 */
	public String getId() {
		return id;
	}

	/**
	 * set the state identifier
	 * 
	 * @param String
	 *            , the state identifier
	 */
	public void setId(String id) {
		this.id = id;
	}
}