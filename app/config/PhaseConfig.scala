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

package config

import models.Phase
import models.Phase.{PostTransition, Transition}
import play.api.Configuration

import javax.inject.Inject

trait PhaseConfig {
  val phase: Phase

  def amendMessageKey(key: String): String

  val maxIdentificationNumberLength: Int

  val areB1892AndB1897Disabled: Boolean

  def lengthError(prefix: String): String = amendMessageKey(s"$prefix.error.length")

  def activeIdentificationNumberHint: String    = amendMessageKey("transportMeans.active.identificationNumber.hint")
  def departureIdentificationNumberHint: String = amendMessageKey("transportMeans.departure.meansIdentificationNumber.hint")
}

class TransitionConfig @Inject() (configuration: Configuration) extends PhaseConfig {
  override val phase: Phase = Transition

  override def amendMessageKey(key: String): String = s"$key.transition"

  override val maxIdentificationNumberLength: Int = 27

  override val areB1892AndB1897Disabled: Boolean = configuration.get[Boolean]("flag.areB1892AndB1897Disabled")

}

class PostTransitionConfig() extends PhaseConfig {
  override val phase: Phase = PostTransition

  override def amendMessageKey(key: String): String = s"$key.postTransition"

  override val maxIdentificationNumberLength: Int = 35

  override val areB1892AndB1897Disabled: Boolean = false
}
