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
@OptIn(ExperimentalCoroutinesApi::class)
class PausingCoroutineScopeImpl(private val delegate: CoroutineScope) : PausingCoroutineScope {

  private val lock = MutableStateFlow(false)

  val dp = object : PausingDispatcherProvider {

    val delegateProvider = delegate.dispatcherProvider

    override val default: PausingDispatcher = PausingDispatcherImpl(
      delegate, delegateProvider.default, lock
    )
    override val io: PausingDispatcher = PausingDispatcherImpl(
      delegate, delegateProvider.io, lock
    )
    override val main: PausingDispatcher = PausingDispatcherImpl(
      delegate, delegateProvider.main, lock
    )
    override val mainImmediate: PausingDispatcher = PausingDispatcherImpl(
      delegate, delegateProvider.mainImmediate, lock
    )
    override val unconfined: PausingDispatcher = PausingDispatcherImpl(
      delegate, delegateProvider.unconfined, lock
    )
  }

  override val coroutineContext: CoroutineContext =
    delegate.coroutineContext + dp + delegate.coroutineContext[ContinuationInterceptor].let { continuationInterceptor ->
      when (continuationInterceptor) {
        is PausingDispatcher -> continuationInterceptor
        is CoroutineDispatcher -> PausingDispatcherImpl(delegate, continuationInterceptor, lock)
        else                   -> dp.default
      }
    }

  override fun resume() {
    lock.value = false
  }

  override fun pause() {
    lock.value = true
  }
}

@ExperimentalDispatchApi
@OptIn(ExperimentalCoroutinesApi::class)
class PausingDispatcherProviderImpl(
  private val scope: CoroutineScope,
  private val dispatcherProvider: DispatcherProvider
) : PausingDispatcherProvider {

  private val lock = MutableStateFlow(false)
  override val default: PausingDispatcher =
    PausingDispatcherImpl(scope, dispatcherProvider.default, lock)
  override val io: PausingDispatcher = PausingDispatcherImpl(scope, dispatcherProvider.io, lock)
  override val main: PausingDispatcher = PausingDispatcherImpl(scope, dispatcherProvider.main, lock)
  override val mainImmediate: PausingDispatcher =
    PausingDispatcherImpl(scope, dispatcherProvider.mainImmediate, lock)
  override val unconfined: PausingDispatcher =
    PausingDispatcherImpl(scope, dispatcherProvider.unconfined, lock)
}

@ExperimentalDispatchApi
@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
class PausingDispatcherImpl(
  coroutineScope: CoroutineScope,
  delegate: CoroutineDispatcher,
  private val paused: MutableStateFlow<Boolean> = MutableStateFlow(false)
) : PausingDispatcher() {

  private var finished: Boolean = false
  private var working: Boolean = false

  private val delegate: CoroutineDispatcher =
    (delegate as? PausingDispatcherImpl)?.delegate ?: delegate

  val actor = coroutineScope.actor<DelegatedArgs>(capacity = Channel.UNLIMITED) {
    for ((context, block) in channel) {

      if (paused.value) {
        paused.takeWhile { shouldWait -> shouldWait }
          .collect()
      }

      working = true

      this@PausingDispatcherImpl.delegate.dispatch(context, block)

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

  override fun dispatch(context: CoroutineContext, block: Runnable) {

    actor.sendBlocking(DelegatedArgs(context, block))
  }

  data class DelegatedArgs(val context: CoroutineContext, val block: Runnable)
}
