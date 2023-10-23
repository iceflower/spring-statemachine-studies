package com.example.statemachine.simple;

import static java.util.function.Predicate.not;

import com.example.statemachine.simple.doument.DocumentEvent;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

public class TestArgumentsHelper {
  public static Stream<Arguments> getDocumentEvents(final Set<DocumentEvent> exclusions) {

    return Arrays.stream(DocumentEvent.values())
      .filter(not(exclusions::contains))
      .map(Arguments::of);
  }

}
