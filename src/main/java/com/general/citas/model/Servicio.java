package com.general.citas.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


//Entidad Uno

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "services")
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false , name = "uuid")
    private String uuid;

    @Column(nullable = false, unique = true , name ="nombre")
    private String name;

    @Column(nullable = false , name ="descripcion")
    private String description;

    @Column(nullable = false , name ="duracion")
    private Integer duration;

    @Column(nullable = false , name ="precio")
    private BigDecimal price;

    @OneToMany(mappedBy = "service",  fetch = FetchType.LAZY , cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();


}