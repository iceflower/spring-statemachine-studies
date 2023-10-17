package com.example.statemachine.simple.doument.config.action;

import com.example.statemachine.simple.doument.DocumentEvent;
import com.example.statemachine.simple.doument.DocumentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

public class SendEmailAction implements Action<DocumentState, DocumentEvent> {

  private static final Logger logger
    = LoggerFactory.getLogger(SendEmailAction.class);

  @Override
  public void execute(StateContext<DocumentState, DocumentEvent> context) {
    logger.info("이메일 전송 : [문서가 대외 공개되었습니다.]");
  }
}
