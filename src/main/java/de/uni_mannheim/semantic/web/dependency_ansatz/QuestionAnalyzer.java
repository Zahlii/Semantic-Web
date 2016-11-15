package de.uni_mannheim.semantic.web.dependency_ansatz;


import de.uni_mannheim.semantic.web.stanford_nlp.QuestionType;
import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.text.TextHelper;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import de.uni_mannheim.semantic.web.stanford_nlp.model.Word;
import edu.stanford.nlp.semgraph.SemanticGraph;
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
    private String filterComparator = null; // if we want to filter, < > =
    private String limitAndOffset = null; // LIMIT x offset x
    private String filterValue = null; // > 250000

    private HashMap<String,LookupResult> allEntities;
    private HashMap<String,LookupResult> allCategories;


    private QuestionType type;

    public QuestionAnalyzer(String question) throws Exception {
        this.question = question;

        System.out.println("========================");
        System.out.println(question);

        this.sentence = new StanfordSentence(this.question);
        hardcodedHints();

        this.type = this.sentence.getType();



        //System.out.println(allEntities);

        this.extractOrdering();
    }

    private void hardcodedHints() {
        sentence.replace("U.S.","United States");
    }

    private void extractOrdering() {
        boolean found = false;

        orderProperty = null; // verb/adjective for which property we are looking
        sortOrder = null; // asc or desc
        filterComparator = null; // if we want to filter
        limitAndOffset = null;

        for(int i=0;i<sentence.length();i++) {
            Word w = sentence.getWord(i);
            String t = w.getText();
            boolean isFirst = i==0;
            boolean isLast = i>=sentence.length()-1;
            boolean isSecondLast = i>=sentence.length()-2;
            boolean isThirdLast = i>=sentence.length()-3;
            boolean isSame =  w.getText().equals("same");

            String tag = w.getPOSTag();
            if(tag.matches("(RB|JJ)(R|S)") && !TextHelper.isCapitalized(t)|| isSame) {
                found = true;

                Word wnext = !isLast ? sentence.getWord(i+1) : null;
                Word wnnext = !isSecondLast ? sentence.getWord(i+2) : null;
                Word wnnnext = !isThirdLast ? sentence.getWord(i+3) : null;
                Word wprev = !isFirst ? sentence.getWord(i-1) : null;


                if(tag.matches("(RB|JJ)R")) { // more, younger, blabla
                    if (t.equals("more")) {
                        filterComparator = ">";
                        sentence.replace("(more|than)\\s", "");
                        if(!isLast) {
                            orderProperty = wnext.getText(); // more episodes than ...
                            sentence.replace(orderProperty+"\\s", "");
                            if(orderProperty.equals("than")) {
                                if (!isSecondLast) {
                                    filterValue = Util.replaceNumbers(wnnext.getText()); // more than xxxx
                                    sentence.replace(wnnext.getText()+"\\s", "");
                                    if(!isThirdLast) {
                                        orderProperty = wnnnext.getText(); // more than xxx caves
                                        sentence.replace(orderProperty+"\\s", "");
                                    }
                                }
                            }
                        }
                    } else if (t.equals("less")) {
                        filterComparator = "<";
                        sentence.replace("(more|than)\\s", "");
                        if(!isLast) {
                            orderProperty = wnext.getText(); // more episodes than ...
                            sentence.replace(orderProperty+"\\s", "");
                            if(orderProperty.equals("than")) {
                                if (!isSecondLast) {
                                    filterValue = Util.replaceNumbers(wnnext.getText()); // more than xxxx
                                    sentence.replace(wnnext.getText()+"\\s", "");
                                    if(!isThirdLast) {
                                        orderProperty = wnnnext.getText(); // more than xxx caves
                                        sentence.replace(orderProperty+"\\s", "");
                                    }
                                }
                            }
                        }
                    } else if (t.equals("same")) {
                        filterComparator = "=";
                        sentence.replace("(same|as)\\s", "");
                        if(!isLast) {
                            orderProperty = wnext.getText();
                            sentence.replace(orderProperty+"\\s", "");
                        }
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
                                    limitAndOffset = "LIMIT 1";
                                    break;
                                case "second":
                                    limitAndOffset = "LIMIT 1 OFFSET 1";
                                    break;
                                case "third":
                                    limitAndOffset = "LIMIT 1 OFFSET 2";
                                    break;
                                case "fifth":
                                    limitAndOffset = "LIMIT 1 OFFSET 4";
                                    break;
                                default:
                                    String q = t1.replaceAll("th","");
                                    int n = Integer.parseInt(Util.replaceNumbers(q),10);
                                    limitAndOffset = "LIMIT 1 OFFSET "+(n-1);
                                    break;
                            }
                        }
                    }

                    if (t.equals("most")) {
                        sortOrder = "DESC";
                        sentence.replace("(most)\\s", "");
                        if(!isLast) {
                            orderProperty = wnext.getText(); // more episodes than ...
                            sentence.replace(orderProperty+"\\s", "");
                        }
                    } else if (t.equals("least")) {
                        sortOrder = "ASC";
                        sentence.replace("(least)\\s", "");
                        if(!isLast) {
                            orderProperty = wnext.getText(); // more episodes than ...
                            sentence.replace(orderProperty+"\\s", "");
                        }
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
                        sortOrder = "ASC";
                        break;
                    case "youngest":
                        orderProperty = "birthDate";
                        sortOrder = "DESC";
                        break;
                    case "highest":
                        orderProperty = "height";
                        sortOrder = "DESC";
                        break;
                    case "tallest":
                        orderProperty = "height";
                        sortOrder = "DESC";
                        break;
                    case "smallest":
                        orderProperty = "height";
                        sortOrder = "DESC";
                        break;
                    case "latest":
                        orderProperty = "date";
                        sortOrder = "DESC";
                        break;
                    case "first":
                        orderProperty = "date";
                        sortOrder = "ASC";
                        break;
                }
            }

            System.out.println(this.getOrderByAndFilterClause());


        }

        this.findAndMergeEntitiesAndCategories();
    }

    private String getOrderByAndFilterClause() {
        StringBuilder b = new StringBuilder();

        if(orderProperty != null && sortOrder != null)
            b.append("ORDER BY ").append(sortOrder).append("(").append(orderProperty).append(")");

        if(limitAndOffset != null)
            b.append(limitAndOffset);

        if(filterComparator != null && filterValue != null && orderProperty != null) {
            b.append("FILTER(").append(orderProperty).append(filterComparator).append(filterValue).append(")");
        }

        return b.toString();
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
        //sentence.basicAnnotate();


        this.removeUnneededPOSTags();

        sentence.basicAnnotate();

        SemanticGraph g = this.sentence.getSemanticGraph();
        System.out.println(g.toString());
        System.out.println("Final cleaned text: " + sentence.getCleanedText());

    }

    private void removeUnneededPOSTags() {
        for(int i=0;i<sentence.length();i++) {
            Word w = sentence.getWord(i);

            String tag = w.getPOSTag();

            // TODO
            if(tag.matches("(DT|IN)"))
                sentence.replace(w.getText()+"\\s","");
        }
    }

    public static void main(String[] args) throws Exception {
        File q = new File(System.getProperty("user.dir") + "/data/questions.txt");
        String f = TextHelper.readFile(q.getAbsolutePath(), Charset.forName("UTF8"));

        String[] questions = f.split("\r\n");

        int i=0;
        for(String quest : questions) {
            if(i++>20) break;
            //new QuestionAnalyzer(quest);
        }

        new QuestionAnalyzer("Who is the Formula 1 race driver with the most races?");
        new QuestionAnalyzer("What is the second highest mountain on Earth?");
        new QuestionAnalyzer("Does Breaking Bad have more episodes than Game of Thrones?");
        new QuestionAnalyzer("Which U.S. states are in the same time zone as Utah?");
    }
}
