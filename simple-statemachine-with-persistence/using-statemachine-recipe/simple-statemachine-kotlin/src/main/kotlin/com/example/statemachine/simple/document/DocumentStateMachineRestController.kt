package com.example.statemachine.simple.document

import com.example.statemachine.simple.document.config.DocumentIdChecker
import com.example.statemachine.simple.document.config.exception.PersistenceDataIsNotExistException
import com.example.statemachine.simple.document.persist.DocumentStateMachine
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.*

@RestController
class DocumentStateMachineRestController(
  private val jdbcTemplate: JdbcTemplate,
  private val documentStateMachine: DocumentStateMachine
) {
  private val documentIdChecker: DocumentIdChecker = DocumentIdChecker(jdbcTemplate)


  @get:GetMapping("/state-list")
  val stateList: Array<DocumentState>
    get() = DocumentState.entries.toTypedArray()

  @get:GetMapping("/event-list")
  val eventList: Array<DocumentEvent>
    get() = DocumentEvent.entries.toTypedArray()

  @GetMapping("/document-list")
  fun documentList(): List<DocumentVo> {
    return documentStateMachine.listDbEntries()
  }

  @GetMapping("/{documentId}/current-state")
  fun currentState(@PathVariable("documentId") documentId: Int): DocumentState {
    return getStateFromDatabase(documentId)
  }

  @PostMapping("/{documentId}/send/{event}")
  fun sendEvent(
    @PathVariable("documentId") documentId: Int,
    @PathVariable("event") event: DocumentEvent
  ): EventResultStatement {
    val prevState = getStateFromDatabase(documentId)

    documentStateMachine.change(documentId, event)

    val changedState = getStateFromDatabase(documentId)

    return EventResultStatement(documentId, event, State(prevState, changedState))
  }


  @ExceptionHandler(PersistenceDataIsNotExistException::class)
  fun handlePersistenceDataIsNotExistException(
    ex: PersistenceDataIsNotExistException
  ): ResponseEntity<Map<String, String>> {
    return ResponseEntity.unprocessableEntity()
      .body(mapOf("message" to ex.message!!))
  }


  @Throws(PersistenceDataIsNotExistException::class)
  private fun getStateFromDatabase(documentId: Int): DocumentState {
    documentIdChecker.isExist(documentId)
    return jdbcTemplate.queryForObject(
      "SELECT state FROM documents WHERE id = ?",
      DocumentState::class.java, documentId
    )
  }

  data class State(val prev: DocumentState, val changed: DocumentState)

  data class EventResultStatement(val documentId: Int, val event: DocumentEvent, val state: State)
}
