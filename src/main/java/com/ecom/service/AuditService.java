package com.ecom.service;


import com.ecom.entity.AuditLog;
import com.ecom.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface AuditService {
    void logEvent(User user, AuditLog.EventType eventType, boolean success, String details, HttpServletRequest request);
}
