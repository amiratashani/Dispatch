/*
 * Copyright (C) 2019-2020 Rick Busarow
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

package com.rickbusarow.dispatcherprovider.test

import com.rickbusarow.dispatcherprovider.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*

/**
 * [DispatcherProvider] implementation for testing, where each property is a [TestCoroutineDispatcher].
 *
 * A default version will create a different `TestCoroutineDispatcher` for each property.
 */
@ExperimentalCoroutinesApi
class TestDispatcherProvider(
  override val default: TestCoroutineDispatcher = TestCoroutineDispatcher(),
  override val io: TestCoroutineDispatcher = TestCoroutineDispatcher(),
  override val main: TestCoroutineDispatcher = TestCoroutineDispatcher(),
  override val mainImmediate: TestCoroutineDispatcher = TestCoroutineDispatcher(),
  override val unconfined: TestCoroutineDispatcher = TestCoroutineDispatcher()
) : DispatcherProvider

/**
 * Convenience factory function for [TestDispatcherProvider], creating an implementation
 * where all properties point to the same underlying [TestCoroutineDispatcher].
 */
@ExperimentalCoroutinesApi
fun TestDispatcherProvider(dispatcher: TestCoroutineDispatcher): TestDispatcherProvider =
  TestDispatcherProvider(
    default = dispatcher,
    io = dispatcher,
    main = dispatcher,
    mainImmediate = dispatcher,
    unconfined = dispatcher
  )
