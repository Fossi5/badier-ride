package com.badier.badier_ride.validation;

import com.badier.badier_ride.dto.RouteRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EndAfterStartValidator implements ConstraintValidator<EndAfterStart, RouteRequest> {

    @Override
    public boolean isValid(RouteRequest request, ConstraintValidatorContext context) {
        if (request.getEndTime() == null || request.getStartTime() == null) {
            return true; // pas de contrainte si l'un des deux est absent
        }
        return request.getEndTime().isAfter(request.getStartTime());
    }
}
