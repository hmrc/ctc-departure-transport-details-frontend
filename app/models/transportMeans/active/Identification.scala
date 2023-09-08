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

package models.transportMeans.active

import models.{EnumerableType, Index, Radioable, UserAnswers, WithName}
import pages.transportMeans.BorderModeOfTransportPage

sealed trait Identification extends Radioable[Identification] {
  val borderModeType: Int

  override val messageKeyPrefix: String = Identification.messageKeyPrefix
}

object Identification extends EnumerableType[Identification] {

  case object ImoShipIdNumber extends WithName("imoShipIdNumber") with Identification {
    override val borderModeType: Int = 10
    override val code: String        = borderModeType.toString
  }

  case object SeaGoingVessel extends WithName("seaGoingVessel") with Identification {
    override val borderModeType: Int = 11
    override val code: String        = borderModeType.toString
  }

  case object TrainNumber extends WithName("trainNumber") with Identification {
    override val borderModeType: Int = 21
    override val code: String        = borderModeType.toString
  }

  case object RegNumberRoadVehicle extends WithName("regNumberRoadVehicle") with Identification {
    override val borderModeType: Int = 30
    override val code: String        = borderModeType.toString
  }

  case object IataFlightNumber extends WithName("iataFlightNumber") with Identification {
    override val borderModeType: Int = 40
    override val code: String        = borderModeType.toString
  }

  case object RegNumberAircraft extends WithName("regNumberAircraft") with Identification {
    override val borderModeType: Int = 41
    override val code: String        = borderModeType.toString
  }

  case object EuropeanVesselIdNumber extends WithName("europeanVesselIdNumber") with Identification {
    override val borderModeType: Int = 80
    override val code: String        = borderModeType.toString
  }

  case object InlandWaterwaysVehicle extends WithName("inlandWaterwaysVehicle") with Identification {
    override val borderModeType: Int = 81
    override val code: String        = borderModeType.toString
  }

  val messageKeyPrefix: String = "transportMeans.active.identification"

  val values: Seq[Identification] = Seq(
    ImoShipIdNumber,
    SeaGoingVessel,
    TrainNumber,
    RegNumberRoadVehicle,
    IataFlightNumber,
    RegNumberAircraft,
    EuropeanVesselIdNumber,
    InlandWaterwaysVehicle
  )

  def values(userAnswers: UserAnswers, index: Index): Seq[Identification] =
    if (index.isFirst) {
      userAnswers.get(BorderModeOfTransportPage).map(_.borderModeType) match {
        case Some(borderModeType) =>
          Identification.values.filter(_.borderModeType.toString.startsWith(borderModeType.toString))
        case _ =>
          values
      }
    } else {
      values
    }
}
