package com.example.backend.model;

/**
 * AppUser represents a user account in the calendar application (backend
 * model).
 * 
 * Contains user authentication information (email, password) and profile data
 * (name).
 * Each user has a unique ID assigned by the system.
 * 
 * This is the backend equivalent of the frontend AppUser model.
 * Used by controllers and services for user management operations.
 */
public class AppUser {
    private Integer id;
    private String name;
    private String email;
    private String password;

    /**
     * Default constructor for creating an empty AppUser.
     */
    public AppUser() {
    }

    /**
     * Full constructor for creating an AppUser with all fields.
     * 
     * @param id       The unique user ID
     * @param name     The user's display name
     * @param email    The user's email address (used for login)
     * @param password The user's password
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
     * Sets the user ID. Protected against null values.
     * Used by Jackson for JSON deserialization.
     * 
     * @param id The new user ID (ignored if null)
     */
    public void setId(Integer id) {
        // setId is kept for Jackson deserialization if an integer is passed.
        if (id != null) {
            this.id = id;
        }
    }

    /**
     * Sets the user ID from a string value.
     * Overloaded method to handle form data that may provide ID as string.
     * 
     * @param id The user ID as a string, or null/empty for null ID
     * @throws NumberFormatException if the string is not a valid integer
     */
    // Overloaded method to handle potential string-based ID from form data
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

    /**
     * Returns a string representation of this AppUser.
     * Password is masked with '***' for security.
     * 
     * @return String representation showing id, name, email, and masked password
     */
    @Override
    public String toString() {
        return "AppUser{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='***'" +
                '}';
    }
}