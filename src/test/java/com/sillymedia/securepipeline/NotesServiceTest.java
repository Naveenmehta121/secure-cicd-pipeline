package com.sillymedia.securepipeline;

import com.sillymedia.securepipeline.model.Note;
import com.sillymedia.securepipeline.service.NotesService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Unit tests for NotesService — no Spring context, runs in milliseconds. */
class NotesServiceTest {

    private final NotesService service = new NotesService();

    @Test
    void createAssignsIncrementingIds() {
        Note first = service.create("One", "First");
        Note second = service.create("Two", "Second");
        assertThat(first.id()).isEqualTo(1);
        assertThat(second.id()).isEqualTo(2);
        assertThat(service.findAll()).containsExactly(first, second);
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(service.findById(999)).isEmpty();
    }

    @Test
    void createRejectsBlankTitleAndBody() {
        assertThatThrownBy(() -> service.create("  ", "body"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title");
        assertThatThrownBy(() -> service.create("title", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("body");
    }

    @Test
    void deleteRemovesOnlyThatNote() {
        Note kept = service.create("Keep", "me");
        Note gone = service.create("Drop", "me");
        assertThat(service.delete(gone.id())).isTrue();
        assertThat(service.delete(gone.id())).isFalse(); // already gone
        assertThat(service.findAll()).containsExactly(kept);
    }
}
