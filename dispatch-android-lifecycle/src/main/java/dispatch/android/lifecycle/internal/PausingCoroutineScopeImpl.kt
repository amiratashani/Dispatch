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
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*

@ExperimentalDispatchApi
@OptIn(ExperimentalCoroutinesApi::class)
class PausingCoroutineScopeImpl(private val delegate: CoroutineScope) : PausingCoroutineScope {

  private val lock = MutableStateFlow(false)

  val dp = PausingDispatcherProviderImpl(delegate, delegate.dispatcherProvider, lock)

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
