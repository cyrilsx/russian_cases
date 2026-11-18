const state = {
    lang: localStorage.getItem("rc-lang") || "fr",
    theme: localStorage.getItem("rc-theme") || "auto",
    token: localStorage.getItem("rc-token") || null,
    username: localStorage.getItem("rc-username") || null,
    role: null,
    levels: [],
    questions: [],
    questionIndex: 0,
    selectedOption: null,
    view: "quiz",
    adminTab: "levels",
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
        overall: "Score global",
        authTitle: "Connexion",
        username: "Nom d'utilisateur",
        password: "Mot de passe (6 caractères min.)",
        login: "Se connecter",
        register: "Créer un compte",
        logout: "Se déconnecter",
        authError: "Échec de connexion : vérifie tes identifiants.",
        registerError: "Ce nom est déjà pris ou invalide.",
        loggedInAs: "Connecté : ",
        quiz: "Quiz",
        admin: "Administration",
        adminLevels: "Niveaux",
        adminQuestions: "Questions",
        adminRules: "Règles",
        adminUsers: "Utilisateurs",
        adminProgress: "Progression",
        add: "Ajouter",
        edit: "Modifier",
        del: "Supprimer",
        save: "Enregistrer",
        cancel: "Annuler",
        actions: "Actions",
        confirmDelete: "Confirmer la suppression ?",
        resetProgress: "Réinitialiser",
        confirmReset: "Réinitialiser toute la progression de cet utilisateur ?",
        role: "Rôle",
        createdAt: "Créé le",
        id: "ID",
        code: "Code",
        levelNumber: "Numéro",
        titleFr: "Titre (FR)",
        titleEn: "Titre (EN)",
        descriptionFr: "Description (FR)",
        descriptionEn: "Description (EN)",
        caseLabel: "Cas",
        promptFr: "Énoncé (FR)",
        promptEn: "Énoncé (EN)",
        optionsFr: "Options (FR), une par ligne",
        optionsEn: "Options (EN), une par ligne",
        correctIndex: "Index de la bonne réponse (0-based)",
        hintFr: "Indice (FR)",
        hintEn: "Indice (EN)",
        ruleId: "Règle liée",
        none: "(aucune)",
        level: "Niveau",
        attempts: "Essais",
        correctCount: "Justes",
        lastAttempt: "Dernier essai",
        questionId: "Question",
        userId: "Utilisateur",
        filterByLevel: "Filtrer par niveau",
        all: "Tous",
        saved: "Enregistré.",
        error: "Erreur",
        skipLink: "Aller au contenu principal",
        navLabel: "Navigation principale",
        footer: "Les cas russes — quiz, indices et rappels de règles pour maîtriser les six cas grammaticaux russes.",
        adminForm: "Formulaire d'administration",
        confirm: "Confirmer",
        ok: "OK",
        loading: "Chargement…",
        noLevels: "Aucun niveau disponible pour le moment.",
        noQuestions: "Aucune question dans ce niveau pour le moment.",
        loadError: "Impossible de charger les données.",
        retry: "Réessayer",
        questionOf: (i, n) => "Question " + i + " / " + n,
        optionsLabel: "Réponses possibles",
        progressLabel: "Progression dans le niveau",
        theme: "Thème",
        themeAuto: "automatique",
        themeLight: "clair",
        themeDark: "sombre",
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
        overall: "Overall score",
        authTitle: "Sign in",
        username: "Username",
        password: "Password (min. 6 characters)",
        login: "Sign in",
        register: "Create account",
        logout: "Sign out",
        authError: "Sign-in failed: check your credentials.",
        registerError: "This username is taken or invalid.",
        loggedInAs: "Signed in as ",
        quiz: "Quiz",
        admin: "Administration",
        adminLevels: "Levels",
        adminQuestions: "Questions",
        adminRules: "Rules",
        adminUsers: "Users",
        adminProgress: "Progress",
        add: "Add",
        edit: "Edit",
        del: "Delete",
        save: "Save",
        cancel: "Cancel",
        actions: "Actions",
        confirmDelete: "Confirm deletion?",
        resetProgress: "Reset",
        confirmReset: "Reset all progress of this user?",
        role: "Role",
        createdAt: "Created",
        id: "ID",
        code: "Code",
        levelNumber: "Number",
        titleFr: "Title (FR)",
        titleEn: "Title (EN)",
        descriptionFr: "Description (FR)",
        descriptionEn: "Description (EN)",
        caseLabel: "Case",
        promptFr: "Prompt (FR)",
        promptEn: "Prompt (EN)",
        optionsFr: "Options (FR), one per line",
        optionsEn: "Options (EN), one per line",
        correctIndex: "Correct option index (0-based)",
        hintFr: "Hint (FR)",
        hintEn: "Hint (EN)",
        ruleId: "Linked rule",
        none: "(none)",
        level: "Level",
        attempts: "Attempts",
        correctCount: "Correct",
        lastAttempt: "Last attempt",
        questionId: "Question",
        userId: "User",
        filterByLevel: "Filter by level",
        all: "All",
        saved: "Saved.",
        error: "Error",
        skipLink: "Skip to main content",
        navLabel: "Main navigation",
        footer: "Russian Cases — quizzes, hints and rule reminders to master the six Russian grammatical cases.",
        adminForm: "Administration form",
        confirm: "Confirm",
        ok: "OK",
        loading: "Loading…",
        noLevels: "No levels available yet.",
        noQuestions: "No questions in this level yet.",
        loadError: "Could not load the data.",
        retry: "Retry",
        questionOf: (i, n) => "Question " + i + " / " + n,
        optionsLabel: "Possible answers",
        progressLabel: "Progress in this level",
        theme: "Theme",
        themeAuto: "auto",
        themeLight: "light",
        themeDark: "dark",
    },
};

