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

interface PausingCoroutineScope : CoroutineScope,
                                  PauseController {
  companion object {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(delegate: CoroutineScope) = object : PausingCoroutineScope {

      private val lock = MutableStateFlow(false)

      val dp = object : PausingDispatcherProvider {

        val delegateProvider = delegate.dispatcherProvider

        override val default: PausingDispatcher = PausingDispatcher(
          delegate, delegateProvider.default, lock
        )
        override val io: PausingDispatcher = PausingDispatcher(
          delegate, delegateProvider.io, lock
        )
        override val main: PausingDispatcher = PausingDispatcher(
          delegate, delegateProvider.main, lock
        )
        override val mainImmediate: PausingDispatcher = PausingDispatcher(
          delegate, delegateProvider.mainImmediate, lock
        )
        override val unconfined: PausingDispatcher = PausingDispatcher(
          delegate, delegateProvider.unconfined, lock
        )
      }

      override val coroutineContext: CoroutineContext = delegate.coroutineContext + dp

      override fun resume() {
        lock.value = false
      }

      override fun pause() {
        lock.value = true
      }
    }
  }
}

fun CoroutineScope.pausing() = PausingCoroutineScope(this)

interface PausingDispatcherProvider : DispatcherProvider {
  override val default: PausingDispatcher
  override val io: PausingDispatcher
  override val main: PausingDispatcher
  override val mainImmediate: PausingDispatcher
  override val unconfined: PausingDispatcher

  companion object {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
      scope: CoroutineScope,
      dispatcherProvider: DispatcherProvider = scope.dispatcherProvider
    ) = object : PausingDispatcherProvider {
      private val lock = MutableStateFlow(false)
      override val default: PausingDispatcher =
        PausingDispatcher(scope, dispatcherProvider.default, lock)
      override val io: PausingDispatcher = PausingDispatcher(scope, dispatcherProvider.io, lock)
      override val main: PausingDispatcher = PausingDispatcher(scope, dispatcherProvider.main, lock)
      override val mainImmediate: PausingDispatcher =
        PausingDispatcher(scope, dispatcherProvider.mainImmediate, lock)
      override val unconfined: PausingDispatcher =
        PausingDispatcher(scope, dispatcherProvider.unconfined, lock)
    }
  }
}

interface PausingContinuationInterceptor : PauseController,
                                           ContinuationInterceptor

interface PauseController {
  fun resume()
  fun pause()
}

@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
class PausingDispatcher(
  private val coroutineScope: CoroutineScope,
  private val delegate: CoroutineDispatcher,
  private val paused: MutableStateFlow<Boolean> = MutableStateFlow(false)
) : CoroutineDispatcher(),
    PausingContinuationInterceptor {

  private var finished: Boolean = false
  private var working: Boolean = false

  init {
    require(coroutineScope.coroutineContext[ContinuationInterceptor] !is PausingDispatcher) { "actor's dispatcher can't be a pausing dispatcher" }
  }

  val actor = coroutineScope.actor<DelegatedArgs>(capacity = Channel.UNLIMITED) {
    for ((context, block) in channel) {

      if (paused.value) {
        paused.takeWhile { shouldWait -> shouldWait }
          .collect()
      }

      working = true

      delegate.dispatch(context, block)

      working = false
    }
  }

  override fun resume() {
    if (!finished) {
      paused.value = false
    }
  }

  override fun pause() {
    paused.value = true
  }

  fun finish() {
    actor.close()
  }

  override fun dispatch(context: CoroutineContext, block: Runnable) {

    actor.sendBlocking(DelegatedArgs(context, block))
  }

  data class DelegatedArgs(val context: CoroutineContext, val block: Runnable)
}
