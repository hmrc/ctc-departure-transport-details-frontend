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
import config.Constants.DeclarationType._
import models.OptionalBoolean
import models.domain._
import models.reference.Country
import pages.external.DeclarationTypePage
import pages.preRequisites._

case class PreRequisitesDomain(
  ucr: Option[String],
  countryOfDispatch: Option[Country],
  itemsDestinationCountry: Option[Country],
  containerIndicator: OptionalBoolean
) extends JourneyDomainModel

object PreRequisitesDomain {

  implicit val countryOfDispatchReader: Read[Option[Country]] =
    DeclarationTypePage.reader.apply(_).flatMap {
      case ReaderSuccess(TIR, pages) =>
        SameCountryOfDispatchYesNoPage.filterOptionalDependent(identity)(CountryOfDispatchPage.reader).apply(pages)
      case ReaderSuccess(_, pages) =>
        UserAnswersReader.none.apply(pages)
    }

  implicit val userAnswersReader: Read[PreRequisitesDomain] =
    (
      SameUcrYesNoPage.filterOptionalDependent(identity)(UniqueConsignmentReferencePage.reader),
      countryOfDispatchReader,
      TransportedToSameCountryYesNoPage.filterOptionalDependent(identity)(ItemsDestinationCountryPage.reader),
      ContainerIndicatorPage.reader
    ).map(PreRequisitesDomain.apply)
}
