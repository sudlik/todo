package org.example.todo.ui;

import com.google.common.base.CaseFormat;
import org.example.todo.presenter.ErrorPresenter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Objects;

public class ResponseEntityFactory {

    public static ResponseEntity<ErrorPresenter> createBadRequest(BindingResult bindingResult) {
        if (bindingResult.getFieldErrorCount() == 1) {
            ErrorPresenter.ErrorData errorData = createErrorData(Objects.requireNonNull(bindingResult.getFieldError()));

            return createResponseEntity(
                    HttpStatus.BAD_REQUEST,
                    new ErrorPresenter(errorData.getPath(), errorData.getMessage(), errorData.getValue()));
        } else {
            ErrorPresenter.ErrorData[] errorDataList = bindingResult.getFieldErrors()
                    .stream()
                    .map(ResponseEntityFactory::createErrorData)
                    .toArray(ErrorPresenter.ErrorData[]::new);

            return createError(HttpStatus.BAD_REQUEST, errorDataList);
        }
    }

    private static ErrorPresenter.ErrorData createErrorData(FieldError fieldError) {
        String message = fieldError.getDefaultMessage();

        if (message != null) {
            message = message.toLowerCase();
        }

        return new ErrorPresenter.ErrorData(
                CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldError.getField()),
                message,
                fieldError.getRejectedValue());
    }

    public static ResponseEntity<ErrorPresenter> createBadRequest(String message, Object value) {
        return createError(HttpStatus.BAD_REQUEST, message, value);
    }

    public static ResponseEntity<?> createNoContent() {
        return ResponseEntity.noContent().build();
    }

    public static <T> ResponseEntity<T> createCreated(T created) {
        return createResponseEntity(HttpStatus.CREATED, created);
    }

    public static <T> ResponseEntity<T> createOk(T body) {
        return createResponseEntity(HttpStatus.OK, body);
    }

    public static ResponseEntity<ErrorPresenter> createNotFound(String message, Object value) {
        return createError(HttpStatus.NOT_FOUND, message, value);
    }

    public static ResponseEntity<ErrorPresenter> createError(
            HttpStatus httpStatus,
            ErrorPresenter.ErrorData ...errors) {
        return createError(httpStatus, httpStatus.getReasonPhrase(), errors);
    }

    public static ResponseEntity<ErrorPresenter> createError(HttpStatus httpStatus, String message, ErrorPresenter.ErrorData ...errors) {
        return createResponseEntity(httpStatus, new ErrorPresenter(message.toLowerCase(), errors));
    }

    public static ResponseEntity<ErrorPresenter> createError(HttpStatus httpStatus, String message, Object value) {
        return createResponseEntity(httpStatus, new ErrorPresenter(message.toLowerCase(), value));
    }

    public static <T> ResponseEntity<T> createResponseEntity(HttpStatus httpStatus, T body) {
        return ResponseEntity
                .status(httpStatus)
                .body(body);
    }
}
