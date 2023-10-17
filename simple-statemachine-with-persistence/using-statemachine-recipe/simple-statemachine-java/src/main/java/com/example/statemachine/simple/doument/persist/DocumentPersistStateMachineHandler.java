package com.example.statemachine.simple.doument.persist;

import com.example.statemachine.simple.doument.DocumentEvent;
import com.example.statemachine.simple.doument.DocumentState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.recipes.persist.GenericPersistStateMachineHandler;

public class DocumentPersistStateMachineHandler
  extends GenericPersistStateMachineHandler<DocumentState, DocumentEvent> {

  public DocumentPersistStateMachineHandler(
    final StateMachine<DocumentState, DocumentEvent> stateMachine) {
    super(stateMachine);
  }

}
