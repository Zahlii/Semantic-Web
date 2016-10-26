package de.uni_mannheim.semantic.web.crawl.run_once;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.uni_mannheim.semantic.web.crawl.model.OntologyClass;
import de.uni_mannheim.semantic.web.crawl.model.Property;

public class OntologyClassCrawler {
	private static String classLink = "http://mappings.dbpedia.org/server/ontology/classes/";

	public static void main(String[] args) {
		ArrayList<OntologyClass> list = crawlClasses();
		System.out.println(list.get(0).getProperties().size());
		// ArrayList<Property> list = crawlProperties(classLink+"owl%3AThing");
		// for(int i=0; i<list.size(); i++){
		// System.out.println(list.getWord(i).getName());
		// System.out.println(list.getWord(i).getLabel());
		// System.out.println(list.getWord(i).getDomain());
		// System.out.println(list.getWord(i).getRange());
		// System.out.println(list.getWord(i).getDescription());
		// System.out.println();
		//
		// }
	}

	/**
	 * returns a list with all Ontology Classes and their properties
	 * 
	 * @return
	 */
	public static ArrayList<OntologyClass> crawlClasses() {
		ArrayList<OntologyClass> oClasses = new ArrayList<OntologyClass>();
		Document doc;
		try {
			doc = Jsoup.connect(classLink).get();
			Elements classes = doc.select("a");
			int all = classes.size() / 3;
			int done = 1;

			for (int i = 0; i < classes.size(); i++) {
				// for(int i=0; i<10; i++){
				String name = classes.get(i).attr("name");
				if (!name.equals("")) {
					i++;
					String link = classLink + classes.get(i).attr("href");
					OntologyClass oc = new OntologyClass(name, link);
					oc = crawlProperties(oc);
					oClasses.add(oc);
					i++;

					String tmp = "Classes crawled: " + done + "/" + all;
					System.out.println(tmp);
					done++;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return oClasses;
	}

	/**
	 * crawls all properties of a class
	 * 
	 * @param propLink
	 * @return list with all properties of a class
	 */
	private static OntologyClass crawlProperties(OntologyClass oc) {
		ArrayList<Property> properties = new ArrayList<Property>();
		Document doc;
		try {
			doc = Jsoup.connect(oc.getLink()).get();
			Elements desc = doc.select("table").get(0).select("tr");
			String superclass;
			try {
				superclass = classLink + desc.get(desc.size() - 1).select("td").get(1).select("a").attr("href");
			} catch (Exception e) {
				superclass = "";
			}

			Elements rows = doc.select("table").get(1).select("tr");

			for (int i = 0; i < rows.size(); i++) {
				if (i > 1) {
					Elements fields = rows.get(i).select("td");
					String name = "";
					String label = "";
					String domain = "";
					String range = "";
					String description = "";

					for (int j = 0; j < fields.size(); j++) {
						Element f = fields.get(j);
						switch (j) {
						case 0: {
							name = f.text().replaceAll(" \\(edit\\)", "");
							break;
						}
						case 1: {
							label = f.text().replaceAll("'", "");
							break;
						}
						case 2: {
							domain = classLink + f.select("a").attr("href");
							break;
						}
						case 3: {
							String tmp = f.select("a").attr("href");
							if (tmp.equals("")) {
								range = f.select("em").text();
							} else {
								range = classLink + tmp;
							}
							break;
						}
						case 4: {
							description = f.text();
							break;
						}
						}
					}
					Property p = new Property(name, oc.getLink(), label, domain, range, description);
					properties.add(p);
				}
			}
			oc.setSuperclass(superclass);
			oc.setProperties(properties);

		} catch (Exception e) {

		}
		return oc;
	}
}
