package Domain;

import java.util.ArrayList;

public class OntologyClass {
	private String name;
	private String link;
	private ArrayList<Property> properties;
	
	public OntologyClass(String name, String link, ArrayList<Property> properties){
		this.setName(name);
		this.setLink(link);
		this.setProperties(properties);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public ArrayList<Property> getProperties() {
		return properties;
	}

	public void setProperties(ArrayList<Property> properties) {
		this.properties = properties;
	}
}
