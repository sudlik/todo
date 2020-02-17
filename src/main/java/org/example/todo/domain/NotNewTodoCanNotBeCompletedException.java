package org.example.todo.domain;

public class NotNewTodoCanNotBeCompletedException extends RuntimeException {
    public final String id;

    public NotNewTodoCanNotBeCompletedException(String id) {
        super();

        this.id = id;
    }
}
