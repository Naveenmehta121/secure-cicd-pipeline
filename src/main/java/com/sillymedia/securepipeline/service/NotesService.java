package com.sillymedia.securepipeline.service;

import com.sillymedia.securepipeline.model.Note;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory note store. {@link ConcurrentHashMap} + {@link AtomicLong}
 * make it safe for concurrent HTTP requests without any locking code
 * of our own. Deliberately no database: the point of this project is
 * the pipeline, not persistence — restart the app and notes are gone.
 */
@Service
public class NotesService {

    private final Map<Long, Note> notes = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    /** Returns all notes, oldest first. */
    public List<Note> findAll() {
        return new ArrayList<>(notes.values()).stream()
                .sorted((a, b) -> Long.compare(a.id(), b.id()))
                .toList();
    }

    /** Returns one note, or empty if the id does not exist. */
    public Optional<Note> findById(long id) {
        return Optional.ofNullable(notes.get(id));
    }

    /**
     * Creates a note. Blank titles/body are rejected here (not in the
     * controller) so the rule is enforced no matter who calls it.
     */
    public Note create(String title, String body) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("body must not be blank");
        }
        long id = nextId.getAndIncrement();
        Note note = new Note(id, title.strip(), body.strip(), Instant.now());
        notes.put(id, note);
        return note;
    }

    /** Deletes a note; returns true only if it existed. */
    public boolean delete(long id) {
        return notes.remove(id) != null;
    }
}
