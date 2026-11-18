package com.cyrilsx.russiancases.seed;

import com.cyrilsx.russiancases.model.GrammaticalCase;
import com.cyrilsx.russiancases.model.LevelEntity;
import com.cyrilsx.russiancases.model.QuestionEntity;
import com.cyrilsx.russiancases.model.Role;
import com.cyrilsx.russiancases.model.RuleEntity;
import com.cyrilsx.russiancases.model.UserEntity;
import com.cyrilsx.russiancases.repository.LevelRepository;
import com.cyrilsx.russiancases.repository.QuestionRepository;
import com.cyrilsx.russiancases.repository.RuleRepository;
import com.cyrilsx.russiancases.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements ApplicationRunner {

    private final LevelRepository levelRepository;
    private final RuleRepository ruleRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public DataSeeder(LevelRepository levelRepository,
                      RuleRepository ruleRepository,
                      QuestionRepository questionRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.admin.username}") String adminUsername,
                      @Value("${app.admin.password}") String adminPassword) {
        this.levelRepository = levelRepository;
        this.ruleRepository = ruleRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureAdminUser();
        if (questionRepository.count() > 0) {
            return;
        }

        Map<GrammaticalCase, RuleEntity> rules = new HashMap<>();
        rules.put(GrammaticalCase.NOMINATIVE, rule(GrammaticalCase.NOMINATIVE,
                "Le nominatif",
                "The nominative",
                "Le nominatif répond à la question кто? / что? (qui ? / quoi ?). C'est le cas du sujet et de l'attribut du sujet. "
                        + "Terminaisons principales : -а/-я (féminin), -о/-е (neutre), consonne ou -ь (masculin).",
                "The nominative answers the question кто? / что? (who? / what?). It is the case of the subject. "
                        + "Main endings: -а/-я (feminine), -о/-е (neuter), a consonant or -ь (masculine)."));
        rules.put(GrammaticalCase.GENITIVE, rule(GrammaticalCase.GENITIVE,
                "Le génitif",
                "The genitive",
                "Le génitif exprime la possession (книга мамы), l'absence (у меня нет книги) et suit les nombres 2, 3, 4 (две книги). "
                        + "Terminaisons : -ы/-и, -а/-я (masculin), -а pour les masculins animés après un nombre.",
                "The genitive expresses possession (мамина книга), absence (у меня нет книги) and follows the numerals 2, 3, 4 (две книги). "
                        + "Endings: -ы/-и, -а/-я (masculine), -а for animate masculines after a numeral."));
        rules.put(GrammaticalCase.DATIVE, rule(GrammaticalCase.DATIVE,
                "Le datif",
                "The dative",
                "Le datif marque le complément d'objet indirect (я даю книгу брату) et s'emploie avec к (vers) et по. "
                        + "Terminaisons : -у/-ю (masculin/neutre), -е (féminin), -ам/-ям (pluriel).",
                "The dative marks the indirect object (я даю книгу брату) and is used with к (to) and по. "
                        + "Endings: -у/-ю (masculine/neuter), -е (feminine), -ам/-ям (plural)."));
        rules.put(GrammaticalCase.ACCUSATIVE, rule(GrammaticalCase.ACCUSATIVE,
                "L'accusatif",
                "The accusative",
                "L'accusatif marque le complément d'objet direct (я читаю книгу). Les masculins animés prennent la terminaison "
                        + "du génitif (-а/-я : я вижу Ивана). Les inanimés ont la même forme que le nominatif (я вижу дом).",
                "The accusative marks the direct object (я читаю книгу). Animate masculine nouns take the genitive ending "
                        + "(-а/-я: я вижу Ивана). Inanimate nouns look like the nominative (я вижу дом)."));
        rules.put(GrammaticalCase.INSTRUMENTAL, rule(GrammaticalCase.INSTRUMENTAL,
                "L'instrumental",
                "The instrumental",
                "L'instrumental exprime le moyen, l'instrument (я пишу ручкой = j'écris avec un stylo) et s'emploie après с (avec). "
                        + "Terminaisons : -ом/-ем (masculin/neutre), -ой/-ей (féminin), -ами/-ями (pluriel).",
                "The instrumental expresses the means or instrument (я пишу ручкой = I write with a pen) and follows с (with). "
                        + "Endings: -ом/-ем (masculine/neuter), -ой/-ей (feminine), -ами/-ями (plural)."));
        rules.put(GrammaticalCase.PREPOSITIONAL, rule(GrammaticalCase.PREPOSITIONAL,
                "Le prépositionnel",
                "The prepositional",
                "Le prépositionnel ne s'emploie qu'après une préposition : pour situer (в Москве, на столе) ou parler d'un sujet "
                        + "(о жизни). Terminaisons : -е (la plupart des noms), -и pour certains noms en -ия/-ие.",
                "The prepositional is only used after a preposition: for location (в Москве, на столе) or topic (о жизни). "
                        + "Endings: -е (most nouns), -и for some -ия/-ие nouns."));
        ruleRepository.saveAll(rules.values());

        LevelEntity level1 = level(1, "L1",
                "Niveau 1 — Nominatif et accusatif",
                "Level 1 — Nominative and accusative",
                "Reconnaître le sujet (nominatif) et le complément d'objet direct (accusatif).",
                "Identify the subject (nominative) and the direct object (accusative).");
        LevelEntity level2 = level(2, "L2",
                "Niveau 2 — Génitif et datif",
                "Level 2 — Genitive and dative",
                "Exprimer la possession et l'absence (génitif) et le complément d'objet indirect (datif).",
                "Express possession and absence (genitive) and the indirect object (dative).");
        LevelEntity level3 = level(3, "L3",
                "Niveau 3 — Instrumental, prépositionnel et révision",
                "Level 3 — Instrumental, prepositional and review",
                "Le moyen (instrumental), le lieu et le sujet de conversation (prépositionnel), et une révision des autres cas.",
                "Means (instrumental), location and topic (prepositional), and a review of the other cases.");
        levelRepository.saveAll(List.of(level1, level2, level3));

        question(level1, GrammaticalCase.NOMINATIVE,
                "Это ___ (книга).", "This is a ___ (книга / book).",
                List.of("книга", "книги", "книге", "книгой"),
                List.of("kniga", "knigi", "knige", "knigoy"),
                0,
                "« Это » demande quoi ? → le sujet est au nominatif. Féminin en -а → nominatif en -а.",
                "\"Это\" asks \"what?\" → the subject is nominative. Feminine in -а stays -а.",
                rules.get(GrammaticalCase.NOMINATIVE));
        question(level1, GrammaticalCase.ACCUSATIVE,
                "Я читаю ___ (книга).", "I am reading a ___ (книга / book).",
                List.of("книгу", "книга", "книге", "книги"),
                List.of("knigu", "kniga", "knige", "knigi"),
                0,
                "Complément d'objet direct → accusatif. Féminin en -а → -у.",
                "Direct object → accusative. Feminine -а becomes -у.",
                rules.get(GrammaticalCase.ACCUSATIVE));
        question(level1, GrammaticalCase.NOMINATIVE,
                "___ (стол) стоит в комнате.", "The ___ (стол / table) is standing in the room.",
                List.of("стол", "стола", "столу", "столом"),
                List.of("stol", "stola", "stolu", "stolom"),
                0,
                "Le sujet du verbe стоит est au nominatif : masculin en consonne.",
                "The subject of стоит is nominative: masculine ends in a consonant.",
                rules.get(GrammaticalCase.NOMINATIVE));
        question(level1, GrammaticalCase.ACCUSATIVE,
                "Мы видим ___ (дом).", "We can see the ___ (дом / house).",
                List.of("дом", "дома", "дому", "домом"),
                List.of("dom", "doma", "domu", "domom"),
                0,
                "Masculin inanimé : l'accusatif a la même forme que le nominatif.",
                "Inanimate masculine: the accusative looks like the nominative.",
                rules.get(GrammaticalCase.ACCUSATIVE));
        question(level1, GrammaticalCase.ACCUSATIVE,
                "Она знает ___ (Иван).", "She knows ___ (Иван / Ivan).",
                List.of("Иван", "Ивана", "Ивану", "Иваном"),
                List.of("Ivan", "Ivana", "Ivanu", "Ivanom"),
                1,
                "Иван est une personne (animé) : masculin animé à l'accusatif → -а.",
                "Иван is a person (animate): animate masculine accusative takes -а.",
                rules.get(GrammaticalCase.ACCUSATIVE));
        question(level1, GrammaticalCase.NOMINATIVE,
                "___ (мама) готовит обед.", "___ (мама / mother) is cooking dinner.",
                List.of("Мама", "Мамы", "Маме", "Мамой"),
                List.of("Mama", "Mamy", "Mame", "Mamoy"),
                0,
                "Le sujet est au nominatif : féminin en -а.",
                "The subject is nominative: feminine in -а.",
                rules.get(GrammaticalCase.NOMINATIVE));

        question(level2, GrammaticalCase.GENITIVE,
                "У меня нет ___ (сестра).", "I don't have a ___ (сестра / sister).",
                List.of("сестры", "сестра", "сестре", "сестру"),
                List.of("sestry", "sestra", "sestre", "sestru"),
                0,
                "Après нет → génitif. Féminin en -а → -ы.",
                "After нет → genitive. Feminine -а becomes -ы.",
                rules.get(GrammaticalCase.GENITIVE));
        question(level2, GrammaticalCase.DATIVE,
                "Я даю книгу ___ (брат).", "I am giving the book to my ___ (брат / brother).",
                List.of("брату", "брат", "брата", "братом"),
                List.of("bratu", "brat", "brata", "bratom"),
                0,
                "Complément d'objet indirect → datif. Masculin → -у.",
                "Indirect object → dative. Masculine takes -у.",
                rules.get(GrammaticalCase.DATIVE));
        question(level2, GrammaticalCase.GENITIVE,
                "Книга ___ (мама) лежит на столе.", "My ___'s (мама / mother) book is on the table.",
                List.of("мамы", "мама", "маме", "маму"),
                List.of("mamy", "mama", "mame", "mamu"),
                0,
                "La possession s'exprime au génitif : мама → мамы.",
                "Possession uses the genitive: мама → мамы.",
                rules.get(GrammaticalCase.GENITIVE));
        question(level2, GrammaticalCase.DATIVE,
                "Мы идём к ___ (врач).", "We are going to the ___ (врач / doctor).",
                List.of("врачу", "врач", "врача", "врачом"),
                List.of("vrachu", "vrach", "vracha", "vrachom"),
                0,
                "La préposition к demande le datif : masculin → -у.",
                "The preposition к requires the dative: masculine takes -у.",
                rules.get(GrammaticalCase.DATIVE));
        question(level2, GrammaticalCase.GENITIVE,
                "У неё две ___ (книга).", "She has two ___ (книга / books).",
                List.of("книги", "книга", "книге", "книгу"),
                List.of("knigi", "kniga", "knige", "knigu"),
                0,
                "Après les nombres 2, 3, 4 → génitif singulier : книга → книги.",
                "After the numerals 2, 3, 4 → singular genitive: книга → книги.",
                rules.get(GrammaticalCase.GENITIVE));
        question(level2, GrammaticalCase.DATIVE,
                "Он подарил цветы ___ (девушка).", "He gave flowers to a ___ (девушка / girl).",
                List.of("девушке", "девушка", "девушку", "девушкой"),
                List.of("devushke", "devushka", "devushku", "devushkoy"),
                0,
                "Complément d'objet indirect → datif. Féminin en -а → -е.",
                "Indirect object → dative. Feminine -а becomes -е.",
                rules.get(GrammaticalCase.DATIVE));

        question(level3, GrammaticalCase.INSTRUMENTAL,
                "Я пишу ___ (ручка).", "I write with a ___ (ручка / pen).",
                List.of("ручкой", "ручка", "ручке", "ручку"),
                List.of("ruchkoy", "ruchka", "ruchke", "ruchku"),
                0,
                "Le moyen s'exprime à l'instrumental : féminin en -а → -ой.",
                "The means uses the instrumental: feminine -а becomes -ой.",
                rules.get(GrammaticalCase.INSTRUMENTAL));
        question(level3, GrammaticalCase.PREPOSITIONAL,
                "Мы говорим о ___ (жизнь).", "We are talking about ___ (жизнь / life).",
                List.of("жизни", "жизнь", "жизнью", "жизню"),
                List.of("zhizni", "zhizn", "zhiznyu", "zhiznyyu"),
                0,
                "Après о → prépositionnel. Féminin en -ь → -и.",
                "After о → prepositional. Feminine -ь becomes -и.",
                rules.get(GrammaticalCase.PREPOSITIONAL));
        question(level3, GrammaticalCase.INSTRUMENTAL,
                "Он работает ___ (инженер).", "He works as an ___ (инженер / engineer).",
                List.of("инженером", "инженер", "инженеру", "инженера"),
                List.of("inzhenerom", "inzhener", "inzheneru", "inzhenera"),
                0,
                "Le métier, le rôle → instrumental. Masculin → -ом.",
                "Occupation/role → instrumental. Masculine takes -ом.",
                rules.get(GrammaticalCase.INSTRUMENTAL));
        question(level3, GrammaticalCase.PREPOSITIONAL,
                "Они живут в ___ (Москва).", "They live in ___ (Москва / Moscow).",
                List.of("Москве", "Москва", "Москвой", "Москву"),
                List.of("Moskve", "Moskva", "Moskvoy", "Moskvu"),
                0,
                "в + lieu → prépositionnel. Féminin en -а → -е.",
                "в + place → prepositional. Feminine -а becomes -е.",
                rules.get(GrammaticalCase.PREPOSITIONAL));
        question(level3, GrammaticalCase.INSTRUMENTAL,
                "Я интересуюсь ___ (история).", "I am interested in ___ (история / history).",
                List.of("историей", "история", "истории", "историю"),
                List.of("istoriyey", "istoriya", "istorii", "istoriyu"),
                0,
                "Après интересуюсь → instrumental. Féminin en -ия → -ией.",
                "After интересуюсь → instrumental. Feminine -ия becomes -ией.",
                rules.get(GrammaticalCase.INSTRUMENTAL));
        question(level3, GrammaticalCase.GENITIVE,
                "Это дом ___ (отец).", "This is ___'s (отец / father) house.",
                List.of("отца", "отец", "отцу", "отцом"),
                List.of("otsa", "otes", "otsu", "otsom"),
                0,
                "Possession → génitif. Masculin animé en consonne → -а.",
                "Possession → genitive. Animate masculine consonant takes -а.",
                rules.get(GrammaticalCase.GENITIVE));
        question(level3, GrammaticalCase.PREPOSITIONAL,
                "Мы думаем о ___ (проблема).", "We are thinking about a ___ (проблема / problem).",
                List.of("проблеме", "проблема", "проблемой", "проблему"),
                List.of("probleme", "problema", "problemoy", "problemu"),
                0,
                "Après о → prépositionnel. Féminin en -а → -е.",
                "After о → prepositional. Feminine -а becomes -е.",
                rules.get(GrammaticalCase.PREPOSITIONAL));
        question(level3, GrammaticalCase.INSTRUMENTAL,
                "С ___ (друг) мы путешествуем.", "We travel with a ___ (друг / friend).",
                List.of("другом", "друг", "друга", "другу"),
                List.of("drugom", "drug", "druga", "drugu"),
                0,
                "Après с (avec) → instrumental. Masculin en consonne → -ом.",
                "After с (with) → instrumental. Masculine consonant takes -ом.",
                rules.get(GrammaticalCase.INSTRUMENTAL));
    }

    private void ensureAdminUser() {
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            UserEntity admin = new UserEntity();
            admin.setUsername(adminUsername);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setCreatedAt(Instant.now());
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }
    }

    private RuleEntity rule(GrammaticalCase grammaticalCase, String titleFr, String titleEn,
                            String contentFr, String contentEn) {
        RuleEntity rule = new RuleEntity();
        rule.setGrammaticalCase(grammaticalCase);
        rule.setTitleFr(titleFr);
        rule.setTitleEn(titleEn);
        rule.setContentFr(contentFr);
        rule.setContentEn(contentEn);
        return rule;
    }

    private LevelEntity level(int number, String code, String titleFr, String titleEn,
                              String descriptionFr, String descriptionEn) {
        LevelEntity level = new LevelEntity();
        level.setLevelNumber(number);
        level.setCode(code);
        level.setTitleFr(titleFr);
        level.setTitleEn(titleEn);
        level.setDescriptionFr(descriptionFr);
        level.setDescriptionEn(descriptionEn);
        return level;
    }

    private void question(LevelEntity level, GrammaticalCase grammaticalCase,
                          String promptFr, String promptEn,
                          List<String> optionsFr, List<String> optionsEn,
                          int correctIndex, String hintFr, String hintEn,
                          RuleEntity rule) {
        QuestionEntity question = new QuestionEntity();
        question.setLevel(level);
        question.setGrammaticalCase(grammaticalCase);
        question.setPromptFr(promptFr);
        question.setPromptEn(promptEn);
        question.setOptionsFr(optionsFr);
        question.setOptionsEn(optionsEn);
        question.setCorrectIndex(correctIndex);
        question.setHintFr(hintFr);
        question.setHintEn(hintEn);
        question.setRule(rule);
        questionRepository.save(question);
    }
}
