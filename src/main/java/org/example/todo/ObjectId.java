package org.example.todo;

import java.util.UUID;

public class ObjectId {

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
