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

import kotlinx.coroutines.*
import java.util.concurrent.atomic.*
import kotlin.coroutines.*

@OptIn(ObsoleteCoroutinesApi::class)
class RecordingDispatcher(private val name: String) : CoroutineDispatcher() {

  val delegate = newSingleThreadContext(name)

  val dispatchCount = AtomicInteger(0)

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    dispatchCount.incrementAndGet()
    delegate.dispatch(context, block)
  }

  override fun toString(): String = "RecordingDispatcher ($name)"
}
