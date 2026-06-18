package com.concursos.api_concursos.repository;

import com.concursos.api_concursos.model.LogScraper;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogScraperRepository extends JpaRepository<LogScraper, Long> {
}