package com.sillymedia.securepipeline.controller;

import com.sillymedia.securepipeline.model.Note;
import com.sillymedia.securepipeline.service.NotesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST API for notes. Every method returns a {@link ResponseEntity}
 * so the HTTP status is explicit: 200 OK, 201 Created, 400 Bad
 * Request (blank title/body), 404 Not Found (unknown id).
 *
 * <pre>
 *   GET    /api/notes        -> list all notes
 *   GET    /api/notes/{id}   -> one note (404 if missing)
 *   POST   /api/notes        -> create, body {"title":"...","body":"..."}
 *   DELETE /api/notes/{id}   -> delete (404 if missing)
 *   GET    /api/health       -> {"status":"UP"} — used by the pipeline smoke test
 * </pre>
 */
@RestController
@RequestMapping("/api")
public class NotesController {

    private final NotesService notesService;

    // Constructor injection (Spring's recommended style): the service
    // is a required dependency, and tests can pass a fake one in.
    public NotesController(NotesService notesService) {
        this.notesService = notesService;
    }

    @GetMapping("/notes")
    public List<Note> listNotes() {
        return notesService.findAll();
    }

    @GetMapping("/notes/{id}")
    public ResponseEntity<Note> getNote(@PathVariable long id) {
        return notesService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/notes")
    public ResponseEntity<?> createNote(@RequestBody Map<String, String> body) {
        try {
            Note created = notesService.create(body.get("title"), body.get("body"));
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            // Blank title/body -> 400 with a readable message, not a 500.
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable long id) {
        return notesService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
