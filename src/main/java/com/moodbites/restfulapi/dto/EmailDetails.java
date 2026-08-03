package com.moodbites.restfulapi.dto;

// Importing required classes
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

// Annotations
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

// Class
public class EmailDetails {

    // Class data members
    private String recipient;
    private String msgBody;
    private String subject;
}