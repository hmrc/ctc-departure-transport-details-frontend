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

package models.journeyDomain.authorisationsAndLimit.authorisations

import models.journeyDomain._
import models.journeyDomain.JourneyDomainModel
import models.{Index, RichJsArray, UserAnswers}
import pages.sections.Section
import pages.sections.authorisationsAndLimit.AuthorisationsSection

case class AuthorisationsDomain(authorisations: Seq[AuthorisationDomain]) extends JourneyDomainModel {

  override def page(userAnswers: UserAnswers): Option[Section[_]] = Some(AuthorisationsSection)
}

object AuthorisationsDomain {

  implicit val userAnswersReader: Read[AuthorisationsDomain] = {

    val authorisationsReader: Read[Seq[AuthorisationDomain]] =
      AuthorisationsSection.arrayReader.to {
        case x if x.isEmpty =>
          AuthorisationDomain.userAnswersReader(Index(0)).toSeq
        case x =>
          x.traverse[AuthorisationDomain](AuthorisationDomain.userAnswersReader(_).apply(_))
      }

    authorisationsReader.map(AuthorisationsDomain.apply)
  }
}
