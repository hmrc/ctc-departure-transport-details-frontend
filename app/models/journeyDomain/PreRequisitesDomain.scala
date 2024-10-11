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

package models.journeyDomain

import config.Constants.DeclarationType.*
import config.PhaseConfig
import models.journeyDomain.*
import models.reference.Country
import models.{OptionalBoolean, Phase}
import pages.external.DeclarationTypePage
import pages.preRequisites.*

case class PreRequisitesDomain(
  ucr: Option[String],
  countryOfDispatch: Option[Country],
  countryOfDestination: Option[OptionalBoolean],
  itemsDestinationCountry: Option[Country],
  containerIndicator: OptionalBoolean
) extends JourneyDomainModel

object PreRequisitesDomain {

  implicit def countryOfDispatchReader(implicit phaseConfig: PhaseConfig): Read[Option[Country]] =
    phaseConfig.phase match {
      case Phase.Transition =>
        DeclarationTypePage.reader.to {
          case TIR =>
            SameCountryOfDispatchYesNoPage.filterOptionalDependent(identity)(CountryOfDispatchPage.reader)
          case _ =>
            UserAnswersReader.none
        }
      case Phase.PostTransition => SameCountryOfDispatchYesNoPage.filterOptionalDependent(identity)(CountryOfDispatchPage.reader)
    }

  implicit def countryOfDestinationReader(implicit phaseConfig: PhaseConfig): Read[Option[OptionalBoolean]] =
    phaseConfig.phase match {
      case Phase.PostTransition =>
        SameCountryOfDispatchYesNoPage.filterOptionalDependent(!_)(AddCountryOfDestinationPage.reader)
      case Phase.Transition => UserAnswersReader.none
    }

  implicit def itemsDestinationCountryReader(implicit phaseConfig: PhaseConfig): Read[Option[Country]] =
    phaseConfig.phase match {
      case Phase.PostTransition =>
        SameCountryOfDispatchYesNoPage.reader.to {
          case true => TransportedToSameCountryYesNoPage.filterOptionalDependent(identity)(ItemsDestinationCountryPage.reader)
          case false =>
            AddCountryOfDestinationPage.optionalReader.to {
              case Some(OptionalBoolean.yes) => TransportedToSameCountryYesNoPage.filterOptionalDependent(identity)(ItemsDestinationCountryPage.reader)
              case _                         => UserAnswersReader.none
            }
        }
      case Phase.Transition => TransportedToSameCountryYesNoPage.filterOptionalDependent(identity)(ItemsDestinationCountryPage.reader)
    }

  implicit def userAnswersReader(phaseConfig: PhaseConfig): Read[PreRequisitesDomain] =
    (
      SameUcrYesNoPage.filterOptionalDependent(identity)(UniqueConsignmentReferencePage.reader),
      countryOfDispatchReader(phaseConfig),
      countryOfDestinationReader(phaseConfig),
      itemsDestinationCountryReader(phaseConfig),
      ContainerIndicatorPage.reader
    ).map(PreRequisitesDomain.apply)
}
