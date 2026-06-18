package com.concursos.api_concursos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditalScrapedDataDTO {
    private String titulo;
    private String orgao;
    private String urlEdital;
    private LocalDate dataPublicacao;
    private LocalDateTime dataInscricaoInicio;
    private LocalDateTime dataInscricaoFim;
    private List<CargoDTO> cargos;
}