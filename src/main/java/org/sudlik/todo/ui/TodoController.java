package org.sudlik.todo.ui;

import org.sudlik.todo.ObjectId;
import org.sudlik.todo.domain.TodoEntity;
import org.sudlik.todo.domain.TodoRepository;
import org.sudlik.todo.presenter.PresenterCollection;
import org.sudlik.todo.presenter.TodoPresenter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
class TodoController {

    private TodoRepository todoRepository;

    TodoController(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @GetMapping(value = "todos", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> listTodos(@RequestParam(value = "status", required = false) String status) {
        Iterable<TodoEntity> todos;

        if (status != null) {
            if (!status.equals(TodoEntity.Status.NEW.toString()) && !status.equals(TodoEntity.Status.COMPLETED.toString())) {
                return ResponseEntityFactory.createBadRequest("Unknown status", status);
            }

            todos = todoRepository.findByStatus(TodoEntity.Status.valueOf(status.toUpperCase()));
        } else {
            todos = todoRepository.findAll();
        }

        List<TodoPresenter> collect = StreamSupport.stream(todos.spliterator(), false)
                .map(todoEntity -> new TodoPresenter(todoEntity.getId(), todoEntity.getDescription(), todoEntity.getStatus().toString()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new PresenterCollection<>(collect));
    }

    @PostMapping(value = "todos", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> addTodo(@Valid @RequestBody AddTodoPayload payload, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntityFactory.createBadRequest(bindingResult);
        }

        TodoEntity todoEntity = new TodoEntity(ObjectId.generate(), payload.getDescription());

        todoRepository.save(todoEntity);

        return ResponseEntityFactory.createCreated(new TodoPresenter(todoEntity.getId(), todoEntity.getDescription(), todoEntity.getStatus().toString()));
    }

    @PutMapping(value = "todos/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> completeTodo(@PathVariable String id, @Valid @RequestBody CompleteTodoPayload payload, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntityFactory.createBadRequest(bindingResult);
        }

        if (!payload.getStatus().equals(TodoEntity.Status.COMPLETED.toString())) {
            return ResponseEntityFactory.createBadRequest("Unexpected status", payload.getStatus());
        }

        Optional<TodoEntity> todoEntityOptional = todoRepository.findById(id);

        if (todoEntityOptional.isPresent()) {
            todoEntityOptional.get().complete();

            todoRepository.save(todoEntityOptional.get());

            return createTodoResponse(todoEntityOptional.get());
        } else {
            return ResponseEntityFactory.createNotFound("Todo not found", id);
        }
    }

    @DeleteMapping(value = "todos/{id}")
    ResponseEntity<?> removeTodo(@PathVariable String id) {
        todoRepository.findById(id).ifPresent(todoEntity -> todoRepository.delete(todoEntity));

        return ResponseEntityFactory.createNoContent();
    }

    @DeleteMapping(value = "todos")
    ResponseEntity<?> eraseTodoList() {
        todoRepository.deleteAll();

        return ResponseEntity.ok(new PresenterCollection<>());
    }

    private static ResponseEntity<TodoPresenter> createTodoResponse(TodoEntity todoEntity) {
        return ResponseEntityFactory.createOk(new TodoPresenter(todoEntity.getId(), todoEntity.getDescription(), todoEntity.getStatus().toString()));
    }
}
