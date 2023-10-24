package com.example.statemachine.simple

import org.springframework.messaging.support.MessageBuilder
import org.springframework.statemachine.StateMachine
import org.springframework.statemachine.test.StateMachineTestPlan
import org.springframework.statemachine.test.StateMachineTestPlanBuilder
import reactor.core.publisher.Mono

class StateMachineUnitTestHelper<S, E>(private val stateMachine: StateMachine<S, E>) {
  fun getStateMachineTestPlan(state: S): StateMachineTestPlan<S, E> {
    return StateMachineTestPlanBuilder.builder<S, E>()
      .stateMachine(stateMachine)
      .step()
      .expectState(state)
      .and()
      .build()
  }

  fun getStateMachineTestPlan(
    event: E,
    prevState: S,
    expectedState: S
  ): StateMachineTestPlan<S, E> {
    return StateMachineTestPlanBuilder.builder<S, E>()
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
      .build()
  }

  fun sendEvent(documentId: Int, event: E) {
    val message = Mono.just(
      MessageBuilder.withPayload(event!!)
        .setHeader("document", documentId)
        .build()
    )


    stateMachine.sendEvent(message)
      .blockLast()
  }
}
