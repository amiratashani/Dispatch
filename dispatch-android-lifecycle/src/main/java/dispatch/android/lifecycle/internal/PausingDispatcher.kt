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

import dispatch.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*

@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
internal class PausingDispatcher(
  private val coroutineScope: CoroutineScope,
  private val paused: MutableStateFlow<Boolean> = MutableStateFlow(false)
) : CoroutineDispatcher() {

  private val delegate =
    coroutineScope.coroutineContext[ContinuationInterceptor] as? CoroutineDispatcher

  private var finished: Boolean = false
  private var working: Boolean = false

  val a = coroutineScope.actor<Runnable>(capacity = Channel.UNLIMITED) {
    for (runnable in channel) {

      if (paused.value) {
        paused.takeWhile { shouldWait -> !shouldWait }
          .collect()
      }

      working = true

      runnable.run()

      working = false
    }
  }

  suspend fun resume() = withMain {
    if (!finished) {
      paused.value = false
    }
  }

  suspend fun pause() = withMain {
    paused.value = true
  }

  fun finish() {
    a.close()
  }

  override fun isDispatchNeeded(context: CoroutineContext): Boolean {

    return delegate?.isDispatchNeeded(context) ?: true
  }

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    if (isDispatchNeeded(context)) {
      copy(context).a.sendBlocking(block)
    } else {
      a.sendBlocking(block)
    }
  }

  private fun copy(
    context: CoroutineContext
  ): PausingDispatcher = PausingDispatcher(
    coroutineScope = coroutineScope + context,
    paused = paused
  )
}
