package com.example.statemachine.simple.document

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.statemachine.StateMachine
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class StateMachineRunner(val stateMachine: StateMachine<DocumentState, DocumentEvent>) :
  CommandLineRunner {

  private val logger: Logger = LoggerFactory.getLogger(StateMachineRunner::class.java)

  @Throws(Exception::class)
  override fun run(vararg args: String?) {
    logger.info("상태 머신 시작")

    // 코루틴 문법을 사용시, `.blockLast()` 같은 블로킹 메소드를 사용하지 않아도 된다.
    with(stateMachine) {
      sendEvent(eventMessage(DocumentEvent.DOCUMENT_PUBLISHED_BY_USER)).blockLast()
      sendEvent(eventMessage(DocumentEvent.APPROVED)).blockLast()
      sendEvent(eventMessage(DocumentEvent.APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR)).blockLast()
      sendEvent(eventMessage(DocumentEvent.EXPIRED)).blockLast()
    }

    logger.info("상태 머신 종료")

  }

  private fun eventMessage(event: DocumentEvent): Mono<Message<DocumentEvent>> {

    return Mono.just(
      MessageBuilder.withPayload(event)
        .build()
    )
  }
}
