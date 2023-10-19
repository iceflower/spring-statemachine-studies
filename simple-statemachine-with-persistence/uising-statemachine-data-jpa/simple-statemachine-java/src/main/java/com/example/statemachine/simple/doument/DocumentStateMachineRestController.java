package com.example.statemachine.simple.doument;

import com.example.statemachine.simple.doument.config.listener.StateMachineLogListener;
import java.util.List;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DocumentStateMachineRestController {

  public final static String MACHINE_ID_1 = "data-jpa-persist-1";
  public final static String MACHINE_ID_2 = "data-jpa-persist-2";
  private final static String[] MACHINES = new String[]{ MACHINE_ID_1, MACHINE_ID_2 };
  private final StateMachineService<DocumentState, DocumentEvent> stateMachineService;
  private final StateMachinePersist<DocumentState, DocumentEvent, String> stateMachinePersist;
  private final StateMachineLogListener listener;
  private StateMachine<DocumentState, DocumentEvent> currentStateMachine;

  public DocumentStateMachineRestController(
    StateMachineService<DocumentState, DocumentEvent> stateMachineService,
    StateMachinePersist<DocumentState, DocumentEvent, String> stateMachinePersist) {
    this.stateMachineService = stateMachineService;
    this.stateMachinePersist = stateMachinePersist;
    this.listener = new StateMachineLogListener();
  }

  @GetMapping("/state-machine-list")
  public String[] getStateMachineList() {

    return MACHINES;
  }

  @GetMapping("/state-list")
  public DocumentState[] getStateList() {

    return DocumentState.values();
  }


  @GetMapping("/event-list")
  public DocumentEvent[] getSventList() {

    return DocumentEvent.values();
  }

  @GetMapping("/{machine-id}/current-state")
  public StateMachineStatement currentState(@PathVariable("machine-id") String machineId)
    throws Exception {

    getStateMachine(machineId);

    final var stateMachineContext = stateMachinePersist.read(machineId);

    return new StateMachineStatement(machineId, listener.getMessages(), stateMachineContext);
  }

  @PostMapping("/{machine-id}/send/{event}")
  public EventResultStatement sendEvent(
    @PathVariable("machine-id") String machineId,
    @PathVariable("event") final DocumentEvent event) {

    final var stateMachine = getStateMachine(machineId);

    final var prevState = stateMachine.getState()
      .getId();

    final var result = stateMachine.sendEvent(makeEventMessage(event))
      .blockLast();

    final var changedState = result.getRegion()
      .getState().getId();

    return new EventResultStatement(machineId, event, new State(prevState, changedState));
  }


  private synchronized StateMachine<DocumentState, DocumentEvent> getStateMachine(
    final String machineId) {

    listener.resetMessages();

    // currentStateMachine 이 null일 경우,
    // 새로운 stateMachine 객체를 생성한다.
    if (currentStateMachine == null) {
      currentStateMachine = stateMachineService.acquireStateMachine(machineId);
      currentStateMachine.addStateListener(listener);
      currentStateMachine.startReactively().block();

      return currentStateMachine;
    }

    // currentStateMachine 이 null은 아니지만, currentStateMachine의 id가 주어진 machineId와 다를 경우
    // 기존 stateMachine 객체는 release 및 stop 처리를 진행한 후, machineId를 가진 새로운 stateMachine 객체를 생성한다.
    if (!ObjectUtils.nullSafeEquals(currentStateMachine.getId(), machineId)) {
      stateMachineService.releaseStateMachine(currentStateMachine.getId());
      currentStateMachine.stopReactively().block();
      currentStateMachine = stateMachineService.acquireStateMachine(machineId);
      currentStateMachine.addStateListener(listener);
      currentStateMachine.startReactively().block();

      return currentStateMachine;
    }

    // 먼저 서술된 케이스와 무관한 경우, 기존 객체를 그대로 return 시켜준다.
    return currentStateMachine;
  }

  public Mono<Message<DocumentEvent>> makeEventMessage(final DocumentEvent event) {

    return Mono.just(MessageBuilder.withPayload(event)
      .build());
  }


  public record StateMachineStatement(
    String machineId,
    List<String> messages,
    StateMachineContext<DocumentState, DocumentEvent> stateMachineContext) {

  }

  public record EventResultStatement(
    String machineId, DocumentEvent event, State state) {

  }

  public record State(DocumentState prev, DocumentState changed) {

  }
}
