package com.concursos.api_concursos.model;

import com.concursos.api_concursos.enums.StatusEdital;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "edital")
@Data
@NoArgsConstructor
public class Edital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "banca_id", nullable = false)
    private Banca banca;

    @Column(name = "url_edital", nullable = false, unique = true, length = 500)
    private String urlEdital;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String orgao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusEdital status;

    @Column(name = "data_publicacao")
    private LocalDate dataPublicacao;

    @Column(name = "data_inscricao_inicio")
    private LocalDateTime dataInscricaoInicio;

    @Column(name = "data_inscricao_fim")
    private LocalDateTime dataInscricaoFim;


    // Mapeamento JSONB
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_cargos", columnDefinition = "jsonb")
    private String jsonCargos;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    @PreUpdate
    public void atualizarTimestamp() {
        this.atualizadoEm = LocalDateTime.now();
    }
}