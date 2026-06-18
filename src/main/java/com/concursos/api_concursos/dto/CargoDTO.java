package com.concursos.api_concursos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CargoDTO {
    private String nome;
    private Integer vagas;
    private String remuneracao;
    private String escolaridade;
}