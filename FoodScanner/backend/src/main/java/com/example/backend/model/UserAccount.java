package com.example.backend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by mlenarto on 9/21/15.
 */

/**
 * UserAccount entity
 */
@Entity
public class UserAccount {
    /**
     * Unique identifier of this Entity in the database.
     */
    @Id
    private Long key;

    private String firstName;
    private String lastName;
    private String email;

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }

    public void setFirstName() { this.firstName = firstName; }
    public void setLastName() { this.lastName = lastName; }
    public void setEmail() { this.email = email; }
}
