package com.queueshield.resourceservice.resource;

import com.queueshield.resourceservice.common.PageResponse;
import com.queueshield.resourceservice.resource.dto.ResourceRequest;
import com.queueshield.resourceservice.resource.dto.ResourceResponse;
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
@RequestMapping("/api/resources")
@Tag(name = "Resources", description = "Emergency resource inventory")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    public ResponseEntity<ResourceResponse> create(@Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.create(request));
    }

    @GetMapping
    public PageResponse<ResourceResponse> list(
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) ResourceStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(resourceService.list(type, status, pageable));
    }

    @GetMapping("/availability-ratio")
    @Operation(summary = "System-wide available/total ratio, used by priority-service's scarcity factor (Redis-cached, write-through)")
    public AvailabilityRatioResponse availabilityRatio() {
        long[] availableAndTotal = resourceService.availabilityAndTotal();
        return AvailabilityRatioResponse.of(availableAndTotal[0], availableAndTotal[1]);
    }

    @GetMapping("/{id}")
    public ResourceResponse getById(@PathVariable Long id) {
        return resourceService.getById(id);
    }

    @PutMapping("/{id}")
    public ResourceResponse update(@PathVariable Long id, @Valid @RequestBody ResourceRequest request) {
        return resourceService.update(id, request);
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release one previously-reserved unit back to available. Called by assignment-service on completion/cancellation.")
    public ResourceResponse release(@PathVariable Long id) {
        return resourceService.release(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