const t = () => i18n[state.lang];

function esc(s) {
    return String(s == null ? "" : s)
        .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}

async function api(path, options = {}) {
    const response = await fetch(path, {
        ...options,
        headers: {
            "Accept-Language": state.lang,
            "Content-Type": "application/json",
            ...(state.token ? { Authorization: "Bearer " + state.token } : {}),
            ...(options.headers || {}),
        },
    });
    if (response.status === 401 && state.token) {
        logout();
        throw new Error("Session expired");
    }
    if (!response.ok) {
        let message = "API error " + response.status;
        try {
            const body = await response.json();
            if (body && body.message) message = body.message;
        } catch (e) { /* keep default */ }
        throw new Error(message);
    }
    if (response.status === 204) {
        return null;
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
    el("auth-title").textContent = t().authTitle;
    el("username-label").textContent = t().username;
    el("password-label").textContent = t().password;
    el("login-btn").textContent = t().login;
    el("register-btn").textContent = t().register;
    el("logout-label").textContent = t().logout;
    el("skip-link").textContent = t().skipLink;
    el("main-nav").setAttribute("aria-label", t().navLabel);
    el("admin-view").setAttribute("aria-label", t().admin);
    el("admin-form").setAttribute("aria-label", t().adminForm);
    el("footer-text").textContent = t().footer;
    el("modal-cancel").textContent = t().cancel;
    el("options").setAttribute("aria-label", t().optionsLabel);
    el("progress-bar").setAttribute("aria-label", t().progressLabel);
    if (!el("quiz-section").classList.contains("hidden") && state.questions.length) {
        el("question-counter").textContent = t().questionOf(state.questionIndex + 1, state.questions.length);
    }
    el("nav-quiz-btn").textContent = t().quiz;
    el("nav-admin-btn").textContent = t().admin;
    document.querySelectorAll(".admin-tab").forEach((b) => {
        b.textContent = t()["admin" + b.dataset.tab.charAt(0).toUpperCase() + b.dataset.tab.slice(1)] || b.dataset.tab;
    });
    el("lang-fr").classList.toggle("active", state.lang === "fr");
    el("lang-en").classList.toggle("active", state.lang === "en");
    localStorage.setItem("rc-lang", state.lang);
    if (state.token) {
        loadLevels();
        loadSummary();
        if (state.view === "admin") renderAdminTab();
    }
}

function showAuth() {
    el("auth-section").classList.remove("hidden");
    el("main-content").classList.add("hidden");
    el("current-user").classList.add("hidden");
    el("logout-btn").classList.add("hidden");
}

function showApp() {
    el("auth-section").classList.add("hidden");
    el("main-content").classList.remove("hidden");
    el("current-user").textContent = t().loggedInAs + state.username;
    el("current-user").classList.remove("hidden");
    el("logout-btn").classList.remove("hidden");
}

async function authenticate(mode) {
    const feedback = el("auth-feedback");
    feedback.textContent = "";
    try {
        const result = await api("/api/auth/" + mode, {
            method: "POST",
            body: JSON.stringify({
                username: el("username").value.trim(),
                password: el("password").value,
            }),
        });
        state.token = result.token;
        state.username = result.username;
        localStorage.setItem("rc-token", result.token);
        localStorage.setItem("rc-username", result.username);
        showApp();
        applyLang();
        loadMe();
    } catch (e) {
        feedback.textContent = mode === "register" ? t().registerError : t().authError;
        feedback.className = "feedback ko";
    }
}

async function loadMe() {
    try {
        const me = await api("/api/auth/me");
        state.role = me.role;
        el("nav-admin-btn").classList.toggle("hidden", state.role !== "ADMIN");
    } catch (e) {
        state.role = null;
        el("nav-admin-btn").classList.add("hidden");
    }
}

function logout() {
    state.token = null;
    state.username = null;
    state.role = null;
    localStorage.removeItem("rc-token");
    localStorage.removeItem("rc-username");
    showAuth();
}

let modalResolve = null;

function openModal(message, mode) {
    const dlg = el("modal-dialog");
    el("modal-message").textContent = message;
    el("modal-ok").textContent = mode === "confirm" ? t().confirm : t().ok;
    el("modal-cancel").classList.toggle("hidden", mode !== "confirm");
    dlg.showModal();
    return new Promise((resolve) => { modalResolve = resolve; });
}

function askConfirm(message) {
    return openModal(message, "confirm");
}

function showAlert(message) {
    return openModal(message, "alert");
}

function applyTheme(theme) {
    state.theme = theme;
    localStorage.setItem("rc-theme", theme);
    if (theme === "auto") document.documentElement.removeAttribute("data-theme");
    else document.documentElement.setAttribute("data-theme", theme);
    const icons = { auto: "◐", light: "☀", dark: "🌙" };
    el("theme-icon").textContent = icons[theme];
    const label = t()["theme" + theme.charAt(0).toUpperCase() + theme.slice(1)];
    el("theme-btn").setAttribute("aria-label", t().theme + " : " + label);
}

function cycleTheme() {
    const order = ["auto", "light", "dark"];
    applyTheme(order[(order.indexOf(state.theme) + 1) % order.length]);
}

function toast(message, kind) {
    const item = document.createElement("div");
    item.className = "toast" + (kind ? " " + kind : "");
    item.textContent = message;
    el("toasts").appendChild(item);
    requestAnimationFrame(() => item.classList.add("show"));
    setTimeout(() => {
        item.classList.remove("show");
        setTimeout(() => item.remove(), 250);
    }, 3500);
}

function setLangAuto(node, text) {
    if (/[\u0400-\u04FF]/.test(text)) node.setAttribute("lang", "ru");
    else node.removeAttribute("lang");
}

function setView(view) {
    state.view = view;
    el("nav-quiz-btn").classList.toggle("active", view === "quiz");
    el("nav-admin-btn").classList.toggle("active", view === "admin");
    el("quiz-view").classList.toggle("hidden", view !== "quiz");
    el("admin-view").classList.toggle("hidden", view !== "admin");
    if (view === "admin") renderAdminTab();
}

async function loadLevels() {
    const container = el("levels");
    container.innerHTML = "<p class='loading' role='status'>" + esc(t().loading) + "</p>";
    let levels;
    try {
        levels = await api("/api/levels");
    } catch (e) {
        container.innerHTML = "<p class='feedback ko' role='alert'>" + esc(t().loadError) + "</p>" +
            "<button class='btn secondary' id='levels-retry'>" + esc(t().retry) + "</button>";
        el("levels-retry").addEventListener("click", loadLevels);
        return;
    }
    state.levels = levels;
    container.innerHTML = "";
    if (!levels.length) {
        container.innerHTML = "<p class='empty'>" + esc(t().noLevels) + "</p>";
        return;
    }
    for (const level of levels) {
        const card = document.createElement("button");
        card.className = "level-card";
        card.type = "button";
        card.innerHTML = "<h3></h3><p class='description'></p><small></small>";
        card.querySelector("h3").textContent = level.title;
        card.querySelector(".description").textContent = level.description;
        card.querySelector("small").textContent = level.questionCount + " " + t().questions;
        card.addEventListener("click", () => startLevel(level));
        container.appendChild(card);
    }
}

async function startLevel(level) {
    let questions;
    try {
        questions = await api("/api/levels/" + level.id + "/questions");
    } catch (e) {
        toast(e.message, "ko");
        return;
    }
    if (!questions.length) {
        toast(t().noQuestions, "ko");
        return;
    }
    state.questions = questions;
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
    setLangAuto(el("question-case"), q.caseLabel);
    el("question-prompt").textContent = q.prompt;
    setLangAuto(el("question-prompt"), q.prompt);
    el("feedback").textContent = "";
    el("feedback").className = "feedback";
    el("hint-box").classList.add("hidden");
    el("rule-box").classList.add("hidden");
    el("next-btn").classList.add("hidden");
    el("submit-btn").classList.remove("hidden");
    el("submit-btn").disabled = true;
    el("question-counter").textContent = t().questionOf(state.questionIndex + 1, state.questions.length);
    const options = el("options");
    options.innerHTML = "";
    q.options.forEach((option, index) => {
        const btn = document.createElement("button");
        btn.className = "option";
        btn.type = "button";
        btn.setAttribute("role", "radio");
        btn.setAttribute("aria-checked", "false");
        btn.textContent = option;
        setLangAuto(btn, option);
        btn.addEventListener("click", () => selectOption(index));
        options.appendChild(btn);
    });
    updateProgress(false);
}

function selectOption(index) {
    state.selectedOption = index;
    el("options").querySelectorAll(".option").forEach((o, i) => {
        o.classList.toggle("selected", i === index);
        o.setAttribute("aria-checked", i === index ? "true" : "false");
    });
    el("submit-btn").disabled = false;
}

function updateProgress(completed) {
    const total = state.questions.length || 1;
    const pct = Math.min(100, Math.round(100 * (state.questionIndex + (completed ? 1 : 0)) / total));
    el("progress-fill").style.width = pct + "%";
    el("progress-bar").setAttribute("aria-valuenow", String(pct));
}

async function submitAnswer() {
    if (state.selectedOption === null) {
        return;
    }
    const q = state.questions[state.questionIndex];
    const result = await api("/api/questions/" + q.id + "/answer", {
        method: "POST",
        body: JSON.stringify({ optionIndex: state.selectedOption }),
    });
    const feedback = el("feedback");
    if (result.correct) {
        feedback.textContent = t().correct;
        feedback.classList.add("ok");
        el("options").children[result.correctIndex].classList.add("correct");
        el("options").querySelectorAll(".option").forEach((o) => { o.disabled = true; });
        el("submit-btn").classList.add("hidden");
        el("next-btn").classList.remove("hidden");
        el("next-btn").focus();
    } else {
        feedback.textContent = t().wrong;
        feedback.classList.add("ko");
        el("options").children[state.selectedOption].classList.add("wrong");
    }
    el("hint-box").textContent = result.hint;
    setLangAuto(el("hint-box"), result.hint);
    if (result.ruleTitle) {
        el("rule-box").textContent = result.ruleTitle + "\n\n" + result.ruleContent;
        setLangAuto(el("rule-box"), result.ruleTitle + result.ruleContent);
    }
    updateProgress(true);
    loadSummary();
}

async function loadSummary() {
    try {
        const summary = await api("/api/progress/me/summary");
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

/* ---------------- Admin ---------------- */

async function renderAdminTab() {
    document.querySelectorAll(".admin-tab").forEach((b) =>
        b.classList.toggle("active", b.dataset.tab === state.adminTab));
    el("admin-form").close();
    const content = el("admin-content");
    content.innerHTML = "";
    try {
        if (state.adminTab === "levels") await renderAdminLevels(content);
        else if (state.adminTab === "questions") await renderAdminQuestions(content);
        else if (state.adminTab === "rules") await renderAdminRules(content);
        else if (state.adminTab === "users") await renderAdminUsers(content);
        else if (state.adminTab === "progress") await renderAdminProgress(content);
    } catch (e) {
        content.innerHTML = "<p class='feedback ko'>" + esc(e.message) + "</p>";
    }
}

function adminTable(headers, rowsHtml) {
    return "<table class='admin-table'><thead><tr>" +
        headers.map((h) => "<th>" + esc(h) + "</th>").join("") +
        "</tr></thead><tbody>" + rowsHtml + "</tbody></table>";
}

function actionButtons(onEdit, onDelete) {
    return "<button class='btn secondary act-edit'>" + esc(t().edit) + "</button> " +
        "<button class='btn secondary act-del'>" + esc(t().del) + "</button>";
}

async function renderAdminLevels(content) {
    const levels = await api("/api/admin/levels");
    let rows = "";
    for (const l of levels) {
        rows += "<tr><td>" + l.id + "</td><td>" + esc(l.code) + "</td><td>" + l.levelNumber + "</td><td>" +
            esc(l.titleFr) + "</td><td>" + esc(l.titleEn) + "</td><td>" + l.questionCount + "</td><td>" +
            actionButtons() + "</td></tr>";
    }
    content.innerHTML =
        "<button class='btn primary' id='admin-add'>" + esc(t().add) + "</button>" +
        adminTable([t().id, t().code, t().levelNumber, t().titleFr, t().titleEn, t().questions, t().actions], rows);
    el("admin-add").addEventListener("click", () => levelForm(null));
    content.querySelectorAll("tbody tr").forEach((tr, i) => {
        tr.querySelector(".act-edit").addEventListener("click", () => levelForm(levels[i]));
        tr.querySelector(".act-del").addEventListener("click", async () => {
            if (!(await askConfirm(t().confirmDelete))) return;
            try {
                await api("/api/admin/levels/" + levels[i].id, { method: "DELETE" });
                renderAdminTab();
            } catch (e) { showAlert(e.message); }
        });
    });
}

function levelForm(level) {
    showAdminForm([
        { key: "levelNumber", label: t().levelNumber, type: "number", value: level ? level.levelNumber : "" },
        { key: "code", label: t().code, value: level ? level.code : "" },
        { key: "titleFr", label: t().titleFr, value: level ? level.titleFr : "" },
        { key: "titleEn", label: t().titleEn, value: level ? level.titleEn : "" },
        { key: "descriptionFr", label: t().descriptionFr, type: "textarea", value: level ? level.descriptionFr : "" },
        { key: "descriptionEn", label: t().descriptionEn, type: "textarea", value: level ? level.descriptionEn : "" },
    ], async (values) => {
        const body = {
            levelNumber: Number(values.levelNumber),
            code: values.code,
            titleFr: values.titleFr,
            titleEn: values.titleEn,
            descriptionFr: values.descriptionFr,
            descriptionEn: values.descriptionEn,
        };
        if (level) await api("/api/admin/levels/" + level.id, { method: "PUT", body: JSON.stringify(body) });
        else await api("/api/admin/levels", { method: "POST", body: JSON.stringify(body) });
        renderAdminTab();
    });
}

async function renderAdminQuestions(content) {
    const [levels, questions, rules, cases] = await Promise.all([
        api("/api/admin/levels"), api("/api/admin/questions"), api("/api/admin/rules"), api("/api/cases"),
    ]);
    let filter = "<label>" + esc(t().filterByLevel) + " <select id='admin-q-level'>" +
        "<option value=''>" + esc(t().all) + "</option>" +
        levels.map((l) => "<option value='" + l.id + "'>" + esc(l.titleFr) + "</option>").join("") +
        "</select></label> ";
    let rows = "";
    for (const q of questions) {
        rows += "<tr data-level='" + q.levelId + "'><td>" + q.id + "</td><td>" + q.levelId + "</td><td>" +
            esc(q.caseName) + "</td><td>" + esc(q.promptFr) + "</td><td>" + q.correctIndex + "</td><td>" +
            actionButtons() + "</td></tr>";
    }
    content.innerHTML = filter +
        "<button class='btn primary' id='admin-add'>" + esc(t().add) + "</button>" +
        adminTable([t().id, t().level, t().caseLabel, t().promptFr, t().correctIndex, t().actions], rows);
    el("admin-add").addEventListener("click", () => questionForm(null, levels, rules, cases));
    el("admin-q-level").addEventListener("change", (e) => {
        const v = e.target.value;
        content.querySelectorAll("tbody tr").forEach((tr) => {
            tr.style.display = !v || tr.dataset.level === v ? "" : "none";
        });
    });
    content.querySelectorAll("tbody tr").forEach((tr, i) => {
        tr.querySelector(".act-edit").addEventListener("click", () => questionForm(questions[i], levels, rules, cases));
        tr.querySelector(".act-del").addEventListener("click", async () => {
            if (!(await askConfirm(t().confirmDelete))) return;
            try {
                await api("/api/admin/questions/" + questions[i].id, { method: "DELETE" });
                renderAdminTab();
            } catch (e) { showAlert(e.message); }
        });
    });
}

function questionForm(question, levels, rules, cases) {
    showAdminForm([
        { key: "levelId", label: t().level, type: "select", options: levels.map((l) => ({ value: l.id, label: l.titleFr })), value: question ? question.levelId : (levels[0] && levels[0].id) },
        { key: "caseName", label: t().caseLabel, type: "select", options: cases.map((c) => ({ value: c.name, label: c.label })), value: question ? question.caseName : cases[0].name },
        { key: "promptFr", label: t().promptFr, type: "textarea", value: question ? question.promptFr : "" },
        { key: "promptEn", label: t().promptEn, type: "textarea", value: question ? question.promptEn : "" },
        { key: "optionsFr", label: t().optionsFr, type: "textarea", value: question ? question.optionsFr.join("\n") : "" },
        { key: "optionsEn", label: t().optionsEn, type: "textarea", value: question ? question.optionsEn.join("\n") : "" },
        { key: "correctIndex", label: t().correctIndex, type: "number", value: question ? question.correctIndex : 0 },
        { key: "hintFr", label: t().hintFr, type: "textarea", value: question ? question.hintFr : "" },
        { key: "hintEn", label: t().hintEn, type: "textarea", value: question ? question.hintEn : "" },
        { key: "ruleId", label: t().ruleId, type: "select", options: [{ value: "", label: t().none }].concat(rules.map((r) => ({ value: r.id, label: r.caseName }))), value: question && question.ruleId != null ? question.ruleId : "" },
    ], async (values) => {
        const split = (s) => s.split("\n").map((x) => x.trim()).filter((x) => x.length > 0);
        const body = {
            levelId: Number(values.levelId),
            caseName: values.caseName,
            promptFr: values.promptFr,
            promptEn: values.promptEn,
            optionsFr: split(values.optionsFr),
            optionsEn: split(values.optionsEn),
            correctIndex: Number(values.correctIndex),
            hintFr: values.hintFr,
            hintEn: values.hintEn,
            ruleId: values.ruleId === "" ? null : Number(values.ruleId),
        };
        if (question) await api("/api/admin/questions/" + question.id, { method: "PUT", body: JSON.stringify(body) });
        else await api("/api/admin/questions", { method: "POST", body: JSON.stringify(body) });
        renderAdminTab();
    });
}

async function renderAdminRules(content) {
    const rules = await api("/api/admin/rules");
    let rows = "";
    for (const r of rules) {
        rows += "<tr><td>" + r.id + "</td><td>" + esc(r.caseName) + "</td><td>" + esc(r.titleFr) + "</td><td>" +
            esc(r.titleEn) + "</td><td>" + actionButtons() + "</td></tr>";
    }
    content.innerHTML =
        "<button class='btn primary' id='admin-add'>" + esc(t().add) + "</button>" +
        adminTable([t().id, t().caseLabel, t().titleFr, t().titleEn, t().actions], rows);
    el("admin-add").addEventListener("click", () => ruleForm(null, rules));
    content.querySelectorAll("tbody tr").forEach((tr, i) => {
        tr.querySelector(".act-edit").addEventListener("click", () => ruleForm(rules[i], rules));
        tr.querySelector(".act-del").addEventListener("click", async () => {
            if (!(await askConfirm(t().confirmDelete))) return;
            try {
                await api("/api/admin/rules/" + rules[i].id, { method: "DELETE" });
                renderAdminTab();
            } catch (e) { showAlert(e.message); }
        });
    });
}

function ruleForm(rule, existing) {
    const taken = new Set(existing.map((r) => r.caseName));
    showAdminForm([
        { key: "caseName", label: t().caseLabel, type: "select", options: ["NOMINATIVE", "GENITIVE", "DATIVE", "ACCUSATIVE", "INSTRUMENTAL", "PREPOSITIONAL"].map((c) => ({ value: c, label: c })), value: rule ? rule.caseName : "" },
        { key: "titleFr", label: t().titleFr, value: rule ? rule.titleFr : "" },
        { key: "titleEn", label: t().titleEn, value: rule ? rule.titleEn : "" },
        { key: "contentFr", label: t().descriptionFr, type: "textarea", rows: 6, value: rule ? rule.contentFr : "" },
        { key: "contentEn", label: t().descriptionEn, type: "textarea", rows: 6, value: rule ? rule.contentEn : "" },
    ], async (values) => {
        const body = {
            caseName: values.caseName,
            titleFr: values.titleFr,
            titleEn: values.titleEn,
            contentFr: values.contentFr,
            contentEn: values.contentEn,
        };
        if (rule) await api("/api/admin/rules/" + rule.id, { method: "PUT", body: JSON.stringify(body) });
        else await api("/api/admin/rules", { method: "POST", body: JSON.stringify(body) });
        renderAdminTab();
    });
}

async function renderAdminUsers(content) {
    const users = await api("/api/admin/users");
    let rows = "";
    for (const u of users) {
        const roleOptions = ["USER", "ADMIN"].map((r) =>
            "<option value='" + r + "'" + (u.role === r ? " selected" : "") + ">" + r + "</option>").join("");
        rows += "<tr><td>" + u.id + "</td><td>" + esc(u.username) + "</td><td><select class='role-select'>" +
            roleOptions + "</select></td><td>" + esc((u.createdAt || "").slice(0, 19).replace("T", " ")) + "</td><td>" +
            "<button class='btn secondary act-del'>" + esc(t().del) + "</button></td></tr>";
    }
    content.innerHTML = adminTable([t().id, t().username, t().role, t().createdAt, t().actions], rows);
    content.querySelectorAll("tbody tr").forEach((tr, i) => {
        tr.querySelector(".role-select").addEventListener("change", async (e) => {
            try {
                await api("/api/admin/users/" + users[i].id + "/role", {
                    method: "PUT",
                    body: JSON.stringify({ role: e.target.value }),
                });
            } catch (err) { showAlert(err.message); renderAdminTab(); }
        });
        tr.querySelector(".act-del").addEventListener("click", async () => {
            if (!(await askConfirm(t().confirmDelete))) return;
            try {
                await api("/api/admin/users/" + users[i].id, { method: "DELETE" });
                renderAdminTab();
            } catch (e) { showAlert(e.message); }
        });
    });
}

async function renderAdminProgress(content) {
    const progress = await api("/api/admin/progress");
    const seen = new Set();
    let rows = "";
    for (const p of progress) {
        if (seen.has(p.userId)) continue;
        seen.add(p.userId);
        rows += "<tr><td>" + esc(p.userId) + "</td><td>" +
            "<button class='btn secondary act-reset'>" + esc(t().resetProgress) + "</button></td></tr>";
    }
    content.innerHTML = adminTable([t().userId, t().actions], rows);
    content.querySelectorAll("tbody tr").forEach((tr) => {
        tr.querySelector(".act-reset").addEventListener("click", async () => {
            const userId = tr.firstElementChild.textContent;
            if (!(await askConfirm(t().confirmReset))) return;
            try {
                await api("/api/admin/progress/" + encodeURIComponent(userId), { method: "DELETE" });
                renderAdminTab();
            } catch (e) { showAlert(e.message); }
        });
    });
}

function showAdminForm(fields, onSubmit) {
    const form = el("admin-form");
    let html = "";
    for (const f of fields) {
        html += "<label>" + esc(f.label) + "</label>";
        if (f.type === "textarea") {
            html += "<textarea class='fld' data-key='" + f.key + "' rows='" + (f.rows || 3) + "'>" + esc(f.value) + "</textarea>";
        } else if (f.type === "select") {
            html += "<select class='fld' data-key='" + f.key + "'>" +
                f.options.map((o) => "<option value='" + esc(o.value) + "'" + (String(o.value) === String(f.value) ? " selected" : "") + ">" + esc(o.label) + "</option>").join("") +
                "</select>";
        } else {
            html += "<input class='fld' type='" + (f.type || "text") + "' data-key='" + f.key + "' value='" + esc(f.value) + "'>";
        }
    }
    html += "<div class='actions'><button class='btn primary' id='admin-save'>" + esc(t().save) + "</button>" +
        "<button class='btn secondary' id='admin-cancel'>" + esc(t().cancel) + "</button></div>" +
        "<p id='admin-form-msg' class='feedback'></p>";
    form.innerHTML = html;
    form.onclick = (e) => { if (e.target === form) form.close(); };
    el("admin-cancel").addEventListener("click", () => form.close());
    el("admin-save").addEventListener("click", async () => {
        const values = {};
        form.querySelectorAll(".fld").forEach((input) => { values[input.dataset.key] = input.value; });
        try {
            await onSubmit(values);
            form.close();
            toast(t().saved, "ok");
        } catch (e) {
            el("admin-form-msg").textContent = e.message;
            el("admin-form-msg").className = "feedback ko";
        }
    });
    form.showModal();
}

/* ---------------- wiring ---------------- */

el("lang-fr").addEventListener("click", () => { state.lang = "fr"; applyLang(); });
el("lang-en").addEventListener("click", () => { state.lang = "en"; applyLang(); });
el("login-btn").addEventListener("click", () => authenticate("login"));
el("register-btn").addEventListener("click", () => authenticate("register"));
el("logout-btn").addEventListener("click", logout);
el("nav-quiz-btn").addEventListener("click", () => setView("quiz"));
el("nav-admin-btn").addEventListener("click", () => setView("admin"));
document.querySelectorAll(".admin-tab").forEach((b) =>
    b.addEventListener("click", () => { state.adminTab = b.dataset.tab; renderAdminTab(); }));
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
el("modal-ok").addEventListener("click", () => {
    el("modal-dialog").close();
    if (modalResolve) { modalResolve(true); modalResolve = null; }
});
el("modal-cancel").addEventListener("click", () => {
    el("modal-dialog").close();
    if (modalResolve) { modalResolve(false); modalResolve = null; }
});
el("modal-dialog").addEventListener("click", (e) => {
    if (e.target === e.currentTarget) {
        e.currentTarget.close();
        if (modalResolve) { modalResolve(false); modalResolve = null; }
    }
});
el("theme-btn").addEventListener("click", cycleTheme);
el("options").addEventListener("keydown", (e) => {
    if (!["ArrowDown", "ArrowRight", "ArrowUp", "ArrowLeft"].includes(e.key)) return;
    const opts = Array.from(el("options").querySelectorAll(".option"));
    if (!opts.length) return;
    const cur = opts.indexOf(document.activeElement);
    e.preventDefault();
    let next;
    if (e.key === "ArrowDown" || e.key === "ArrowRight") next = cur < 0 ? 0 : (cur + 1) % opts.length;
    else next = cur < 0 ? opts.length - 1 : (cur - 1 + opts.length) % opts.length;
    opts[next].focus();
    selectOption(next);
});
[el("username"), el("password")].forEach((input) =>
    input.addEventListener("keydown", (e) => { if (e.key === "Enter") authenticate("login"); }));

applyTheme(state.theme);
applyLang();
if (state.token) {
    showApp();
    loadMe();
} else {
    showAuth();
}
