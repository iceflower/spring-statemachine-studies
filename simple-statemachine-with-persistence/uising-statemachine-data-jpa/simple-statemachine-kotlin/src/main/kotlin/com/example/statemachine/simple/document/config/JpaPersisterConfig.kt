package com.example.statemachine.simple.document.config

import com.example.statemachine.simple.document.DocumentEvent
import com.example.statemachine.simple.document.DocumentState
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.statemachine.data.jpa.JpaPersistingStateMachineInterceptor
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository
import org.springframework.statemachine.persist.StateMachineRuntimePersister

@Configuration
class JpaPersisterConfig {
  @Bean
  fun stateMachineRuntimePersister(
    jpaStateMachineRepository: JpaStateMachineRepository?
  ): StateMachineRuntimePersister<DocumentState, DocumentEvent, String> {
    return JpaPersistingStateMachineInterceptor(
      jpaStateMachineRepository
    )
  }
}
