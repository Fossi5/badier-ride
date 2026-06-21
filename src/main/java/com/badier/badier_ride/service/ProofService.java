package com.badier.badier_ride.service;

import com.badier.badier_ride.entity.RouteDeliveryPoint;
import com.badier.badier_ride.exception.InvalidOperationException;
import com.badier.badier_ride.exception.ResourceNotFoundException;
import com.badier.badier_ride.repository.RouteDeliveryPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProofService {

    @Value("${app.upload.dir:uploads/proofs}")
    private String uploadDir;

    @Value("${app.proof.master-code:1234}")
    private String masterCodeValue;

    private final RouteDeliveryPointRepository routeDeliveryPointRepository;

    public String uploadProof(Long routeId, Long deliveryPointId, MultipartFile file) throws IOException {
        RouteDeliveryPoint rdp = routeDeliveryPointRepository
                .findByRouteIdAndDeliveryPointId(routeId, deliveryPointId)
                .orElseThrow(() -> new ResourceNotFoundException("Point de livraison introuvable dans cette tournée"));

        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

        rdp.setProofImagePath(filename);
        routeDeliveryPointRepository.save(rdp);
        return filename;
    }

    public void validateWithCode(Long routeId, Long deliveryPointId, String code) {
        RouteDeliveryPoint rdp = routeDeliveryPointRepository
                .findByRouteIdAndDeliveryPointId(routeId, deliveryPointId)
                .orElseThrow(() -> new ResourceNotFoundException("Point de livraison introuvable dans cette tournée"));

        boolean masterCode = masterCodeValue.equals(code);
        boolean matchesStored = rdp.getConfirmationCode() != null && rdp.getConfirmationCode().equals(code);
        if (!masterCode && !matchesStored) {
            throw new InvalidOperationException("Code de confirmation invalide");
        }
        rdp.setProofValidated(true);
        routeDeliveryPointRepository.save(rdp);
    }

    public void generateConfirmationCode(Long routeId, Long deliveryPointId) {
        RouteDeliveryPoint rdp = routeDeliveryPointRepository
                .findByRouteIdAndDeliveryPointId(routeId, deliveryPointId)
                .orElseThrow(() -> new ResourceNotFoundException("Point de livraison introuvable dans cette tournée"));

        String code = String.format("%06d", (int) (Math.random() * 1000000));
        rdp.setConfirmationCode(code);
        routeDeliveryPointRepository.save(rdp);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains("."))
            return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }
}
