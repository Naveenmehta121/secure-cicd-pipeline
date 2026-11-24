package com.sillymedia.securepipeline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API tests with MockMvc: the real controller + service run, but no
 * real HTTP server is started, so these are fast and reliable in CI.
 * (Each test class gets a fresh app context, so notes do not leak
 * between test classes.)
 *
 * MockMvc is built by hand from the WebApplicationContext instead of
 * relying on @AutoConfigureMockMvc, so the test works on Spring Boot 3
 * and 4 alike.
 */
@SpringBootTest
class NotesControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void healthReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void createThenListThenDeleteNote() throws Exception {
        // Create -> 201 with the note back, id assigned by the server.
        String body = mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Pipeline note","body":"CI runs this test"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Pipeline note"))
                .andReturn().getResponse().getContentAsString();

        long id = Long.parseLong(body.replaceAll("(?s).*\"id\":(\\d+).*", "$1"));

        // List contains it.
        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Unknown id -> 404.
        mockMvc.perform(get("/api/notes/9999")).andExpect(status().isNotFound());

        // Delete -> 204, then it is gone.
        mockMvc.perform(delete("/api/notes/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/notes/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void createWithBlankTitleIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","body":"no title"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("title must not be blank"));
    }
}
