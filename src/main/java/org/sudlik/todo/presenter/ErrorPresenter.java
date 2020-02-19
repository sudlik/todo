package org.sudlik.todo.presenter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class ErrorPresenter {

    private String path;
    private String message;
    private Object value;
    private ErrorData[] errors;

    public ErrorPresenter(String path, String message, Object value) {
        this.path = path;
        this.message = message;
        this.value = value;
    }

    public ErrorPresenter(String message, Object value) {
        this.message = message;
        this.value = value;
    }

    public ErrorPresenter(String message, ErrorData ...errors) {
        this.message = message;
        this.errors = errors;
    }

    @Getter
    @AllArgsConstructor
    public static class ErrorData {

        private String path;
        private String message;
        private Object value;
    }
}
