package com.example.statemachine.simple

import com.example.statemachine.simple.document.DocumentEvent
import com.example.statemachine.simple.document.DocumentState
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import java.util.*

object EnumConditionHelper {
    fun containsInDocumentEvents(): Matcher<DocumentEvent> {
        return Matchers.`in`(DocumentEvent.entries.toTypedArray())
    }

    fun containsInDocumentStates(): Matcher<DocumentState> {
        return Matchers.`in`(DocumentState.entries.toTypedArray())
    }

    fun containsInDocumentEventNames(): Matcher<String> {
        val documentEventNameList = Arrays.stream(DocumentEvent.entries.toTypedArray())
            .map { obj: DocumentEvent -> obj.name }
            .toList()
        return Matchers.`in`(documentEventNameList)
    }

    fun containsInDocumentStateNames(): Matcher<*> {
        val documentStateNameList = Arrays.stream(DocumentState.entries.toTypedArray())
            .map { obj: DocumentState -> obj.name }
            .toList()
        return Matchers.`in`(documentStateNameList)
    }
}
