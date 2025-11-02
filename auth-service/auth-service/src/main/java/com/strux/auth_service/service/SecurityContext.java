package com.strux.auth_service.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SecurityContext {
    private boolean suspicious = false;
    private boolean deviceTrusted = false;
    private List<String> reasons = new ArrayList<>();

    public void addReason(String reason) {
        this.reasons.add(reason);
        this.suspicious = true;
    }

    public String getReason() {
        return String.join(", ", reasons);
    }

    public String getWarningMessage() {
        if (!suspicious) {
            return null;
        }
        return "Unusual activity detected: " + getReason();
    }
}
