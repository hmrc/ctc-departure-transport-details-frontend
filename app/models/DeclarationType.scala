/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import play.api.libs.json.{Format, Json}

sealed trait DeclarationType

object DeclarationType extends Enumeration {

  type DeclarationType = Value

  val T1: DeclarationType  = Value("T1")
  val T2: DeclarationType  = Value("T2")
  val T2F: DeclarationType = Value("T2F")
  val TIR: DeclarationType = Value("TIR")
  val T: DeclarationType   = Value("T")

  implicit val format: Format[DeclarationType] = Json.formatEnum(this)
}
