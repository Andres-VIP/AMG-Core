package com.acme.platform.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.acme.platform.service.S3ObjectService;

import java.util.List;

@RestController
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3ObjectService s3ObjectService;

    public S3Controller(S3ObjectService s3ObjectService) {
        this.s3ObjectService = s3ObjectService;
    }

    @GetMapping("/keys")
    public ResponseEntity<List<String>> listKeys(@RequestParam(required = false) String prefix) {
        return ResponseEntity.ok(s3ObjectService.listKeys(prefix));
    }

    @PostMapping(value = "/text", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Void> putText(
            @RequestParam @Valid @NotBlank String key,
            @RequestBody(required = false) String body
    ) {
        s3ObjectService.putText(key, body == null ? "" : body);
        return ResponseEntity.accepted().build();
    }

    @GetMapping(value = "/text", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getText(@RequestParam @Valid @NotBlank String key) {
        return ResponseEntity.ok(s3ObjectService.getText(key));
    }
}
