package com.concursos.api_concursos.repository;

import com.concursos.api_concursos.model.Edital;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EditalRepository extends JpaRepository<Edital, Long> {
    Optional<Edital> findByUrlEdital(String urlEdital);
}