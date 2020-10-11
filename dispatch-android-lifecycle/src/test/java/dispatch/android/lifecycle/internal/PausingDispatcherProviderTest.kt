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
import hermit.test.junit.*
import io.kotest.assertions.*
import io.kotest.assertions.throwables.*
import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test

@CoroutineTest
@ExperimentalDispatchApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
internal class PausingDispatcherProviderTest : FreeSpec(
  {

    val testScope = TestProvidedCoroutineScope(context = Job())

    val normalDP = object : DispatcherProvider {
      override val default = RecordingDispatcher("defaultDelegate")
      override val io = RecordingDispatcher("ioDelegate")
      override val main = RecordingDispatcher("mainDelegate")
      override val mainImmediate = RecordingDispatcher("mainImmediateDelegate")
      override val unconfined = RecordingDispatcher("unconfinedDelegate")
    }

    val pausingDP = PausingDispatcherProvider(testScope, normalDP)

    afterTest {
      pausingDP.close()
      testScope.cleanupTestCoroutines()
    }

    "pausing" - {

      "pause should pause all dispatchers" {

        pausingDP.pause()

        pausingDP.default.dispatch(coroutineContext) { fail("should not be invoked") }
        pausingDP.io.dispatch(coroutineContext) { fail("should not be invoked") }
        pausingDP.main.dispatch(coroutineContext) { fail("should not be invoked") }
        pausingDP.mainImmediate.dispatch(coroutineContext) { fail("should not be invoked") }
        pausingDP.unconfined.dispatch(coroutineContext) { fail("should not be invoked") }

        normalDP.default.dispatchCount.get() shouldBe 0
        normalDP.io.dispatchCount.get() shouldBe 0
        normalDP.main.dispatchCount.get() shouldBe 0
        normalDP.mainImmediate.dispatchCount.get() shouldBe 0
        normalDP.unconfined.dispatchCount.get() shouldBe 0
      }

      "resume should resume all dispatchers" {

        var default = false
        var io = false
        var main = false
        var mainImmediate = false
        var unconfined = false

        pausingDP.pause()

        val jobs = listOf(
          launch(pausingDP.default) { default = true },
          launch(pausingDP.io) { io = true },
          launch(pausingDP.main) { main = true },
          launch(pausingDP.mainImmediate) { mainImmediate = true },
          launch(pausingDP.unconfined) { unconfined = true }
        )

        pausingDP.resume()

        jobs.joinAll()

        normalDP.default.dispatchCount.get() shouldBe 1
        normalDP.io.dispatchCount.get() shouldBe 1
        normalDP.main.dispatchCount.get() shouldBe 1
        normalDP.mainImmediate.dispatchCount.get() shouldBe 1
        normalDP.unconfined.dispatchCount.get() shouldBe 1

        default shouldBe true
        io shouldBe true
        main shouldBe true
        mainImmediate shouldBe true
        unconfined shouldBe true
      }
    }

    "closed" - {

      pausingDP.close()

      "dispatching should not perform action" {

        shouldThrow<Exception> {
          pausingDP.default.dispatch(coroutineContext) { fail("should not be invoked") }
          pausingDP.io.dispatch(coroutineContext) { fail("should not be invoked") }
          pausingDP.main.dispatch(coroutineContext) { fail("should not be invoked") }
          pausingDP.mainImmediate.dispatch(coroutineContext) { fail("should not be invoked") }
          pausingDP.unconfined.dispatch(coroutineContext) { fail("should not be invoked") }
        }

        normalDP.default.dispatchCount.get() shouldBe 0
        normalDP.io.dispatchCount.get() shouldBe 0
        normalDP.main.dispatchCount.get() shouldBe 0
        normalDP.mainImmediate.dispatchCount.get() shouldBe 0
        normalDP.unconfined.dispatchCount.get() shouldBe 0
      }

      "dispatching should throw ClosedPausingDispatcherException" {

        shouldThrow<ClosedPausingDispatcherException> {
          pausingDP.default.dispatch(coroutineContext) { fail("should not be invoked") }
        }

        shouldThrow<ClosedPausingDispatcherException> {
          pausingDP.io.dispatch(coroutineContext) { fail("should not be invoked") }
        }

        shouldThrow<ClosedPausingDispatcherException> {
          pausingDP.main.dispatch(coroutineContext) { fail("should not be invoked") }
        }

        shouldThrow<ClosedPausingDispatcherException> {
          pausingDP.mainImmediate.dispatch(coroutineContext) { fail("should not be invoked") }
        }

        shouldThrow<ClosedPausingDispatcherException> {
          pausingDP.unconfined.dispatch(coroutineContext) { fail("should not be invoked") }
        }
      }
    }

    "canceled CoroutineScope" - {

      testScope.cancel()

      "dispatching should throw ClosedPausingDispatcherException" {

        shouldThrow<ClosedPausingDispatcherException> {
          pausingDP.default.dispatch(coroutineContext) { fail("should not be invoked") }
        }

        shouldThrow<ClosedPausingDispatcherException> {
          pausingDP.io.dispatch(coroutineContext) { fail("should not be invoked") }
        }

        shouldThrow<ClosedPausingDispatcherException> {
          pausingDP.main.dispatch(coroutineContext) { fail("should not be invoked") }
        }

        shouldThrow<ClosedPausingDispatcherException> {
          pausingDP.mainImmediate.dispatch(coroutineContext) { fail("should not be invoked") }
        }

        shouldThrow<ClosedPausingDispatcherException> {
          pausingDP.unconfined.dispatch(coroutineContext) { fail("should not be invoked") }
        }
      }
    }
  })

class FoFoFoTest : HermitJUnit5() {

  @ExperimentalCoroutinesApi
  @OptIn(ExperimentalDispatchApi::class)
  @Test
  fun `fooooooo `() = runBlocking<Unit> {

    val testScope = TestProvidedCoroutineScope(context = Job())

    val normalDP = object : DispatcherProvider {
      override val default = RecordingDispatcher("defaultDelegate")
      override val io = RecordingDispatcher("ioDelegate")
      override val main = RecordingDispatcher("mainDelegate")
      override val mainImmediate = RecordingDispatcher("mainImmediateDelegate")
      override val unconfined = RecordingDispatcher("unconfinedDelegate")
    }

    val pausingDP = PausingDispatcherProvider(testScope, normalDP)

    testScope.cancel()

    pausingDP.default.dispatch(coroutineContext) { fail("should not be invoked") }

    shouldThrow<ClosedPausingDispatcherException> {
      pausingDP.default.dispatch(coroutineContext) { fail("should not be invoked") }
    }

    shouldThrow<ClosedPausingDispatcherException> {
      pausingDP.io.dispatch(coroutineContext) { fail("should not be invoked") }
    }

    shouldThrow<ClosedPausingDispatcherException> {
      pausingDP.main.dispatch(coroutineContext) { fail("should not be invoked") }
    }

    shouldThrow<ClosedPausingDispatcherException> {
      pausingDP.mainImmediate.dispatch(coroutineContext) { fail("should not be invoked") }
    }

    shouldThrow<ClosedPausingDispatcherException> {
      pausingDP.unconfined.dispatch(coroutineContext) { fail("should not be invoked") }
    }
  }
}
