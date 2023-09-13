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

package connectors

import config.FrontendAppConfig
import models.reference.supplyChainActors.SupplyChainActorType
import models.reference.transportMeans._
import models.reference.{Country, InlandMode, Nationality}
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.Reads
import sttp.model.HeaderNames
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClient) extends Logging {

  def getCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Country]] = {
    val url = s"${config.referenceDataUrl}/lists/CountryCodesFullList"
    http.GET[Seq[Country]](url, headers = version2Header)
  }

  def getNationalities()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Nationality]] = {
    val url = s"${config.referenceDataUrl}/lists/Nationality"
    http.GET[Seq[Nationality]](url, headers = version2Header)
  }

  def getCountryCodesCommonTransit()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Country]] = {
    val url = s"${config.referenceDataUrl}/lists/CountryCodesCommonTransit"
    http.GET[Seq[Country]](url, headers = version2Header)
  }

  def getTransportModeCodes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[InlandMode]] = {
    val url = s"${config.referenceDataUrl}/lists/TransportModeCode"
    http.GET[Seq[InlandMode]](url, headers = version2Header)
  }

  def getMeansOfTransportIdentificationTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[departure.Identification]] = {
    val url = s"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport"
    http.GET[Seq[departure.Identification]](url, headers = version2Header)
  }

  def getMeansOfTransportIdentificationTypesActive()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[active.Identification]] = {
    val url = s"${config.referenceDataUrl}/lists/TypeOfIdentificationofMeansOfTransportActive"
    http.GET[Seq[active.Identification]](url, headers = version2Header)
  }

  def getSupplyChainActorTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[SupplyChainActorType]] = {
    val url = s"${config.referenceDataUrl}/lists/AdditionalSupplyChainActorRoleCode"
    http.GET[Seq[SupplyChainActorType]](url, headers = version2Header)
  }

  private def version2Header: Seq[(String, String)] = Seq(
    HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"
  )

  implicit def responseHandlerGeneric[A](implicit reads: Reads[A]): HttpReads[Seq[A]] =
    (_: String, _: String, response: HttpResponse) => {
      response.status match {
        case OK =>
          (response.json \ "data").validate[Seq[A]].getOrElse {
            throw new IllegalStateException("[ReferenceDataConnector][responseHandlerGeneric] Reference data could not be parsed")
          }
        case NO_CONTENT =>
          Nil
        case NOT_FOUND =>
          logger.warn("[ReferenceDataConnector][responseHandlerGeneric] Reference data call returned NOT_FOUND")
          throw new IllegalStateException("[ReferenceDataConnector][responseHandlerGeneric] Reference data could not be found")
        case other =>
          logger.warn(s"[ReferenceDataConnector][responseHandlerGeneric] Invalid downstream status $other")
          throw new IllegalStateException(s"[ReferenceDataConnector][responseHandlerGeneric] Invalid Downstream Status $other")
      }
    }
}
