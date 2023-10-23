package com.example.statemachine.simple;

import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import reactor.core.publisher.Mono;

public class StateMachineUnitTestHelper<S, E> {

  private final StateMachine<S, E> stateMachine;

  public StateMachineUnitTestHelper(StateMachine<S, E> stateMachine) {
    this.stateMachine = stateMachine;
  }

  public StateMachineTestPlan<S, E> getStateMachineTestPlan(final S state) {
    return StateMachineTestPlanBuilder.<S, E>builder()
      .stateMachine(stateMachine)
      .step()
      .expectState(state)
      .and()
      .build();
  }



  public StateMachineTestPlan<S, E> getStateMachineTestPlan(final E event, final S prevState, final S expectedState) {
    return StateMachineTestPlanBuilder.<S, E>builder()
      .stateMachine(stateMachine)
      .step()
      .expectState(prevState)
      .and()
      .step()
      .sendEvent(event)
      .and()
      .step()
      .expectState(expectedState)
      .and()
      .build();
  }



  public void sendEvent(final E event) {
    final var message = Mono.just(MessageBuilder.withPayload(event)
      .build());

    stateMachine.sendEvent(message)
      .blockLast();
  }

}
