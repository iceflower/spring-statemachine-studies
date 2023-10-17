package com.example.statemachine.simple.doument.config;

import com.example.statemachine.simple.doument.DocumentEvent;
import com.example.statemachine.simple.doument.DocumentState;
import com.example.statemachine.simple.doument.persist.DocumentPersistStateMachineHandler;
import com.example.statemachine.simple.doument.persist.DocumentStateMachine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.statemachine.StateMachine;

@Configuration
public class PersistHandlerConfig {

  private final StateMachine<DocumentState, DocumentEvent> stateMachine;
  private final JdbcTemplate jdbcTemplate;

  public PersistHandlerConfig(
    final StateMachine<DocumentState, DocumentEvent> stateMachine,
    final JdbcTemplate jdbcTemplate) {

    this.jdbcTemplate = jdbcTemplate;
    this.stateMachine = stateMachine;
  }

  @Bean
  public DocumentStateMachine persistDocumentStateMachine() {

    return new DocumentStateMachine(jdbcTemplate, documentPersistStateMachineHandler());
  }

  @Bean
  public DocumentPersistStateMachineHandler documentPersistStateMachineHandler() {
    return new DocumentPersistStateMachineHandler(stateMachine);
  }
}
