package com.example.statemachine.simple.document.persist

import com.example.statemachine.simple.document.DocumentEvent
import com.example.statemachine.simple.document.DocumentState
import org.springframework.statemachine.StateMachine
import org.springframework.statemachine.recipes.persist.GenericPersistStateMachineHandler

class DocumentPersistStateMachineHandler(
  stateMachine: StateMachine<DocumentState?, DocumentEvent?>?
) : GenericPersistStateMachineHandler<DocumentState?, DocumentEvent?>(stateMachine)
