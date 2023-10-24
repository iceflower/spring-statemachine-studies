package com.example.statemachine.simple.doument.config;

import com.example.statemachine.simple.doument.config.exception.PersistenceDataIsNotExistException;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;

public class DocumentIdChecker {
  private final JdbcTemplate jdbcTemplate;

  public DocumentIdChecker(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public boolean isExist(final int documentId) throws PersistenceDataIsNotExistException {
    final var cnt = Objects.requireNonNull(
      jdbcTemplate.queryForObject("SELECT COUNT(id) FROM documents WHERE id = ?",
        Integer.class, documentId));

    if (cnt != 1) {
      final var message = String.format(
        "주어진 documentId \'%s\' 는 데이터베이스에 존재하지 않는 값입니다", documentId);
      throw new PersistenceDataIsNotExistException(message);
    }

    return true;
  }
}
