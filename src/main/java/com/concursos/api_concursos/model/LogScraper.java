package com.concursos.api_concursos.model;

import com.concursos.api_concursos.enums.LogStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_scraper")
@Data
@NoArgsConstructor
public class LogScraper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "banca_id", nullable = false)
    private Banca banca;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LogStatus status;

    @Column(columnDefinition = "text")
    private String mensagem;
}