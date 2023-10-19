package com.example.statemachine.simple.document.config.lintener

import com.example.statemachine.simple.document.DocumentEvent
import com.example.statemachine.simple.document.DocumentState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.statemachine.listener.StateMachineListenerAdapter
import org.springframework.statemachine.state.State

class StateMachineListenerAdapterImpl :
  StateMachineListenerAdapter<DocumentState, DocumentEvent>() {
  private val logger: Logger = LoggerFactory.getLogger(StateMachineListenerAdapterImpl::class.java)

  override fun stateChanged(
    from: State<DocumentState, DocumentEvent>?,
    to: State<DocumentState, DocumentEvent>?
  ) {
    logger.info("문서의 상태가 {} 에서 {} 로 변경되었습니다.", from?.id, to?.id)
  }
}
