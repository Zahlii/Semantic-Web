package de.uni_mannheim.semantic.web.crawl.model;

import java.util.ArrayList;

public class DBPediaResource {

	private String label;
	private String uri;
	private String description;
	private ArrayList<String> classUris;
	private ArrayList<String> categoryUris;
	
	public DBPediaResource(){
		
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public ArrayList<String> getClassUris() {
		return classUris;
	}
	public void setClassUris(ArrayList<String> classUris) {
		this.classUris = classUris;
	}
	public ArrayList<String> getCategoryUris() {
		return categoryUris;
	}
	public void setCategoryUris(ArrayList<String> categoryUris) {
		this.categoryUris = categoryUris;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("Label: "+label + "\n");
		strBuilder.append("URI: "+uri + "\n");
		strBuilder.append("Description: "+description + "\n");
		
		String c = "Class URIs: ["; 
		for(int i=0; i<classUris.size(); i++){
			if(i==0){
				c+=classUris.get(i);
			}else{
				c+=", " + classUris.get(i);
			}
		}
		c += "]\n";

		strBuilder.append(c);
		String ca =  "Category URIs: ["; 
		for(int i=0; i<categoryUris.size(); i++){
			if(i==0){
				ca+=categoryUris.get(i);
			}else{
				ca+=", " + categoryUris.get(i);
			}
		}
		ca += "]";
		strBuilder.append(ca);
		return strBuilder.toString();
	}
}
