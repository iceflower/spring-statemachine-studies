package com.example.statemachine.simple.document

import com.example.statemachine.simple.EnumConditionHelper.containsInDocumentEventNames
import com.example.statemachine.simple.EnumConditionHelper.containsInDocumentStateNames
import com.example.statemachine.simple.StateMachineUnitTestHelper
import com.example.statemachine.simple.TestArgumentsHelper.getDocumentEvents
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import org.hamcrest.Matchers.equalTo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.statemachine.StateMachine
import org.springframework.statemachine.test.StateMachineTestPlan
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
internal class DocumentStateMachineRestControllerDescribeSpec :
  DescribeSpec({ isolationMode = IsolationMode.InstancePerLeaf }) {
  override fun extensions() = listOf(SpringExtension)

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Autowired
  private lateinit var stateMachine: StateMachine<DocumentState, DocumentEvent>

  private var objectMapper = ObjectMapper()

  init {
    this.describe("DocumentStateMachineRestController 클래스의") {

      this.describe("API Endpoint '/state-list' 는") {
        context("Get 요청이 주어지면") {
          it("state 목록을 돌려준다") {
            val givenData = objectMapper.writer()
              .writeValueAsString(DocumentState.entries.toTypedArray())
            mockMvc.perform(MockMvcRequestBuilders.get("/state-list"))
              .andExpect(status().isOk())
              .andExpect(content().string(givenData))
              .andDo(MockMvcResultHandlers.print())
              .andReturn()
          }
        }
      }

      this.describe("API Endpoint '/event-list' 는") {
        context("Get 요청이 주어지면") {
          it("event 목록을 돌려준다") {
            val givenData = objectMapper.writer()
              .writeValueAsString(DocumentEvent.entries.toTypedArray())
            mockMvc.perform(MockMvcRequestBuilders.get("/event-list"))
              .andExpect(status().isOk())
              .andExpect(content().string(givenData))
              .andDo(MockMvcResultHandlers.print())
          }
        }
      }
    }

    this.describe("API Endpoint '/{documentId}/current-state' 는") {

      context("DB에 존재하지 않는 PathVariable 'documentId' 로 Get 요청이 주어지면") {
        it("http code 422를 돌려준다") {
          mockMvc.perform(MockMvcRequestBuilders.get("/{documentId}/current-state", -99))
            .andExpect(status().isUnprocessableEntity())
            .andDo(MockMvcResultHandlers.print())
        }
      }

      context("DB에 존재하는 PathVariable 'documentId' 로 Get 요청이 주어지면") {
        it("현재 state 를 돌려준다") {
          val state: DocumentState = stateMachine.state.id
          mockMvc.perform(MockMvcRequestBuilders.get("/{documentId}/current-state", 1))
            .andExpect(status().isOk())
            .andExpect(content().json("\"" + state + "\""))
            .andDo(MockMvcResultHandlers.print())
        }
      }
    }


    this.describe("API Endpoint '/{documentId}/send/{event}' 는") {
      val stateMachineUnitTestHelper: StateMachineUnitTestHelper<DocumentState, DocumentEvent> =
        StateMachineUnitTestHelper(stateMachine)

      context("DB에 존재하지 않는 PathVariable 'documentId' 가 주어지면") {
        val param = getDocumentEvents(setOf())
        param.forEachIndexed { index, event ->
          it("statemachine의 상태는 초기 상태인 'DRAFT' 에서 바뀌지 않고, HTTP code 는 422로 돌려준다 (${index + 1}) - $event") {
            mockMvc.perform(MockMvcRequestBuilders.post("/{documentId}/send/{event}", -99, event))
              .andExpect(status().isUnprocessableEntity())
              .andDo(MockMvcResultHandlers.print())
          }
        }
      }

      context("상태머신의 상태가 'DRAFT' 일 때") {
        context("PathVariable 'event' 가 'DOCUMENT_PUBLISHED_BY_USER' 인 Get 요청이 주어지면") {
          it("statemachine의 상태가 'DRAFT' 에서 'UNDER_MEDIATION' 으로 변경되고, 변경된 결과를 돌려준다") {
            val event: DocumentEvent = DocumentEvent.DOCUMENT_PUBLISHED_BY_USER
            val prevState: DocumentState = DocumentState.DRAFT
            val expectedState: DocumentState = DocumentState.UNDER_MEDIATION
            val plan: StateMachineTestPlan<DocumentState, DocumentEvent> =
              stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)

            mockMvc.perform(MockMvcRequestBuilders.post("/{documentId}/send/{event}", 1, event))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.event").exists())
              .andExpect(jsonPath("$.state").exists())
              .andExpect(jsonPath("$.documentId").exists())
              .andExpect(jsonPath("$.state.prev").exists())
              .andExpect(jsonPath("$.state.changed").exists())
              .andExpect(jsonPath("$.event").isString())
              .andExpect(jsonPath("$.state").isMap())
              .andExpect(jsonPath("$.documentId").isNumber())
              .andExpect(jsonPath("$.state.prev").isString())
              .andExpect(jsonPath("$.state.changed").isString())
              .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
              .andExpect(jsonPath("$.event").value(equalTo(event.name)))
              .andExpect(jsonPath("$.documentId").value(equalTo(1)))
              .andExpect(jsonPath("$.state").hasJsonPath())
              .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
              .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name)))
              .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
              .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name)))
              .andDo(MockMvcResultHandlers.print())

            plan.test()
          }
        }
        context("PathVariable 'event' 가 'DOCUMENT_PUBLISHED_BY_ADMINISTRATOR' 인 Get 요청이 주어지면") {
          it("statemachine의 상태가 'DRAFT' 에서 'PUBLIC_DISCLOSURE' 으로 변경되고, 변경된 결과를 돌려준다") {
            val event: DocumentEvent = DocumentEvent.DOCUMENT_PUBLISHED_BY_ADMINISTRATOR
            val prevState: DocumentState = DocumentState.DRAFT
            val expectedState: DocumentState = DocumentState.PUBLIC_DISCLOSURE
            val plan: StateMachineTestPlan<DocumentState, DocumentEvent> =
              stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
            mockMvc.perform(MockMvcRequestBuilders.post("/{documentId}/send/{event}", 1, event))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.event").exists())
              .andExpect(jsonPath("$.state").exists())
              .andExpect(jsonPath("$.documentId").exists())
              .andExpect(jsonPath("$.state.prev").exists())
              .andExpect(jsonPath("$.state.changed").exists())
              .andExpect(jsonPath("$.event").isString())
              .andExpect(jsonPath("$.state").isMap())
              .andExpect(jsonPath("$.documentId").isNumber())
              .andExpect(jsonPath("$.state.prev").isString())
              .andExpect(jsonPath("$.state.changed").isString())
              .andExpect(jsonPath("$.state.prev").isString())
              .andExpect(jsonPath("$.state.changed").isString())
              .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
              .andExpect(jsonPath("$.event").value(equalTo(event.name)))
              .andExpect(jsonPath("$.documentId").value(equalTo(1)))
              .andExpect(jsonPath("$.state").hasJsonPath())
              .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
              .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name)))
              .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
              .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name)))
              .andDo(MockMvcResultHandlers.print())
            plan.test()
          }
        }

        context("PathVariable 'event' 가 'DOCUMENT_PUBLISHED_BY_USER' 혹은 'DOCUMENT_PUBLISHED_BY_ADMINISTRATOR' 가 아닌 다른 값인 Get 요청이 주어지면") {
          val param = getDocumentEvents(
            setOf(
              DocumentEvent.DOCUMENT_PUBLISHED_BY_USER,
              DocumentEvent.DOCUMENT_PUBLISHED_BY_ADMINISTRATOR
            )
          )

          val prevState: DocumentState = DocumentState.DRAFT
          val expectedState: DocumentState = DocumentState.DRAFT
          val plan: StateMachineTestPlan<DocumentState, DocumentEvent> =
            stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
          param.forEachIndexed { index, event ->
            it("statemachine의 상태가 'DRAFT' 에서 다른 값으로 바뀌지 않고, 바뀌지 않은 상태임을 결과로 돌려준다 (${index + 1}) - $event") {
              mockMvc.perform(MockMvcRequestBuilders.post("/{documentId}/send/{event}", 1, event))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.state").exists())
                .andExpect(jsonPath("$.documentId").exists())
                .andExpect(jsonPath("$.state.prev").exists())
                .andExpect(jsonPath("$.state.changed").exists())
                .andExpect(jsonPath("$.event").isString())
                .andExpect(jsonPath("$.state").isMap())
                .andExpect(jsonPath("$.documentId").isNumber())
                .andExpect(jsonPath("$.state.prev").isString())
                .andExpect(jsonPath("$.state.changed").isString())
                .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
                .andExpect(jsonPath("$.event").value(equalTo(event.name)))
                .andExpect(jsonPath("$.documentId").value(equalTo(1)))
                .andExpect(jsonPath("$.state").hasJsonPath())
                .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
                .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name)))
                .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
                .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name)))
                .andDo(MockMvcResultHandlers.print())
              plan.test()

            }
          }
        }
      }

      context("상태머신의 상태가 'UNDER_MEDIATION' 일 때") {
        // 테스트를 위해, 상태기계의 status를 `UNDER_MEDIATION` 로 바꾸기 위한 작업.
        stateMachineUnitTestHelper.sendEvent(1, DocumentEvent.DOCUMENT_PUBLISHED_BY_USER)
        context("PathVariable 'event' 가 'APPROVED' 인 Get 요청이 주어지면") {
          it("statemachine의 상태가 'UNDER_MEDIATION' 에서 'WAITING_FOR_PUBLIC_DISCLOSURE' 으로 변경되고, 변경된 결과를 돌려준다") {
            val event: DocumentEvent = DocumentEvent.APPROVED
            val prevState: DocumentState = DocumentState.UNDER_MEDIATION
            val expectedState: DocumentState = DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE
            val plan: StateMachineTestPlan<DocumentState, DocumentEvent> =
              stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
            mockMvc.perform(MockMvcRequestBuilders.post("/{documentId}/send/{event}", 1, event))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.event").exists())
              .andExpect(jsonPath("$.state").exists())
              .andExpect(jsonPath("$.documentId").exists())
              .andExpect(jsonPath("$.state.prev").exists())
              .andExpect(jsonPath("$.state.changed").exists())
              .andExpect(jsonPath("$.event").isString())
              .andExpect(jsonPath("$.state").isMap())
              .andExpect(jsonPath("$.documentId").isNumber())
              .andExpect(jsonPath("$.state.prev").isString())
              .andExpect(jsonPath("$.state.changed").isString())
              .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
              .andExpect(jsonPath("$.event").value(equalTo(event.name)))
              .andExpect(jsonPath("$.documentId").value(equalTo(1)))
              .andExpect(jsonPath("$.state").hasJsonPath())
              .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
              .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name)))
              .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
              .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name)))
              .andDo(MockMvcResultHandlers.print())
            plan.test()
          }
        }

        context("PathVariable 'event' 가 'NEEDS_TO_BE_ADJUSTED_OR_REJECTED' 인 Get 요청이 주어지면") {
          it("statemachine의 상태가 'UNDER_MEDIATION' 에서 'DRAFT' 으로 변경되고, 변경된 결과를 돌려준다") {
            val event: DocumentEvent = DocumentEvent.NEEDS_TO_BE_ADJUSTED_OR_REJECTED
            val prevState: DocumentState = DocumentState.UNDER_MEDIATION
            val expectedState: DocumentState = DocumentState.DRAFT
            val plan: StateMachineTestPlan<DocumentState, DocumentEvent> =
              stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
            mockMvc.perform(MockMvcRequestBuilders.post("/{documentId}/send/{event}", 1, event))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.event").exists())
              .andExpect(jsonPath("$.state").exists())
              .andExpect(jsonPath("$.documentId").exists())
              .andExpect(jsonPath("$.state.prev").exists())
              .andExpect(jsonPath("$.state.changed").exists())
              .andExpect(jsonPath("$.event").isString())
              .andExpect(jsonPath("$.state").isMap())
              .andExpect(jsonPath("$.documentId").isNumber())
              .andExpect(jsonPath("$.state.prev").isString())
              .andExpect(jsonPath("$.state.changed").isString())
              .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
              .andExpect(jsonPath("$.event").value(equalTo(event.name)))
              .andExpect(jsonPath("$.documentId").value(equalTo(1)))
              .andExpect(jsonPath("$.state").hasJsonPath())
              .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
              .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name)))
              .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
              .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name)))
              .andDo(MockMvcResultHandlers.print())
            plan.test()
          }
        }

        context("PathVariable 'event' 가 'APPROVED' 혹은 'NEEDS_TO_BE_ADJUSTED_OR_REJECTED' 가 아닌 다른 값인 Get 요청이 주어지면") {
          val exclude =
            mutableSetOf(DocumentEvent.APPROVED, DocumentEvent.NEEDS_TO_BE_ADJUSTED_OR_REJECTED)
          val param = DocumentEvent.entries.toTypedArray()
            .filter { event -> !exclude.contains(event) }
            .toList()

          val prevState: DocumentState = DocumentState.UNDER_MEDIATION
          val expectedState: DocumentState = DocumentState.UNDER_MEDIATION
          val plan: StateMachineTestPlan<DocumentState, DocumentEvent> =
            stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
          param.forEachIndexed { index, event ->
            it("statemachine의 상태가 'UNDER_MEDIATION' 에서 다른 값으로 바뀌지 않고, 바뀌지 않은 상태임을 결과로 돌려준다 (${index + 1}) - $event") {
              mockMvc.perform(MockMvcRequestBuilders.post("/{documentId}/send/{event}", 1, event))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.state").exists())
                .andExpect(jsonPath("$.documentId").exists())
                .andExpect(jsonPath("$.state.prev").exists())
                .andExpect(jsonPath("$.state.changed").exists())
                .andExpect(jsonPath("$.event").isString())
                .andExpect(jsonPath("$.state").isMap())
                .andExpect(jsonPath("$.documentId").isNumber())
                .andExpect(jsonPath("$.state.prev").isString())
                .andExpect(jsonPath("$.state.changed").isString())
                .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
                .andExpect(jsonPath("$.event").value(equalTo(event.name)))
                .andExpect(jsonPath("$.documentId").value(equalTo(1)))
                .andExpect(jsonPath("$.state").hasJsonPath())
                .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
                .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name)))
                .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
                .andExpect(jsonPath("$.state.changed").value(equalTo(prevState.name)))
                .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name)))
                .andDo(MockMvcResultHandlers.print())
              plan.test()

            }
          }
        }
      }

      context("상태머신의 상태가 'WAITING_FOR_PUBLIC_DISCLOSURE' 일 때") {
        // 테스트를 위해, 상태기계의 status를 `WAITING_FOR_PUBLIC_DISCLOSURE` 로 바꾸기 위한 작업.
        stateMachineUnitTestHelper.sendEvent(1, DocumentEvent.DOCUMENT_PUBLISHED_BY_USER)
        stateMachineUnitTestHelper.sendEvent(1, DocumentEvent.APPROVED)

        context("PathVariable 'event' 가 'APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR' 인 Get 요청이 주어지면") {
          it("statemachine의 상태가 'WAITING_FOR_PUBLIC_DISCLOSURE' 에서 'PUBLIC_DISCLOSURE' 으로 변경되고, 변경된 결과를 돌려준다") {
            val event: DocumentEvent = DocumentEvent.APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR
            val prevState: DocumentState = DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE
            val expectedState: DocumentState = DocumentState.PUBLIC_DISCLOSURE
            val plan: StateMachineTestPlan<DocumentState, DocumentEvent> =
              stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
            mockMvc.perform(MockMvcRequestBuilders.post("/{documentId}/send/{event}", 1, event))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.event").exists())
              .andExpect(jsonPath("$.state").exists())
              .andExpect(jsonPath("$.documentId").exists())
              .andExpect(jsonPath("$.state.prev").exists())
              .andExpect(jsonPath("$.state.changed").exists())
              .andExpect(jsonPath("$.event").isString())
              .andExpect(jsonPath("$.state").isMap())
              .andExpect(jsonPath("$.documentId").isNumber())
              .andExpect(jsonPath("$.state.prev").isString())
              .andExpect(jsonPath("$.state.changed").isString())
              .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
              .andExpect(jsonPath("$.event").value(equalTo(event.name)))
              .andExpect(jsonPath("$.documentId").value(equalTo(1)))
              .andExpect(jsonPath("$.state").hasJsonPath())
              .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
              .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name)))
              .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
              .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name)))
              .andDo(MockMvcResultHandlers.print())
            plan.test()
          }
        }

        context("PathVariable 'event' 가 'APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR' 가 아닌 다른 값인 Get 요청이 주어지면") {
          val param =
            getDocumentEvents(setOf(DocumentEvent.APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR))

          val prevState: DocumentState = DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE
          val expectedState: DocumentState = DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE
          val plan: StateMachineTestPlan<DocumentState, DocumentEvent> =
            stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
          param.forEachIndexed { index, event ->
            it("statemachine의 상태가 'WAITING_FOR_PUBLIC_DISCLOSURE' 에서 다른 값으로 바뀌지 않고, 바뀌지 않은 상태임을 결과로 돌려준다 (${index + 1}) - $event") {
              mockMvc.perform(MockMvcRequestBuilders.post("/{documentId}/send/{event}", 1, event))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.state").exists())
                .andExpect(jsonPath("$.documentId").exists())
                .andExpect(jsonPath("$.state.prev").exists())
                .andExpect(jsonPath("$.state.changed").exists())
                .andExpect(jsonPath("$.event").isString())
                .andExpect(jsonPath("$.state").isMap())
                .andExpect(jsonPath("$.documentId").isNumber())
                .andExpect(jsonPath("$.state.prev").isString())
                .andExpect(jsonPath("$.state.changed").isString())
                .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
                .andExpect(jsonPath("$.event").value(equalTo(event.name)))
                .andExpect(jsonPath("$.documentId").value(equalTo(1)))
                .andExpect(jsonPath("$.state").hasJsonPath())
                .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
                .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name)))
                .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
                .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name)))
                .andDo(MockMvcResultHandlers.print())
              plan.test()
            }
          }
        }
      }

      context("상태머신의 상태가 'PUBLIC_DISCLOSURE' 일 때") {
        //테스트를 위해, 상태기계의 status를 `PUBLIC_DISCLOSURE` 로 바꾸기 위한 작업.
        stateMachineUnitTestHelper.sendEvent(1, DocumentEvent.DOCUMENT_PUBLISHED_BY_ADMINISTRATOR)
        context("PathVariable 'event' 가 'EXPIRED' 인 Get 요청이 주어지면") {
          it("statemachine의 상태가 'PUBLIC_DISCLOSURE' 에서 'DRAFT' 으로 변경되고, 변경된 결과를 돌려준다") {
            val event: DocumentEvent = DocumentEvent.EXPIRED
            val prevState: DocumentState = DocumentState.PUBLIC_DISCLOSURE
            val expectedState: DocumentState = DocumentState.DRAFT
            val plan: StateMachineTestPlan<DocumentState, DocumentEvent> =
              stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
            mockMvc.perform(MockMvcRequestBuilders.post("/{documentId}/send/{event}", 1, event))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.event").exists())
              .andExpect(jsonPath("$.state").exists())
              .andExpect(jsonPath("$.documentId").exists())
              .andExpect(jsonPath("$.state.prev").exists())
              .andExpect(jsonPath("$.state.changed").exists())
              .andExpect(jsonPath("$.event").isString())
              .andExpect(jsonPath("$.state").isMap())
              .andExpect(jsonPath("$.documentId").isNumber())
              .andExpect(jsonPath("$.state.prev").isString())
              .andExpect(jsonPath("$.state.changed").isString())
              .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
              .andExpect(jsonPath("$.event").value(equalTo(event.name)))
              .andExpect(jsonPath("$.documentId").value(equalTo(1)))
              .andExpect(jsonPath("$.state").hasJsonPath())
              .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
              .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name)))
              .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
              .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name)))
              .andDo(MockMvcResultHandlers.print())
            plan.test()
          }
        }
        context("PathVariable 'event' 가 'EXPIRED' 가 아닌 다른 값인 Get 요청이 주어지면") {
          val param = getDocumentEvents(setOf(DocumentEvent.EXPIRED))

          val prevState: DocumentState = DocumentState.PUBLIC_DISCLOSURE
          val expectedState: DocumentState = DocumentState.PUBLIC_DISCLOSURE
          val plan: StateMachineTestPlan<DocumentState, DocumentEvent> =
            stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
          param.forEachIndexed { index, event ->
            it("statemachine의 상태가 'PUBLIC_DISCLOSURE' 에서 다른 값으로 바뀌지 않고, 바뀌지 않은 상태임을 결과로 돌려준다 (${index + 1}) - $event") {
              mockMvc.perform(MockMvcRequestBuilders.post("/{documentId}/send/{event}", 1, event))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.state").exists())
                .andExpect(jsonPath("$.documentId").exists())
                .andExpect(jsonPath("$.state.prev").exists())
                .andExpect(jsonPath("$.state.changed").exists())
                .andExpect(jsonPath("$.event").isString())
                .andExpect(jsonPath("$.state").isMap())
                .andExpect(jsonPath("$.documentId").isNumber())
                .andExpect(jsonPath("$.state.prev").isString())
                .andExpect(jsonPath("$.state.changed").isString())
                .andExpect(jsonPath("$.event").value(containsInDocumentEventNames()))
                .andExpect(jsonPath("$.event").value(equalTo(event.name)))
                .andExpect(jsonPath("$.documentId").value(equalTo(1)))
                .andExpect(jsonPath("$.state").hasJsonPath())
                .andExpect(jsonPath("$.state.prev").value(containsInDocumentStateNames()))
                .andExpect(jsonPath("$.state.prev").value(equalTo(prevState.name)))
                .andExpect(jsonPath("$.state.changed").value(containsInDocumentStateNames()))
                .andExpect(jsonPath("$.state.changed").value(equalTo(prevState.name)))
                .andExpect(jsonPath("$.state.changed").value(equalTo(expectedState.name)))
                .andDo(MockMvcResultHandlers.print())
              plan.test()
            }
          }
        }
      }
    }
  }
}
