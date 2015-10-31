package fr.lis.ikeyplus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a state of categorical character
 * 
 * @author Florian Causse
 * @created 06-04-2011
 */
public class State {

	private String id = null;
	private String name = null;
	private List<String> mediaObjectKeys = null;

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
		this.mediaObjectKeys = new ArrayList<String>();
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

	/**
	 * get the key list of mediaObject
	 * 
	 * @return List<String>, the key list of mediaObject
	 */
	public List<String> getMediaObjectKeys() {
		return mediaObjectKeys;
	}

	/**
	 * set the key list of mediaObject
	 * 
	 * @param mediaObjectKeys
	 *            , the key list of mediaObject
	 */
	public void setMediaObjectKeys(List<String> mediaObjectKeys) {
		this.mediaObjectKeys = mediaObjectKeys;
	}

	/**
	 * get the first image
	 * 
	 * @return String, the URL to the image
	 */
	public String getFirstImage(DataSet dataSet) {
		if (dataSet != null && mediaObjectKeys != null && mediaObjectKeys.size() > 0) {
			if (dataSet.getMediaObject(mediaObjectKeys.get(0)).startsWith("http")) {
				return dataSet.getMediaObject(mediaObjectKeys.get(0));
			}
		}
		return null;
	}

	/**
	 * get the first image key
	 * 
	 * @return String, the key
	 */
	public String getFirstImageKey() {
		if (mediaObjectKeys.size() > 0) {
			return mediaObjectKeys.get(0);
		}
		return null;
	}

	/**
	 * get the string representation
	 * 
	 * @return String, the name
	 */
	public String toString() {
		return name;
	}
}