package com.example.statemachine.simple.doument.config.listener;

import com.example.statemachine.simple.doument.DocumentEvent;
import com.example.statemachine.simple.doument.DocumentState;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

public class StateMachineListenerAdapterImpl
  extends StateMachineListenerAdapter<DocumentState, DocumentEvent> {

  private static final Logger logger
    = LoggerFactory.getLogger(StateMachineListenerAdapterImpl.class);

  private static String getStateString(
    final State<DocumentState, DocumentEvent> state) {
    final var optional = Optional.ofNullable(state);

    return optional.isEmpty()
      ? "null" : optional.get().getId().toString();
  }

  @Override
  public void stateChanged(
    State<DocumentState, DocumentEvent> from, State<DocumentState, DocumentEvent> to) {

    final var fromStateString = getStateString(from);
    final var toStateString = getStateString(to);

    logger.info("문서의 상태가 {} 에서 {} 로 변경되었습니다.", fromStateString, toStateString);
  }
}
