package com.example.statemachine.simple;

import static org.hamcrest.Matchers.in;

import com.example.statemachine.simple.doument.DocumentEvent;
import com.example.statemachine.simple.doument.DocumentState;
import java.util.Arrays;
import org.hamcrest.Matcher;

public class EnumConditionHelper {

  public static Matcher<DocumentEvent> containsInDocumentEvents() {

    return in(DocumentEvent.values());
  }



  public static Matcher<DocumentState> containsInDocumentStates() {

    return in(DocumentState.values());
  }

  public static Matcher<String> containsInDocumentEventNames() {

    final var documentEventNameList = Arrays.stream(DocumentEvent.values())
      .map(Enum::name)
      .toList();


    return in(documentEventNameList);
  }

  public static Matcher<?> containsInDocumentStateNames() {

    final var documentStateNameList = Arrays.stream(DocumentState.values())
      .map(Enum::name)
      .toList();

    return in(documentStateNameList);
  }

}
