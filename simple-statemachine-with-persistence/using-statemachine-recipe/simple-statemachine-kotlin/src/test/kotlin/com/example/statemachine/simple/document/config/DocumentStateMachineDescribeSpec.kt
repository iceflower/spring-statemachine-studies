package com.example.statemachine.simple.document.config

import com.example.statemachine.simple.StateMachineUnitTestHelper
import com.example.statemachine.simple.TestArgumentsHelper
import com.example.statemachine.simple.document.DocumentEvent
import com.example.statemachine.simple.document.DocumentState
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.statemachine.StateMachine
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DocumentStateMachineDescribeSpec(
  val stateMachine: StateMachine<DocumentState, DocumentEvent>
) : DescribeSpec({ isolationMode = IsolationMode.InstancePerLeaf }) {
  override fun extensions() = listOf(SpringExtension)

  init {
    this.describe("DocumentStateMachine 객체는") {


      context("DB에 존재하지 않는 documentID가 주어지면") {

        val stateMachineUnitTestHelper: StateMachineUnitTestHelper<DocumentState, DocumentEvent> =
          StateMachineUnitTestHelper(stateMachine)

        val param = TestArgumentsHelper.getDocumentEvents(
          setOf()
        )

        param.forEachIndexed { index, event ->
          it("statemachine의 상태가 'DRAFT' 에서 다른 값으로 바뀌지 않고, 기존 상태인 'DRAFT' 가 그대로 유지된다 (${index + 1}) - $event") {
            val prevState = DocumentState.DRAFT
            val prevTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(prevState)
            prevTestPlan.test()

            stateMachineUnitTestHelper.sendEvent(-99, event)

            val expectedState = DocumentState.DRAFT
            val afterTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
            afterTestPlan.test()

          }
        }
      }

      context("DB에 존재하는 documentID가 주어지고") {

        val stateMachineUnitTestHelper: StateMachineUnitTestHelper<DocumentState, DocumentEvent> =
          StateMachineUnitTestHelper(stateMachine)

        context("주어진 documentID의 기존 상태가 'DRAFT' 일 때") {
          context("'DOCUMENT_PUBLISHED_BY_USER' 이벤트가 주어지면") {
            it("statemachine의 상태가 'DRAFT' 에서 'UNDER_MEDIATION' 으로 변경된다") {

              val event = DocumentEvent.DOCUMENT_PUBLISHED_BY_USER


              val prevState = DocumentState.DRAFT
              val prevTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(prevState)
              prevTestPlan.test()

              stateMachineUnitTestHelper.sendEvent(1, event)

              val expectedState = DocumentState.UNDER_MEDIATION
              val afterTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
              afterTestPlan.test()
            }
          }
          context("'DOCUMENT_PUBLISHED_BY_ADMINISTRATOR' 이벤트가 주어지면") {
            it("statemachine의 상태가 'DRAFT' 에서 'PUBLIC_DISCLOSURE' 으로 변경된다") {
              val event = DocumentEvent.DOCUMENT_PUBLISHED_BY_ADMINISTRATOR

              val prevState = DocumentState.DRAFT
              val prevTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(prevState)
              prevTestPlan.test()

              stateMachineUnitTestHelper.sendEvent(1, event)

              val expectedState = DocumentState.PUBLIC_DISCLOSURE
              val afterTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
              afterTestPlan.test()
            }
          }
          context("'DOCUMENT_PUBLISHED_BY_USER' 혹은 'DOCUMENT_PUBLISHED_BY_ADMINISTRATOR' 가 아닌 다른 이벤트가 주어지면") {
            val param = TestArgumentsHelper.getDocumentEvents(
              setOf(
                DocumentEvent.DOCUMENT_PUBLISHED_BY_USER,
                DocumentEvent.DOCUMENT_PUBLISHED_BY_ADMINISTRATOR
              )
            )

            param.forEachIndexed { index, event ->
              it("statemachine의 상태가 'DRAFT' 에서 다른 값으로 바뀌지 않고, 기존 상태인 'DRAFT' 가 그대로 유지된다 (${index + 1}) - $event") {

                val prevState = DocumentState.DRAFT
                val prevTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(prevState)
                prevTestPlan.test()

                stateMachineUnitTestHelper.sendEvent(1, event)

                val expectedState = DocumentState.DRAFT
                val afterTestPlan =
                  stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
                afterTestPlan.test()
              }
            }
          }
        }

        context("주어진 documentID의 기존 상태가 'UNDER_MEDIATION' 일 때") {
          // 테스트를 위해, 상태기계의 status를 `DOCUMENT_PUBLISHED_BY_USER` 로 바꾸기 위한 작업.
          stateMachineUnitTestHelper.sendEvent(1, DocumentEvent.DOCUMENT_PUBLISHED_BY_USER)

          context("'APPROVED' 이벤트가 주어지면") {
            it("statemachine의 상태가 'UNDER_MEDIATION' 에서 'WAITING_FOR_PUBLIC_DISCLOSURE' 으로 변경된다") {

              val event = DocumentEvent.APPROVED

              val prevState = DocumentState.UNDER_MEDIATION
              val prevTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(prevState)
              prevTestPlan.test()

              stateMachineUnitTestHelper.sendEvent(1, event)

              val expectedState = DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE
              val afterTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
              afterTestPlan.test()
            }
          }
          context("'NEEDS_TO_BE_ADJUSTED_OR_REJECTED' 이벤트가 주어지면") {
            it("statemachine의 상태가 'UNDER_MEDIATION' 에서 'DRAFT' 으로 변경된다") {
              val event = DocumentEvent.NEEDS_TO_BE_ADJUSTED_OR_REJECTED

              val prevState = DocumentState.UNDER_MEDIATION
              val prevTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(prevState)
              prevTestPlan.test()

              stateMachineUnitTestHelper.sendEvent(1, event)

              val expectedState = DocumentState.DRAFT
              val afterTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
              afterTestPlan.test()
            }
          }
          context("'APPROVED' 혹은 'NEEDS_TO_BE_ADJUSTED_OR_REJECTED' 가 아닌 다른 이벤트가 주어지면") {
            val param = TestArgumentsHelper.getDocumentEvents(
              setOf(
                DocumentEvent.APPROVED,
                DocumentEvent.NEEDS_TO_BE_ADJUSTED_OR_REJECTED
              )
            )
            param.forEachIndexed { index, event ->
              it("statemachine의 상태가 'UNDER_MEDIATION' 에서 다른 값으로 바뀌지 않고, 기존 상태인 'UNDER_MEDIATION' 가 그대로 유지된다 (${index + 1}) - $event") {

                val prevState = DocumentState.UNDER_MEDIATION
                val prevTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(prevState)
                prevTestPlan.test()

                stateMachineUnitTestHelper.sendEvent(1, event)

                val expectedState = DocumentState.UNDER_MEDIATION
                val afterTestPlan =
                  stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
                afterTestPlan.test()

              }
            }
          }
        }
        context("주어진 documentID의 기존 상태가 'WAITING_FOR_PUBLIC_DISCLOSURE' 일 때") {

          // 테스트를 위해, 상태기계의 status를 `WAITING_FOR_PUBLIC_DISCLOSURE` 로 바꾸기 위한 작업.
          stateMachineUnitTestHelper.sendEvent(1, DocumentEvent.DOCUMENT_PUBLISHED_BY_USER)
          stateMachineUnitTestHelper.sendEvent(1, DocumentEvent.APPROVED)

          context("'APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR' 이벤트가 주어지면") {
            it("statemachine의 상태가 'WAITING_FOR_PUBLIC_DISCLOSURE' 에서 'PUBLIC_DISCLOSURE' 으로 변경된다") {
              val event = DocumentEvent.APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR

              val prevState = DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE
              val prevTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(prevState)
              prevTestPlan.test()

              stateMachineUnitTestHelper.sendEvent(1, event)

              val expectedState = DocumentState.PUBLIC_DISCLOSURE
              val afterTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
              afterTestPlan.test()
            }
          }
          context("'APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR' 가 아닌 다른 이벤트가 주어지면") {
            val param = TestArgumentsHelper.getDocumentEvents(
              setOf(
                DocumentEvent.APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR
              )
            )

            param.forEachIndexed { index, event ->
              it("statemachine의 상태가 'WAITING_FOR_PUBLIC_DISCLOSURE' 에서 다른 값으로 바뀌지 않고, 기존 상태인 'WAITING_FOR_PUBLIC_DISCLOSURE' 가 그대로 유지된다 (${index + 1}) - $event") {
                val prevState = DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE
                val prevTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(prevState)
                prevTestPlan.test()

                stateMachineUnitTestHelper.sendEvent(1, event)

                val expectedState = DocumentState.WAITING_FOR_PUBLIC_DISCLOSURE
                val afterTestPlan =
                  stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
                afterTestPlan.test()
              }
            }
          }
        }
        context("주어진 documentID의 기존 상태가 'PUBLIC_DISCLOSURE' 일 때") {
          // 테스트를 위해, 상태기계의 status를 `PUBLIC_DISCLOSURE` 로 바꾸기 위한 작업.
          stateMachineUnitTestHelper.sendEvent(1, DocumentEvent.DOCUMENT_PUBLISHED_BY_ADMINISTRATOR)

          context("'EXPIRED' 이벤트가 주어지면") {
            it("statemachine의 상태가 'PUBLIC_DISCLOSURE' 에서 'DRAFT' 으로 변경되고, 변경된 결과를 돌려준다") {
              val event = DocumentEvent.EXPIRED

              val prevState = DocumentState.PUBLIC_DISCLOSURE
              val prevTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(prevState)
              prevTestPlan.test()

              stateMachineUnitTestHelper.sendEvent(1, event)

              val expectedState = DocumentState.DRAFT
              val afterTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
              afterTestPlan.test()
            }
          }

          context("'EXPIRED' 가 아닌 다른 이벤트가 주어지면") {
            val param = TestArgumentsHelper.getDocumentEvents(
              setOf(
                DocumentEvent.EXPIRED
              )
            )

            param.forEachIndexed { index, event ->
              it("statemachine의 상태가 'PUBLIC_DISCLOSURE' 에서 다른 값으로 바뀌지 않고, 기존 상태인 'PUBLIC_DISCLOSURE' 가 그대로 유지된다 (${index + 1}) - $event") {
                val prevState = DocumentState.PUBLIC_DISCLOSURE
                val prevTestPlan = stateMachineUnitTestHelper.getStateMachineTestPlan(prevState)
                prevTestPlan.test()

                stateMachineUnitTestHelper.sendEvent(1, event)

                val expectedState = DocumentState.PUBLIC_DISCLOSURE
                val afterTestPlan =
                  stateMachineUnitTestHelper.getStateMachineTestPlan(expectedState)
                afterTestPlan.test()

              }
            }
          }
        }
      }
    }
  }
}
