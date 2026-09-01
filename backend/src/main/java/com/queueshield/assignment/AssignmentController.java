package com.queueshield.assignment;

import com.queueshield.assignment.dto.AssignmentRequest;
import com.queueshield.assignment.dto.AssignmentResponse;
import com.queueshield.assignment.dto.AssignmentStatusUpdateRequest;
import com.queueshield.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assignments")
@Tag(name = "Assignments", description = "Dispatch of responders/resources/shelters to incidents")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    @Operation(summary = "Create a dispatch assignment. Dispatches the responder and/or commits one resource unit.")
    public ResponseEntity<AssignmentResponse> create(@Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.create(request));
    }

    @GetMapping
    public PageResponse<AssignmentResponse> list(
            @RequestParam(required = false) Long incidentId,
            @RequestParam(required = false) AssignmentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(assignmentService.list(incidentId, status, pageable));
    }

    @GetMapping("/{id}")
    public AssignmentResponse getById(@PathVariable Long id) {
        return assignmentService.getById(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Transition an assignment's status; releasing it (COMPLETED/CANCELLED) frees the responder/resource")
    public AssignmentResponse updateStatus(@PathVariable Long id, @Valid @RequestBody AssignmentStatusUpdateRequest request) {
        return assignmentService.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assignmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
