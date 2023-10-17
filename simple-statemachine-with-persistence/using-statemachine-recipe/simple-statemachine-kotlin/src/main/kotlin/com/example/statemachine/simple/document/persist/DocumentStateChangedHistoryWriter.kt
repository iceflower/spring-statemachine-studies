package com.example.statemachine.simple.document.persist

import com.example.statemachine.simple.document.DocumentEvent
import com.example.statemachine.simple.document.DocumentState
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.messaging.Message
import org.springframework.statemachine.StateMachine
import org.springframework.statemachine.recipes.persist.AbstractPersistStateMachineHandler
import org.springframework.statemachine.state.State
import org.springframework.statemachine.transition.Transition

class DocumentStateChangedHistoryWriter(private val jdbcTemplate: JdbcTemplate) :
  AbstractPersistStateMachineHandler.GenericPersistStateChangeListener<DocumentState?, DocumentEvent?> {


  override fun onPersist(
    state: State<DocumentState?, DocumentEvent?>?,
    message: Message<DocumentEvent?>?,
    transition: Transition<DocumentState?, DocumentEvent?>?,
    stateMachine: StateMachine<DocumentState?, DocumentEvent?>?
  ) {
    if (message!!.headers.containsKey("document")) {
      val documentId = message.headers.get("document", Integer::class.java)
      val event = transition!!.trigger.event!!
      val prevState = transition.source.id!!
      val changedState = transition.target.id!!

      jdbcTemplate.update(
        "INSERT INTO documents_state_history(document_id, event, prev_state, changed_state) VALUES (?, ?, ?, ?)",
        documentId, event.toString(), prevState.toString(), changedState.toString()
      )
    }
  }
}
