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

package dispatch.internal.test

import io.kotest.matchers.*
import org.junit.rules.*
import org.junit.runner.*
import org.junit.runners.model.*

class ExpectedFailureRule : TestRule {

  override fun apply(
    base: Statement,
    description: Description
  ) = object : Statement() {

    override fun evaluate() {
      try {
        base.evaluate()
      } catch (e: Error) {
        val failsAnnotation = description.getAnnotation(Fails::class.java)

        if (failsAnnotation != null) {
          failsAnnotation.expected shouldBe e::class
        } else {
          throw e
        }
      }
    }
  }
}
