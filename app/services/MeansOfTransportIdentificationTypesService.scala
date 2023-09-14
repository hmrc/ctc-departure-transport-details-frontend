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

import config.Constants.Fixed
import connectors.ReferenceDataConnector
import models.reference.InlandMode
import models.reference.transportMeans.departure.Identification
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MeansOfTransportIdentificationTypesService @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def getMeansOfTransportIdentificationTypes(inlandMode: InlandMode)(implicit hc: HeaderCarrier): Future[Seq[Identification]] =
    referenceDataConnector.getMeansOfTransportIdentificationTypes().map(filter(_, inlandMode)).map(sort)

  private def filter(
    identificationTypes: Seq[Identification],
    inlandMode: InlandMode
  ): Seq[Identification] =
    inlandMode.code match {
      case inlandModeCode if inlandModeCode != Fixed => identificationTypes.filter(_.code.startsWith(inlandModeCode))
      case _                                         => identificationTypes
    }

  private def sort(identificationTypes: Seq[Identification]): Seq[Identification] =
    identificationTypes.sortBy(_.code.toLowerCase)
}