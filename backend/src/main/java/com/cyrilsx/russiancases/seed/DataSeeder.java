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
        LevelEntity level4 = level(4, "L4",
                "Niveau 4 — Les exceptions",
                "Level 4 — Exceptions",
                "Prépositionnel en -у (в году, в лесу), génitif pluriel à zéro (глаз, раз), noms en -мя (время, имя), pluriels irréguliers (дети, люди).",
                "Prepositional in -у (в году, в лесу), zero-ending plural genitive (глаз, раз), -мя nouns (время, имя), irregular plurals (дети, люди).");
        LevelEntity level5 = level(5, "L5",
                "Niveau 5 — Le soir (вечер), révision complète",
                "Level 5 — Evening (вечер), full review",
                "Un mot, six cas : révise tous les cas grammaticaux avec le mot вечер (le soir).",
                "One word, six cases: review every grammatical case with the word вечер (evening).");
        levelRepository.saveAll(List.of(level1, level2, level3, level4, level5));

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

        question(level1, GrammaticalCase.NOMINATIVE,
                "___ (собака) спит на диване.", "The ___ (собака / dog) is sleeping on the sofa.",
                List.of("собака", "собаки", "собаке", "собакой"),
                List.of("sobaka", "sobaki", "sobake", "sobakoy"),
                0,
                "Le sujet est au nominatif : féminin en -а.",
                "The subject is nominative: feminine in -а.",
                rules.get(GrammaticalCase.NOMINATIVE));
        question(level1, GrammaticalCase.ACCUSATIVE,
                "Я вижу ___ (сестра).", "I can see my ___ (сестра / sister).",
                List.of("сестру", "сестра", "сестре", "сестрой"),
                List.of("sestru", "sestra", "sestre", "sestroy"),
                0,
                "Complément d'objet direct animé → accusatif. Féminin animé en -а → -у.",
                "Animate direct object → accusative. Animate feminine -а becomes -у.",
                rules.get(GrammaticalCase.ACCUSATIVE));
        question(level1, GrammaticalCase.ACCUSATIVE,
                "Он покупает ___ (яблоко).", "He is buying an ___ (яблоко / apple).",
                List.of("яблоко", "яблока", "яблоку", "яблоком"),
                List.of("yabloko", "yabloka", "yabloku", "yablokom"),
                0,
                "Neutre inanimé : l'accusatif a la même forme que le nominatif.",
                "Inanimate neuter: the accusative looks like the nominative.",
                rules.get(GrammaticalCase.ACCUSATIVE));

        question(level2, GrammaticalCase.GENITIVE,
                "У меня пять ___ (книга).", "I have five ___ (книга / books).",
                List.of("книг", "книги", "книгам", "книг"),
                List.of("knig", "knigi", "knigam", "knig"),
                0,
                "Après 5 et plus → génitif pluriel : книга → книг.",
                "After 5 and above → plural genitive: книга → книг.",
                rules.get(GrammaticalCase.GENITIVE));
        question(level2, GrammaticalCase.DATIVE,
                "Я звоню ___ (подруга).", "I am calling my ___ (подруга / friend, f.).",
                List.of("подруге", "подруга", "подругу", "подругой"),
                List.of("podruge", "podruga", "podrugu", "podrugoy"),
                0,
                "Complément d'objet indirect → datif. Féminin en -а → -е.",
                "Indirect object → dative. Feminine -а becomes -е.",
                rules.get(GrammaticalCase.DATIVE));
        question(level2, GrammaticalCase.GENITIVE,
                "У нас нет ___ (время).", "We don't have ___ (время / time).",
                List.of("времени", "время", "временем", "времён"),
                List.of("vremeni", "vremya", "vremem", "vremyon"),
                0,
                "Après нет → génitif. Nom en -мя → -ени : время → времени.",
                "After нет → genitive. -мя noun → -ени: время → времени.",
                rules.get(GrammaticalCase.GENITIVE));

        question(level3, GrammaticalCase.INSTRUMENTAL,
                "Она довольна ___ (подарок).", "She is happy with the ___ (подарок / gift).",
                List.of("подарком", "подарок", "подарку", "подарка"),
                List.of("podarkom", "podarok", "podarku", "podarka"),
                0,
                "Après доволен → instrumental. Masculin → -ом.",
                "After доволен → instrumental. Masculine takes -ом.",
                rules.get(GrammaticalCase.INSTRUMENTAL));
        question(level3, GrammaticalCase.PREPOSITIONAL,
                "Он думает о ___ (работа).", "He is thinking about ___ (работа / work).",
                List.of("работе", "работа", "работой", "работу"),
                List.of("rabote", "rabota", "rabotoy", "rabotu"),
                0,
                "Après о → prépositionnel. Féminin en -а → -е.",
                "After о → prepositional. Feminine -а becomes -е.",
                rules.get(GrammaticalCase.PREPOSITIONAL));
        question(level3, GrammaticalCase.ACCUSATIVE,
                "Мы едим ___ (суп).", "We are eating ___ (суп / soup).",
                List.of("суп", "супа", "супу", "супом"),
                List.of("sup", "supa", "supu", "supom"),
                0,
                "Masculin inanimé à l'accusatif = nominatif.",
                "Inanimate masculine accusative = nominative.",
                rules.get(GrammaticalCase.ACCUSATIVE));

        question(level4, GrammaticalCase.PREPOSITIONAL,
                "В ___ (год) 365 дней.", "There are 365 days in a ___ (год / year).",
                List.of("году", "годе", "год", "года"),
                List.of("godu", "gode", "god", "goda"),
                0,
                "Exception : certains masculins en -у au prépositionnel (в году, в лесу, в снегу, на берегу).",
                "Exception: some masculines take -у in the prepositional (в году, в лесу, в снегу, на берегу).",
                rules.get(GrammaticalCase.PREPOSITIONAL));
        question(level4, GrammaticalCase.PREPOSITIONAL,
                "Они гуляют в ___ (лес).", "They are walking in the ___ (лес / forest).",
                List.of("лесу", "лесе", "лес", "лесом"),
                List.of("lesu", "lese", "les", "lesom"),
                0,
                "Exception : в лесу (comme в году, в снегу, в углу).",
                "Exception: в лесу (like в году, в снегу, в углу).",
                rules.get(GrammaticalCase.PREPOSITIONAL));
        question(level4, GrammaticalCase.GENITIVE,
                "У него нет ___ (глаз).", "He has no ___ (глаз / eyes).",
                List.of("глаз", "глаза", "глазов", "глазам"),
                List.of("glaz", "glaza", "glazov", "glazam"),
                0,
                "Exception : génitif pluriel à zéro après certaines bases (глаз → глаз, раз → раз).",
                "Exception: zero-ending plural genitive after certain stems (глаз → глаз, раз → раз).",
                rules.get(GrammaticalCase.GENITIVE));
        question(level4, GrammaticalCase.GENITIVE,
                "Мы видели это пять ___ (раз).", "We saw it five ___ (раз / times).",
                List.of("раз", "раза", "разов", "разам"),
                List.of("raz", "raza", "razov", "razam"),
                0,
                "Exception : раз → génitif pluriel раз (comme глаз → глаз).",
                "Exception: раз → plural genitive раз (like глаз → глаз).",
                rules.get(GrammaticalCase.GENITIVE));
        question(level4, GrammaticalCase.GENITIVE,
                "Он не знает ___ (имя) ребёнка.", "He doesn't know the child's ___ (имя / name).",
                List.of("имени", "имя", "именем", "имена"),
                List.of("imeni", "imya", "imenem", "imena"),
                0,
                "Exception : les noms en -мя (время, имя) → -ени au génitif/datitif.",
                "Exception: -мя nouns (время, имя) take -ени in the genitive/dative.",
                rules.get(GrammaticalCase.GENITIVE));
        question(level4, GrammaticalCase.PREPOSITIONAL,
                "Мы говорим об ___ (имя).", "We are talking about a ___ (имя / name).",
                List.of("имени", "имя", "именем", "именами"),
                List.of("imeni", "imya", "imenem", "imenami"),
                0,
                "Exception : prépositionnel en -и pour les noms en -мя (о времени, об имени).",
                "Exception: prepositional in -и for -мя nouns (о времени, об имени).",
                rules.get(GrammaticalCase.PREPOSITIONAL));
        question(level4, GrammaticalCase.GENITIVE,
                "У них трое ___ (ребёнок).", "They have three ___ (ребёнок / children).",
                List.of("детей", "ребёнок", "ребёнка", "детьми"),
                List.of("detey", "rebyonok", "rebyonka", "detmi"),
                0,
                "Exception : pluriel irrégulier ребёнок → детей (au génitif pluriel).",
                "Exception: irregular plural ребёнок → детей (plural genitive).",
                rules.get(GrammaticalCase.GENITIVE));
        question(level4, GrammaticalCase.GENITIVE,
                "В офисе работают пять ___ (человек).", "Five ___ (человек / people) work in the office.",
                List.of("человек", "людей", "людьми", "человека"),
                List.of("chelovek", "lyudey", "lyudmi", "cheloveka"),
                0,
                "Exception : génitif pluriel de человек = человек (zéro), pas людей.",
                "Exception: plural genitive of человек is человек (zero ending), not людей.",
                rules.get(GrammaticalCase.GENITIVE));

        question(level5, GrammaticalCase.NOMINATIVE,
                "___ (вечер) уже наступил.", "The ___ (вечер / evening) has already come.",
                List.of("вечер", "вечера", "вечеру", "вечеров"),
                List.of("vecher", "vechera", "vecheru", "vecherov"),
                0,
                "Sujet → nominatif : masculin en consonne.",
                "Subject → nominative: masculine consonant.",
                rules.get(GrammaticalCase.NOMINATIVE));
        question(level5, GrammaticalCase.ACCUSATIVE,
                "Мы ждём ___ (вечер).", "We are waiting for the ___ (вечер / evening).",
                List.of("вечер", "вечера", "вечером", "вечере"),
                List.of("vecher", "vechera", "vecherom", "vechere"),
                0,
                "Inanimé masculin : accusatif = nominatif.",
                "Inanimate masculine: accusative = nominative.",
                rules.get(GrammaticalCase.ACCUSATIVE));
        question(level5, GrammaticalCase.PREPOSITIONAL,
                "Они говорят о ___ (вечер).", "They are talking about the ___ (вечер / evening).",
                List.of("вечере", "вечер", "вечером", "вечера"),
                List.of("vechere", "vecher", "vecherom", "vechera"),
                0,
                "Après о → prépositionnel : masculin en -е.",
                "After о → prepositional: masculine in -е.",
                rules.get(GrammaticalCase.PREPOSITIONAL));
        question(level5, GrammaticalCase.PREPOSITIONAL,
                "В ___ (год) много вечеров.", "There are many evenings in a ___ (год / year).",
                List.of("году", "годе", "год", "года"),
                List.of("godu", "gode", "god", "goda"),
                0,
                "Exception du niveau 4 : в году.",
                "Level 4 exception: в году.",
                rules.get(GrammaticalCase.PREPOSITIONAL));
        question(level5, GrammaticalCase.INSTRUMENTAL,
                "Каждый ___ (вечер) он читает.", "Every ___ (вечер / evening) he reads.",
                List.of("вечерами", "вечеров", "вечер", "вечером"),
                List.of("vecherami", "vecherov", "vecher", "vecherom"),
                0,
                "Pluriel instrumental : вечер → вечерами (chaque soir).",
                "Plural instrumental: вечер → вечерами (every evening).",
                rules.get(GrammaticalCase.INSTRUMENTAL));
        question(level5, GrammaticalCase.GENITIVE,
                "Конец ___ (вечер) близок.", "The end of the ___ (вечер / evening) is near.",
                List.of("вечера", "вечер", "вечере", "вечеру"),
                List.of("vechera", "vecher", "vechere", "vecheru"),
                0,
                "Après « конец » → génitif : masculin en -а.",
                "After « конец » → genitive: masculine in -а.",
                rules.get(GrammaticalCase.GENITIVE));
        question(level5, GrammaticalCase.DATIVE,
                "К ___ (вечер) всё готово.", "Everything is ready by the ___ (вечер / evening).",
                List.of("вечеру", "вечер", "вечера", "вечером"),
                List.of("vecheru", "vecher", "vechera", "vecherom"),
                0,
                "Préposition к → datif : masculin en -у.",
                "Preposition к → dative: masculine in -у.",
                rules.get(GrammaticalCase.DATIVE));
        question(level5, GrammaticalCase.INSTRUMENTAL,
                "Он доволен ___ (вечер).", "He is happy with the ___ (вечер / evening).",
                List.of("вечером", "вечер", "вечера", "вечере"),
                List.of("vecherom", "vecher", "vechera", "vechere"),
                0,
                "Après доволен → instrumental : masculin en -ом.",
                "After доволен → instrumental: masculine in -ом.",
                rules.get(GrammaticalCase.INSTRUMENTAL));

        question(level1, GrammaticalCase.NOMINATIVE,
                "___ (кот) сидит на окне.", "The ___ (кот / cat) is sitting on the window.",
                List.of("кот", "кота", "коту", "котом"),
                List.of("kot", "kota", "kotu", "kotom"),
                0,
                "Sujet → nominatif : masculin en consonne.",
                "Subject → nominative: masculine consonant.",
                rules.get(GrammaticalCase.NOMINATIVE));
        question(level1, GrammaticalCase.ACCUSATIVE,
                "Она читает ___ (газета).", "She is reading a ___ (газета / newspaper).",
                List.of("газету", "газета", "газете", "газетой"),
                List.of("gazetu", "gazeta", "gazete", "gazetoy"),
                0,
                "COD → accusatif. Féminin en -а → -у.",
                "Direct object → accusative. Feminine -а becomes -у.",
                rules.get(GrammaticalCase.ACCUSATIVE));
        question(level1, GrammaticalCase.NOMINATIVE,
                "___ (студент) учит уроки.", "The ___ (студент / student) is doing homework.",
                List.of("студент", "студента", "студенту", "студентом"),
                List.of("student", "studenta", "studentu", "studentom"),
                0,
                "Sujet → nominatif.",
                "Subject → nominative.",
                rules.get(GrammaticalCase.NOMINATIVE));
        question(level1, GrammaticalCase.ACCUSATIVE,
                "Я вижу ___ (машина).", "I can see a ___ (машина / car).",
                List.of("машину", "машина", "машине", "машиной"),
                List.of("mashinu", "mashina", "mashine", "mashinoy"),
                0,
                "COD inanimé → accusatif en -у (féminin animé-looking, mais inanimé).",
                "Inanimate direct object → accusative in -у.",
                rules.get(GrammaticalCase.ACCUSATIVE));
        question(level1, GrammaticalCase.ACCUSATIVE,
                "Он любит ___ (дедушка).", "He loves his ___ (дедушка / grandfather).",
                List.of("дедушку", "дедушка", "дедушке", "дедушкой"),
                List.of("dedushku", "dedushka", "dedushke", "dedushkoy"),
                0,
                "Animé masculin → accusatif en -у/-а (comme le génitif).",
                "Animate masculine → accusative in -у/-а (like genitive).",
                rules.get(GrammaticalCase.ACCUSATIVE));
        question(level1, GrammaticalCase.NOMINATIVE,
                "___ (окно) открыто.", "The ___ (окно / window) is open.",
                List.of("окно", "окна", "окну", "окном"),
                List.of("okno", "okna", "oknu", "oknom"),
                0,
                "Sujet → nominatif : neutre en -о.",
                "Subject → nominative: neuter in -о.",
                rules.get(GrammaticalCase.NOMINATIVE));

        question(level2, GrammaticalCase.GENITIVE,
                "У неё нет ___ (сумка).", "She doesn't have a ___ (сумка / bag).",
                List.of("сумки", "сумка", "сумке", "сумку"),
                List.of("sumki", "sumka", "sumke", "sumku"),
                0,
                "Après нет → génitif. Féminin en -а → -ы.",
                "After нет → genitive. Feminine -а becomes -ы.",
                rules.get(GrammaticalCase.GENITIVE));
        question(level2, GrammaticalCase.DATIVE,
                "Мы помогаем ___ (друг).", "We are helping our ___ (друг / friend).",
                List.of("другу", "друг", "друга", "другом"),
                List.of("drugu", "drug", "druga", "drugom"),
                0,
                "COI → datif. Masculin → -у.",
                "Indirect object → dative. Masculine takes -у.",
                rules.get(GrammaticalCase.DATIVE));
        question(level2, GrammaticalCase.GENITIVE,
                "Машина ___ (отец) новая.", "___'s (отец / father) car is new.",
                List.of("отца", "отец", "отцу", "отцом"),
                List.of("otsa", "otes", "otsu", "otsom"),
                0,
                "Possession → génitif. Masculin animé → -а.",
                "Possession → genitive. Animate masculine takes -а.",
                rules.get(GrammaticalCase.GENITIVE));
        question(level2, GrammaticalCase.DATIVE,
                "Он идёт по ___ (улица).", "He is walking along ___ (улица / street).",
                List.of("улице", "улица", "улицей", "улицу"),
                List.of("ulitse", "ulitsa", "ulitsey", "ulitsu"),
                0,
                "Après по → datif. Féminin en -а → -е.",
                "After по → dative. Feminine -а becomes -е.",
                rules.get(GrammaticalCase.DATIVE));
        question(level2, GrammaticalCase.GENITIVE,
                "У меня три ___ (сестра).", "I have three ___ (сестра / sisters).",
                List.of("сестры", "сестра", "сестре", "сестру"),
                List.of("sestry", "sestra", "sestre", "sestru"),
                0,
                "Après 2-4 → génitif singulier : сестра → сестры.",
                "After 2-4 → singular genitive: сестра → сестры.",
                rules.get(GrammaticalCase.GENITIVE));
        question(level2, GrammaticalCase.DATIVE,
                "Я верю ___ (слово).", "I believe in a ___ (слово / word)... (datif).",
                List.of("слову", "слово", "словом", "слова"),
                List.of("slovu", "slovo", "slovom", "slova"),
                0,
                "Après верю → datif. Neutre en -о → -у.",
                "After верю → dative. Neuter -о becomes -у.",
                rules.get(GrammaticalCase.DATIVE));

        question(level3, GrammaticalCase.INSTRUMENTAL,
                "Чай с ___ (лимон).", "Tea with ___ (лимон / lemon).",
                List.of("лимоном", "лимон", "лимону", "лимона"),
                List.of("limonom", "limon", "limonu", "limona"),
                0,
                "Après с → instrumental. Masculin → -ом.",
                "After с → instrumental. Masculine takes -ом.",
                rules.get(GrammaticalCase.INSTRUMENTAL));
        question(level3, GrammaticalCase.PREPOSITIONAL,
                "Книги лежат в ___ (шкаф).", "The books are in the ___ (шкаф / cupboard).",
                List.of("шкафу", "шкафе", "шкаф", "шкафом"),
                List.of("shkafu", "shkafe", "shkaf", "shkafom"),
                0,
                "Exception : в шкафу (prépositionnel en -у comme в году).",
                "Exception: в шкафу (prepositional in -у like в году).",
                rules.get(GrammaticalCase.PREPOSITIONAL));
        question(level3, GrammaticalCase.INSTRUMENTAL,
                "Они разговаривают по ___ (телефон).", "They are talking on the ___ (телефон / phone).",
                List.of("телефону", "телефон", "телефоном", "телефона"),
                List.of("telefonu", "telefon", "telefonom", "telefona"),
                0,
                "Après по → instrumental. Masculin → -у.",
                "After по → instrumental. Masculine takes -у.",
                rules.get(GrammaticalCase.INSTRUMENTAL));
        question(level3, GrammaticalCase.PREPOSITIONAL,
                "Картина висит на ___ (стена).", "The picture hangs on the ___ (стена / wall).",
                List.of("стене", "стена", "стеной", "стену"),
                List.of("stene", "stena", "stenoy", "stenu"),
                0,
                "Après на → prépositionnel. Féminin en -а → -е.",
                "After на → prepositional. Feminine -а becomes -е.",
                rules.get(GrammaticalCase.PREPOSITIONAL));
        question(level3, GrammaticalCase.ACCUSATIVE,
                "Мы встретили ___ (учитель).", "We met a ___ (учитель / teacher).",
                List.of("учителя", "учитель", "учителю", "учителем"),
                List.of("uchitelya", "uchitel", "uchitelyu", "uchitelem"),
                0,
                "Animé masculin → accusatif en -я (comme génitif).",
                "Animate masculine → accusative in -я (like genitive).",
                rules.get(GrammaticalCase.ACCUSATIVE));
        question(level3, GrammaticalCase.INSTRUMENTAL,
                "Мой папа работает ___ (врач).", "My dad works as a ___ (врач / doctor).",
                List.of("врачом", "врач", "врача", "врачу"),
                List.of("vrachom", "vrach", "vracha", "vrachu"),
                0,
                "Métier → instrumental. Masculin → -ом.",
                "Occupation → instrumental. Masculine takes -ом.",
                rules.get(GrammaticalCase.INSTRUMENTAL));

        question(level4, GrammaticalCase.PREPOSITIONAL,
                "Он живёт на ___ (берег).", "He lives on the ___ (берег / shore).",
                List.of("берегу", "береге", "берег", "берегом"),
                List.of("beregu", "berege", "bereg", "beregom"),
                0,
                "Exception : на берегу (prépositionnel en -у).",
                "Exception: на берегу (prepositional in -у).",
                rules.get(GrammaticalCase.PREPOSITIONAL));
        question(level4, GrammaticalCase.PREPOSITIONAL,
                "Снег лежит в ___ (снег).", "The snow lies in the ___ (снег / snow).",
                List.of("снегу", "снеге", "снег", "снегом"),
                List.of("snegu", "snege", "sneg", "snegom"),
                0,
                "Exception : в снегу (prépositionnel en -у).",
                "Exception: в снегу (prepositional in -у).",
                rules.get(GrammaticalCase.PREPOSITIONAL));
        question(level4, GrammaticalCase.GENITIVE,
                "У неё много ___ (деньги).", "She has a lot of ___ (деньги / money).",
                List.of("денег", "деньги", "деньгам", "деньгами"),
                List.of("deneg", "denezh", "denyam", "denyami"),
                0,
                "Exception : pluriel en -ег/-ей : деньги → денег (génitif pluriel).",
                "Exception: plural in -ег: деньги → денег (plural genitive).",
                rules.get(GrammaticalCase.GENITIVE));
        question(level4, GrammaticalCase.INSTRUMENTAL,
                "Он доволен ___ (время).", "He is happy with the ___ (время / time).",
                List.of("временем", "время", "времени", "времён"),
                List.of("vemenem", "vremya", "vremeni", "vremyon"),
                0,
                "Exception : instrumental des noms en -мя → -енем (временем).",
                "Exception: -мя nouns instrumental → -енем (временем).",
                rules.get(GrammaticalCase.INSTRUMENTAL));
        question(level4, GrammaticalCase.DATIVE,
                "К ___ (имя) добавили фамилию.", "A surname was added to the ___ (имя / first name).",
                List.of("имени", "имя", "имён", "именем"),
                List.of("imeni", "imya", "imyon", "imenem"),
                0,
                "Exception : datif des noms en -мя → -ени (к имени).",
                "Exception: -мя nouns dative → -ени (к имени).",
                rules.get(GrammaticalCase.DATIVE));
        question(level4, GrammaticalCase.GENITIVE,
                "В комнате четверо ___ (друг).", "There are four ___ (друг / friends) in the room.",
                List.of("друзей", "друг", "друзья", "друзьями"),
                List.of("druzey", "drug", "druzia", "druziami"),
                0,
                "Exception : pluriel irrégulier друг → друзей (génitif pluriel).",
                "Exception: irregular plural друг → друзей (plural genitive).",
                rules.get(GrammaticalCase.GENITIVE));

        question(level5, GrammaticalCase.GENITIVE,
                "В году много ___ (вечер).", "There are many ___ (вечер / evenings) in a year.",
                List.of("вечеров", "вечера", "вечер", "вечеру"),
                List.of("vecherov", "vechera", "vecher", "vecheru"),
                0,
                "Génitif pluriel régulier : вечер → вечеров.",
                "Regular plural genitive: вечер → вечеров.",
                rules.get(GrammaticalCase.GENITIVE));
        question(level5, GrammaticalCase.ACCUSATIVE,
                "Он опоздал на ___ (вечер).", "He was late for the ___ (вечер / evening event).",
                List.of("вечер", "вечера", "вечере", "вечером"),
                List.of("vecher", "vechera", "vechere", "vecherom"),
                0,
                "« на + accusatif » pour un événement (на концерт, на вечер).",
                "« на + accusative » for an event (на концерт, на вечер).",
                rules.get(GrammaticalCase.ACCUSATIVE));
        question(level5, GrammaticalCase.INSTRUMENTAL,
                "Он работает каждый ___ (вечер).", "He works every ___ (вечер / evening).",
                List.of("вечером", "вечер", "вечера", "вечере"),
                List.of("vecherom", "vecher", "vechera", "vechere"),
                0,
                "Instrumental singulier : chaque soir → вечером.",
                "Singular instrumental: every evening → вечером.",
                rules.get(GrammaticalCase.INSTRUMENTAL));
        question(level5, GrammaticalCase.GENITIVE,
                "После ___ (вечер) наступает ночь.", "Night comes after the ___ (вечер / evening).",
                List.of("вечера", "вечер", "вечере", "вечеру"),
                List.of("vechera", "vecher", "vechere", "vecheru"),
                0,
                "Après после → génitif : masculin en -а.",
                "After после → genitive: masculine in -а.",
                rules.get(GrammaticalCase.GENITIVE));
        question(level5, GrammaticalCase.NOMINATIVE,
                "___ (вечер) бывают длинными зимой.", "___ (вечер / evenings) are long in winter.",
                List.of("вечера", "вечеров", "вечер", "вечерам"),
                List.of("vechera", "vecherov", "vecher", "vecheram"),
                0,
                "Pluriel nominatif : вечер → вечера.",
                "Plural nominative: вечер → вечера.",
                rules.get(GrammaticalCase.NOMINATIVE));
        question(level5, GrammaticalCase.PREPOSITIONAL,
                "Вечер в ___ (театр) начался.", "The evening at the ___ (театр / theatre) has begun.",
                List.of("театре", "театр", "театра", "театром"),
                List.of("teatre", "teatr", "teatra", "teatrom"),
                0,
                "Après в → prépositionnel : masculin en -е.",
                "After в → prepositional: masculine in -е.",
                rules.get(GrammaticalCase.PREPOSITIONAL));
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
