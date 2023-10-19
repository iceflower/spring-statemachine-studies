package com.example.statemachine.simple.document

import com.example.statemachine.simple.document.config.lintener.StateMachineLogListener
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.statemachine.StateMachine
import org.springframework.statemachine.StateMachineContext
import org.springframework.statemachine.StateMachinePersist
import org.springframework.statemachine.service.StateMachineService
import org.springframework.util.ObjectUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class DocumentStateMachineRestController(
  private val stateMachineService: StateMachineService<DocumentState, DocumentEvent>,
  private val stateMachinePersist: StateMachinePersist<DocumentState, DocumentEvent, String>
) {
  private val listener: StateMachineLogListener = StateMachineLogListener()
  private var currentStateMachine: StateMachine<DocumentState, DocumentEvent>? = null

  companion object {
    const val MACHINE_ID_1 = "data-jpa-persist-1"
    const val MACHINE_ID_2 = "data-jpa-persist-2"

  }

  @GetMapping("/state-machine-list")
  fun getStateMachineList(): Array<String> {
    return arrayOf(MACHINE_ID_1, MACHINE_ID_2)
  }

  @get:GetMapping("/state-list")
  val stateList: Array<DocumentState>
    get() = DocumentState.entries.toTypedArray()

  @get:GetMapping("/event-list")
  val ventList: Array<DocumentEvent>
    get() = DocumentEvent.entries.toTypedArray()

  @GetMapping("/{machine-id}/current-state")
  @Throws(Exception::class)
  fun currentState(@PathVariable("machine-id") machineId: String): StateMachineStatement {
    getStateMachine(machineId)
    val stateMachineContext = stateMachinePersist.read(machineId)
    return StateMachineStatement(machineId, listener.getMessages(), stateMachineContext)
  }

  @PostMapping("/{machine-id}/send/{event}")
  fun sendEvent(
    @PathVariable("machine-id") machineId: String,
    @PathVariable("event") event: DocumentEvent
  ): EventResultStatement {
    val stateMachine = getStateMachine(machineId)
    val prevState = stateMachine!!.state
      .id
    val result = stateMachine.sendEvent(makeEventMessage(event))
      .blockLast()!!
    val changedState = result.region
      .state.id
    return EventResultStatement(machineId, event, State(prevState, changedState))
  }

  @Synchronized
  private fun getStateMachine(
    machineId: String
  ): StateMachine<DocumentState, DocumentEvent>? {
    listener.resetMessages()

    // currentStateMachine 이 null일 경우,
    // 새로운 stateMachine 객체를 생성한다.
    if (currentStateMachine == null) {
      currentStateMachine = stateMachineService.acquireStateMachine(machineId)
      currentStateMachine!!.addStateListener(listener)
      currentStateMachine!!.startReactively().block()
      return currentStateMachine
    }

    // currentStateMachine 이 null은 아니지만, currentStateMachine의 id가 주어진 machineId와 다를 경우
    // 기존 stateMachine 객체는 release 및 stop 처리를 진행한 후, machineId를 가진 새로운 stateMachine 객체를 생성한다.
    if (!ObjectUtils.nullSafeEquals(currentStateMachine!!.id, machineId)) {
      stateMachineService.releaseStateMachine(currentStateMachine!!.id)
      currentStateMachine!!.stopReactively().block()
      currentStateMachine = stateMachineService.acquireStateMachine(machineId)
      currentStateMachine!!.addStateListener(listener)
      currentStateMachine!!.startReactively().block()
      return currentStateMachine
    }

    // 먼저 서술된 케이스와 무관한 경우, 기존 객체를 그대로 return 시켜준다.
    return currentStateMachine
  }

  fun makeEventMessage(event: DocumentEvent): Mono<Message<DocumentEvent>> {
    return Mono.just(
      MessageBuilder.withPayload(event)
        .build()
    )
  }


  data class StateMachineStatement(
    val machineId: String,
    val messages: List<String>,
    val stateMachineContext: StateMachineContext<DocumentState, DocumentEvent>
  )

  data class EventResultStatement(
    val machineId: String, val event: DocumentEvent, val state: State
  )

  data class State(val prev: DocumentState, val changed: DocumentState)
}
