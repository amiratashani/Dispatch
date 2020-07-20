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

import dispatch.test.*
import hermit.test.coroutines.*
import hermit.test.junit.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import kotlin.coroutines.*

@CoroutineTest
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
internal class PausingDispatcherTest : HermitJUnit5() {

  val testScope by resetsScope<TestProvidedCoroutineScope>()

  @Test
  fun delegation() = runBlocking<Unit> {

    val dispatchers = List(5) { newSingleThreadContext("single $it") }

    val rd = newSingleThreadContext("root")
    val rd2 = newSingleThreadContext("2")

    val scope = CoroutineScope(rd)

    val root = PausingDispatcher(scope)

    println(root)

//    val out = mutableListOf<String>()

    withContext(root) {
      println("====================")

      withContext(rd2) {
        println("------------------------")
//        root.pause()
        delay(100)
        println(".......................")
      }
      println("99999999999999")
    }
    val job = launch(root) {

      println(" --> ${coroutineContext[ContinuationInterceptor]}")
      delay(100)
      withContext(dispatchers[0]) {
        println(" --> ${coroutineContext[ContinuationInterceptor]}")
        delay(100)
        withContext(dispatchers[1]) {
          println(" --> ${coroutineContext[ContinuationInterceptor]}")

          root.pause()
          delay(100)
          withContext(dispatchers[2]) {
            println(" --> ${coroutineContext[ContinuationInterceptor]}")
            delay(100)
            withContext(dispatchers[3]) {
              println(" --> ${coroutineContext[ContinuationInterceptor]}")
              delay(100)
              withContext(dispatchers[4]) {
                println(" --> ${coroutineContext[ContinuationInterceptor]}")
                delay(100)
                println(" <-- ${coroutineContext[ContinuationInterceptor]}")
              }
              println(" <-- ${coroutineContext[ContinuationInterceptor]}")
            }
            println(" <-- ${coroutineContext[ContinuationInterceptor]}")
          }
          println(" <-- ${coroutineContext[ContinuationInterceptor]}")
        }
        println(" <-- ${coroutineContext[ContinuationInterceptor]}")
      }
      println(" <-- ${coroutineContext[ContinuationInterceptor]}")
    }
    job.join()
  }
}



