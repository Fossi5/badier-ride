package com.badier.badier_ride.service;

import com.badier.badier_ride.entity.Route;
import com.badier.badier_ride.repository.RouteRepository;
import com.badier.badier_ride.repository.AddressRepository;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final RouteRepository routeRepository;
    private final AddressRepository addressRepository;

    public byte[] exportRoutesCsv() throws IOException {
        List<Route> routes = routeRepository.findAll();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {
            writer.writeNext(new String[]{"ID", "Nom", "Chauffeur", "Répartiteur", "Statut", "Départ", "Fin"});
            for (Route r : routes) {
                writer.writeNext(new String[]{
                    String.valueOf(r.getId()),
                    r.getName(),
                    r.getDriver() != null ? r.getDriver().getUsername() : "",
                    r.getDispatcher() != null ? r.getDispatcher().getUsername() : "",
                    r.getStatus().name(),
                    r.getStartTime() != null ? r.getStartTime().toString() : "",
                    r.getEndTime() != null ? r.getEndTime().toString() : ""
                });
            }
            writer.flush();
            return out.toByteArray();
        }
    }

    public byte[] exportAddressesCsv() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {
            writer.writeNext(new String[]{"ID", "Rue", "Ville", "Code postal", "Pays", "Latitude", "Longitude", "Vérifié"});
            addressRepository.findAll().forEach(a -> writer.writeNext(new String[]{
                String.valueOf(a.getId()),
                a.getStreet(),
                a.getCity(),
                a.getPostalCode(),
                a.getCountry() != null ? a.getCountry() : "",
                a.getLatitude() != null ? String.valueOf(a.getLatitude()) : "",
                a.getLongitude() != null ? String.valueOf(a.getLongitude()) : "",
                String.valueOf(a.getIsVerified())
            }));
            writer.flush();
            return out.toByteArray();
        }
    }
}
