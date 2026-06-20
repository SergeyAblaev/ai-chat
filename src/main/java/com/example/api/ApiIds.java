package com.example.api;

import java.util.UUID;

public final class ApiIds {

    private ApiIds() {}

    public static String next(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "");
    }
}
