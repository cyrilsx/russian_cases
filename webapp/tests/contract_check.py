import re, sys, pathlib

ROOT = pathlib.Path(__file__).resolve().parents[2]
WEB = ROOT / "webapp/src/main/resources/META-INF/resources"
JAVA_WEB = ROOT / "backend/src/main/java/com/cyrilsx/russiancases/web"
JAVA_DTO = JAVA_WEB / "dto"

js = (WEB / "app.js").read_text(encoding="utf-8")

failures = []
def check(cond, label):
    print(("PASS " if cond else "FAIL ") + label)
    if not cond:
        failures.append(label)

# ---- 1. routes: controllers -> (method, template); app.js -> (method, regex) ----
routes = []
for f in JAVA_WEB.glob("*Controller.java"):
    src = f.read_text(encoding="utf-8")
    m = re.search(r'@RequestMapping\("([^"]+)"\)', src)
    base = m.group(1) if m else ""
    for mm in re.finditer(r'@(Get|Post|Put|Delete)Mapping(?:\("([^"]+)"\))?\s*\n\s*public', src):
        method = mm.group(1).upper() + ("" if mm.group(1) == "Get" else "")
        method = {"Get": "GET", "Post": "POST", "Put": "PUT", "Delete": "DELETE"}[mm.group(1)]
        path = base + (mm.group(2) or "")
        routes.append((method, path))

def template_to_regex(t):
    return "^" + re.sub(r"\{\w+\}", r"[^/]+", t) + "$"

route_re = [(m, re.compile(template_to_regex(p))) for m, p in routes]

js_calls = []
for m in re.finditer(r'api\("([^"]+)"(?:\s*,\s*\{\s*method:\s*"(\w+)")?', js):
    path, method = m.group(1), m.group(2) or "GET"
    if path.endswith("/"):
        continue  # prefix of a concatenated call, covered by the explicit list below
    js_calls.append((method, path))
# add the concatenated ones explicitly (they appear as api("/api/x/" + ...))
concat = [
    ("POST", "/api/auth/register"),
    ("POST", "/api/auth/login"),
    ("GET", "/api/levels/{id}/questions"),
    ("POST", "/api/questions/{id}/answer"),
    ("DELETE", "/api/admin/levels/{id}"),
    ("PUT", "/api/admin/levels/{id}"),
    ("DELETE", "/api/admin/questions/{id}"),
    ("PUT", "/api/admin/questions/{id}"),
    ("DELETE", "/api/admin/rules/{id}"),
    ("PUT", "/api/admin/rules/{id}"),
    ("PUT", "/api/admin/users/{id}/role"),
    ("DELETE", "/api/admin/users/{id}"),
    ("DELETE", "/api/admin/progress/{userId}"),
]
for method, tpl in concat + [(m, p) for m, p in js_calls]:
    rx = re.compile(template_to_regex(tpl))
    check(any(rm == method and rr.match(tpl) for rm, rr in route_re),
          f"route {method} {tpl} exists in backend controllers")

# ---- 2. DTO fields used by app.js ----
def record_fields(name):
    src = (JAVA_DTO / (name + ".java")).read_text(encoding="utf-8")
    start = re.search(r"record\s+\w+\s*\(", src).end()
    depth, i = 1, start
    while depth > 0 and i < len(src):
        if src[i] == "(": depth += 1
        elif src[i] == ")": depth -= 1
        i += 1
    body = src[start:i-1]
    words = re.split(r"[\s,()<>;]+", body)
    types = {"String", "Long", "long", "int", "boolean", "List", "Instant", "Integer", "Map", "Set", "record"}
    return {w for w in words if w and w not in types}

usage = {
    "AuthResponse": ["token", "username"],
    "LevelDto": ["id", "title", "description", "questionCount"],
    "QuestionDto": ["id", "caseLabel", "prompt", "options"],
    "AnswerResponse": ["correct", "correctIndex", "hint", "ruleTitle", "ruleContent"],
    "ProgressSummaryDto": ["totalCorrect", "totalAttempts", "percent", "levels"],
    "LevelProgressDto": ["title", "correct", "total", "percent"],
    "CaseDto": ["name", "label"],
    "AdminLevelDto": ["id", "code", "levelNumber", "titleFr", "titleEn", "questionCount"],
    "AdminQuestionDto": ["id", "levelId", "caseName", "promptFr", "correctIndex", "optionsFr", "optionsEn", "hintFr", "hintEn", "ruleId"],
    "AdminRuleDto": ["id", "caseName", "titleFr", "titleEn"],
    "UserDto": ["id", "username", "role", "createdAt"],
    "AdminProgressDto": ["userId"],
    "LevelUpsertRequest": ["levelNumber", "code", "titleFr", "titleEn", "descriptionFr", "descriptionEn"],
    "QuestionUpsertRequest": ["levelId", "caseName", "promptFr", "promptEn", "optionsFr", "optionsEn", "correctIndex", "hintFr", "hintEn", "ruleId"],
    "RuleUpsertRequest": ["caseName", "titleFr", "titleEn", "contentFr", "contentEn"],
    "AnswerRequest": ["optionIndex"],
    "AuthRequest": ["username", "password"],
    "RoleUpdateRequest": ["role"],
}
for dto, fields in usage.items():
    have = record_fields(dto)
    for f in fields:
        check(f in have, f"{dto}.{f} used by app.js exists")

# /api/auth/me returns Map.of("username", ..., "role", ...) -> app.js uses me.role
auth_src = (JAVA_WEB / "AuthController.java").read_text(encoding="utf-8")
check('"role"' in auth_src and '"username"' in auth_src, 'GET /api/auth/me exposes username+role (app.js uses me.role)')

# ---- 3. static assets referenced by index.html exist ----
html = (WEB / "index.html").read_text(encoding="utf-8")
for ref in re.findall(r'(?:href|src)="([^"#]+)"', html):
    if ref.startswith("http"):
        continue
    check((WEB / ref).exists(), f"asset referenced by index.html exists: {ref}")

print()
if failures:
    print(f"{len(failures)} FAILURES")
    sys.exit(1)
print("ALL CONTRACT CHECKS PASSED")
