package com.example.statemachine.simple.document.config

import com.example.statemachine.simple.document.DocumentEvent
import com.example.statemachine.simple.document.DocumentState
import com.example.statemachine.simple.document.persist.DocumentPersistStateMachineHandler
import com.example.statemachine.simple.document.persist.DocumentStateMachine
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.statemachine.StateMachine

@Configuration
class PersistHandlerConfig(
  private val stateMachine: StateMachine<DocumentState?, DocumentEvent?>,
  private val jdbcTemplate: JdbcTemplate
) {

  @Bean
  fun persistDocumentStateMachine(): DocumentStateMachine {
    return DocumentStateMachine(jdbcTemplate, documentPersistStateMachineHandler())
  }

  @Bean
  fun documentPersistStateMachineHandler(): DocumentPersistStateMachineHandler {
    return DocumentPersistStateMachineHandler(stateMachine)
  }
}
