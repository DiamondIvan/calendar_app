package com.example.frontend.model;

/**
 * AppUser represents a user account in the calendar application.
 * 
 * Contains user authentication information (email, password) and profile data
 * (name).
 * Each user has a unique ID assigned by the backend.
 * 
 * This model is used for:
 * - User authentication (login/registration)
 * - Storing the currently logged-in user
 * - Associating events with specific users
 */
public class AppUser {
    private Integer id;
    private String name;
    private String email;
    private String password;

    /**
     * Default constructor for creating an empty AppUser.
     * Typically used for JSON deserialization or manual field population.
     */
    public AppUser() {
    }

    /**
     * Full constructor for creating an AppUser with all fields.
     * 
     * @param id       The unique user ID (assigned by backend)
     * @param name     The user's display name
     * @param email    The user's email address (used for login)
     * @param password The user's password (should ideally be hashed in production)
     */
    public AppUser(Integer id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Integer getId() {
        return id;
    }

    /**
     * Sets the user ID.
     * Only sets if the provided ID is not null (to prevent accidental clearing).
     * 
     * @param id The new user ID
     */
    public void setId(Integer id) {
        if (id != null) {
            this.id = id;
        }
    }

    /**
     * Sets the user ID from a string value.
     * Parses the string to an Integer, or sets to null if string is null/empty.
     * Useful for form input handling.
     * 
     * @param id The user ID as a string
     * @throws NumberFormatException if the string is not a valid integer
     */
    public void setId(String id) {
        this.id = (id == null || id.isEmpty()) ? null : Integer.parseInt(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
