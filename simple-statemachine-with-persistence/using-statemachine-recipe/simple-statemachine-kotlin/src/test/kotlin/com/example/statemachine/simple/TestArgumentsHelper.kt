package com.example.statemachine.simple

import com.example.statemachine.simple.document.DocumentEvent


object TestArgumentsHelper {
  fun getDocumentEvents(exclusions: Set<DocumentEvent>): List<DocumentEvent> {
    return DocumentEvent.entries.toTypedArray()
      .filter { event -> !exclusions.contains(event) }
      .toList()
  }
}
