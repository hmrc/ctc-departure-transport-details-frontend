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
import models.OptionalBoolean
import models.journeyDomain.*
import models.reference.Country
import pages.preRequisites.*

case class PreRequisitesDomain(
  ucr: Option[String],
  countryOfDispatch: Option[Country],
  countryOfDestination: Option[Country],
  containerIndicator: OptionalBoolean
) extends JourneyDomainModel

object PreRequisitesDomain {

  implicit def countryOfDispatchReader(): Read[Option[Country]] = SameCountryOfDispatchYesNoPage.filterOptionalDependent(identity)(CountryOfDispatchPage.reader)

  implicit def countryOfDestinationReader(): Read[Option[Country]] =
    AddCountryOfDestinationPage.reader.to {
      case OptionalBoolean.yes => TransportedToSameCountryYesNoPage.filterOptionalDependent(identity)(ItemsDestinationCountryPage.reader)
      case _                   => UserAnswersReader.none
    }

  implicit val userAnswersReader: Read[PreRequisitesDomain] =
    (
      SameUcrYesNoPage.filterOptionalDependent(identity)(UniqueConsignmentReferencePage.reader),
      countryOfDispatchReader(),
      countryOfDestinationReader(),
      ContainerIndicatorPage.reader
    ).map(PreRequisitesDomain.apply)
}
