package eulerity.taskmanager.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import eulerity.taskmanager.dto.TaskRequest;
import eulerity.taskmanager.enums.Priority;
import eulerity.taskmanager.enums.Status;
import eulerity.taskmanager.repository.TaskRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Full-stack integration test: real Spring context + real H2 database, driven
 * through the HTTP layer with MockMvc. Exercises the complete CRUD lifecycle and
 * the structured error responses end to end.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void clean() {
        // Each test starts from an empty table for deterministic assertions.
        taskRepository.deleteAll();
    }

    @Test
    void fullCrudLifecycle() throws Exception {
        // --- CREATE (no priority/status -> defaults applied) ---
        TaskRequest create = TaskRequest.builder()
                .title("Write report")
                .description("Q2 numbers")
                .dueDate(LocalDate.of(2026, 7, 1))
                .build();

        String createdJson = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Write report"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(createdJson).get("id").asLong();

        // --- READ one ---
        mockMvc.perform(get("/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.description").value("Q2 numbers"));

        // --- READ all ---
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // --- UPDATE (full replacement) ---
        TaskRequest update = TaskRequest.builder()
                .title("Write report v2")
                .description("final")
                .dueDate(LocalDate.of(2026, 7, 5))
                .priority(Priority.LOW)
                .status(Status.DONE)
                .build();

        mockMvc.perform(put("/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Write report v2"))
                .andExpect(jsonPath("$.priority").value("LOW"))
                .andExpect(jsonPath("$.status").value("DONE"));

        // --- DELETE ---
        mockMvc.perform(delete("/tasks/{id}", id))
                .andExpect(status().isNoContent());

        // --- READ deleted -> 404 ---
        mockMvc.perform(get("/tasks/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/tasks/" + id));
    }

    @Test
    void create_withBlankTitle_returns400WithFieldErrors() throws Exception {
        String body = "{\"title\":\"   \",\"description\":\"no title\"}";

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.title").value("title is required"));
    }

    @Test
    void malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not valid json "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request body"));
    }

    @Test
    void badPathVariableType_returns400() throws Exception {
        mockMvc.perform(get("/tasks/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Parameter 'id' must be of type Long"));
    }
}
