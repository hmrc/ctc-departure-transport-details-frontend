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

import cats.implicits._
import models.DeclarationType.Option4
import models.domain.{UserAnswersReader, _}
import models.reference.Country
import pages.external.DeclarationTypePage
import pages.preRequisites._

case class PreRequisitesDomain(
  ucr: Option[String],
  countryOfDispatch: Option[Country],
  itemsDestinationCountry: Option[Country],
  containerIndicator: Boolean
)

object PreRequisitesDomain {

  implicit val countryOfDispatchReader: UserAnswersReader[Option[Country]] =
    DeclarationTypePage.reader.flatMap {
      case Option4 => SameCountryOfDispatchYesNoPage.filterOptionalDependent(identity)(CountryOfDispatchPage.reader)
      case _       => none[Country].pure[UserAnswersReader]
    }

  implicit val userAnswersReader: UserAnswersReader[PreRequisitesDomain] = (
    SameUcrYesNoPage.filterOptionalDependent(identity)(UniqueConsignmentReferencePage.reader),
    countryOfDispatchReader,
    TransportedToSameCountryYesNoPage.filterOptionalDependent(identity)(ItemsDestinationCountryPage.reader),
    ContainerIndicatorPage.reader
  ).tupled.map((PreRequisitesDomain.apply _).tupled)
}
