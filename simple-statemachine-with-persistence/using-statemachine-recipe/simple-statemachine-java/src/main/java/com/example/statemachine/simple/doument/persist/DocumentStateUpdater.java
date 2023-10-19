package com.example.statemachine.simple.doument.persist;

import com.example.statemachine.simple.doument.DocumentEvent;
import com.example.statemachine.simple.doument.DocumentState;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.recipes.persist.AbstractPersistStateMachineHandler.GenericPersistStateChangeListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

public class DocumentStateUpdater
  implements GenericPersistStateChangeListener<DocumentState, DocumentEvent> {

  private final JdbcTemplate jdbcTemplate;

  public DocumentStateUpdater(final JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void onPersist(
    State<DocumentState, DocumentEvent> state, Message<DocumentEvent> message,
    Transition<DocumentState, DocumentEvent> transition,
    StateMachine<DocumentState, DocumentEvent> stateMachine) {

    if (Objects.nonNull(message) && message.getHeaders().containsKey("document")) {
      final var documentId = message.getHeaders().get("document", Integer.class);

      jdbcTemplate.update(
        "update documents set state = ? where id = ?",
        state.getId().toString(), documentId);
    }
  }
}
