package com.example.statemachine.simple.doument.config;

import com.example.statemachine.simple.doument.DocumentEvent;
import com.example.statemachine.simple.doument.DocumentState;
import com.example.statemachine.simple.doument.config.action.SendEmailAction;
import com.example.statemachine.simple.doument.config.guard.PersistGuard;
import com.example.statemachine.simple.doument.config.listener.StateMachineListenerAdapterImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;

@Configuration
@EnableStateMachine
public class DocumentStateMachineConfig
  extends EnumStateMachineConfigurerAdapter<DocumentState, DocumentEvent> {

  private final JdbcTemplate jdbcTemplate;

  public DocumentStateMachineConfig(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  // 상태 기계 설정을 진행한다.
  @Override
  public void configure(StateMachineConfigurationConfigurer<DocumentState, DocumentEvent> config)
    throws Exception {
    config.withConfiguration()
      .autoStartup(true)
      .listener(getListener());
  }

  // 상태 기계에서 사용할 'state (상태)' 를 매핑한다.
  @Override
  public void configure(StateMachineStateConfigurer<DocumentState, DocumentEvent> states)
    throws Exception {

    states.withStates()
      .initial(DocumentState.DRAFT)
      .state(DocumentState.UNDER_MEDIATION)
      .state(DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE)
      .state(DocumentState.PUBLIC_DISCLOSURE, new SendEmailAction());
    // .end() // 최종 상태가 존재하는 경우 이 메소드를 통해 설정할 수 있다.
  }

  // 상태 기계의 'event (이벤트)' 와 'state transition (상태 전이)' 를 매핑한다.
  @Override
  public void configure(StateMachineTransitionConfigurer<DocumentState, DocumentEvent> transitions)
    throws Exception {

    final var persistGuard = new PersistGuard(jdbcTemplate);

    transitions.withExternal()
      .source(DocumentState.DRAFT).target(DocumentState.UNDER_MEDIATION)
      .event(DocumentEvent.DOCUMENT_PUBLISHED_BY_USER)
      .guard(persistGuard)
      .and()
      .withExternal()
      .source(DocumentState.DRAFT).target(DocumentState.PUBLIC_DISCLOSURE)
      .event(DocumentEvent.DOCUMENT_PUBLISHED_BY_ADMINISTRATOR)
      .guard(persistGuard)
      .and()
      .withExternal()
      .source(DocumentState.UNDER_MEDIATION).target(DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE)
      .event(DocumentEvent.APPROVED)
      .guard(persistGuard)
      .and()
      .withExternal()
      .source(DocumentState.UNDER_MEDIATION).target(DocumentState.DRAFT)
      .event(DocumentEvent.NEEDS_TO_BE_ADJUSTED_OR_REJECTED)
      .guard(persistGuard)
      .and()
      .withExternal()
      .source(DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE).target(DocumentState.PUBLIC_DISCLOSURE)
      .event(DocumentEvent.APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR)
      .guard(persistGuard)
      .and()
      .withExternal()
      .source(DocumentState.PUBLIC_DISCLOSURE).target(DocumentState.DRAFT)
      .event(DocumentEvent.EXPIRED)
      .guard(persistGuard);

  }

  @Bean
  public StateMachineListenerAdapter<DocumentState, DocumentEvent> getListener() {
    return new StateMachineListenerAdapterImpl();
  }
}
