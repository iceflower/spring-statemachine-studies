package com.example.statemachine.simple.document.config.guard

import com.example.statemachine.simple.document.DocumentEvent
import com.example.statemachine.simple.document.DocumentState
import com.example.statemachine.simple.document.config.DocumentIdChecker
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.statemachine.StateContext
import org.springframework.statemachine.guard.Guard

class PersistGuard(
  jdbcTemplate: JdbcTemplate
) : Guard<DocumentState, DocumentEvent> {

  private val documentIdChecker: DocumentIdChecker = DocumentIdChecker(jdbcTemplate)

  override fun evaluate(context: StateContext<DocumentState, DocumentEvent>): Boolean {
    val messageHeaders = context.messageHeaders

    if (!messageHeaders.containsKey("document")) {
      return false
    }

    val documentId = messageHeaders.get("document", java.lang.Integer::class.java)!!.toInt()

    return documentIdChecker.isExist(documentId)
  }
}
