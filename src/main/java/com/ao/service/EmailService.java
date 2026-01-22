package com.ao.service;

import com.ao.entity.Tender;

import java.util.List;

public interface EmailService {
    void sendAlert(String subject, String body);
}
