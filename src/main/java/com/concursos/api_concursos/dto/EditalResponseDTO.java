package com.concursos.api_concursos.dto;

import com.concursos.api_concursos.enums.StatusEdital;
import com.concursos.api_concursos.model.Edital;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EditalResponseDTO(
    Long id,
    String bancaSigla,
    String titulo,
    String orgao,
    String urlEdital,
    StatusEdital status,
    LocalDate dataPublicacao,
    LocalDateTime dataInscricaoInicio,
    LocalDateTime dataInscricaoFim,
    String jsonCargos,
    LocalDateTime atualizadoEm
) {
    public EditalResponseDTO(Edital edital) {
        this(
            edital.getId(),
            edital.getBanca().getSigla(),
            edital.getTitulo(),
            edital.getOrgao(),
            edital.getUrlEdital(),
            edital.getStatus(),
            edital.getDataPublicacao(),
            edital.getDataInscricaoInicio(),
            edital.getDataInscricaoFim(),
            edital.getJsonCargos(),
            edital.getAtualizadoEm()
        );
    }
}