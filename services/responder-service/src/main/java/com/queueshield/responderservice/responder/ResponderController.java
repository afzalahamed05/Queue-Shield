package com.queueshield.responderservice.responder;

import com.queueshield.responderservice.common.PageResponse;
import com.queueshield.responderservice.responder.dto.DispatchRequest;
import com.queueshield.responderservice.responder.dto.ResponderRequest;
import com.queueshield.responderservice.responder.dto.ResponderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/responders")
@Tag(name = "Responders", description = "Emergency responders and their dispatch status")
public class ResponderController {

    private final ResponderService responderService;

    public ResponderController(ResponderService responderService) {
        this.responderService = responderService;
    }

    @PostMapping
    public ResponseEntity<ResponderResponse> create(@Valid @RequestBody ResponderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(responderService.create(request));
    }

    @GetMapping
    public PageResponse<ResponderResponse> list(
            @RequestParam(required = false) ResponderRole role,
            @RequestParam(required = false) ResponderStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(responderService.list(role, status, pageable));
    }

    @GetMapping("/summary")
    public ResponderSummaryResponse summary() {
        return new ResponderSummaryResponse(responderService.countAll(), responderService.countActive(), responderService.countAvailable());
    }

    @GetMapping("/{id}")
    public ResponderResponse getById(@PathVariable Long id) {
        return responderService.getById(id);
    }

    @PutMapping("/{id}")
    public ResponderResponse update(@PathVariable Long id, @Valid @RequestBody ResponderRequest request) {
        return responderService.update(id, request);
    }

    @PostMapping("/{id}/dispatch")
    @Operation(summary = "Synchronously dispatch this responder (409 if not available). Called by assignment-service.")
    public ResponderResponse dispatch(@PathVariable Long id, @Valid @RequestBody DispatchRequest request) {
        return responderService.dispatch(id, request);
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release this responder back to AVAILABLE. Called by assignment-service on completion/cancellation.")
    public ResponderResponse release(@PathVariable Long id) {
        return responderService.release(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        responderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
