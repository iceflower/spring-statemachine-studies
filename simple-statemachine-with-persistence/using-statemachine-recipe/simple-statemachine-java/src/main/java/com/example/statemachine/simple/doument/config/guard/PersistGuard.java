package com.example.statemachine.simple.doument.config.guard;

import com.example.statemachine.simple.doument.DocumentEvent;
import com.example.statemachine.simple.doument.DocumentState;
import com.example.statemachine.simple.doument.config.DocumentIdChecker;
import com.example.statemachine.simple.doument.config.exception.PersistenceDataIsNotExistException;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

public class PersistGuard implements Guard<DocumentState, DocumentEvent> {

  private final DocumentIdChecker documentIdChecker;

  public PersistGuard(JdbcTemplate jdbcTemplate) {

    this.documentIdChecker = new DocumentIdChecker(jdbcTemplate);


  }

  @Override
  public boolean evaluate(StateContext<DocumentState, DocumentEvent> context) {

    final var messageHeaders = context.getMessageHeaders();

    if (!messageHeaders.containsKey("document")) {
      return false;
    }

    try {
      final var documentId = Objects.requireNonNull(
        messageHeaders.get("document", Integer.class));


      return documentIdChecker.isExist(documentId);
    } catch (NullPointerException | IllegalArgumentException | PersistenceDataIsNotExistException ex) {
      return false;
    }
  }
}
