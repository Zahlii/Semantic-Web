package de.uni_mannheim.semantic.web.v2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SentenceV2 {
	enum QuestionType {
		ONE,
		ORDERED,
		ALL,
		NUMBER_OF
	}

	class SortOrder {
		String Text;
		int limit;
		int offset;
		
		SortOrder() {
			
		}
	}

	public static void main(String[] args) {
	/*1,N or COUNT | TYPE
		Give me all | astronauts.  // ALL

	1,N or COUNT | TYPE | COUNTRY
		Give me all | astronauts | from Russia.
		Give me all | organizations | from Munich.

	1,N or COUNT | TYPE | PREDICATE | RESOURCE
		Give me the number of | people | who were born in | Vienna. // NUMBER_OF
		Give me all | films | that were directed by | Steven Spielberg.
		Give me the | person | who is the spouse of | Amanda Palmer. // ONE
		Give me the number of | people | who were the spouse of | Jane Fonda.

	1 | ORDER_BY_OFFSET_LIMIT | TYPE  // ORDERED
		Give me the | 10 highest | mountains.
		Give me the | 2nd highest | mountain | in Germany.
		Give me the | youngest | Formula One Racer.
		Give me the | 10 largest | Cities in Germany.

OPTIONAL
	[[1,N or COUNT | PREDICATE | RESOURCE // ONE
		Give me the | population of | Germany.
		Give me the | vice president of | John F. Kennedy]]*/
		
		SentenceV2 s1 = new SentenceV2("Give me the youngest Formula One Racer.");
		SentenceV2 s2 = new SentenceV2("Give me all astronauts from Russia.");		
		SentenceV2 s3 = new SentenceV2("Give me the person who is the spouse of Amanda Palmer.");
		
	}
	
	
	private String _text;
	private String _baseText;
	private QuestionType _type;
	private SortOrder order;
	
	public SentenceV2(String text) {
		this._text = text.substring(0,text.length()-1);
		
		
		parseSentence();
		
		System.out.println(_baseText);
		
		
	}
	private void parseSentence() {
		if(_text.startsWith("Give me all ")) {
			_type = QuestionType.ALL;
			_baseText = _text.replace("Give me all ", "");
		} else if(_text.startsWith("Give me the number of ")) {
			_type = QuestionType.NUMBER_OF;
			_baseText = _text.replace("Give me the number of ", "");
		} else if(_text.startsWith("Give me the ")) {
			_baseText = _text.replace("Give me the ", "");
			if(extractOrdering()) {
				_type = QuestionType.ORDERED;
			} else {
				_type = QuestionType.ONE;
			}
		}
	}
	
	private boolean extractOrdering() {
		final String valid = "(youngest|oldest|largest|smallest|highest|lowest|fastest|slowest)";
		final String regex = "((\\d+(st|nd|rd|th)\\s)|(\\d+\\s+))?" + valid;
		
		Pattern r = Pattern.compile(regex);
		Matcher m = r.matcher(_baseText);
		
		
		if(m.find()) {
			order = new SortOrder();
			
			_baseText = _baseText.replace(m.group(0),"").trim();
			String t = m.group(0).replaceAll("\\d+(st|nd|rd|th)", "").replaceAll("\\d+", "").replaceAll("\\s+", "");
			order.Text = t;
			
			if(m.group(1) == null) { // youngest
				order.limit = 1;
				order.offset = 0;
			} else if(m.group(2) == null) {// 10 youngest
				order.limit = Integer.parseInt(m.group(1).replaceAll("\\D+",""));
				order.offset = 0;
			} else {
				order.offset = Integer.parseInt(m.group(1).replaceAll("\\D+",""))-1;
				order.limit = 1;
			}

			return true;
		} else {
			return false;
		}
	}

}
