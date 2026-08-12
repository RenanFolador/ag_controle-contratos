package com.organization.contractmanager.controller;

import com.organization.contractmanager.domain.NotificationDeadline;
import com.organization.contractmanager.dto.NotificationDeadlineRequest;
import com.organization.contractmanager.dto.NotificationDeadlineResponse;
import com.organization.contractmanager.dto.NotificationDeadlineStatusRequest;
import com.organization.contractmanager.service.NotificationDeadlineService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/notification-deadlines")
public class NotificationDeadlineController {
    private final NotificationDeadlineService service;

    public NotificationDeadlineController(NotificationDeadlineService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationDeadlineResponse> findAll() {
        return service.findAll().stream().map(this::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<NotificationDeadlineResponse> create(
            @Valid @RequestBody NotificationDeadlineRequest request) {
        NotificationDeadlineResponse response = toResponse(
                service.create(request.daysBefore(), request.enabled()));
        return ResponseEntity.created(URI.create(
                "/api/v1/admin/notification-deadlines/" + response.id())).body(response);
    }

    @PatchMapping("/{id}/enabled")
    public NotificationDeadlineResponse setEnabled(
            @PathVariable UUID id,
            @RequestBody NotificationDeadlineStatusRequest request) {
        return toResponse(service.setEnabled(id, request.enabled()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable UUID id) {
        service.remove(id);
        return ResponseEntity.noContent().build();
    }

    private NotificationDeadlineResponse toResponse(NotificationDeadline deadline) {
        return new NotificationDeadlineResponse(
                deadline.getId(), deadline.getDaysBefore(), deadline.isEnabled(),
                deadline.getCreatedAt(), deadline.getUpdatedAt());
    }
}
