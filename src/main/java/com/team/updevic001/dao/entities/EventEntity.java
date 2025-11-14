package com.team.updevic001.dao.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class EventEntity {

    @Id
    private String id;

    private String title;

    private LocalDateTime date;

    private String location;

    private String shortDescription;

    @Column(length = 5000)
    private String description;

    private String imageUrl;

    private String imageKey;

    private String category;

    private BigDecimal price;

    private boolean isFree;
}
