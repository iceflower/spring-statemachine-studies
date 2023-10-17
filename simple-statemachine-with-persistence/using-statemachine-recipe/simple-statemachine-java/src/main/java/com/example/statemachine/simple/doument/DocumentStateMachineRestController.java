package com.example.statemachine.simple.doument;

import com.example.statemachine.simple.doument.persist.DocumentStateMachine;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DocumentStateMachineRestController {

  private final JdbcTemplate jdbcTemplate;
  private final DocumentStateMachine documentStateMachine;


  public DocumentStateMachineRestController(
    final JdbcTemplate jdbcTemplate,
    final DocumentStateMachine documentStateMachine) {
    this.jdbcTemplate = jdbcTemplate;
    this.documentStateMachine = documentStateMachine;
  }

  @GetMapping("/state-list")
  public DocumentState[] getStateList() {

    return DocumentState.values();
  }


  @GetMapping("/event-list")
  public DocumentEvent[] getEventList() {

    return DocumentEvent.values();
  }

  @GetMapping("/document-list")
  public List<DocumentVo> documentList() {

    return documentStateMachine.listDbEntries();
  }


  @GetMapping("/{documentId}/current-state")
  public DocumentState currentState(@PathVariable("documentId") final int documentId) {

    return getStateFromDatabase(documentId);
  }


  @PostMapping("/{documentId}/send/{event}")
  public EventResultStatement sendEvent(
    @PathVariable("documentId") final int documentId,
    @PathVariable("event") final DocumentEvent event) {

    final var prevState = getStateFromDatabase(documentId);

    documentStateMachine.change(documentId, event);

    final var changedState = getStateFromDatabase(documentId);

    return new EventResultStatement(documentId, event, new State(prevState, changedState));
  }

  private DocumentState getStateFromDatabase(final int documentId) {
    return jdbcTemplate.queryForObject(
      "SELECT state FROM documents WHERE id = ?",
      DocumentState.class, documentId);
  }

  public record EventResultStatement(int documentId, DocumentEvent event, State state) {

  }

  public record State(DocumentState prev, DocumentState changed) {

  }
}
