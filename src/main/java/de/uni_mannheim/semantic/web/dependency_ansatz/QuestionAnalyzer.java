package de.uni_mannheim.semantic.web.dependency_ansatz;


import de.uni_mannheim.semantic.web.stanford_nlp.QuestionType;
import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.text.TextHelper;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import de.uni_mannheim.semantic.web.stanford_nlp.model.Word;
import utils.Util;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class QuestionAnalyzer {
    private String question;
    private StanfordSentence sentence;
    private String orderProperty = null; // verb/adjective for which property we are looking
    private String sortOrder = null; // asc or desc
    private String filterComparator = null; // if we want to filter
    private String limitAndOffset = null;

    private HashMap<String,LookupResult> allEntities;
    private HashMap<String,LookupResult> allCategories;

    private QuestionType type;

    public QuestionAnalyzer(String question) throws Exception {
        this.question = question;

        System.out.println("========================");
        System.out.println(question);

        this.sentence = new StanfordSentence(this.question);
        this.type = this.sentence.getType();



        //System.out.println(allEntities);

        this.extractOrdering();
    }

    private void extractOrdering() {
        boolean found = false;

        String orderProperty = null; // verb/adjective for which property we are looking
        String orderType = null; // asc or desc
        String comparator = null; // if we want to filter
        String quantifier = null;

        for(int i=0;i<sentence.length();i++) {
            Word w = sentence.getWord(i);
            boolean isFirst = i==0;
            boolean isLast = i==sentence.length()-1;

            String tag = w.getPOSTag();
            if(tag.matches("(RB|JJ)(R|S)")) {
                found = true;

                Word wnext = !isLast ? sentence.getWord(i+1) : null;
                Word wprev = !isFirst ? sentence.getWord(i-1) : null;

                String t = w.getText();
                if(tag.matches("(RB|JJ)R")) { // more, younger, blabla
                    if (t.equals("more")) {
                        comparator = ">";
                        sentence.replace("(more|than)\\s", "");
                        if(!isLast)
                            orderProperty = wnext.getText();
                    } else if (t.equals("less")) {
                        comparator = "<";
                        sentence.replace("(less|than)\\s", "");
                        if(!isLast)
                            orderProperty = wnext.getText();
                    } else if (t.equals("same")) {
                        comparator = "=";
                        sentence.replace("(same|as)\\s", "");
                        if(!isLast)
                            orderProperty = wnext.getText();
                    } else {
                        orderProperty = t;
                        sentence.replace(t + "\\s", "");
                    }
                } else if(tag.matches("(RB|JJ)S")) { // most, youngest, ...
                    if(!isFirst) {
                        String t1 = wprev.getText();

                        sentence.replace(t1+"\\s","");
                        // second, third, ...
                        if(wprev.getPOSTag().equals("JJ")) {
                            switch(t1) {
                                case "first":
                                    quantifier = "LIMIT 1";
                                    break;
                                case "second":
                                    quantifier = "LIMIT 1 OFFSET 1";
                                    break;
                                case "third":
                                    quantifier = "LIMIT 1 OFFSET 2";
                                    break;
                                case "fifth":
                                    quantifier = "LIMIT 1 OFFSET 4";
                                    break;
                                default:
                                    String q = t1.replaceAll("th","");
                                    int n = Integer.parseInt(Util.replaceNumbers(q),10);
                                    quantifier = "LIMIT 1 OFFSET "+(n-1);
                                    break;
                            }
                        }
                    }

                    if (t.equals("most")) {
                        orderType = "DESC";
                        sentence.replace("(most)\\s", "");
                        if(!isLast)
                            orderProperty = wnext.getText();
                    } else if (t.equals("least")) {
                        orderType = "ASC";
                        sentence.replace("(least)\\s", "");
                        if(!isLast)
                            orderProperty = wnext.getText();
                    }  else {
                        orderProperty = t;
                        sentence.replace(t + "\\s", "");
                    }
                }
            }
        }

        if(found) {
            if(orderProperty != null) {
                switch (orderProperty) {
                    case "oldest":
                        orderProperty = "birthDate";
                        orderType = "ASC";
                        break;
                    case "youngest":
                        orderProperty = "birthDate";
                        orderType = "DESC";
                        break;
                    case "highest":
                        orderProperty = "height";
                        orderType = "DESC";
                        break;
                    case "tallest":
                        orderProperty = "height";
                        orderType = "DESC";
                        break;
                    case "smallest":
                        orderProperty = "height";
                        orderType = "DESC";
                        break;
                    case "latest":
                        orderProperty = "date";
                        orderType = "DESC";
                        break;
                    case "first":
                        orderProperty = "date";
                        orderType = "ASC";
                        break;
                }
            }
            System.out.println(orderProperty +" | "+orderType + " | "+ comparator + " | "+quantifier);


        }

        this.findAndMergeEntitiesAndCategories();
    }

    private void findAndMergeEntitiesAndCategories() {
        this.sentence.findEntities();
        allEntities = this.sentence.dbpediaResource.getResults();

        //allEntities = new HashMap<>();

        this.sentence.findCategory();
        allCategories = this.sentence.dbpediaCategory.getResults();

        List<LookupResult> allresults = new ArrayList<LookupResult>(allEntities.size()+ allCategories.size());
        allresults.addAll(allEntities.values());
        allresults.addAll(allCategories.values());
        allresults.sort(this.sentence.dbpediaResource.lookupResultComparator);

        for(LookupResult now : allresults) {
            String s = now.getSearchedTitle();
            this.sentence.replace(s,now.getVarName());
        }
        sentence.basicAnnotate();

        System.out.println("Final cleaned text: " + sentence.getCleanedText());


        this.removeUnneededPOSTags();
    }

    private void removeUnneededPOSTags() {
        for(int i=0;i<sentence.length();i++) {
            Word w = sentence.getWord(i);

            String tag = w.getPOSTag();

            // TODO
            if(tag.matches("DET"))
                sentence.replace(w.getText()+"\\s","");
        }
    }

    public static void main(String[] args) throws Exception {
        File q = new File(System.getProperty("user.dir") + "/data/questions.txt");
        String f = TextHelper.readFile(q.getAbsolutePath(), Charset.forName("UTF8"));

        String[] questions = f.split("\r\n");

        /*for(String quest : questions) {
            new QuestionAnalyzer(quest);
        }*/

        new QuestionAnalyzer("Who is the Formula 1 race driver with the most races?");
        new QuestionAnalyzer("What is the second highest mountain on Earth?");
        new QuestionAnalyzer("Does Breaking Bad have more episodes than Game of Thrones?");
    }
}
