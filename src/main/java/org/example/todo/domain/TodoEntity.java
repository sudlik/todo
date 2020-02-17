package org.example.todo.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TodoEntity {

    @Id
    @Column(columnDefinition = "BINARY(32)")
    @NotBlank
    private String id;

    @NotBlank
    @Column
    private String description;

    @NotNull
    @Column
    private Status status;

    public TodoEntity(String id, String description) {
        this.id = id;
        this.description = description;
        this.status = Status.NEW;
    }

    public void complete() {
        if (!status.equals(Status.NEW)) {
            throw new NotNewTodoCanNotBeCompletedException(id);
        }

        status = Status.COMPLETED;
    }

    public enum Status {
        NEW("new"),
        COMPLETED("completed");

        private String value;

        Status(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
