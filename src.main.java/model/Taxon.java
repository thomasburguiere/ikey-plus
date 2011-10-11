package model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a taxon
 * 
 * @author Florian Causse
 * @created 06-04-2011
 */
public class Taxon {

	private String id;
	private String name = null;
	private List<String> mediaObjectKeys = null;

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
		mediaObjectKeys = new ArrayList<String>();
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
		if (mediaObjectKeys.get(0) != null) {
			return mediaObjectKeys.get(0);
		}
		return null;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}