package com.sillymedia.securepipeline.model;

import java.time.Instant;

/**
 * One note. A plain Java record: immutable, and Jackson turns it
 * into JSON automatically ({@code {"id":1,"title":"...","body":"...",
 * "createdAt":"..."}}). No JPA, no database — storage is in memory
 * (see {@link com.sillymedia.securepipeline.service.NotesService}).
 */
public record Note(long id, String title, String body, Instant createdAt) {
}
