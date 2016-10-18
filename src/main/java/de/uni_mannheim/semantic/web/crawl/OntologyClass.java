package de.uni_mannheim.semantic.web.crawl;

import java.util.ArrayList;

public class OntologyClass {
	private String name;
	private String link;
	private ArrayList<Property> properties;
	private String superclass;

	public OntologyClass(String name, String link) {
		this.setName(name);
		this.setLink(link);
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

	public String getSuperclass() {
		return superclass;
	}

	public void setSuperclass(String superclass) {
		this.superclass = superclass;
	}
}
