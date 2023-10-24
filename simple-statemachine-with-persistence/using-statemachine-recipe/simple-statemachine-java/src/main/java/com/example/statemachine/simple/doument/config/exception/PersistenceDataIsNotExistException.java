package com.example.statemachine.simple.doument.config.exception;

public class PersistenceDataIsNotExistException extends RuntimeException {

  public PersistenceDataIsNotExistException(String message) {
    super(message);
  }
}
