package com.ecom.service.impl;


import com.ecom.entity.AuditLog;
import com.ecom.entity.User;
import com.ecom.repository.AuditLogRepository;
import com.ecom.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async("taskExecutor")
    @Override
    public void logEvent(User user, AuditLog.EventType eventType, boolean success, 
                         String details, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        AuditLog log = AuditLog.builder()
                .user(user)
                .eventType(eventType)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .details(details)
                .success(success)
                .build();

        auditLogRepository.save(log);
    }
}
