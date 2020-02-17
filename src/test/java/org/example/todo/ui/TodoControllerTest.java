package org.example.todo.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.todo.domain.TodoEntity;
import org.example.todo.domain.TodoRepository;
import org.example.todo.presenter.TodoPresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest()
@AutoConfigureMockMvc
public class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TodoRepository todoRepository;

    @Before
    public void setUp() {
        todoRepository.deleteAll();
    }

    @Test
    public void descriptionCanNotBeBlank() throws Exception {
        AddTodoPayload blankDescription = new AddTodoPayload(" ");

        mockMvc
                .perform(post("/todos")
                        .content(objectMapper.writeValueAsBytes(blankDescription))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.path").value("description"))
                .andExpect(jsonPath("$.message").value("must not be blank"))
                .andExpect(jsonPath("$.value").value(blankDescription.getDescription()));
    }

    @Test
    public void addTodo() throws Exception {
        AddTodoPayload addTodoPayload = new AddTodoPayload("some description");

        mockMvc
                .perform(post("/todos")
                    .content(objectMapper.writeValueAsBytes(addTodoPayload))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(matchesPattern("^\\p{XDigit}{32}$")))
                .andExpect(jsonPath("$.description").value(addTodoPayload.getDescription()))
                .andExpect(jsonPath("$.status").value(TodoEntity.Status.NEW.toString()));

        assertNotEmptyTodoList();
    }

    @Test
    public void completeTodo() throws Exception {
        TodoPresenter todoPresenter = createNewTodo();
        CompleteTodoPayload completeTodoPayload = new CompleteTodoPayload("completed");

        mockMvc
                .perform(put("/todos/{id}", todoPresenter.getId())
                        .content(objectMapper.writeValueAsBytes(completeTodoPayload))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(todoPresenter.getId()))
                .andExpect(jsonPath("$.description").value(todoPresenter.getDescription()))
                .andExpect(jsonPath("$.status").value(TodoEntity.Status.COMPLETED.toString()));
    }

    @Test
    public void listAllTodos() throws Exception {
        TodoPresenter newTodo = createNewTodo();
        TodoPresenter completedTodo = createCompletedTodo();

        mockMvc
                .perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.collection").value(hasSize(2)))
                .andExpect(jsonPath("$.collection[*].id").value(containsInAnyOrder(newTodo.getId(), completedTodo.getId())))
                .andExpect(jsonPath("$.collection[*].description").value(containsInAnyOrder(newTodo.getDescription(), completedTodo.getDescription())))
                .andExpect(jsonPath("$.collection[*].status").value(containsInAnyOrder(newTodo.getStatus(), completedTodo.getStatus())));
    }

    @Test
    public void listOnlyNewTodo() throws Exception {
        TodoPresenter newTodo = createNewTodo();
        createCompletedTodo();

        mockMvc
                .perform(get("/todos").queryParam("status", TodoEntity.Status.NEW.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.collection").value(hasSize(1)))
                .andExpect(jsonPath("$.collection[0].id").value(newTodo.getId()))
                .andExpect(jsonPath("$.collection[0].description").value(newTodo.getDescription()))
                .andExpect(jsonPath("$.collection[0].status").value(TodoEntity.Status.NEW.toString()));
    }

    @Test
    public void listOnlyCompletedTodo() throws Exception {
        TodoPresenter completedTodo = createCompletedTodo();
        createNewTodo();

        mockMvc
                .perform(get("/todos").queryParam("status", TodoEntity.Status.COMPLETED.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.collection").value(hasSize(1)))
                .andExpect(jsonPath("$.collection[0].id").value(completedTodo.getId()))
                .andExpect(jsonPath("$.collection[0].description").value(completedTodo.getDescription()))
                .andExpect(jsonPath("$.collection[0].status").value(TodoEntity.Status.COMPLETED.toString()));
    }

    @Test
    public void remoteTodo() throws Exception {
        TodoPresenter todoPresenter = createNewTodo();

        mockMvc
                .perform(delete("/todos/{id}", todoPresenter.getId()))
                .andExpect(status().isNoContent());

        assertEmptyTodoList();
    }

    @Test
    public void eraseTodoList() throws Exception {
        createNewTodo();
        createNewTodo();

        mockMvc
                .perform(delete("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.size").value(0))
                .andExpect(jsonPath("$.collection").value(hasSize(0)));

        assertEmptyTodoList();
    }

    @Test
    public void notNewTodoCanNotBeCompleted() throws Exception {
        TodoPresenter notNewTodo = createCompletedTodo();
        CompleteTodoPayload completeTodoPayload = new CompleteTodoPayload("completed");

        mockMvc
                .perform(put("/todos/{id}", notNewTodo.getId())
                        .content(objectMapper.writeValueAsBytes(completeTodoPayload))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message").value("not new todo can not be completed"))
                .andExpect(jsonPath("$.value").value(notNewTodo.getId()));
    }

    private TodoPresenter createNewTodo() throws Exception {
        AddTodoPayload addTodoPayload = new AddTodoPayload("some description");

        String response = mockMvc
                .perform(post("/todos")
                .content(objectMapper.writeValueAsBytes(addTodoPayload))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, TodoPresenter.class);
    }

    private TodoPresenter createCompletedTodo() throws Exception {
        TodoPresenter todoPresenter = createNewTodo();

        return makeTodoCompleted(todoPresenter.getId());
    }

    private TodoPresenter makeTodoCompleted(String todoId) throws Exception {
        CompleteTodoPayload completeTodoPayload = new CompleteTodoPayload("completed");

        String response = mockMvc
                .perform(put("/todos/{id}", todoId)
                .content(objectMapper.writeValueAsBytes(completeTodoPayload))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, TodoPresenter.class);
    }

    private void assertEmptyTodoList() throws Exception {
        mockMvc
                .perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.size").value(0))
                .andExpect(jsonPath("$.collection").value(hasSize(0)));
    }

    private void assertNotEmptyTodoList() throws Exception {
        mockMvc
                .perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.collection").value(hasSize(1)));
    }
}