package com.team.updevic001.model.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventRequest {

    private String title;

    private LocalDateTime date;

    private String location;

    private String shortDescription;

    private String description;

    private String category;

    private BigDecimal price;

    private boolean isFree;
}
