/*
 * Copyright (C) 2021 Rick Busarow
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

plugins {
  `kotlin-dsl`
}

repositories {
  google()
  jcenter()
  maven("https://oss.sonatype.org/content/repositories/snapshots")
}

kotlinDslPluginOptions {
  experimentalWarning.set(false)
}

dependencies {

  compileOnly(gradleApi())

  implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.4.30") // update Dependencies.kt as well
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.30") // update Dependencies.kt as well
  implementation("com.android.tools.build:gradle:4.1.2") // update Dependencies.kt as well
}
