package com.strux.auth_service.dto;

public enum UserRole {
    USER,            //default user
    ADMIN,           // Site Admini
    COMPANY_ADMIN,   // Sirket admini
    PROJECT_MANAGER, // Proje manager
    WORKER,          // Kole
    UNIT_OWNER,      // Konut sahibi
    VIEWER           // Viewer
}