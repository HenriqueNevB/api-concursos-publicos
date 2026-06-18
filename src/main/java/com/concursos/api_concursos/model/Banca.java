package com.concursos.api_concursos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "banca")
@Data
@NoArgsConstructor
public class Banca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 20)
    private String sigla;

    @Column(name = "site_oficial", length = 255)
    private String siteOficial;

    @Column(name = "scraper_bean", length = 100)
    private String scraperBean;
}