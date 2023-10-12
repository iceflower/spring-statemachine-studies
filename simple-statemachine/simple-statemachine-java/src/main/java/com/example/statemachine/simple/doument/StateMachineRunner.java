package com.example.statemachine.simple.doument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class StateMachineRunner implements CommandLineRunner {

  private static final Logger logger
    = LoggerFactory.getLogger(StateMachineRunner.class);
  private final StateMachine<DocumentState, DocumentEvent> stateMachine;

  public StateMachineRunner(final StateMachine<DocumentState, DocumentEvent> stateMachine) {
    this.stateMachine = stateMachine;
  }

  @Override
  public void run(String... args) throws Exception {
    logger.info("상태 머신 시작");

    stateMachine.sendEvent(eventMessage(DocumentEvent.DOCUMENT_PUBLISHED_BY_USER)).blockLast();
    stateMachine.sendEvent(eventMessage(DocumentEvent.APPROVED)).blockLast();
    stateMachine.sendEvent(
      eventMessage(DocumentEvent.APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR)).blockLast();
    stateMachine.sendEvent(eventMessage(DocumentEvent.EXPIRED)).blockLast();

    logger.info("상태 머신 종료");
  }

  private Mono<Message<DocumentEvent>> eventMessage(final DocumentEvent event) {

    return Mono.just(MessageBuilder.withPayload(event)
      .build());
  }
}
