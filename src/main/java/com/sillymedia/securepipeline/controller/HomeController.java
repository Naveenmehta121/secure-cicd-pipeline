package com.sillymedia.securepipeline.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The human-facing home page at {@code /}: a tiny HTML page (no
 * template engine) with a live demo of the API. Kept in code so the
 * app has zero static-file surprises.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    @ResponseBody
    public String home() {
        return """
                <!doctype html>
                <html lang="en">
                <head><meta charset="utf-8"><title>SecurePipeline</title></head>
                <body style="font-family: sans-serif; max-width: 640px; margin: 2rem auto;">
                  <h1>SecurePipeline &#128274;</h1>
                  <p>A tiny Spring Boot notes app, shipped through a GitHub Actions
                     pipeline with a Trivy security scan.</p>
                  <h2>Try it</h2>
                  <ul>
                    <li><a href="/api/health">GET /api/health</a></li>
                    <li><a href="/api/notes">GET /api/notes</a></li>
                  </ul>
                  <pre>curl -X POST localhost:8080/api/notes \\
                    -H 'Content-Type: application/json' \\
                    -d '{"title":"First note","body":"Hello, pipeline!"}'</pre>
                  <p>Source &amp; pipeline: see <code>README.md</code> and
                     <code>.github/workflows/ci.yml</code>.</p>
                </body>
                </html>
                """;
    }
}
