package com.acme.platform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(min = 2, max = 50)
    @Column(nullable = false)
    private String name;
    
    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;
    
    @Size(max = 100)
    private String address;
    
    @NotBlank
    @Size(min = 6, max = 100)
    @Column(nullable = false)
    private String password;
    
    public User() {
    }
    
    public User(String name, String email, String address, String password) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.password = password;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}

