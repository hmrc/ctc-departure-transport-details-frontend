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

package models.transportMeans.departure

import config.Constants.Fixed
import models.{EnumerableType, Radioable, UserAnswers, WithName}
import pages.transportMeans.InlandModePage

sealed trait Identification extends Radioable[Identification] {
  val identificationType: Int

  override val messageKeyPrefix: String = Identification.messageKeyPrefix
}

object Identification extends EnumerableType[Identification] {

  case object ImoShipIdNumber extends WithName("imoShipIdNumber") with Identification {
    override val identificationType: Int = 10
    override val code: String            = identificationType.toString
  }

  case object SeaGoingVessel extends WithName("seaGoingVessel") with Identification {
    override val identificationType: Int = 11
    override val code: String            = identificationType.toString
  }

  case object WagonNumber extends WithName("wagonNumber") with Identification {
    override val identificationType: Int = 20
    override val code: String            = identificationType.toString
  }

  case object TrainNumber extends WithName("trainNumber") with Identification {
    override val identificationType: Int = 21
    override val code: String            = identificationType.toString
  }

  case object RegNumberRoadVehicle extends WithName("regNumberRoadVehicle") with Identification {
    override val identificationType: Int = 30
    override val code: String            = identificationType.toString
  }

  case object RegNumberRoadTrailer extends WithName("regNumberRoadTrailer") with Identification {
    override val identificationType: Int = 31
    override val code: String            = identificationType.toString
  }

  case object IataFlightNumber extends WithName("iataFlightNumber") with Identification {
    override val identificationType: Int = 40
    override val code: String            = identificationType.toString
  }

  case object RegNumberAircraft extends WithName("regNumberAircraft") with Identification {
    override val identificationType: Int = 41
    override val code: String            = identificationType.toString
  }

  case object EuropeanVesselIdNumber extends WithName("europeanVesselIdNumber") with Identification {
    override val identificationType: Int = 80
    override val code: String            = identificationType.toString
  }

  case object InlandWaterwaysVehicle extends WithName("inlandWaterwaysVehicle") with Identification {
    override val identificationType: Int = 81
    override val code: String            = identificationType.toString
  }

  val messageKeyPrefix: String = "transportMeans.departure.identification"

  val values: Seq[Identification] = Seq(
    SeaGoingVessel,
    IataFlightNumber,
    InlandWaterwaysVehicle,
    ImoShipIdNumber,
    WagonNumber,
    TrainNumber,
    RegNumberRoadVehicle,
    RegNumberRoadTrailer,
    RegNumberAircraft,
    EuropeanVesselIdNumber
  )

  def values(userAnswers: UserAnswers): Seq[Identification] =
    userAnswers.get(InlandModePage).map(_.code) match {
      case Some(inlandModeCode) if inlandModeCode != Fixed => values.filter(_.identificationType.toString.startsWith(inlandModeCode))
      case _                                               => values
    }
}
