package org.sudlik.todo.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface TodoRepository extends CrudRepository<TodoEntity, String> {

    Collection<TodoEntity> findByStatus(TodoEntity.Status status);
}
