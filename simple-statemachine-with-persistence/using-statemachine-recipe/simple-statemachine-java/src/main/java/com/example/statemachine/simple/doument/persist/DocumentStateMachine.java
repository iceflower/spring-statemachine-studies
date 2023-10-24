package com.example.statemachine.simple.doument.persist;

import com.example.statemachine.simple.doument.DocumentEvent;
import com.example.statemachine.simple.doument.DocumentState;
import com.example.statemachine.simple.doument.DocumentVo;
import com.example.statemachine.simple.doument.config.DocumentIdChecker;
import com.example.statemachine.simple.doument.config.exception.PersistenceDataIsNotExistException;
import java.util.List;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.messaging.support.MessageBuilder;

public class DocumentStateMachine {

  private final JdbcTemplate jdbcTemplate;
  private final DocumentIdChecker documentIdChecker;
  private final DocumentPersistStateMachineHandler handler;

  public DocumentStateMachine(JdbcTemplate jdbcTemplate,
    DocumentPersistStateMachineHandler documentPersistStateMachineHandler) {
    this.jdbcTemplate = jdbcTemplate;
    this.documentIdChecker =  new DocumentIdChecker(jdbcTemplate);
    this.handler = documentPersistStateMachineHandler;
    this.handler.addPersistStateChangeListener(new DocumentStateUpdater(jdbcTemplate));
    this.handler.addPersistStateChangeListener(new DocumentStateChangedHistoryWriter(jdbcTemplate));
  }

  public List<DocumentVo> listDbEntries() {
    return jdbcTemplate.query(
      "SELECT id, state FROM documents", documentRowMapper());
  }

  public void change(final int documentId, final DocumentEvent event)
    throws PersistenceDataIsNotExistException {

    documentIdChecker.isExist(documentId);

    final var d = jdbcTemplate.queryForObject(
      "SELECT id, state FROM documents WHERE id = ?",
      documentRowMapper(), documentId);

    final var message = MessageBuilder.withPayload(event)
      .setHeader("document", documentId)
      .build();

    handler.handleEventWithStateReactively(
        message, Objects.requireNonNull(d).state())
      .block();
  }

  private static RowMapper<DocumentVo> documentRowMapper() {
    return (rs, rowNum) -> new DocumentVo(
      rs.getInt("id"),
      DocumentState.valueOf(rs.getString("state")));
  }

}
