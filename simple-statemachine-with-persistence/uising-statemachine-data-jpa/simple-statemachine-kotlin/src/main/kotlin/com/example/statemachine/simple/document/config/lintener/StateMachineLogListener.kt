/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.statemachine.simple.document.config.lintener

import com.example.statemachine.simple.document.DocumentEvent
import com.example.statemachine.simple.document.DocumentState
import org.springframework.statemachine.StateContext
import org.springframework.statemachine.StateContext.Stage
import org.springframework.statemachine.listener.StateMachineListenerAdapter
import java.util.*

class StateMachineLogListener : StateMachineListenerAdapter<DocumentState, DocumentEvent>() {
  private val messages = LinkedList<String>()
  fun getMessages(): List<String> {
    return messages
  }

  fun resetMessages() {
    messages.clear()
  }

  override fun stateContext(stateContext: StateContext<DocumentState, DocumentEvent>) {
    if (stateContext.stage == Stage.STATE_ENTRY) {
      messages.addFirst("Enter " + stateContext.target.id)
    } else if (stateContext.stage == Stage.STATE_EXIT) {
      messages.addFirst("Exit " + stateContext.source.id)
    } else if (stateContext.stage == Stage.STATEMACHINE_START) {
      messages.addLast("Machine started")
    } else if (stateContext.stage == Stage.STATEMACHINE_STOP) {
      messages.addFirst("Machine stopped")
    }
  }
}
