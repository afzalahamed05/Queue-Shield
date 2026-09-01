package com.queueshield.shelter;

import com.queueshield.common.PageResponse;
import com.queueshield.shelter.dto.ShelterRequest;
import com.queueshield.shelter.dto.ShelterResponse;
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
@RequestMapping("/api/shelters")
@Tag(name = "Shelters", description = "Emergency shelters and capacity tracking")
public class ShelterController {

    private final ShelterService shelterService;

    public ShelterController(ShelterService shelterService) {
        this.shelterService = shelterService;
    }

    @PostMapping
    public ResponseEntity<ShelterResponse> create(@Valid @RequestBody ShelterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shelterService.create(request));
    }

    @GetMapping
    public PageResponse<ShelterResponse> list(
            @RequestParam(required = false) ShelterStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(shelterService.list(status, pageable));
    }

    @GetMapping("/{id}")
    public ShelterResponse getById(@PathVariable Long id) {
        return shelterService.getById(id);
    }

    @PutMapping("/{id}")
    public ShelterResponse update(@PathVariable Long id, @Valid @RequestBody ShelterRequest request) {
        return shelterService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shelterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
