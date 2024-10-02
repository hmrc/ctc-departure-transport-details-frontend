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

package services

import cats.data.NonEmptySet
import models.ProcedureType._
import models.journeyDomain.{ReaderSuccess, _}
import models.reference.authorisations.AuthorisationType
import models.{Index, UserAnswers}
import pages.authorisationsAndLimit.authorisations.index.{InferredAuthorisationTypePage, IsMandatoryPage}
import pages.external.{ApprovedOperatorPage, ProcedureTypePage}

import javax.inject.Inject

class AuthorisationInferenceService @Inject() () {

  def inferAuthorisations(userAnswers: UserAnswers, authorisationTypes: NonEmptySet[AuthorisationType]): UserAnswers = {

    lazy val authTypeACR = authorisationTypes.find(_.isACR)
    lazy val authTypeTRD = authorisationTypes.find(_.isTRD)

    val reader: UserAnswersReader[Option[UserAnswers]] = {
      def updateUserAnswers(authorisationTypes: Option[AuthorisationType]*): Option[UserAnswers] =
        authorisationTypes.flatten.zipWithIndex.foldLeft(Option(userAnswers)) {
          case (acc, (authorisationType, i)) =>
            acc
              .flatMap(_.set(InferredAuthorisationTypePage(Index(i)), authorisationType).toOption)
              .flatMap(_.set(IsMandatoryPage(Index(i)), true).toOption)
        }

      (
        ProcedureTypePage.reader,
        ApprovedOperatorPage.inferredReader
      ).to {
        case (procedureType, reducedDataset) =>
          UserAnswersReader.success {
            (procedureType, reducedDataset) match {
              case (Simplified, false) =>
                updateUserAnswers(authTypeACR)
              case (Simplified, true) =>
                updateUserAnswers(authTypeACR, authTypeTRD)
              case _ =>
                Some(userAnswers)
            }
          }
      }.apply(Nil)
    }

    reader.apply(userAnswers) match {
      case Right(ReaderSuccess(Some(value), _)) => value
      case _                                    => userAnswers
    }
  }
}
