package Domain;

public class Property {

	private String name;
	private String label;
	private String domain;
	private String range;
	private String description;
	private String ontologyClass;
	
	public Property(String name, String ontologyClass, String label, String domain, String range, String description){
		this.setName(name);
		this.setOntologyClass(ontologyClass);
		this.setLabel(label);
		this.setDomain(domain);
		this.setRange(range);
		this.setDescription(description);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOntologyClass() {
		return ontologyClass;
	}

	public void setOntologyClass(String ontologyClass) {
		this.ontologyClass = ontologyClass;
	}
}
