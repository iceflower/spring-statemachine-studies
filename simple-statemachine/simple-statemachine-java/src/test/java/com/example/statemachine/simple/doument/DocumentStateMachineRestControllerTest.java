package com.example.statemachine.simple.doument;


import static com.example.statemachine.simple.EnumConditionHelper.containsInDocumentEventNames;
import static com.example.statemachine.simple.EnumConditionHelper.containsInDocumentStateNames;
import static com.example.statemachine.simple.TestArgumentsHelper.getDocumentEvents;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.statemachine.simple.StateMachineUnitTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("DocumentStateMachineRestController 는")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DocumentStateMachineRestControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  StateMachine<DocumentState, DocumentEvent> stateMachine;

  ObjectMapper objectMapper = new ObjectMapper();
  StateMachineUnitTestHelper<DocumentState, DocumentEvent> stateMachineUnitTestHelper;

  @BeforeEach
  void initializeHelper() {
    // @DirtiesContext 어노테이션 때문에, 테스트 케이스 작동 직전에, 초기화를 진행해줘야 한다.
    stateMachineUnitTestHelper = new StateMachineUnitTestHelper<>(stateMachine);
  }

  @Nested
  @DisplayName("API Endpoint '/state-list' 는")
  class DescribeOf_getStateList {

    @Nested
    @DisplayName("get 요청이 주어지면")
    class ContextWith_get_request {

      @Test
      @DisplayName("state 목록을 돌려준다")
      void it_returns_state_list() throws Exception {

        final var givenData = objectMapper.writer()
          .writeValueAsString(DocumentState.values());

        mockMvc.perform(get("/state-list"))
          .andExpect(status().isOk())
          .andExpect(content().string(givenData))
          .andDo(print())
          .andReturn();
      }
    }
  }

  @Nested
  @DisplayName("API Endpoint '/event-list' 는")
  class DescribeOf_getEventList {

    @Nested
    @DisplayName("Get 요청이 주어지면")
    class ContextWith_get_request {

      @Test
      @DisplayName("event 목록을 돌려준다")
      void it_returns_state_list() throws Exception {

        final var givenData = objectMapper.writer()
          .writeValueAsString(DocumentEvent.values());

        mockMvc.perform(get("/event-list"))
          .andExpect(status().isOk())
          .andExpect(content().string(givenData))
          .andDo(print());
      }
    }
  }

  @Nested
  @DisplayName("API Endpoint '/current-state' 는")
  class DescribeOf_currentState {

    @Nested
    @DisplayName("Get 요청이 주어지면")
    class ContextWhen_get_request {

      @Test
      @DisplayName("statemachine의 현재 status를  돌려준다")
      void it_returns_state_list() throws Exception {

        final var state = stateMachine.getState().getId();
        mockMvc.perform(get("/current-state"))
          .andExpect(status().isOk())
          .andExpect(content().json("\"" + state + "\""))
          .andDo(print());
      }
    }
  }

  @Nested
  @DisplayName("API Endpoint '/send/{event}' 는")
  class DescribeOf_sendEvent {

    @Nested
    @DisplayName("상태머신의 상태가 'DRAFT' 일 때")
    class ContextWhen_state_is_draft {

      @Nested
      @DisplayName("PathVariable 'event' 가 'DOCUMENT_PUBLISHED_BY_USER' 인 Get 요청이 주어지면")
      class ContextWhen_get_request_with_DOCUMENT_PUBLISHED_BY_USER {

        @Test
        @DisplayName("statemachine의 상태가 'DRAFT' 에서 'UNDER_MEDIATION' 으로 변경되고, 변경된 결과를 돌려준다")
        void it_returns_state_list() throws Exception {

          final var event = DocumentEvent.DOCUMENT_PUBLISHED_BY_USER;

          final var prevState = DocumentState.DRAFT;
          final var expectedState = DocumentState.UNDER_MEDIATION;
          final var plan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState);

          mockMvc.perform(post("/send/{event}", event))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.event").exists())
            .andExpect(jsonPath("$.state").exists())
            .andExpect(jsonPath("$.state.prev").exists())
            .andExpect(jsonPath("$.state.changed").exists())
            .andExpect(jsonPath("$.event").isString())
            .andExpect(jsonPath("$.state").isMap())
            .andExpect(jsonPath("$.state.prev").isString())
            .andExpect(jsonPath("$.state.changed").isString())
            .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
            .andExpect(jsonPath("$.event").value(equalTo(event.name())))
            .andExpect(jsonPath("$.state").hasJsonPath())
            .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name())))
            .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.changed").value(not(equalTo(prevState.name()))))
            .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name())))
            .andDo(print());

          plan.test();
        }
      }

      @Nested
      @DisplayName("PathVariable 'event' 가 'DOCUMENT_PUBLISHED_BY_ADMINISTRATOR' 인 Get 요청이 주어지면")
      class ContextWhen_get_request_with_DOCUMENT_PUBLISHED_BY_ADMINISTRATOR {

        @Test
        @DisplayName("statemachine의 상태가 'DRAFT' 에서 'PUBLIC_DISCLOSURE' 으로 변경되고, 변경된 결과를 돌려준다")
        void it_returns_state_list() throws Exception {

          final var event = DocumentEvent.DOCUMENT_PUBLISHED_BY_ADMINISTRATOR;

          final var prevState = DocumentState.DRAFT;
          final var expectedState = DocumentState.PUBLIC_DISCLOSURE;
          final var plan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState);

          mockMvc.perform(post("/send/{event}", event))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.event").exists())
            .andExpect(jsonPath("$.state").exists())
            .andExpect(jsonPath("$.state.prev").exists())
            .andExpect(jsonPath("$.state.changed").exists())
            .andExpect(jsonPath("$.event").isString())
            .andExpect(jsonPath("$.state").isMap())
            .andExpect(jsonPath("$.state.prev").isString())
            .andExpect(jsonPath("$.state.changed").isString())
            .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
            .andExpect(jsonPath("$.event").value(equalTo(event.name())))
            .andExpect(jsonPath("$.state").hasJsonPath())
            .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name())))
            .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.changed").value(not(equalTo(prevState.name()))))
            .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name())))
            .andDo(print());

          plan.test();
        }
      }

      @Nested
      @DisplayName("PathVariable 'event' 가 'DOCUMENT_PUBLISHED_BY_USER' 혹은 'DOCUMENT_PUBLISHED_BY_ADMINISTRATOR' 가 아닌 다른 값인 Get 요청이 주어지면")
      class ContextWhen_get_request_with_order_events {

        private static Stream<Arguments> paramProvider() {
          return getDocumentEvents(Set.of(DocumentEvent.DOCUMENT_PUBLISHED_BY_USER,
            DocumentEvent.DOCUMENT_PUBLISHED_BY_ADMINISTRATOR));
        }

        @ParameterizedTest
        @MethodSource("paramProvider")
        @DisplayName("statemachine의 상태가 'DRAFT' 에서 다른 값으로 바뀌지 않고, 바뀌지 않은 상태임을 결과로 돌려준다")
        void it_returns_state_list(DocumentEvent event) throws Exception {

          final var expectedState = DocumentState.DRAFT;
          final var plan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState);

          mockMvc.perform(post("/send/{event}", event))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.event").exists())
            .andExpect(jsonPath("$.state").exists())
            .andExpect(jsonPath("$.state.prev").exists())
            .andExpect(jsonPath("$.state.changed").exists())
            .andExpect(jsonPath("$.event").isString())
            .andExpect(jsonPath("$.state").isMap())
            .andExpect(jsonPath("$.state.prev").isString())
            .andExpect(jsonPath("$.state.changed").isString())
            .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
            .andExpect(jsonPath("$.event").value(event.name()))
            .andExpect(jsonPath("$.state").hasJsonPath())
            .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.prev").value(equalTo(expectedState.name())))
            .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name())))
            .andDo(print());

          plan.test();
        }
      }
    }

    @Nested
    @DisplayName("상태머신의 상태가 'UNDER_MEDIATION' 일 때")
    class ContextWhen_state_is_UNDER_MEDIATION {

      @BeforeEach
      void prepare() {
        // 테스트를 위해, 상태기계의 status를 `UNDER_MEDIATION` 로 바꾸기 위한 작업.
        stateMachineUnitTestHelper.sendEvent(DocumentEvent.DOCUMENT_PUBLISHED_BY_USER);
      }

      @Nested
      @DisplayName("PathVariable 'event' 가 'APPROVED' 인 Get 요청이 주어지면")
      class ContextWhen_get_request_with_APPROVED {

        @Test
        @DisplayName("statemachine의 상태가 'UNDER_MEDIATION' 에서 'WAITING_FOR_PUBLIC_DISCLOSURE' 으로 변경되고, 변경된 결과를 돌려준다")
        void it_returns_state_list() throws Exception {

          final var event = DocumentEvent.APPROVED;

          final var prevState = DocumentState.UNDER_MEDIATION;
          final var expectedState = DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE;
          final var plan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState);

          mockMvc.perform(post("/send/{event}", event))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.event").exists())
            .andExpect(jsonPath("$.state").exists())
            .andExpect(jsonPath("$.state.prev").exists())
            .andExpect(jsonPath("$.state.changed").exists())
            .andExpect(jsonPath("$.event").isString())
            .andExpect(jsonPath("$.state").isMap())
            .andExpect(jsonPath("$.state.prev").isString())
            .andExpect(jsonPath("$.state.changed").isString())
            .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
            .andExpect(jsonPath("$.event").value(equalTo(event.name())))
            .andExpect(jsonPath("$.state").hasJsonPath())
            .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name())))
            .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.changed").value(not(equalTo(prevState.name()))))
            .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name())))
            .andDo(print());

          plan.test();
        }
      }

      @Nested
      @DisplayName("PathVariable 'event' 가 'NEEDS_TO_BE_ADJUSTED_OR_REJECTED' 인 Get 요청이 주어지면")
      class ContextWhen_get_request_with_NEEDS_TO_BE_ADJUSTED_OR_REJECTED {

        @Test
        @DisplayName("statemachine의 상태가 'UNDER_MEDIATION' 에서 'DRAFT' 으로 변경되고, 변경된 결과를 돌려준다")
        void it_returns_state_list() throws Exception {

          final var event = DocumentEvent.NEEDS_TO_BE_ADJUSTED_OR_REJECTED;

          final var prevState = DocumentState.UNDER_MEDIATION;
          final var expectedState = DocumentState.DRAFT;
          final var plan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState);

          mockMvc.perform(post("/send/{event}", event))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.event").exists())
            .andExpect(jsonPath("$.state").exists())
            .andExpect(jsonPath("$.state.prev").exists())
            .andExpect(jsonPath("$.state.changed").exists())
            .andExpect(jsonPath("$.event").isString())
            .andExpect(jsonPath("$.state").isMap())
            .andExpect(jsonPath("$.state.prev").isString())
            .andExpect(jsonPath("$.state.changed").isString())
            .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
            .andExpect(jsonPath("$.event").value(equalTo(event.name())))
            .andExpect(jsonPath("$.state").hasJsonPath())
            .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name())))
            .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.changed").value(not(equalTo(prevState.name()))))
            .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name())))
            .andDo(print());

          plan.test();
        }
      }

      @Nested
      @DisplayName("PathVariable 'event' 가 'APPROVED' 혹은 'NEEDS_TO_BE_ADJUSTED_OR_REJECTED' 가 아닌 다른 값인 Get 요청이 주어지면")
      class ContextWhen_get_request_with_order_events {

        private static Stream<Arguments> paramProvider() {
          return getDocumentEvents(
            Set.of(DocumentEvent.APPROVED, DocumentEvent.NEEDS_TO_BE_ADJUSTED_OR_REJECTED));
        }

        @ParameterizedTest
        @MethodSource("paramProvider")
        @DisplayName("statemachine의 상태가 'UNDER_MEDIATION' 에서 다른 값으로 바뀌지 않고, 바뀌지 않은 상태임을 결과로 돌려준다")
        void it_returns_state_list(DocumentEvent event) throws Exception {

          final var expectedState = DocumentState.UNDER_MEDIATION;
          final var plan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState);

          mockMvc.perform(post("/send/{event}", event))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.event").exists())
            .andExpect(jsonPath("$.state").exists())
            .andExpect(jsonPath("$.state.prev").exists())
            .andExpect(jsonPath("$.state.changed").exists())
            .andExpect(jsonPath("$.event").isString())
            .andExpect(jsonPath("$.state").isMap())
            .andExpect(jsonPath("$.state.prev").isString())
            .andExpect(jsonPath("$.state.changed").isString())
            .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
            .andExpect(jsonPath("$.event").value(event.name()))
            .andExpect(jsonPath("$.state").hasJsonPath())
            .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.prev").value(equalTo(expectedState.name())))
            .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name())))
            .andDo(print());

          plan.test();
        }
      }
    }

    @Nested
    @DisplayName("상태머신의 상태가 'WAITING_FOR_PUBLIC_DISCLOSURE' 일 때")
    class ContextWhen_state_is_WAITING_FOR_PUBLIC_DISCLOSURE {

      @BeforeEach
      void prepare() {
        // 테스트를 위해, 상태기계의 status를 `WAITING_FOR_PUBLIC_DISCLOSURE` 로 바꾸기 위한 작업.
        stateMachineUnitTestHelper.sendEvent(DocumentEvent.DOCUMENT_PUBLISHED_BY_USER);
        stateMachineUnitTestHelper.sendEvent(DocumentEvent.APPROVED);
      }

      @Nested
      @DisplayName("PathVariable 'event' 가 'APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR' 인 Get 요청이 주어지면")
      class ContextWhen_get_request_with_APPROVED {

        @Test
        @DisplayName("statemachine의 상태가 'WAITING_FOR_PUBLIC_DISCLOSURE' 에서 'PUBLIC_DISCLOSURE' 으로 변경되고, 변경된 결과를 돌려준다")
        void it_returns_state_list() throws Exception {

          final var event = DocumentEvent.APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR;

          final var prevState = DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE;
          final var expectedState = DocumentState.PUBLIC_DISCLOSURE;
          final var plan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState);

          mockMvc.perform(post("/send/{event}", event))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.event").exists())
            .andExpect(jsonPath("$.state").exists())
            .andExpect(jsonPath("$.state.prev").exists())
            .andExpect(jsonPath("$.state.changed").exists())
            .andExpect(jsonPath("$.event").isString())
            .andExpect(jsonPath("$.state").isMap())
            .andExpect(jsonPath("$.state.prev").isString())
            .andExpect(jsonPath("$.state.changed").isString())
            .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
            .andExpect(jsonPath("$.event").value(equalTo(event.name())))
            .andExpect(jsonPath("$.state").hasJsonPath())
            .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name())))
            .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.changed").value(not(equalTo(prevState.name()))))
            .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name())))
            .andDo(print());

          plan.test();
        }
      }


      @Nested
      @DisplayName("PathVariable 'event' 가 'APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR' 가 아닌 다른 값인 Get 요청이 주어지면")
      class ContextWhen_get_request_with_order_events {

        private static Stream<Arguments> paramProvider() {
          return getDocumentEvents(
            Set.of(DocumentEvent.APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR));
        }

        @ParameterizedTest
        @MethodSource("paramProvider")
        @DisplayName("statemachine의 상태가 'WAITING_FOR_PUBLIC_DISCLOSURE' 에서 다른 값으로 바뀌지 않고, 바뀌지 않은 상태임을 결과로 돌려준다")
        void it_returns_state_list(DocumentEvent event) throws Exception {

          final var expectedState = DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE;
          final var plan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState);

          mockMvc.perform(post("/send/{event}", event))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.event").exists())
            .andExpect(jsonPath("$.state").exists())
            .andExpect(jsonPath("$.state.prev").exists())
            .andExpect(jsonPath("$.state.changed").exists())
            .andExpect(jsonPath("$.event").isString())
            .andExpect(jsonPath("$.state").isMap())
            .andExpect(jsonPath("$.state.prev").isString())
            .andExpect(jsonPath("$.state.changed").isString())
            .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
            .andExpect(jsonPath("$.event").value(event.name()))
            .andExpect(jsonPath("$.state").hasJsonPath())
            .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.prev").value(equalTo(expectedState.name())))
            .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name())))
            .andDo(print());

          plan.test();
        }
      }
    }


    @Nested
    @DisplayName("상태머신의 상태가 'PUBLIC_DISCLOSURE' 일 때")
    class ContextWhen_state_is_PUBLIC_DISCLOSURE {

      @BeforeEach
      void prepare() {
        // 테스트를 위해, 상태기계의 status를 `PUBLIC_DISCLOSURE` 로 바꾸기 위한 작업.
        stateMachineUnitTestHelper.sendEvent(DocumentEvent.DOCUMENT_PUBLISHED_BY_ADMINISTRATOR);
      }

      @Nested
      @DisplayName("PathVariable 'event' 가 'EXPIRED' 인 Get 요청이 주어지면")
      class ContextWhen_get_request_with_EXPIRED {

        @Test
        @DisplayName("statemachine의 상태가 'PUBLIC_DISCLOSURE' 에서 'DRAFT' 으로 변경되고, 변경된 결과를 돌려준다")
        void it_returns_state_list() throws Exception {

          final var event = DocumentEvent.EXPIRED;

          final var prevState = DocumentState.PUBLIC_DISCLOSURE;
          final var expectedState = DocumentState.DRAFT;
          final var plan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState);

          mockMvc.perform(post("/send/{event}", event))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.event").exists())
            .andExpect(jsonPath("$.state").exists())
            .andExpect(jsonPath("$.state.prev").exists())
            .andExpect(jsonPath("$.state.changed").exists())
            .andExpect(jsonPath("$.event").isString())
            .andExpect(jsonPath("$.state").isMap())
            .andExpect(jsonPath("$.state.prev").isString())
            .andExpect(jsonPath("$.state.changed").isString())
            .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
            .andExpect(jsonPath("$.event").value(equalTo(event.name())))
            .andExpect(jsonPath("$.state").hasJsonPath())
            .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name())))
            .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.changed").value(not(equalTo(prevState.name()))))
            .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name())))
            .andDo(print());

          plan.test();
        }
      }


      @Nested
      @DisplayName("PathVariable 'event' 가 'EXPIRED' 가 아닌 다른 값인 Get 요청이 주어지면")
      class ContextWhen_get_request_with_order_events {

        private static Stream<Arguments> paramProvider() {
          return getDocumentEvents(Set.of(DocumentEvent.EXPIRED));
        }

        @ParameterizedTest
        @MethodSource("paramProvider")
        @DisplayName("statemachine의 상태가 'WAITING_FOR_PUBLIC_DISCLOSURE' 에서 다른 값으로 바뀌지 않고, 바뀌지 않은 상태임을 결과로 돌려준다")
        void it_returns_state_list(DocumentEvent event) throws Exception {

          final var expectedState = DocumentState.PUBLIC_DISCLOSURE;
          final var plan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState);

          mockMvc.perform(post("/send/{event}", event))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.event").exists())
            .andExpect(jsonPath("$.state").exists())
            .andExpect(jsonPath("$.state.prev").exists())
            .andExpect(jsonPath("$.state.changed").exists())
            .andExpect(jsonPath("$.event").isString())
            .andExpect(jsonPath("$.state").isMap())
            .andExpect(jsonPath("$.state.prev").isString())
            .andExpect(jsonPath("$.state.changed").isString())
            .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
            .andExpect(jsonPath("$.event").value(event.name()))
            .andExpect(jsonPath("$.state").hasJsonPath())
            .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.prev").value(equalTo(expectedState.name())))
            .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
            .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name())))
            .andDo(print());

          plan.test();
        }
      }
    }
  }
}
