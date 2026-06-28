package com.concursos.api_concursos.repository;

import com.concursos.api_concursos.enums.StatusEdital;
import com.concursos.api_concursos.model.Edital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

public interface EditalRepository extends JpaRepository<Edital, Long>, JpaSpecificationExecutor<Edital> {
    
    Optional<Edital> findByUrlEdital(String urlEdital);

    @Transactional
    @Modifying
    @Query("UPDATE Edital e SET e.status = :novoStatus " +
           "WHERE e.status = :statusAtual AND e.dataInscricaoFim < :hoje")
    int atualizarStatusEditaisVencidos(
            @Param("statusAtual") StatusEdital statusAtual,
            @Param("novoStatus") StatusEdital novoStatus,
            @Param("hoje") LocalDateTime hoje
    );
}