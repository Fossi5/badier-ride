package com.badier.badier_ride.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequest {
    @NotBlank(message = "Le contenu du message est obligatoire")
    private String content;
}
