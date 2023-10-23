package com.example.statemachine.simple.doument.config;


import static com.example.statemachine.simple.TestArgumentsHelper.getDocumentEvents;

import com.example.statemachine.simple.StateMachineUnitTestHelper;
import com.example.statemachine.simple.doument.DocumentEvent;
import com.example.statemachine.simple.doument.DocumentState;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.annotation.DirtiesContext;

@DisplayName("DocumentStateMachine 은")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DocumentStateMachineTest {

  @Autowired
  StateMachine<DocumentState, DocumentEvent> stateMachine;

  StateMachineUnitTestHelper<DocumentState, DocumentEvent> stateMachineUnitTestHelper;

  @BeforeEach
  void initializeHelper() {
    // @DirtiesContext 어노테이션 때문에, 테스트 케이스 작동 직전에, 초기화를 진행해줘야 한다.
    stateMachineUnitTestHelper = new StateMachineUnitTestHelper<>(stateMachine);
  }


  @Nested
  class DescribeOf_DocumentStateMachine {

    @Nested
    @DisplayName("기존 상태가 'DRAFT' 일 때")
    class ContextWhen_state_is_DRAFT {

      @Nested
      @DisplayName("'DOCUMENT_PUBLISHED_BY_USER' 이벤트가 주어지면")
      class ContextWith_given_DOCUMENT_PUBLISHED_BY_USER_event {

        @Test
        @DisplayName("statemachine의 상태가 'DRAFT' 에서 'UNDER_MEDIATION' 으로 변경된다")
        void its_state_changed() throws Exception {

          final var event = DocumentEvent.DOCUMENT_PUBLISHED_BY_USER;

          final var prevState = DocumentState.DRAFT;
          final var expectedState = DocumentState.UNDER_MEDIATION;

          final var plan
            = stateMachineUnitTestHelper.getStateMachineTestPlan(event, prevState, expectedState);

          plan.test();
        }
      }

      @Nested
      @DisplayName("'DOCUMENT_PUBLISHED_BY_ADMINISTRATOR' 이벤트가 주어지면")
      class ContextWith_given_DOCUMENT_PUBLISHED_BY_ADMINISTRATOR_event {

        @Test
        @DisplayName("statemachine의 상태가 'DRAFT' 에서 'PUBLIC_DISCLOSURE' 으로 변경된다")
        void its_state_changed() throws Exception {

          final var event = DocumentEvent.DOCUMENT_PUBLISHED_BY_ADMINISTRATOR;

          final var prevState = DocumentState.DRAFT;
          final var expectedState = DocumentState.PUBLIC_DISCLOSURE;

          final var plan
            = stateMachineUnitTestHelper.getStateMachineTestPlan(event, prevState, expectedState);

          plan.test();
        }
      }

      @Nested
      @DisplayName("'DOCUMENT_PUBLISHED_BY_USER' 혹은 'DOCUMENT_PUBLISHED_BY_ADMINISTRATOR' 가 아닌 다른 이벤트가 주어지면")
      class ContextWith_given_other_events {

        private static Stream<Arguments> paramProvider() {
          return getDocumentEvents(Set.of(DocumentEvent.DOCUMENT_PUBLISHED_BY_USER, DocumentEvent.DOCUMENT_PUBLISHED_BY_ADMINISTRATOR));
        }

        @ParameterizedTest
        @MethodSource("paramProvider")
        @DisplayName("statemachine의 상태가 'DRAFT' 에서 다른 값으로 바뀌지 않고, 기존 상태인 'DRAFT' 가 그대로 유지된다")
        void its_state_not_changed(DocumentEvent event) throws Exception {

          final var prevState = DocumentState.DRAFT;
          final var expectedState = DocumentState.DRAFT;

          final var plan
            = stateMachineUnitTestHelper.getStateMachineTestPlan(event, prevState, expectedState);

          plan.test();
        }
      }
    }

    @Nested
    @DisplayName("기존 상태가 'UNDER_MEDIATION' 일 때")
    class ContextWhen_state_is_UNDER_MEDIATION {

      @BeforeEach
      void prepare() {
        // 테스트를 위해, 상태기계의 status를 `DOCUMENT_PUBLISHED_BY_USER` 로 바꾸기 위한 작업.
        stateMachineUnitTestHelper.sendEvent(DocumentEvent.DOCUMENT_PUBLISHED_BY_USER);
      }

      @Nested
      @DisplayName("'APPROVED' 인 이벤트가 주어지면")
      class ContextWith_given_APPROVED_event {

        @Test
        @DisplayName("statemachine의 상태가 'UNDER_MEDIATION' 에서 'WAITING_FOR_PUBLIC_DISCLOSURE' 으로 변경된다")
        void its_state_changed() throws Exception {

          final var event = DocumentEvent.APPROVED;

          final var prevState = DocumentState.UNDER_MEDIATION;
          final var expectedState = DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE;

          final var plan
            = stateMachineUnitTestHelper.getStateMachineTestPlan(event, prevState, expectedState);

          plan.test();
        }
      }

      @Nested
      @DisplayName("'NEEDS_TO_BE_ADJUSTED_OR_REJECTED' 이벤트가 주어지면")
      class ContextWith_given_NEEDS_TO_BE_ADJUSTED_OR_REJECTED_event {

        @Test
        @DisplayName("statemachine의 상태가 'UNDER_MEDIATION' 에서 'DRAFT' 으로 변경된다")
        void its_state_changed() throws Exception {

          final var event = DocumentEvent.NEEDS_TO_BE_ADJUSTED_OR_REJECTED;

          final var prevState = DocumentState.UNDER_MEDIATION;
          final var expectedState = DocumentState.DRAFT;

          final var plan
            = stateMachineUnitTestHelper.getStateMachineTestPlan(event, prevState, expectedState);

          plan.test();
        }
      }

      @Nested
      @DisplayName("'APPROVED' 혹은 'NEEDS_TO_BE_ADJUSTED_OR_REJECTED' 가 아닌 다른 이벤트가 주어지면")
      class ContextWith_given_other_events {

        private static Stream<Arguments> paramProvider() {
          return getDocumentEvents(Set.of(DocumentEvent.APPROVED, DocumentEvent.NEEDS_TO_BE_ADJUSTED_OR_REJECTED));
        }

        @ParameterizedTest
        @MethodSource("paramProvider")
        @DisplayName("statemachine의 상태가 'UNDER_MEDIATION' 에서 다른 값으로 바뀌지 않고, 기존 상태인 'UNDER_MEDIATION' 가 그대로 유지된다")
        void its_state_not_changed(DocumentEvent event) throws Exception {

          final var prevState = DocumentState.UNDER_MEDIATION;
          final var expectedState = DocumentState.UNDER_MEDIATION;

          final var plan
            = stateMachineUnitTestHelper.getStateMachineTestPlan(event, prevState, expectedState);

          plan.test();
        }
      }
    }

    @Nested
    @DisplayName("기존 상태가 'WAITING_FOR_PUBLIC_DISCLOSURE' 일 때")
    class ContextWhen_state_is_WAITING_FOR_PUBLIC_DISCLOSURE {

      @BeforeEach
      void prepare() {
        // 테스트를 위해, 상태기계의 status를 `WAITING_FOR_PUBLIC_DISCLOSURE` 로 바꾸기 위한 작업.
        stateMachineUnitTestHelper.sendEvent(DocumentEvent.DOCUMENT_PUBLISHED_BY_USER);
        stateMachineUnitTestHelper.sendEvent(DocumentEvent.APPROVED);
      }

      @Nested

      @DisplayName("'APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR' 이벤트가 주어지면")
      class ContextWith_given_APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR_event {

        @Test
        @DisplayName("statemachine의 상태가 'WAITING_FOR_PUBLIC_DISCLOSURE' 에서 'PUBLIC_DISCLOSURE' 으로 변경되고, 변경된 결과를 돌려준다")
        void its_state_changed() throws Exception {

          final var event = DocumentEvent.APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR;

          final var prevState = DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE;
          final var expectedState = DocumentState.PUBLIC_DISCLOSURE;
          final var plan = stateMachineUnitTestHelper.getStateMachineTestPlan(event, prevState,
            expectedState);

          plan.test();
        }
      }

      @Nested
      @DisplayName("'APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR' 가 아닌 다른 이벤트가 주어지면")
      class ContextWhen_get_request_with_order_events {

        private static Stream<Arguments> paramProvider() {
          return getDocumentEvents(Set.of(DocumentEvent.APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR));
        }

        @ParameterizedTest
        @MethodSource("paramProvider")
        @DisplayName("statemachine의 상태가 'UNDER_MEDIATION' 에서 다른 값으로 바뀌지 않고, 기존 상태인 'UNDER_MEDIATION' 가 그대로 유지된다")
        void its_state_not_changed(DocumentEvent event) throws Exception {

          final var prevState = DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE;
          final var expectedState = DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE;

          final var plan = stateMachineUnitTestHelper.getStateMachineTestPlan(event, prevState,
            expectedState);

          plan.test();
        }
      }
    }

    @Nested
    @DisplayName("기존 상태가 'PUBLIC_DISCLOSURE' 일 때")
    class ContextWhen_state_is_PUBLIC_DISCLOSURE {

      @BeforeEach
      void prepare() {
        // 테스트를 위해, 상태기계의 status를 `PUBLIC_DISCLOSURE` 로 바꾸기 위한 작업.
        stateMachineUnitTestHelper.sendEvent(DocumentEvent.DOCUMENT_PUBLISHED_BY_ADMINISTRATOR);
      }

      @Nested

      @DisplayName("'EXPIRED' 이벤트가 주어지면")
      class ContextWith_given_EXPIRED_event {

        @Test
        @DisplayName("statemachine의 상태가 'PUBLIC_DISCLOSURE' 에서 'DRAFT' 으로 변경되고, 변경된 결과를 돌려준다")
        void its_state_changed() throws Exception {

          final var event = DocumentEvent.EXPIRED;

          final var prevState = DocumentState.PUBLIC_DISCLOSURE;
          final var expectedState = DocumentState.DRAFT;
          final var plan = stateMachineUnitTestHelper.getStateMachineTestPlan(event, prevState,
            expectedState);

          plan.test();
        }
      }

      @Nested
      @DisplayName("'EXPIRED' 가 아닌 다른 이벤트가 주어지면")
      class ContextWith_other_events {

        private static Stream<Arguments> paramProvider() {
          return getDocumentEvents(Set.of(DocumentEvent.EXPIRED));
        }

        @ParameterizedTest
        @MethodSource("paramProvider")
        @DisplayName("statemachine의 상태가 'EXPIRED' 에서 다른 값으로 바뀌지 않고, 기존 상태인 'EXPIRED' 가 그대로 유지된다")
        void its_state_not_changed(DocumentEvent event) throws Exception {

          final var prevState = DocumentState.PUBLIC_DISCLOSURE;
          final var expectedState = DocumentState.PUBLIC_DISCLOSURE;

          final var plan = stateMachineUnitTestHelper.getStateMachineTestPlan(event, prevState,
            expectedState);

          plan.test();
        }
      }
    }
  }
}
