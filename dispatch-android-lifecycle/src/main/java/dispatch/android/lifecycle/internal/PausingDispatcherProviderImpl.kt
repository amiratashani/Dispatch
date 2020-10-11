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

@ExperimentalDispatchApi
@OptIn(ExperimentalCoroutinesApi::class)
class PausingDispatcherProviderImpl(
  scope: CoroutineScope,
  dispatcherProvider: DispatcherProvider,
  private val paused: MutableStateFlow<Boolean> = MutableStateFlow(false)
) : PausingDispatcherProvider {

  override val default: PausingDispatcher = PausingDispatcherImpl(
    scope, dispatcherProvider.default, paused
  )
  override val io: PausingDispatcher = PausingDispatcherImpl(scope, dispatcherProvider.io, paused)
  override val main: PausingDispatcher =
    PausingDispatcherImpl(scope, dispatcherProvider.main, paused)
  override val mainImmediate: PausingDispatcher = PausingDispatcherImpl(
    scope, dispatcherProvider.mainImmediate, paused
  )
  override val unconfined: PausingDispatcher = PausingDispatcherImpl(
    scope, dispatcherProvider.unconfined, paused
  )

  override fun resume() {
    paused.value = false
  }

  override fun pause() {
    paused.value = true
  }

  override fun close(cause: Throwable?) {
    default.close(cause)
    io.close(cause)
    main.close(cause)
    mainImmediate.close(cause)
    unconfined.close(cause)
  }
}
