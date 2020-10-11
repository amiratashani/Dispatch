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
import dispatch.test.*
import io.kotest.assertions.*
import io.kotest.assertions.throwables.*
import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED

@CoroutineTest
@ExperimentalDispatchApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
internal class PausingDispatcherTest : FreeSpec(
  {

    val testScope = TestProvidedCoroutineScope()

    val out = Channel<Int>(UNLIMITED)

    val delegate = RecordingDispatcher("delegate")

    val pausingDispatcher = PausingDispatcher(testScope, delegate)

    afterTest {
      pausingDispatcher.close()
      testScope.cleanupTestCoroutines()
    }

    "delegation" - {

      "should delegate dispatch when not paused" {

        val pausingJob = launch(pausingDispatcher) { out.send(1) }

        out.receive() shouldBe 1
        delegate.dispatchCount.get() shouldBe 1

        pausingJob.join()
      }

      "should not delegate dispatch when paused" {

        pausingDispatcher.pause()

        pausingDispatcher.dispatch(coroutineContext) {}

        delegate.dispatchCount.get() shouldBe 0
      }

      "should delegate dispatch after resume" {

        pausingDispatcher.pause()

        val pausingJob = launch {
          pausingDispatcher.dispatch(coroutineContext) {}
        }

        pausingDispatcher.resume()

        pausingJob.join()

        delegate.dispatchCount.get() shouldBe 1
      }
    }

    "backpressure" - {

      "should execute queued blocks in order when resumed" {

        pausingDispatcher.pause()

        val pausingJob = launch {
          pausingDispatcher.dispatch(coroutineContext) { out.sendBlocking(1) }
          pausingDispatcher.dispatch(coroutineContext) { out.sendBlocking(2) }
          pausingDispatcher.dispatch(coroutineContext) { out.sendBlocking(3) }
          pausingDispatcher.dispatch(coroutineContext) { out.sendBlocking(4) }
          pausingDispatcher.dispatch(coroutineContext) { out.sendBlocking(5) }
        }

        pausingDispatcher.resume()

        pausingJob.join()
        out.close()

        out.toList() shouldBe listOf(1, 2, 3, 4, 5)

        delegate.dispatchCount.get() shouldBe 5
      }
    }

    "cancellation" - {

      "should not dispatch when closed" {

        pausingDispatcher.close()

        shouldThrow<Exception> {
          pausingDispatcher.dispatch(coroutineContext) { fail("should not be invoked") }
        }

        delegate.dispatchCount.get() shouldBe 0
      }

      "should throw ClosedPausingDispatcherException" {

        pausingDispatcher.close()

        val exception = shouldThrow<ClosedPausingDispatcherException> {
          pausingDispatcher.dispatch(coroutineContext) { fail("should not be invoked") }
        }

        exception.message shouldBe "pausing dispatcher was closed"
      }
    }

  })
