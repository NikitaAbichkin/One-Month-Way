package com.company.Day1.Practice.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class AuditLog {
    @Id
    @GeneratedValue
    Long id;
    String message;

    protected AuditLog() {
    }

    public AuditLog(String message){
        this.message = message;
    }
}
