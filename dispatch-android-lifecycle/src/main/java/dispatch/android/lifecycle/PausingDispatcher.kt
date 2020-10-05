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

package dispatch.android.lifecycle

import dispatch.android.lifecycle.internal.*
import dispatch.core.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

@ExperimentalDispatchApi
abstract class PausingDispatcher : CoroutineDispatcher(),
                                   PausingContinuationInterceptor,
                                   PauseController

@ExperimentalDispatchApi
fun PausingDispatcher(
  coroutineScope: CoroutineScope,
  delegate: CoroutineDispatcher
): PausingDispatcher = PausingDispatcherImpl(coroutineScope, delegate)

@ExperimentalDispatchApi
interface PausingDispatcherProvider : DispatcherProvider {
  override val default: PausingDispatcher
  override val io: PausingDispatcher
  override val main: PausingDispatcher
  override val mainImmediate: PausingDispatcher
  override val unconfined: PausingDispatcher

}

@ExperimentalDispatchApi
fun PausingDispatcherProvider(
  scope: CoroutineScope,
  dispatcherProvider: DispatcherProvider = scope.dispatcherProvider
): PausingDispatcherProvider = PausingDispatcherProviderImpl(scope, dispatcherProvider)

@ExperimentalDispatchApi
interface PausingContinuationInterceptor : PauseController,
                                           ContinuationInterceptor

@ExperimentalDispatchApi
interface PauseController {
  fun resume()
  fun pause()
}

@ExperimentalDispatchApi
interface PausingCoroutineScope : CoroutineScope,
                                  PauseController

@ExperimentalDispatchApi
fun CoroutineScope.pausing(): PausingCoroutineScope = PausingCoroutineScope(this)

@ExperimentalDispatchApi
fun PausingCoroutineScope(delegateScope: CoroutineScope): PausingCoroutineScope =
  PausingCoroutineScopeImpl(delegateScope)
