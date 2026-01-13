package com.example.backend.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * BackendPaths provides utility methods for resolving backend directory paths.
 * 
 * This class handles the complexity of finding the correct backend directory
 * regardless of where the application is launched from (IDE, JAR, repo root,
 * etc.).
 * 
 * The primary use case is locating the "csvFiles" directory for data
 * persistence.
 */
public final class BackendPaths {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private BackendPaths() {
    }

    /**
     * Resolves the backend directory path intelligently based on the current
     * working directory.
     * 
     * Resolution strategy (in order):
     * 1. Check if "csvFiles" exists directly under current working directory
     * 2. Check if "backend/csvFiles" exists under current working directory
     * 3. Check parent directory for "csvFiles"
     * 4. Check parent directory for "backend/csvFiles"
     * 5. Fallback: return "./backend" relative to current directory
     * 
     * This approach handles:
     * - Launching from backend module directory (backend is CWD)
     * - Launching from repository root (backend is subdirectory)
     * - Launching from build output directories (target/, out/, etc.)
     * 
     * @return Path to the backend directory containing csvFiles
     */
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
