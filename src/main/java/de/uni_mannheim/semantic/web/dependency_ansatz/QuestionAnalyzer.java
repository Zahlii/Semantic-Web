package de.uni_mannheim.semantic.web.dependency_ansatz;


import de.uni_mannheim.semantic.web.stanford_nlp.QuestionType;
import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.text.TextHelper;
import de.uni_mannheim.semantic.web.stanford_nlp.model.Word;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;


public class QuestionAnalyzer {
    private String question;
    private StanfordSentence sentence;

    private QuestionType type;

    public QuestionAnalyzer(String question) throws Exception {
        this.question = question;
        System.out.println("========================");
        this.sentence = new StanfordSentence(this.question);
        this.type = this.sentence.getType();

        this.extractOrdering();
    }

    private void extractOrdering() {
        boolean found = false;

        String orderProperty = null; // verb/adjective for which property we are looking
        String orderType = null; // asc or desc
        String comparator = null; // if we want to filter

        for(Word w : sentence.getAdjectives()) {
            if(w.getPOSTag().matches("JJ(R|S)")) {
                found = true;

                String t = w.getText();
                if(t == "more") {
                    comparator = ">";
                    sentence.replace("(more|than)","");
                } else if(t == "less") {
                    comparator = "<";
                    sentence.replace("(less|than)","");
                } else if(t == "same"){
                    comparator = "=";
                    sentence.replace("(same|as)","");
                } else {
                    orderProperty = w.getStem();
                    sentence.replace(t,"");
                }
            }
        }

        if(found) {
            System.out.println(sentence.getCleanedText());
            sentence.basicAnnotate();
        }
    }

    public static void main(String[] args) throws Exception {
        File q = new File(System.getProperty("user.dir") + "/data/questions.txt");
        String f = TextHelper.readFile(q.getAbsolutePath(), Charset.forName("UTF8"));

        /*String[] questions = f.split("\r\n");

        for(String quest : questions) {
            new QuestionAnalyzer(quest);
        }*/

        new QuestionAnalyzer("Does Breaking Bad have more episodes than Game of Thrones?");
    }
}
