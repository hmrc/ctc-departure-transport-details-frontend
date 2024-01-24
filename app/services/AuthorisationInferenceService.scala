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

import config.Constants.ModeOfTransport._
import models.ProcedureType.{Normal, Simplified}
import models.domain._
import models.journeyDomain.ReaderSuccess
import models.reference.authorisations.AuthorisationType
import models.{Index, UserAnswers}
import pages.authorisationsAndLimit.authorisations.index.InferredAuthorisationTypePage
import pages.external.{ApprovedOperatorPage, ProcedureTypePage}
import pages.transportMeans.InlandModePage

import javax.inject.Inject

class AuthorisationInferenceService @Inject() () {

  def inferAuthorisations(userAnswers: UserAnswers, authorisationTypes: Seq[AuthorisationType]): UserAnswers = {

    lazy val authTypeACR = authorisationTypes.find(_.isACR)
    lazy val authTypeTRD = authorisationTypes.find(_.isTRD)

    val reader: UserAnswersReader[Option[UserAnswers]] =
      (
        ProcedureTypePage.reader,
        ApprovedOperatorPage.inferredReader,
        InlandModePage.optionalReader.apply(_: Pages).map(_.to(_.map(_.code)))
      ).to {
        case (procedureType, reducedDataset, inlandMode) =>
          val foo = (procedureType, reducedDataset, inlandMode) match {
            case (Simplified, _, _) =>
              authTypeACR.flatMap(userAnswers.set(InferredAuthorisationTypePage(Index(0)), _).toOption)
            case (Normal, true, Some(Maritime | Rail | Air)) =>
              authTypeTRD.flatMap(userAnswers.set(InferredAuthorisationTypePage(Index(0)), _).toOption)
            case _ =>
              Some(userAnswers)
          }
          UserAnswersReader.success(foo)
      }.apply(Nil)

    reader.apply(userAnswers) match {
      case Right(ReaderSuccess(Some(value), _)) => value
      case _                                    => userAnswers
    }
  }
}
