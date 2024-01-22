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

import config.Constants.AdditionalDeclarationType._
import models.domain._
import models.journeyDomain.JourneyDomainModel
import models.journeyDomain.authorisationsAndLimit.limit.LimitDomain
import pages.external.AdditionalDeclarationTypePage

case class AuthorisationsAndLimitDomain(
  authorisationsDomain: AuthorisationsDomain,
  limitDomain: Option[LimitDomain]
) extends JourneyDomainModel

object AuthorisationsAndLimitDomain {

  def limitReader(authDomain: AuthorisationsDomain): Read[Option[LimitDomain]] = {
    lazy val anyAuthTypeIsC521 = authDomain.authorisations.exists(_.authorisationType.isACR)
    AdditionalDeclarationTypePage.reader.to {
      case Standard if anyAuthTypeIsC521 => LimitDomain.userAnswersReader.toOption
      case _                             => UserAnswersReader.none
    }
  }

  implicit val userAnswersReader: Read[AuthorisationsAndLimitDomain] =
    AuthorisationsDomain.userAnswersReader.to {
      authorisations =>
        (
          Read(authorisations),
          limitReader(authorisations)
        ).map(AuthorisationsAndLimitDomain.apply)
    }
}
