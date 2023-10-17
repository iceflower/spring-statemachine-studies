package com.example.statemachine.simple.doument;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DocumentStateMachineRestController {
  private final StateMachine<DocumentState, DocumentEvent> stateMachine;

  public DocumentStateMachineRestController(
    StateMachine<DocumentState, DocumentEvent> stateMachine) {
    this.stateMachine = stateMachine;
  }

  @GetMapping("/state-list")
  public DocumentState[] getStateList() {

    return DocumentState.values();
  }


  @GetMapping("/event-list")
  public DocumentEvent[] getSventList() {

    return DocumentEvent.values();
  }

  @GetMapping("/current-state")
  public DocumentState currentState() {

    return stateMachine.getState()
      .getId();
  }

  @PostMapping("/send/{event}")
  public EventResultStatement sendEvent(@PathVariable("event") final DocumentEvent event) {

    final var prevState = stateMachine.getState()
      .getId();

    final var result = stateMachine.sendEvent(makeEventMessage(event))
      .blockLast();

    final var changedState = result.getRegion()
      .getState().getId();

    return new EventResultStatement(event, new State(prevState, changedState));
  }

  private Mono<Message<DocumentEvent>> makeEventMessage(final DocumentEvent event) {

    return Mono.just(MessageBuilder.withPayload(event)
      .build());
  }

  private record EventResultStatement(DocumentEvent event, State state) {

  }
  private record State(DocumentState prev, DocumentState changed) {

  }
}
