/*
 * Copyright (C) 2020 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dispatch.android.lifecycle.internal

import dispatch.android.lifecycle.*
import dispatch.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*

@ExperimentalDispatchApi
@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
class PausingDispatcherImpl(
  coroutineScope: CoroutineScope,
  delegate: CoroutineDispatcher,
  private val paused: MutableStateFlow<Boolean> = MutableStateFlow(false)
) : PausingDispatcher() {

  private val delegate: CoroutineDispatcher =
    (delegate as? PausingDispatcherImpl)?.delegate ?: delegate

  val actor = coroutineScope.actor<DelegatedArgs>(capacity = Channel.UNLIMITED) {
    for ((context, block) in channel) {
      if (paused.value) {
        paused.takeWhile { shouldWait -> shouldWait }
          .collect()
      }
      this@PausingDispatcherImpl.delegate.dispatch(context, block)
    }
  }

  override fun resume() {
    paused.value = false
  }

  override fun pause() {
    paused.value = true
  }

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    actor.sendBlockingOrNull(DelegatedArgs(context, block))
      ?: throw ClosedPausingDispatcherException("pausing dispatcher was closed")
  }

  override fun close(cause: Throwable?) {
    actor.close(cause)
  }

  data class DelegatedArgs(val context: CoroutineContext, val block: Runnable)
}

public class ClosedPausingDispatcherException(message: String?) : IllegalStateException(message)
