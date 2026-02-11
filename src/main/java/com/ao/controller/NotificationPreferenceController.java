package com.ao.controller;

import com.ao.dto.NotificationPreferenceRequest;
import com.ao.dto.NotificationPreferenceResponse;
import com.ao.service.NotificationPreferenceAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceAdminService notificationPreferenceAdminService;

    @GetMapping
    public ResponseEntity<List<NotificationPreferenceResponse>> findAll() {
        return ResponseEntity.ok(notificationPreferenceAdminService.findAll());
    }

    @PostMapping
    public ResponseEntity<NotificationPreferenceResponse> upsert(@Valid @RequestBody NotificationPreferenceRequest request) {
        NotificationPreferenceResponse response = notificationPreferenceAdminService.upsert(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
