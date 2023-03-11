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

import models.ProcedureType.Simplified
import models.authorisations.AuthorisationType
import models.domain.GettableAsReaderOps
import models.transportMeans.departure.InlandMode.{Air, Maritime, Rail}
import models.transportMeans.{active, BorderModeOfTransport}
import models.{Index, UserAnswers}
import pages.external.{ApprovedOperatorPage, ProcedureTypePage}
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.departure.InlandModePage

class InferenceService {

  def inferActiveIdentifier(userAnswers: UserAnswers, index: Index): Option[active.Identification] =
    if (index.isFirst) {
      userAnswers.get(BorderModeOfTransportPage) match {
        case Some(BorderModeOfTransport.ChannelTunnel)     => Some(active.Identification.TrainNumber)
        case Some(BorderModeOfTransport.IrishLandBoundary) => Some(active.Identification.RegNumberRoadVehicle)
        case _                                             => None
      }
    } else {
      None
    }

  def inferAuthorisationType(userAnswers: UserAnswers, index: Index): Option[AuthorisationType] =
    if (index.isFirst) {
      val reader = for {
        procedureType           <- ProcedureTypePage.reader
        reducedDataSetIndicator <- ApprovedOperatorPage.inferredReader
        inlandMode              <- InlandModePage.reader
      } yield (reducedDataSetIndicator, inlandMode, procedureType) match {
        case (true, Maritime | Rail | Air, _) => Some(AuthorisationType.TRD)
        case (true, _, Simplified)            => Some(AuthorisationType.ACR)
        case _                                => None
      }
      reader.run(userAnswers).toOption.flatten
    } else {
      None
    }
}
