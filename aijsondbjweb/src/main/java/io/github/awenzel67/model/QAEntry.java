package io.github.awenzel67.model;

import java.io.Serializable;

public class QAEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String question;
    private String answer;
    
    public QAEntry() {
    }
    
    public QAEntry(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
