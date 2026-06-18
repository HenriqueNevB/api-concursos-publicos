package com.concursos.api_concursos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BancaDataDTO {
    private String titulo;
    private String urlEdital;
    private String orgao;
    private List<CargoDTO> cargos;
}