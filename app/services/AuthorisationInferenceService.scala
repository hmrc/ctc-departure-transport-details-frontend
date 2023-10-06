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
import models.domain.{GettableAsReaderOps, UserAnswersReader}
import models.reference.authorisations.AuthorisationType
import models.{Index, UserAnswers}
import pages.authorisationsAndLimit.authorisations.index.InferredAuthorisationTypePage
import pages.external.{ApprovedOperatorPage, ProcedureTypePage}
import pages.transportMeans.InlandModePage

import javax.inject.Inject

class AuthorisationInferenceService @Inject() () {

  def inferAuthorisations(userAnswers: UserAnswers, authorisationTypes: Seq[AuthorisationType]): UserAnswers = {

    val authTypeACR = authorisationTypes.find(_.isACR)
    val authTypeTRD = authorisationTypes.find(_.isTRD)

    val reader: UserAnswersReader[Option[UserAnswers]] = for {
      procedureType           <- ProcedureTypePage.reader
      reducedDataSetIndicator <- ApprovedOperatorPage.inferredReader
      inlandMode              <- InlandModePage.reader
    } yield (reducedDataSetIndicator, inlandMode.code, procedureType) match {
      case (true, Maritime | Rail | Air, Simplified) =>
        (authTypeACR, authTypeTRD) match {
          case (Some(acr), Some(trd)) =>
            userAnswers
              .set(InferredAuthorisationTypePage(Index(0)), trd)
              .flatMap(_.set(InferredAuthorisationTypePage(Index(1)), acr))
              .toOption
          case _ => None
        }
      case (true, Maritime | Rail | Air, Normal) =>
        authTypeTRD match {
          case Some(trd) =>
            userAnswers
              .set(InferredAuthorisationTypePage(Index(0)), trd)
              .toOption
          case _ => None
        }
      case (_, _, Simplified) =>
        authTypeACR match {
          case Some(acr) =>
            userAnswers
              .set(InferredAuthorisationTypePage(Index(0)), acr)
              .toOption
          case _ => None
        }
      case _ =>
        Some(userAnswers)
    }

    reader.apply(userAnswers) match {
      case Right(Some(value)) => value
      case _                  => userAnswers
    }
  }
}
