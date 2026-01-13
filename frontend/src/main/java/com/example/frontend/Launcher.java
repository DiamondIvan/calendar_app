package com.example.frontend;

/**
 * Launcher is the entry point for the JavaFX application.
 * 
 * This simple launcher class exists to work around a JavaFX module system issue
 * where the main class needs to be separate from the Application class.
 * It simply delegates to the App class's main method.
 */
public class Launcher {
    /**
     * Main entry point for the application.
     * Delegates to {@link App#main(String[])} to start the JavaFX application.
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        App.main(args);
    }
}
