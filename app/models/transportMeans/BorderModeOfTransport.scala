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

package models.transportMeans

import models.{EnumerableType, Radioable, WithName}

// TODO - delete
sealed trait BorderModeOfTransport extends Radioable[BorderModeOfTransport] {
  val borderModeType: Int
  override val code: String = this.toString

  override val messageKeyPrefix: String = BorderModeOfTransport.messageKeyPrefix
}

object BorderModeOfTransport extends EnumerableType[BorderModeOfTransport] {

  case object Sea extends WithName("maritime") with BorderModeOfTransport {
    override val borderModeType: Int = 1
  }

  case object ChannelTunnel extends WithName("rail") with BorderModeOfTransport {
    override val borderModeType: Int = 2
  }

  case object IrishLandBoundary extends WithName("road") with BorderModeOfTransport {
    override val borderModeType: Int = 3
  }

  case object Air extends WithName("air") with BorderModeOfTransport {
    override val borderModeType: Int = 4
  }

  val messageKeyPrefix: String = "transportMeans.borderModeOfTransport"

  val values: Seq[BorderModeOfTransport] = Seq(
    Sea,
    Air,
    ChannelTunnel,
    IrishLandBoundary
  )
}
