package com.example.statemachine.simple.document.config

import com.example.statemachine.simple.document.DocumentEvent
import com.example.statemachine.simple.document.DocumentState
import com.example.statemachine.simple.document.config.action.SendEmailAction
import com.example.statemachine.simple.document.config.lintener.StateMachineListenerAdapterImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.statemachine.config.EnableStateMachineFactory
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer
import org.springframework.statemachine.listener.StateMachineListenerAdapter
import org.springframework.statemachine.persist.StateMachineRuntimePersister

@Configuration
@EnableStateMachineFactory
class DocumentStateMachineConfig(
  private val stateMachineRuntimePersister: StateMachineRuntimePersister<DocumentState, DocumentEvent, String>
) : EnumStateMachineConfigurerAdapter<DocumentState, DocumentEvent>() {
  // 상태 기계 설정을 진행한다.
  @Throws(Exception::class)
  override fun configure(config: StateMachineConfigurationConfigurer<DocumentState, DocumentEvent>) {
    config.withConfiguration()
      .autoStartup(true)
      .listener(listener)

    // 런타임 persister 설정
    config.withPersistence()
      .runtimePersister(stateMachineRuntimePersister)
  }

  // 상태 기계에서 사용할 'state (상태)' 를 매핑한다.
  @Throws(Exception::class)
  override fun configure(states: StateMachineStateConfigurer<DocumentState, DocumentEvent>) {
    states.withStates()
      .initial(DocumentState.DRAFT)
      .state(DocumentState.UNDER_MEDIATION)
      .state(DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE)
      .state(DocumentState.PUBLIC_DISCLOSURE, SendEmailAction()) // state 별 로직을 설정 가능.
    // .end() // 최종 상태가 존재하는 경우 이 메소드를 통해 설정할 수 있다.
  }

  // 상태 기계의 'event (이벤트)' 와 'state transition (상태 전이)' 를 매핑한다.
  @Throws(Exception::class)
  override fun configure(transitions: StateMachineTransitionConfigurer<DocumentState, DocumentEvent>) {
    transitions.withExternal()
      .source(DocumentState.DRAFT).target(DocumentState.UNDER_MEDIATION)
      .event(DocumentEvent.DOCUMENT_PUBLISHED_BY_USER) //.guard(validationGuard()) // 특정 조건에서만 상태 전이가 이루어지도록 설정할 수 있다.
      .and()
      .withExternal()
      .source(DocumentState.DRAFT).target(DocumentState.PUBLIC_DISCLOSURE)
      .event(DocumentEvent.DOCUMENT_PUBLISHED_BY_ADMINISTRATOR)
      .and()
      .withExternal()
      .source(DocumentState.UNDER_MEDIATION)
      .target(DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE)
      .event(DocumentEvent.APPROVED)
      .and()
      .withExternal()
      .source(DocumentState.UNDER_MEDIATION).target(DocumentState.DRAFT)
      .event(DocumentEvent.NEEDS_TO_BE_ADJUSTED_OR_REJECTED)
      .and()
      .withExternal()
      .source(DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE)
      .target(DocumentState.PUBLIC_DISCLOSURE)
      .event(DocumentEvent.APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR)
      .and()
      .withExternal()
      .source(DocumentState.PUBLIC_DISCLOSURE).target(DocumentState.DRAFT)
      .event(DocumentEvent.EXPIRED)
  }

  @get:Bean
  val listener: StateMachineListenerAdapter<DocumentState, DocumentEvent>
    get() = StateMachineListenerAdapterImpl()
}