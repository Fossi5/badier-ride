package com.badier.badier_ride.controller;

import com.badier.badier_ride.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/routes")
    public ResponseEntity<byte[]> exportRoutes() throws Exception {
        byte[] csv = exportService.exportRoutesCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tournees.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/addresses")
    public ResponseEntity<byte[]> exportAddresses() throws Exception {
        byte[] csv = exportService.exportAddressesCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=adresses.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
