package com.cyrilsx.russiancases.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private LevelEntity level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GrammaticalCase grammaticalCase;

    @Column(nullable = false, length = 2000)
    private String promptFr;

    @Column(nullable = false, length = 2000)
    private String promptEn;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "question_options_fr", joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "option_index")
    @Column(name = "option_text", nullable = false, length = 500)
    private List<String> optionsFr = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "question_options_en", joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "option_index")
    @Column(name = "option_text", nullable = false, length = 500)
    private List<String> optionsEn = new ArrayList<>();

    @Column(nullable = false)
    private int correctIndex;

    @Column(nullable = false, length = 2000)
    private String hintFr;

    @Column(nullable = false, length = 2000)
    private String hintEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private RuleEntity rule;

    public Long getId() {
        return id;
    }

    public LevelEntity getLevel() {
        return level;
    }

    public void setLevel(LevelEntity level) {
        this.level = level;
    }

    public GrammaticalCase getGrammaticalCase() {
        return grammaticalCase;
    }

    public void setGrammaticalCase(GrammaticalCase grammaticalCase) {
        this.grammaticalCase = grammaticalCase;
    }

    public String getPromptFr() {
        return promptFr;
    }

    public void setPromptFr(String promptFr) {
        this.promptFr = promptFr;
    }

    public String getPromptEn() {
        return promptEn;
    }

    public void setPromptEn(String promptEn) {
        this.promptEn = promptEn;
    }

    public List<String> getOptionsFr() {
        return optionsFr;
    }

    public void setOptionsFr(List<String> optionsFr) {
        this.optionsFr = optionsFr;
    }

    public List<String> getOptionsEn() {
        return optionsEn;
    }

    public void setOptionsEn(List<String> optionsEn) {
        this.optionsEn = optionsEn;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public void setCorrectIndex(int correctIndex) {
        this.correctIndex = correctIndex;
    }

    public String getHintFr() {
        return hintFr;
    }

    public void setHintFr(String hintFr) {
        this.hintFr = hintFr;
    }

    public String getHintEn() {
        return hintEn;
    }

    public void setHintEn(String hintEn) {
        this.hintEn = hintEn;
    }

    public RuleEntity getRule() {
        return rule;
    }

    public void setRule(RuleEntity rule) {
        this.rule = rule;
    }
}
