package com.example.statemachine.simple.document.config

import com.example.statemachine.simple.document.DocumentEvent
import com.example.statemachine.simple.document.DocumentState
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.statemachine.config.StateMachineFactory
import org.springframework.statemachine.persist.StateMachineRuntimePersister
import org.springframework.statemachine.service.DefaultStateMachineService
import org.springframework.statemachine.service.StateMachineService

@Configuration
class StateMachineServiceConfig {
  @Bean
  fun stateMachineService(
    stateMachineFactory: StateMachineFactory<DocumentState, DocumentEvent>?,
    stateMachineRuntimePersister: StateMachineRuntimePersister<DocumentState, DocumentEvent, String?>?
  ): StateMachineService<DocumentState, DocumentEvent> {
    return DefaultStateMachineService(stateMachineFactory, stateMachineRuntimePersister)
  }
}
