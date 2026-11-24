# SecurePipeline — a tiny app shipped through a real CI/CD pipeline with a security gate

A small Spring Boot notes app (create / list / delete notes over a REST API) whose
whole point is the **pipeline around it**: every push runs build + tests, then a
Trivy vulnerability scan. If the scan finds a HIGH or CRITICAL vulnerability,
the build **fails** and nothing gets deployed. The deploy step is intentionally
a no-op — this project is about proving the gate works, not about hosting.

Built by Naveen Kumar (BTech CSE, Galgotias University) as interview-ready
evidence for a DevSecOps internship: the pipeline *is* the project.

## What it is

| Piece | What it does |
|---|---|
| `src/main/java/.../NotesController.java` | REST API: `GET /api/notes`, `GET /api/notes/{id}`, `POST /api/notes`, `DELETE /api/notes/{id}`, `GET /api/health` |
| `src/main/java/.../NotesService.java` | In-memory notes store (a `ConcurrentHashMap` + id counter) — no database, so the app runs anywhere |
| `src/main/java/.../HomeController.java` | Tiny HTML home page at `/` with links to try the API |
| `.github/workflows/ci.yml` | The GitHub Actions pipeline: build-and-test → security-scan → deploy (manual/optional) |
| `docs/` | Real Trivy scan evidence from the build that got blocked (see below) |

## The pipeline (`.github/workflows/ci.yml`)

```
push / pull request to main
        │
        ▼
┌──────────────────┐
│ 1. build-and-test │  mvn -B verify — compile, run all 8 tests, build the jar
└────────┬─────────┘  Test reports are uploaded as an artifact, even on failure
         ▼
┌──────────────────┐
│ 2. security-scan  │  Trivy scans the repo + dependencies for known CVEs
└────────┬─────────┘  severity: CRITICAL,HIGH · exit-code: 1 → any HIGH/CRITICAL
         │            finding FAILS the job, so the pipeline stops here
         ▼
┌──────────────────┐
│ 3. deploy         │  Manual / optional only. Prints what *would* be deployed
└──────────────────┘  and stops. No server, no cloud account, no secrets.
```

- Triggers: every push and pull request to `main`.
- Permissions are least-privilege: the workflow can only read code and write
  security findings (SARIF) to GitHub code scanning.
- Cost: free — Trivy is open source and GitHub Actions' free tier covers a
  repo this size. No paid scanner anywhere.

## When the pipeline blocked a build (real story, real evidence)

1. The first build used Spring Boot **3.3.5**. Trivy scanned it and found
   **42 vulnerabilities — 33 HIGH and 9 CRITICAL** — in the dependencies
   (Jackson, Spring Framework, Tomcat). The scan failed the build, exactly
   as designed. Evidence is in `docs/`:
   - `docs/trivy-scan-3.3.5-blocked.log` — the full Trivy output
   - `docs/trivy-scan-3.3.5-blocked.json` — the same scan as JSON
   - `docs/sbom-3.3.5.json` — the software bill of materials that was scanned
2. The fix was to upgrade the framework: `pom.xml` now uses Spring Boot
   **4.2.0**, which pulls in the fixed Jackson / Framework / Tomcat versions.
   That is the whole point of the gate — a vulnerable dependency cannot
   silently ship; someone has to upgrade it first.

## How to run it

Requirements: Java 17+ and Maven 3.9+ (or use the Maven wrapper if you add one).
No database, no Docker, no secrets.

```bash
# 1. Run the tests
mvn -B test

# 2. Start the app (runs on http://localhost:8080)
mvn -B spring-boot:run

# 3. Try it (in a second terminal)
curl localhost:8080/api/health
curl localhost:8080/api/notes
curl -X POST localhost:8080/api/notes \
  -H 'Content-Type: application/json' \
  -d '{"title":"First note","body":"Hello, pipeline!"}'
curl -X DELETE localhost:8080/api/notes/1
```

Or build the jar and run it directly:

```bash
mvn -B package
java -jar target/secure-pipeline-0.1.0.jar
```

## How to run the security scan locally

Trivy is free and open source — install it from
https://aquasecurity.github.io/trivy/ (or `brew install trivy`), then:

```bash
# Scan the built jar for known CVEs (fails with exit code 1 on HIGH/CRITICAL)
trivy fs --scanners vuln --severity HIGH,CRITICAL --exit-code 1 target/secure-pipeline-0.1.0.jar

# Scan the whole repo (what the GitHub Action does)
trivy fs --scanners vuln --severity HIGH,CRITICAL --exit-code 1 .
```

## 60-second demo

1. Open the Actions tab on GitHub and show the three jobs: build-and-test ✓,
   security-scan ✓, deploy (manual).
2. Show `docs/trivy-scan-3.3.5-blocked.log`: 42 vulnerabilities on Spring Boot
   3.3.5 — the build went red.
3. Show `pom.xml`: Spring Boot 4.2.0 — the upgrade that fixed it.
4. Run the app live: `curl localhost:8080/api/health` → `{"status":"UP"}`, then
   create a note with `POST /api/notes`.

## Explain this in an interview

- **Why a pipeline at all?** "So a human never has to remember to test or
  scan — every push is built, tested and scanned automatically, and a
  vulnerable build can never reach production by accident."
- **What does the Trivy step actually do?** "It compares every library in
  `pom.xml` (and the built jar) against a database of known CVEs, and fails
  the build on any HIGH or CRITICAL finding. `ignore-unfixed: true` means it
  only blocks on vulnerabilities that actually have a fix."
- **What happened when it blocked a build?** "The first build on Spring Boot
  3.3.5 had 42 HIGH/CRITICAL CVEs — Jackson, Spring Framework, Tomcat. The
  scan failed, so I upgraded to Spring Boot 4.2.0, which pulls the fixed
  versions. The evidence is in `docs/`."
- **Why is deploy a no-op?** "Deployment needs a real server and credentials.
  The honest thing here is a manual, gated deploy step that prints what it
  *would* deploy — the security gate is the part this project proves."
- **What would you add next?** "A staging environment, Docker image scanning
  with `trivy image`, and Dependabot so dependency upgrades arrive as pull
  requests that the same pipeline checks."

## Honest limits

- The deploy job does **not** deploy anything — there is no server, cloud
  account or secret in this repo, by design.
- The app stores notes **in memory** — restart the app and the notes are gone.
  That is deliberate (zero setup), not a bug.
- Trivy in CI scans the filesystem + dependency list; it does not do runtime
  testing (DAST) or secret scanning — those would be the next steps.
- The Trivy evidence in `docs/` is from the old 3.3.5 build that was blocked.
  The current 4.2.0 build's scan runs in CI on every push; re-run Trivy
  locally (commands above) to see its current result.

## Tech stack

Java 17 · Spring Boot 4.2.0 · Spring Web · JUnit 5 / MockMvc · Maven ·
GitHub Actions · Trivy · MIT License
