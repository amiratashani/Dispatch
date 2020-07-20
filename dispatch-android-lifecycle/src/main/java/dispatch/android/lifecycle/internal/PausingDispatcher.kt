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
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*

@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
internal class PausingDispatcher(
  private val coroutineScope: CoroutineScope,
  private val paused: MutableStateFlow<Boolean> = MutableStateFlow(false),
  private val delegate: CoroutineContext? = coroutineScope.coroutineContext[ContinuationInterceptor] as? CoroutineDispatcher
) : CoroutineDispatcher() {

  private var finished: Boolean = false
  private var working: Boolean = false

  val a = coroutineScope.actor<Runnable>(capacity = Channel.UNLIMITED) {
    for (runnable in channel) {

      if (paused.value) {
        paused.takeWhile { shouldWait -> shouldWait }
          .collect()
      }

      working = true
      println(this)
      runnable.run()

      working = false
    }
  }

  fun resume() {
    if (!finished) {
      paused.value = false
    }
  }

  fun pause() {
    paused.value = true
  }

  fun finish() {
    a.close()
  }

//  override fun isDispatchNeeded(context: CoroutineContext): Boolean {
//
//    return true
//
////    if (context === coroutineScope.coroutineContext) {
////      return false
////    }
////
////    val old = delegate
////    val new = context.replacePausingWithDelegate()[ContinuationInterceptor]
////
////    println("""~~~~~~~~~~~~~~~~~~~~~~~~
////      |$old
////      |$new
////    """.trimMargin())
////
////    return context.replacePausingWithDelegate()[ContinuationInterceptor] != coroutineScope.coroutineContext.replacePausingWithDelegate()[ContinuationInterceptor]
//
////    println(1)
////    val newDelegate =
////      (context[ContinuationInterceptor] as? PausingDispatcher)?.delegate ?: return true
////    println(
////      """2 -->
////      |       $newDelegate
////      |       $delegate""".trimMargin()
////    )
////    return newDelegate != delegate
//  }

  override fun dispatch(context: CoroutineContext, block: Runnable) {

    val needed = isDispatchNeeded(context)

    println("needed --> $needed")


    if (needed) {

      copy(context).a.sendBlocking(block)
    } else {
      a.sendBlocking(block)
    }
  }

  private fun copy(
    context: CoroutineContext
  ): PausingDispatcher = PausingDispatcher(
    coroutineScope = coroutineScope,
    paused = paused,
    delegate = context
  )
}
