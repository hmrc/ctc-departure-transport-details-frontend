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

import models.ProcedureType.{Normal, Simplified}
import models.authorisations.AuthorisationType.{ACR, TRD}
import models.domain.{GettableAsReaderOps, UserAnswersReader}
import models.transportMeans.InlandMode.{Air, Maritime, Rail}
import models.{Index, UserAnswers}
import pages.authorisationsAndLimit.authorisations.index.InferredAuthorisationTypePage
import pages.external.{ApprovedOperatorPage, ProcedureTypePage}
import pages.transportMeans.InlandModePage

import javax.inject.Inject

class AuthorisationInferenceService @Inject() () {

  def inferAuthorisations(userAnswers: UserAnswers): Option[UserAnswers] = {

    val reader: UserAnswersReader[Option[UserAnswers]] = for {
      procedureType           <- ProcedureTypePage.reader
      reducedDataSetIndicator <- ApprovedOperatorPage.inferredReader
      inlandMode              <- InlandModePage.reader
    } yield (reducedDataSetIndicator, inlandMode, procedureType) match {
      case (true, Maritime | Rail | Air, Simplified) =>
        userAnswers
          .set(InferredAuthorisationTypePage(Index(0)), TRD)
          .flatMap(_.set(InferredAuthorisationTypePage(Index(1)), ACR))
          .toOption
      case (true, Maritime | Rail | Air, Normal) =>
        userAnswers
          .set(InferredAuthorisationTypePage(Index(0)), TRD)
          .toOption
      case (_, _, Simplified) =>
        userAnswers
          .set(InferredAuthorisationTypePage(Index(0)), ACR)
          .toOption
      case _ => None
    }

    reader.apply(userAnswers).getOrElse(None)
  }
}

// Probably want to return a Try/Option to make sure we have Inferred a value
// From the controller we call this and redirect using the navigator
