package com.example.backend.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class BackendPaths {

    private BackendPaths() {
    }

    public static Path resolveBackendDir() {
        // Try to find the backend directory relative to where the JAR/classes are
        // located
        String userDir = System.getProperty("user.dir");
        Path cwd = Paths.get(userDir).toAbsolutePath();

        // If launched from the backend module directory, "csvFiles" exists directly
        // under CWD.
        if (cwd.resolve("csvFiles").toFile().exists()) {
            return cwd;
        }

        // If launched from repo root, backend/csvFiles exists.
        if (cwd.resolve("backend").resolve("csvFiles").toFile().exists()) {
            return cwd.resolve("backend");
        }

        // Try parent directory (in case we're in target or similar)
        Path parent = cwd.getParent();
        if (parent != null) {
            if (parent.resolve("csvFiles").toFile().exists()) {
                return parent;
            }
            if (parent.resolve("backend").resolve("csvFiles").toFile().exists()) {
                return parent.resolve("backend");
            }
        }

        // Fallback: keep existing behavior but prefer the conventional repo structure.
        return cwd.resolve("backend");
    }
}
