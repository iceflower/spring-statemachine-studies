package com.example.statemachine.simple.doument.config;

import com.example.statemachine.simple.doument.DocumentEvent;
import com.example.statemachine.simple.doument.DocumentState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;

@Configuration
public class StateMachineServiceConfig {

  @Bean
  public StateMachineService<DocumentState, DocumentEvent> stateMachineService(
    StateMachineFactory<DocumentState, DocumentEvent> stateMachineFactory,
    StateMachineRuntimePersister<DocumentState, DocumentEvent, String> stateMachineRuntimePersister) {

    return new DefaultStateMachineService<>(stateMachineFactory, stateMachineRuntimePersister);
  }

}
