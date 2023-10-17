package com.example.statemachine.simple.document

import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.statemachine.StateMachine
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class DocumentStateMachineRestController(
  private val stateMachine: StateMachine<DocumentState, DocumentEvent>
) {
  
  @get:GetMapping("/state-list")
  val stateList: Array<DocumentState>
    get() = DocumentState.entries.toTypedArray()

  @get:GetMapping("/event-list")
  val eventList: Array<DocumentEvent>
    get() = DocumentEvent.entries.toTypedArray()

  @GetMapping("/current-state")
  fun currentState(): DocumentState {
    return stateMachine.state.id
  }

  @PostMapping("/send/{event}")
  fun sendEvent(@PathVariable("event") event: DocumentEvent): EventResultStatement {
    val prevState = stateMachine.state.id

    val result = stateMachine.sendEvent(makeEventMessage(event))
      .blockLast()!!

    val changedState = result.region
      .state.id

    return EventResultStatement(event, State(prevState, changedState))
  }

  private fun makeEventMessage(event: DocumentEvent): Mono<Message<DocumentEvent>> {
    return Mono.just(
      MessageBuilder.withPayload(event)
        .build()
    )
  }

  data class State(val prev: DocumentState, val changed: DocumentState)

  data class EventResultStatement(val event: DocumentEvent, val state: State)
}
