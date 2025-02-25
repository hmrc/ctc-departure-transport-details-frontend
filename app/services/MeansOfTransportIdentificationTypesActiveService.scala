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

import config.Constants.MeansOfTransportIdentification.UnknownIdentification
import connectors.ReferenceDataConnector
import models.Index
import models.reference.BorderMode
import models.reference.transportMeans.active.Identification
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MeansOfTransportIdentificationTypesActiveService @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def getMeansOfTransportIdentificationTypesActive(index: Index, borderModeOfTransport: Option[BorderMode])(implicit
    hc: HeaderCarrier
  ): Future[Seq[Identification]] =
    referenceDataConnector
      .getMeansOfTransportIdentificationTypesActive()
      .map(_.resolve())
      .map(_.toSeq)
      .map(filter(_, index, borderModeOfTransport))

  private def filter(
    identificationTypes: Seq[Identification],
    index: Index,
    borderModeOfTransport: Option[BorderMode]
  ): Seq[Identification] = {
    val identificationTypesExcludingUnknown = identificationTypes.filterNot(_.code == UnknownIdentification)

    borderModeOfTransport match {
      case Some(borderMode) if index.isFirst =>
        identificationTypesExcludingUnknown.filter(_.code.startsWith(borderMode.code))
      case _ => identificationTypesExcludingUnknown
    }
  }
}
