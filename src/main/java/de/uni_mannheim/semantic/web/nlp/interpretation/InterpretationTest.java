package de.uni_mannheim.semantic.web.nlp.interpretation;

import java.util.ArrayList;

import de.uni_mannheim.semantic.web.crawl.OntologyClass;
import de.uni_mannheim.semantic.web.crawl.Property;
import de.uni_mannheim.semantic.web.info.DBPedia;
import de.uni_mannheim.semantic.web.info.DBPedia_Terms;
import de.uni_mannheim.semantic.web.nlp.Sentence;

public class InterpretationTest extends DBResourceInterpretation {

	public InterpretationTest(Sentence s) {
		super(s);
		checkForTokenRelations();
		// TODO Auto-generated constructor stub
	}

	private void checkForTokenRelations() {
		for (int i = 0; i < _mainNGram.size(); i++) {
			if (_mainNGram.get(i).getResource().equals(""))
				continue;

			ArrayList<Property> properties = new ArrayList<>();
			ArrayList<OntologyClass> classes = new ArrayList<>();

			// Get all classes
			for (int j = 0; j < _mainNGram.size(); j++) {
				if (j == i)
					continue;
				classes.addAll(DBPedia_Terms.getOntologyClassByName(_mainNGram.get(j).getText()));
			}

			// Get all properties
			for (int j = 0; j < _mainNGram.size(); j++) {
				if (j == i)
					continue;
				properties.addAll(DBPedia_Terms.getOntologyPropertyByName(_mainNGram.get(j).getText()));
			}

			// Check for relations between classes
			for (int j = 0; j < classes.size(); j++) {
				ArrayList<String> relations = new ArrayList<>();

				relations.addAll(DBPedia.checkClassRelationExists(_mainNGram.get(i).getResource(), classes.get(j)));

				for (int k = 0; k < relations.size(); k++) {
					System.out.println(_mainNGram.get(i).getText() + " " + relations.get(k) + " " + classes.get(j));
				}
			}

			// Check for properties
			for (int prop = 0; prop < properties.size(); prop++) {
				ArrayList<String> props = new ArrayList<>();

				props.addAll(DBPedia.checkPropertyExists(_mainNGram.get(i).getResource(), properties.get(prop)));

				for (int k = 0; k < props.size(); k++) {
					System.out.println(
							_mainNGram.get(i).getText() + " " + properties.get(prop).getLabel() + " " + props.get(k));
				}
			}

			// if(_mainNGram.get(i).getResource() == null) continue;
			// DBPedia.scanForDboClasses(_mainNGram.get(i).getResource());
		}
	}

	// private void scanForDboClasses(){
	// for (int i = 0; i < _mainNGram.size(); i++) {
	//// if(_mainNGram.get(i).getResource() == null) continue;
	// DBPedia_Terms.getOntologyClassByName(_mainNGram.get(i).getText());
	//// DBPedia.scanForDboClasses(_mainNGram.get(i).getResource());
	// }
	// }

	// private void scanForDboProperties(){
	// for (int i = 0; i < _mainNGram.size(); i++) {
	// ArrayList<Property> properties = new ArrayList<>();
	//
	// for (int j = 0; j < _mainNGram.size(); j++) {
	// if(j == i) continue;
	// properties.addAll(DBPedia_Terms.getOntologyPropertyByName(_mainNGram.get(i).getText()));
	// }
	//
	// for (int prop = 0; prop < properties.size(); prop++) {
	// DBPedia.checkPropertyExists(obj, prop);
	// }
	//// if(_mainNGram.get(i).getResource() == null) continue;
	//// DBPedia.scanForDboClasses(_mainNGram.get(i).getResource());
	// }
	// }

}
