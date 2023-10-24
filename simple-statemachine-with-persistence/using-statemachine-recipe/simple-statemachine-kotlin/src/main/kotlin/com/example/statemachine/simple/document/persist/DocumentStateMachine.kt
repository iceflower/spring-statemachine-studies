package com.example.statemachine.simple.document.persist

import com.example.statemachine.simple.document.DocumentEvent
import com.example.statemachine.simple.document.DocumentState
import com.example.statemachine.simple.document.DocumentVo
import com.example.statemachine.simple.document.config.DocumentIdChecker
import com.example.statemachine.simple.document.config.exception.PersistenceDataIsNotExistException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import java.sql.ResultSet

class DocumentStateMachine(
  private val jdbcTemplate: JdbcTemplate,
  private val handler: DocumentPersistStateMachineHandler
) {

  private var documentIdChecker: DocumentIdChecker = DocumentIdChecker(jdbcTemplate)

  init {
    handler.addPersistStateChangeListener(DocumentStateUpdater(jdbcTemplate))
    handler.addPersistStateChangeListener(DocumentStateChangedHistoryWriter(jdbcTemplate))
  }

  fun listDbEntries(): List<DocumentVo> {
    return jdbcTemplate.query(
      "SELECT id, state FROM documents", documentRowMapper()
    )
  }

  @Throws(PersistenceDataIsNotExistException::class)
  fun change(documentId: Int, event: DocumentEvent) {

    documentIdChecker.isExist(documentId)

    val d: DocumentVo = jdbcTemplate.queryForObject(
      "SELECT id, state FROM documents WHERE id = ?",
      documentRowMapper(), documentId
    )!!

    val message: Message<DocumentEvent> = MessageBuilder.withPayload<DocumentEvent>(event)
      .setHeader("document", documentId)
      .build()

    handler.handleEventWithStateReactively(message, d.state)
      .block()
  }

  companion object {
    private fun documentRowMapper(): RowMapper<DocumentVo> {
      return RowMapper<DocumentVo> { rs: ResultSet, _: Int ->
        DocumentVo(
          rs.getInt("id"),
          DocumentState.valueOf(rs.getString("state"))
        )
      }
    }
  }
}
