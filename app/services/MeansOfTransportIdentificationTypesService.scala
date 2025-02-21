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

import config.Constants.MeansOfTransportIdentification.*
import config.Constants.ModeOfTransport.*
import connectors.ReferenceDataConnector
import models.reference.InlandMode
import models.reference.transportMeans.departure.Identification
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MeansOfTransportIdentificationTypesService @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def getMeansOfTransportIdentificationTypes(inlandMode: Option[InlandMode])(implicit hc: HeaderCarrier): Future[Seq[Identification]] =
    referenceDataConnector
      .getMeansOfTransportIdentificationTypes()
      .map(_.resolve())
      .map(_.toSeq)
      .map(filter(_, inlandMode))

  private def filter(
    identificationTypes: Seq[Identification],
    inlandMode: Option[InlandMode]
  ): Seq[Identification] =
    inlandMode match {
      case Some(InlandMode(code, _)) if code != Fixed && code != Unknown => identificationTypes.filter(_.code.startsWith(code))
      case _                                                             => identificationTypes.filterNot(_.code == UnknownIdentification)
    }
}
