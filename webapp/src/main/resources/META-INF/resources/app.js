const state = {
    lang: localStorage.getItem("rc-lang") || "fr",
    userId: localStorage.getItem("rc-user") || (() => {
        const id = "user-" + Math.random().toString(36).slice(2, 10);
        localStorage.setItem("rc-user", id);
        return id;
    })(),
    levels: [],
    questions: [],
    questionIndex: 0,
    selectedOption: null,
};

const i18n = {
    fr: {
        title: "Les cas russes",
        levels: "Niveaux",
        back: "Retour",
        submit: "Valider",
        hint: "Indice",
        rule: "Rappel de règle",
        next: "Question suivante →",
        progress: "Ma progression",
        correct: "Juste ! Bravo !",
        wrong: "Pas tout à fait — réessaie ou demande un indice.",
        questions: "questions",
        answered: "répondues",
        overall: "Score global",
    },
    en: {
        title: "Russian Cases",
        levels: "Levels",
        back: "Back",
        submit: "Submit",
        hint: "Hint",
        rule: "Rule reminder",
        next: "Next question →",
        progress: "My progress",
        correct: "Correct! Well done!",
        wrong: "Not quite — try again or ask for a hint.",
        questions: "questions",
        answered: "answered",
        overall: "Overall score",
    },
};

const t = () => i18n[state.lang];

async function api(path, options = {}) {
    const response = await fetch(path, {
        ...options,
        headers: {
            "Accept-Language": state.lang,
            "Content-Type": "application/json",
            ...(options.headers || {}),
        },
    });
    if (!response.ok) {
        throw new Error("API error " + response.status);
    }
    return response.json();
}

function el(id) {
    return document.getElementById(id);
}

function applyLang() {
    document.documentElement.lang = state.lang;
    el("app-title").textContent = t().title;
    el("levels-heading").textContent = t().levels;
    el("back-label").textContent = t().back;
    el("submit-btn").textContent = t().submit;
    el("hint-btn").textContent = t().hint;
    el("rule-btn").textContent = t().rule;
    el("next-btn").textContent = t().next;
    el("progress-heading").textContent = t().progress;
    el("lang-fr").classList.toggle("active", state.lang === "fr");
    el("lang-en").classList.toggle("active", state.lang === "en");
    localStorage.setItem("rc-lang", state.lang);
    loadLevels();
    loadSummary();
}

async function loadLevels() {
    state.levels = await api("/api/levels");
    const container = el("levels");
    container.innerHTML = "";
    for (const level of state.levels) {
        const card = document.createElement("button");
        card.className = "level-card";
        card.innerHTML = "<h3></h3><p class='description'></p><small></small>";
        card.querySelector("h3").textContent = level.title;
        card.querySelector(".description").textContent = level.description;
        card.querySelector("small").textContent = level.questionCount + " " + t().questions;
        card.addEventListener("click", () => startLevel(level));
        container.appendChild(card);
    }
}

async function startLevel(level) {
    state.questions = await api("/api/levels/" + level.id + "/questions");
    state.questionIndex = 0;
    el("levels-section").classList.add("hidden");
    el("quiz-section").classList.remove("hidden");
    el("level-title").textContent = level.title;
    el("level-description").textContent = level.description;
    showQuestion();
}

function showQuestion() {
    const q = state.questions[state.questionIndex];
    state.selectedOption = null;
    el("question-case").textContent = q.caseLabel;
    el("question-prompt").textContent = q.prompt;
    el("feedback").textContent = "";
    el("feedback").className = "feedback";
    el("hint-box").classList.add("hidden");
    el("rule-box").classList.add("hidden");
    el("next-btn").classList.add("hidden");
    el("submit-btn").classList.remove("hidden");
    const options = el("options");
    options.innerHTML = "";
    q.options.forEach((option, index) => {
        const btn = document.createElement("button");
        btn.className = "option";
        btn.textContent = option;
        btn.addEventListener("click", () => {
            state.selectedOption = index;
            options.querySelectorAll(".option").forEach((o) => o.classList.remove("selected"));
            btn.classList.add("selected");
        });
        options.appendChild(btn);
    });
    el("progress-fill").style.width = (100 * state.questionIndex / state.questions.length) + "%";
}

async function submitAnswer() {
    if (state.selectedOption === null) {
        return;
    }
    const q = state.questions[state.questionIndex];
    const result = await api("/api/questions/" + q.id + "/answer", {
        method: "POST",
        body: JSON.stringify({ userId: state.userId, optionIndex: state.selectedOption }),
    });
    const feedback = el("feedback");
    if (result.correct) {
        feedback.textContent = t().correct;
        feedback.classList.add("ok");
        el("options").children[result.correctIndex].classList.add("correct");
        el("submit-btn").classList.add("hidden");
        el("next-btn").classList.remove("hidden");
    } else {
        feedback.textContent = t().wrong;
        feedback.classList.add("ko");
        el("options").children[state.selectedOption].classList.add("wrong");
    }
    el("hint-box").textContent = result.hint;
    if (result.ruleTitle) {
        el("rule-box").textContent = result.ruleTitle + "\n\n" + result.ruleContent;
    }
    el("progress-fill").style.width = (100 * (state.questionIndex + 1) / state.questions.length) + "%";
    loadSummary();
}

async function loadSummary() {
    try {
        const summary = await api("/api/progress/" + state.userId + "/summary");
        const container = el("progress-summary");
        container.innerHTML = "";
        const overall = document.createElement("div");
        overall.className = "level-progress";
        overall.innerHTML = "<strong></strong><span></span>";
        overall.querySelector("strong").textContent = t().overall;
        overall.querySelector("span").textContent =
            summary.totalCorrect + " / " + summary.totalAttempts + " (" + summary.percent + " %)";
        container.appendChild(overall);
        for (const level of summary.levels) {
            const row = document.createElement("div");
            row.className = "level-progress";
            row.innerHTML = "<span></span><span></span>";
            row.querySelector("span").textContent = level.title;
            row.lastElementChild.textContent = level.correct + " / " + level.total + " (" + level.percent + " %)";
            container.appendChild(row);
        }
    } catch (e) {
        // progress not available yet
    }
}

el("lang-fr").addEventListener("click", () => { state.lang = "fr"; applyLang(); });
el("lang-en").addEventListener("click", () => { state.lang = "en"; applyLang(); });
el("back-btn").addEventListener("click", () => {
    el("quiz-section").classList.add("hidden");
    el("levels-section").classList.remove("hidden");
});
el("submit-btn").addEventListener("click", submitAnswer);
el("next-btn").addEventListener("click", () => {
    if (state.questionIndex + 1 < state.questions.length) {
        state.questionIndex++;
        showQuestion();
    } else {
        el("quiz-section").classList.add("hidden");
        el("levels-section").classList.remove("hidden");
        loadLevels();
    }
});
el("hint-btn").addEventListener("click", () => el("hint-box").classList.toggle("hidden"));
el("rule-btn").addEventListener("click", () => el("rule-box").classList.toggle("hidden"));

applyLang();
