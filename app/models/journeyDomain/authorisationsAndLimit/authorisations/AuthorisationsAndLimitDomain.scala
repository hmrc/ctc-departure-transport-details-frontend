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

import cats.implicits.{catsSyntaxApplicativeId, none}
import config.Constants.{`PRE-LODGE`, STANDARD}
import models.authorisations.AuthorisationType
import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.JourneyDomainModel
import models.journeyDomain.authorisationsAndLimit.limit.LimitDomain
import pages.authorisationsAndLimit.authorisations.AddLimitDateYesNoPage
import pages.external.AdditionalDeclarationTypePage

case class AuthorisationsAndLimitDomain(authorisationsDomain: AuthorisationsDomain, limitDomain: Option[LimitDomain]) extends JourneyDomainModel

object AuthorisationsAndLimitDomain {

  def limitReader(authDomain: AuthorisationsDomain): UserAnswersReader[Option[LimitDomain]] = {
    lazy val anyAuthTypeIsC521 = authDomain.authorisations.exists(_.authorisationType == AuthorisationType.ACR)
    AdditionalDeclarationTypePage.reader.flatMap {
      case `PRE-LODGE` if anyAuthTypeIsC521 => AddLimitDateYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[LimitDomain])
      case STANDARD if anyAuthTypeIsC521    => UserAnswersReader[LimitDomain].map(Some(_))
      case _                                => none[LimitDomain].pure[UserAnswersReader]
    }
  }

  implicit val userAnswersReader: UserAnswersReader[AuthorisationsAndLimitDomain] = {
    for {
      authorisations <- UserAnswersReader[AuthorisationsDomain]
      limit          <- limitReader(authorisations)
    } yield AuthorisationsAndLimitDomain(authorisations, limit)
  }
}
