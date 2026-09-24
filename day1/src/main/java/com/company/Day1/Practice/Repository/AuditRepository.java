package com.company.Day1.Practice.Repository;

import com.company.Day1.Practice.Entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditLog, Long> {
}