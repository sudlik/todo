package org.sudlik.todo.ui;

import org.sudlik.todo.domain.NotNewTodoCanNotBeCompletedException;
import org.sudlik.todo.presenter.ErrorPresenter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(NotNewTodoCanNotBeCompletedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorPresenter> handleNotNewTodoCanNotBeCompleted(NotNewTodoCanNotBeCompletedException ex) {
        return ResponseEntityFactory.createBadRequest("not new todo can not be completed", ex.id);
    }
}
