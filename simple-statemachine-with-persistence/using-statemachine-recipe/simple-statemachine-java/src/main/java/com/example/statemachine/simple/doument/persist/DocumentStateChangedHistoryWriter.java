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

public class DocumentStateChangedHistoryWriter
  implements GenericPersistStateChangeListener<DocumentState, DocumentEvent> {

  private final JdbcTemplate jdbcTemplate;

  public DocumentStateChangedHistoryWriter(final JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void onPersist(
    State<DocumentState, DocumentEvent> state, Message<DocumentEvent> message,
    Transition<DocumentState, DocumentEvent> transition,
    StateMachine<DocumentState, DocumentEvent> stateMachine) {

    if (Objects.nonNull(message) && message.getHeaders().containsKey("document")) {
      final var documentId = message.getHeaders().get("document", Integer.class);
      final var event = transition.getTrigger().getEvent();
      final var prevState = transition.getSource().getId();
      final var changedState = transition.getTarget().getId();

      jdbcTemplate.update(
        "INSERT INTO documents_state_history(document_id, event, prev_state, changed_state) VALUES (?, ?, ?, ?)",
        documentId, event.toString(), prevState.toString(), changedState.toString());
    }
  }
}
