package org.example.todo.presenter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TodoPresenter {

    private String id;
    private String description;
    private String status;
}
