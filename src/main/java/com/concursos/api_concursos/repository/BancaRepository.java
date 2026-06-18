package com.concursos.api_concursos.repository;

import com.concursos.api_concursos.model.Banca;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BancaRepository extends JpaRepository<Banca, Long> {
    boolean existsBySigla(String sigla);
}