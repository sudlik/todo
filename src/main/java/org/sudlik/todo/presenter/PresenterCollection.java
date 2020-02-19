package org.sudlik.todo.presenter;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
public class PresenterCollection<T> {

    private Collection<T> collection;
    private int size;

    public PresenterCollection(@NonNull Collection<T> collection) {
        this.collection = collection;
        size = collection.size();
    }

    public PresenterCollection() {
        this(Collections.emptyList());
    }
}
