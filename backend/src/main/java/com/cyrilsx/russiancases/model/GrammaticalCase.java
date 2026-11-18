package com.cyrilsx.russiancases.model;

public enum GrammaticalCase {

    NOMINATIVE("Nominatif", "Nominative"),
    GENITIVE("Génitif", "Genitive"),
    DATIVE("Datif", "Dative"),
    ACCUSATIVE("Accusatif", "Accusative"),
    INSTRUMENTAL("Instrumental", "Instrumental"),
    PREPOSITIONAL("Prépositionnel", "Prepositional");

    private final String labelFr;
    private final String labelEn;

    GrammaticalCase(String labelFr, String labelEn) {
        this.labelFr = labelFr;
        this.labelEn = labelEn;
    }

    public String label(String lang) {
        return "fr".equals(lang) ? labelFr : labelEn;
    }
}
