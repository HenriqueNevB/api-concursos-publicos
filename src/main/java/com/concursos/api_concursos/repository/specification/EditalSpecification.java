package com.concursos.api_concursos.repository.specification;

import com.concursos.api_concursos.enums.StatusEdital;
import com.concursos.api_concursos.model.Edital;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EditalSpecification {

    public static Specification<Edital> filtrar(
            String titulo,
            String cargo,
            StatusEdital status,
            String orgao,
            String bancaSigla,
            LocalDate dataInscricaoInicio,
            LocalDate dataInscricaoFim) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por título
            if (titulo != null && !titulo.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("titulo")),
                        "%" + titulo.trim().toLowerCase() + "%"
                ));
            }

            // Filtro por orgão
            if (orgao != null && !orgao.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("orgao")),
                        "%" + orgao.trim().toLowerCase() + "%"
                ));
            }

            // Filtro por status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Filtro por banca
            if (bancaSigla != null && !bancaSigla.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("banca").get("sigla")),
                        bancaSigla.trim().toUpperCase()
                ));
            }

            // Filtro por data de inicio de inscrições
            if (dataInscricaoInicio != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("dataInscricaoInicio").as(LocalDate.class),
                        dataInscricaoInicio
                ));
            }

            // Filtro por data de fim de inscrições
            if (dataInscricaoFim != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("dataInscricaoFim").as(LocalDate.class),
                        dataInscricaoFim
                ));
            }

            // Filtro de cargo
            if (cargo != null && !cargo.trim().isEmpty()) {
                var jsonTextual = criteriaBuilder.function("text", String.class, root.get("jsonCargos"));
                
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(jsonTextual),
                        "%" + cargo.trim().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}