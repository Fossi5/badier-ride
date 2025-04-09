package com.badier.badier_ride.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPointOrderDto {
    private Long id;
    private Integer sequenceOrder;
    private Boolean isStartPoint;
    private Boolean isEndPoint;
}

