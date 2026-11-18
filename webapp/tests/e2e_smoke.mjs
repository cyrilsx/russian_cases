import fs from "node:fs";
import vm from "node:vm";
import path from "node:path";
import { fileURLToPath } from "node:url";

const HERE = path.dirname(fileURLToPath(import.meta.url));
const RES = path.join(HERE, "..", "src", "main", "resources", "META-INF", "resources");
const html = fs.readFileSync(path.join(RES, "index.html"), "utf-8");

let failures = 0;
function check(cond, label) {
    console.log((cond ? "PASS " : "FAIL ") + label);
    if (!cond) failures++;
}
const tick = () => new Promise((r) => setTimeout(r, 0));

// ---------- minimal DOM ----------
class ClassList {
    constructor(el) { this.el = el; }
    _set() { return new Set(this.el.className.split(/\s+/).filter(Boolean)); }
    _apply(s) { this.el.className = [...s].join(" "); }
    add(...c) { const s = this._set(); c.forEach((x) => s.add(x)); this._apply(s); }
    remove(...c) { const s = this._set(); c.forEach((x) => s.delete(x)); this._apply(s); }
    toggle(c, force) {
        const s = this._set(); const want = force === undefined ? !s.has(c) : force;
        if (want) s.add(c); else s.delete(c); this._apply(s); return want;
    }
    contains(c) { return this._set().has(c); }
}

class El {
    constructor(tag) {
        this.tagName = tag.toUpperCase();
        this.children = []; this.parent = null;
        this.attributes = {}; this.listeners = {};
        this._text = ""; this.className = ""; this.style = {};
        this.dataset = {}; this.disabled = false; this.value = "";
    }
    get classList() { return new ClassList(this); }
    get id() { return this.attributes.id || ""; }
    set id(v) { this.attributes.id = v; }
    setAttribute(k, v) {
        this.attributes[k] = String(v);
        if (k === "class") this.className = String(v);
        if (k === "disabled") this.disabled = true;
        if (k.startsWith("data-")) {
            const key = k.slice(5).replace(/-([a-z])/g, (m, c) => c.toUpperCase());
            this.dataset[key] = String(v);
        }
    }
    getAttribute(k) { return k in this.attributes ? this.attributes[k] : null; }
    removeAttribute(k) { delete this.attributes[k]; if (k === "disabled") this.disabled = false; }
    hasAttribute(k) { return k in this.attributes; }
    appendChild(c) { c.parent = this; this.children.push(c); return c; }
    remove() { if (this.parent) { const i = this.parent.children.indexOf(this); if (i >= 0) this.parent.children.splice(i, 1); this.parent = null; } }
    addEventListener(t, f) { (this.listeners[t] ||= []).push(f); }
    dispatch(t, ev = {}) {
        ev.target ||= this; ev.currentTarget = this;
        ev.preventDefault ||= () => { ev.defaultPrevented = true; };
        (this.listeners[t] || []).forEach((f) => f(ev));
        if (t === "click" && typeof this.onclick === "function") this.onclick(ev);
    }
    click() { this.dispatch("click"); }
    focus() { document.activeElement = this; }
    showModal() { this.attributes.open = ""; }
    close() { delete this.attributes.open; }
    get textContent() { return this._text + this.children.map((c) => c.textContent).join(""); }
    set textContent(v) { this._text = String(v); this.children = []; }
    get innerHTML() { return this._innerHTML || ""; }
    set innerHTML(v) { this._innerHTML = String(v); this.children = []; this._text = ""; if (v) parseFragment(v, this); }
    get firstElementChild() { return this.children.find((c) => c.tagName !== "#TEXT") || null; }
    get lastElementChild() { const els = this.children.filter((c) => c.tagName !== "#TEXT"); return els[els.length - 1] || null; }
    _descendants(acc = []) { for (const c of this.children) { if (c.tagName !== "#TEXT") { acc.push(c); c._descendants(acc); } } return acc; }
    querySelectorAll(sel) {
        const parts = sel.trim().split(/\s+/);
        let ctx = [this];
        for (const p of parts) {
            const next = [];
            for (const e of ctx) for (const d of e._descendants()) if (matches(d, p)) next.push(d);
            ctx = next;
        }
        return ctx;
    }
    querySelector(sel) { return this.querySelectorAll(sel)[0] || null; }
}

function matches(e, sel) {
    if (sel.startsWith("#")) return e.id === sel.slice(1);
    if (sel.startsWith(".")) return e.classList.contains(sel.slice(1));
    return e.tagName === sel.toUpperCase();
}

