package com.badier.badier_ride.controller;

import com.badier.badier_ride.service.ProofService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/routes/{routeId}/delivery-points/{deliveryPointId}/proof")
@RequiredArgsConstructor
public class ProofController {

    private final ProofService proofService;

    @Value("${app.upload.dir:uploads/proofs}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadProof(
            @PathVariable Long routeId,
            @PathVariable Long deliveryPointId,
            @RequestParam("file") MultipartFile file) throws Exception {
        String filename = proofService.uploadProof(routeId, deliveryPointId, file);
        return ResponseEntity.ok(Map.of("filename", filename));
    }

    @PostMapping("/validate-code")
    public ResponseEntity<Void> validateCode(
            @PathVariable Long routeId,
            @PathVariable Long deliveryPointId,
            @RequestBody Map<String, String> body) {
        proofService.validateWithCode(routeId, deliveryPointId, body.get("code"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate-code")
    public ResponseEntity<Map<String, String>> generateCode(
            @PathVariable Long routeId,
            @PathVariable Long deliveryPointId) {
        proofService.generateConfirmationCode(routeId, deliveryPointId);
        return ResponseEntity.ok(Map.of("message", "Code généré"));
    }

    @GetMapping("/image/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws Exception {
        Path file = Paths.get(uploadDir).resolve(filename);
        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();
        String contentType = filename.endsWith(".png") ? "image/png" : "image/jpeg";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
