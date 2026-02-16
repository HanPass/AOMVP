package com.ao.service;

import com.ao.dto.AppelOffre;

public interface EmailService {
    void sendAlert(AppelOffre ao, String recipientEmail);
}
