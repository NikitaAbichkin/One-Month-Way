package com.company.Day1.Practice.Servieces;

import com.company.Day1.Practice.Entity.AuditLog;
import com.company.Day1.Practice.Repository.AuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogAuditService {
    private final AuditRepository auditRepo;

    public LogAuditService(AuditRepository auditRepo) {
        this.auditRepo = auditRepo;
    }
//    @Transactional
    public void audit(String message) {
        auditRepo.save(new AuditLog(message));
        throw new RuntimeException("audit failed");
    }
}
