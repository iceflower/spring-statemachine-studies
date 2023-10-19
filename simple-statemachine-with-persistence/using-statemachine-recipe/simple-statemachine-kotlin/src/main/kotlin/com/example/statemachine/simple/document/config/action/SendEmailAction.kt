package com.example.statemachine.simple.document.config.action

import com.example.statemachine.simple.document.DocumentEvent
import com.example.statemachine.simple.document.DocumentState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.statemachine.StateContext
import org.springframework.statemachine.action.Action

class SendEmailAction : Action<DocumentState, DocumentEvent> {

  private val logger: Logger = LoggerFactory.getLogger(SendEmailAction::class.java)

  override fun execute(p0: StateContext<DocumentState, DocumentEvent>) {
    logger.info("이메일 전송 : [문서가 대외 공개되었습니다.]")
  }
}
