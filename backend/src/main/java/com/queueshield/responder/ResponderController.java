package com.queueshield.responder;

import com.queueshield.common.PageResponse;
import com.queueshield.responder.dto.ResponderRequest;
import com.queueshield.responder.dto.ResponderResponse;
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
@Tag(name = "Responders", description = "Emergency responders and response teams")
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

    @GetMapping("/{id}")
    public ResponderResponse getById(@PathVariable Long id) {
        return responderService.getById(id);
    }

    @PutMapping("/{id}")
    public ResponderResponse update(@PathVariable Long id, @Valid @RequestBody ResponderRequest request) {
        return responderService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        responderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
