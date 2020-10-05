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
import dispatch.test.*
import hermit.test.junit.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.*

@CoroutineTest
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
internal class PausingDispatcherTest : HermitJUnit5() {

//  val testScope by resetsScope<TestProvidedCoroutineScope>()

  @Test
  fun delegation() = runBlocking<Unit> {

    val dispatchers = List(5) { newSingleThreadContext("single $it") }

    val tdp = object : DispatcherProvider {
      override val default = dispatchers[0]
      override val io = dispatchers[1]
      override val main = dispatchers[2]
      override val mainImmediate = dispatchers[3]
      override val unconfined = dispatchers[4]
    }

    val alien = newSingleThreadContext("alien")

    val scope = CoroutineScope(tdp).pausing()

    launch {
      repeat(10) {
        delay(1000)
        println("going to resume")
        scope.resume()
        println("resumed")
      }
    }

//    val out = mutableListOf<String>()

    val job = scope.launch {

      println("1 --> ${Thread.currentThread()}")
      delay(100)

      withDefault {

        println("2 --> ${Thread.currentThread()}")

        println("going to pause")
        scope.pause()
        println("paused")
        delay(100)

        withContext(alien) {

          println("3 --> ${Thread.currentThread()}")

          println("going to pause")
          scope.pause()
          println("paused")

          delay(100)

          withMain {

            println("4 --> ${Thread.currentThread()}")

            println("going to pause")
            scope.pause()
            println("paused")
            delay(100)

            withMainImmediate {

              println("5 --> ${Thread.currentThread()}")

              println("going to pause")
              scope.pause()
              println("paused")
              delay(100)

              withUnconfined {

                println("6 --> ${Thread.currentThread()}")

                println("going to pause")
                scope.pause()
                println("paused")
                delay(100)

                println(" <-- ${Thread.currentThread()}")
              }
              println(" <-- ${Thread.currentThread()}")
            }
            println(" <-- ${Thread.currentThread()}")
          }
          println(" <-- ${Thread.currentThread()}")
        }
        println(" <-- ${Thread.currentThread()}")
      }
      println(" <-- ${Thread.currentThread()}")
    }
    job.join()
  }
}