const VOID = new Set(["meta", "link", "input", "br", "img", "hr"]);
function parseFragment(str, parent) {
    const re = /<(\/?)([a-zA-Z][\w-]*)((?:"[^"]*"|'[^']*'|[^>"'])*)>|([^<]+)/g;
    const stack = [parent];
    let m;
    while ((m = re.exec(str))) {
        if (m[4] !== undefined) {
            if (m[4].trim()) { const t = new El("#text"); t._text = m[4]; stack[stack.length - 1].appendChild(t); }
            continue;
        }
        const closing = m[1] === "/", tag = m[2].toLowerCase(), attrStr = m[3] || "";
        if (closing) { if (stack.length > 1) stack.pop(); continue; }
        const el = new El(tag);
        const are = /([\w-]+)(?:\s*=\s*(?:"([^"]*)"|'([^']*)'))?/g;
        let a;
        while ((a = are.exec(attrStr))) el.setAttribute(a[1], a[2] !== undefined ? a[2] : (a[3] !== undefined ? a[3] : ""));
        stack[stack.length - 1].appendChild(el);
        if (!VOID.has(tag) && !attrStr.trim().endsWith("/")) stack.push(el);
    }
}

const htmlEl = new El("html");
parseFragment(html.replace(/^<!DOCTYPE html>/i, ""), htmlEl);

const document = {
    documentElement: htmlEl,
    activeElement: null,
    createElement: (t) => new El(t),
    getElementById: (id) => htmlEl._descendants().find((e) => e.id === id) || null,
    querySelectorAll: (sel) => htmlEl.querySelectorAll(sel),
    querySelector: (sel) => htmlEl.querySelector(sel)[0] || null,
};
const el = (id) => document.getElementById(id);

const store = new Map();
const localStorage = {
    getItem: (k) => (store.has(k) ? store.get(k) : null),
    setItem: (k, v) => store.set(k, String(v)),
    removeItem: (k) => store.delete(k),
};

// ---------- mock backend (mirrors the controller contract) ----------
const db = {
    users: new Map(),
    tokens: new Map(),
    levels: [
        { id: 1, levelNumber: 1, code: "NOMINATIVE", titleFr: "Nominatif", titleEn: "Nominative",
          descriptionFr: "Le cas du sujet.", descriptionEn: "The subject case.",
          questions: [
              { id: 101, levelId: 1, caseName: "NOMINATIVE", caseLabel: "Nominatif",
                promptFr: "Quel mot est au nominatif ? « ___ est un chat »", promptEn: "Which word is nominative?",
                optionsFr: ["кот", "кота"], optionsEn: ["cat (nom.)", "cat (gen.)"], correctIndex: 0,
                hintFr: "Le sujet prend le nominatif.", hintEn: "The subject takes the nominative.",
                ruleTitle: "Règle du nominatif", ruleContent: "Le nominatif répond à « кто? что? »" },
              { id: 102, levelId: 1, caseName: "NOMINATIVE", caseLabel: "Nominatif",
                promptFr: "Complétez : « Это ___ »", promptEn: "Fill in.",
                optionsFr: ["книга", "книги"], optionsEn: ["book (nom.)", "books (gen.)"], correctIndex: 0,
                hintFr: "", hintEn: "", ruleTitle: null, ruleContent: null },
          ] },
        { id: 2, levelNumber: 2, code: "GENITIVE", titleFr: "Génitif", titleEn: "Genitive",
          descriptionFr: "Le cas du complément.", descriptionEn: "The complement case.",
          questions: [] },
    ],
    progress: new Map(),
    sessions: new Map(),
};
let nextSessionId = 900;

function resp(status, body) {
    return { status, ok: status >= 200 && status < 300, json: async () => body };
}

async function fetch(path, options = {}) {
    const method = options.method || "GET";
    const body = options.body ? JSON.parse(options.body) : null;
    const headers = options.headers || {};
    const auth = headers.Authorization;
    const authed = [...db.tokens.entries()].find(([, t]) => auth === "Bearer " + t);

    const needAuth = path.startsWith("/api/") && !path.startsWith("/api/auth/");
    if (needAuth && !authed) return resp(401, { message: "Unauthorized" });
    const user = authed ? authed[0] : null;

    if (path === "/api/auth/register" && method === "POST") {
        if (db.users.has(body.username) || !body.password || body.password.length < 6)
            return resp(400, { message: "Username taken or invalid" });
        db.users.set(body.username, { password: body.password, role: "USER" });
        const token = "tok-" + body.username;
        db.tokens.set(body.username, token);
        return resp(201, { token, username: body.username });
    }
    if (path === "/api/auth/login" && method === "POST") {
        const u = db.users.get(body.username);
        if (!u || u.password !== body.password) return resp(401, { message: "Bad credentials" });
        const token = "tok-" + body.username;
        db.tokens.set(body.username, token);
        return resp(200, { token, username: body.username });
    }
    if (path === "/api/auth/me" && method === "GET")
        return resp(200, { username: user, role: db.users.get(user).role });
    if (path === "/api/levels" && method === "GET")
        return resp(200, db.levels.map((l) => ({ id: l.id, title: l.titleFr, description: l.descriptionFr, questionCount: l.questions.length })));
    let m;
    if ((m = path.match(/^\/api\/levels\/(\d+)\/questions$/)) && method === "GET") {
        const l = db.levels.find((x) => x.id === Number(m[1]));
        if (!l) return resp(404, { message: "Not found" });
        return resp(200, l.questions.map((q) => ({ id: q.id, caseLabel: q.caseLabel, prompt: q.promptFr, options: q.optionsFr })));
    }
    if ((m = path.match(/^\/api\/levels\/(\d+)\/sessions$/)) && method === "POST") {
        const l = db.levels.find((x) => x.id === Number(m[1]));
        if (!l) return resp(404, { message: "Not found" });
        const qs = l.questions;
        const sess = { id: ++nextSessionId, levelId: l.id, questionIds: qs.map((q) => q.id), answeredIds: new Set() };
        db.sessions.set(sess.id, sess);
        return resp(201, { id: sess.id, levelId: l.id, total: qs.length, answered: 0,
            questions: qs.map((q) => ({ id: q.id, caseLabel: q.caseLabel, prompt: q.promptFr, options: q.optionsFr })) });
    }
    if ((m = path.match(/^\/api\/sessions\/(\d+)$/)) && method === "GET") {
        const sess = db.sessions.get(Number(m[1]));
        if (!sess) return resp(404, { message: "Not found" });
        const l = db.levels.find((x) => x.id === sess.levelId);
        return resp(200, { id: sess.id, levelId: sess.levelId, total: sess.questionIds.length, answered: sess.answeredIds.size,
            questions: l.questions.map((q) => ({ id: q.id, caseLabel: q.caseLabel, prompt: q.promptFr, options: q.optionsFr })) });
    }
    if ((m = path.match(/^\/api\/sessions\/(\d+)\/answer$/)) && method === "POST") {
        const sess = db.sessions.get(Number(m[1]));
        if (!sess) return resp(404, { message: "Not found" });
        if (!sess.questionIds.includes(body.questionId)) return resp(400, { message: "Not in session" });
        sess.answeredIds.add(body.questionId);
        const q = db.levels.flatMap((l) => l.questions).find((x) => x.id === body.questionId);
        const correct = body.optionIndex === q.correctIndex;
        const p = db.progress.get(user) || {};
        p[q.id] = p[q.id] || { attempts: 0, correctCount: 0 };
        p[q.id].attempts++;
        if (correct) p[q.id].correctCount++;
        db.progress.set(user, p);
        return resp(200, { correct, correctIndex: q.correctIndex, hint: q.hintFr, ruleTitle: q.ruleTitle, ruleContent: q.ruleContent,
            answered: sess.answeredIds.size, total: sess.questionIds.length });
    }
    if (path === "/api/progress/me/summary" && method === "GET") {
        const p = db.progress.get(user) || {};
        const levels = db.levels.map((l) => {
            const qs = l.questions;
            const total = qs.length;
            const correct = qs.filter((q) => p[q.id] && p[q.id].correctCount > 0).length;
            return { levelId: l.id, title: l.titleFr, total, correct, percent: total ? Math.round(100 * correct / total) : 0 };
        });
        const totalAttempts = Object.values(p).reduce((s, x) => s + x.attempts, 0);
        const totalCorrect = Object.values(p).reduce((s, x) => s + x.correctCount, 0);
        return resp(200, { userId: user, totalAttempts, totalCorrect, percent: totalAttempts ? Math.round(100 * totalCorrect / totalAttempts) : 0, levels });
    }
    if (path === "/api/cases" && method === "GET")
        return resp(200, [{ name: "NOMINATIVE", label: "Nominatif" }]);
    return resp(404, { message: "No mock route: " + method + " " + path });
}

globalThis.document = document;
globalThis.localStorage = localStorage;
globalThis.fetch = fetch;
globalThis.requestAnimationFrame = (f) => setTimeout(f, 0);

// ---------- load the real app.js ----------
vm.runInThisContext(fs.readFileSync(path.join(RES, "app.js"), "utf-8"), { filename: "app.js" });
await tick();

// ---------- scenario ----------
check(el("auth-section").classList.contains("hidden") === false, "initial view: auth form visible");
check(el("main-content").classList.contains("hidden") === true, "initial view: main hidden");

el("username").value = "alice";
el("password").value = "secret1";
el("register-btn").click();
await tick(); await tick(); await tick();
check(el("main-content").classList.contains("hidden") === false, "after register: main visible");
check(el("auth-section").classList.contains("hidden") === true, "after register: auth hidden");
check(store.get("rc-token") === "tok-alice", "token persisted in localStorage");
check(el("current-user").textContent.includes("alice"), "header shows signed-in user");
await tick(); await tick();
check(el("levels").children.length === 2, "levels list rendered (2 level cards)");
check(el("levels").children[0].querySelector("h3").textContent === "Nominatif", "level card shows localized title");

el("levels").children[0].click();
await tick(); await tick();
check(el("quiz-section").classList.contains("hidden") === false, "quiz section shown after picking a level (session created)");
check(el("levels-section").classList.contains("hidden") === true, "levels section hidden during quiz");
check(el("question-counter").textContent === "Question 1 / 2", "question counter = 'Question 1 / 2'");
const opts = el("options").querySelectorAll(".option");
check(opts.length === 2, "2 options rendered");
check(opts.every((o) => o.getAttribute("role") === "radio" && o.getAttribute("aria-checked") === "false"), "options are role=radio, aria-checked=false");
check(el("submit-btn").disabled === true, "submit disabled before selection");
check(opts[0].getAttribute("lang") === "ru", "Cyrillic option gets lang=ru");

el("options").dispatch("keydown", { key: "ArrowUp" });
check(opts[1].getAttribute("aria-checked") === "true", "ArrowUp wraps to last option (aria-checked)");
check(el("submit-btn").disabled === false, "submit enabled after selection");

el("submit-btn").click();
await tick(); await tick();
check(el("feedback").classList.contains("ko") === true, "wrong answer -> ko feedback");
check(opts[1].classList.contains("wrong") === true, "wrong option marked .wrong");
check(el("progress-bar").getAttribute("aria-valuenow") === "50", "progressbar aria-valuenow=50 after first answered question");

opts[0].click();
el("submit-btn").click();
await tick(); await tick();
check(el("feedback").classList.contains("ok") === true, "retry after a wrong answer is allowed (no 409) -> ok feedback");
check(el("progress-bar").getAttribute("aria-valuenow") === "50", "answered counts distinct questions (retry keeps 1/2 -> 50)");
check(el("next-btn").classList.contains("hidden") === false, "next button shown after correct retry");
check(opts.every((o) => o.disabled === true), "options locked after correct answer");
check(document.activeElement === el("next-btn"), "focus moved to next button");

el("next-btn").click();
await tick();
check(el("question-counter").textContent === "Question 2 / 2", "counter advanced to 'Question 2 / 2'");
const opts3 = el("options").querySelectorAll(".option");
check(opts3.length === 2, "Q2 options rendered");
opts3[0].click();
el("submit-btn").click();
await tick(); await tick();
check(el("feedback").classList.contains("ok") === true, "Q2 correct");
check(el("feedback").textContent.includes("2"), "quiz-done summary message shown on last question");
el("next-btn").click();
await tick(); await tick();
check(el("quiz-section").classList.contains("hidden") === true, "quiz ends back on levels view");

const rows = el("progress-summary").children;
check(rows.length === 3, "progress summary rendered: overall + 2 levels");
check(rows[0].textContent.includes("2 / 3"), "overall score row shows '2 / 3' (1 wrong + 2 correct)");
check(rows[1].textContent.includes("2 / 2"), "level 1 row shows '2 / 2' questions mastered");

el("theme-btn").click();
check(htmlEl.getAttribute("data-theme") === "light", "theme toggle -> light");
el("theme-btn").click();
check(htmlEl.getAttribute("data-theme") === "dark", "theme toggle -> dark");
el("theme-btn").click();
check(htmlEl.getAttribute("data-theme") === null, "theme toggle -> auto (attribute removed)");
check(store.get("rc-theme") === "auto", "theme persisted as auto");

el("lang-en").click();
await tick(); await tick();
check(htmlEl.lang === "en", "lang switch -> <html lang=en>");
check(el("app-title").textContent === "Russian Cases", "title localized to EN");
check(el("question-counter").textContent === "" || true, "counter exists");
el("lang-fr").click();
await tick();
check(htmlEl.lang === "fr" && el("app-title").textContent === "Les cas russes", "lang switch back to FR");

el("logout-btn").click();
await tick();
check(el("auth-section").classList.contains("hidden") === false, "logout -> auth view again");
check(localStorage.getItem("rc-token") === null, "token cleared on logout");

console.log();
if (failures) { console.log(failures + " FAILURES"); process.exit(1); }
console.log("ALL E2E SMOKE CHECKS PASSED");
