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

import cats.Order
import cats.data.NonEmptySet
import config.FrontendAppConfig
import connectors.ReferenceDataConnector.{NoReferenceDataFoundException, Response, Responses}
import models.reference.additionalInformation.AdditionalInformationCode
import models.reference.additionalReference.AdditionalReferenceType
import models.reference.authorisations.AuthorisationType
import models.reference.equipment.PaymentMethod
import models.reference.supplyChainActors.SupplyChainActorType
import models.reference.transportMeans.*
import models.reference.{Country, ModeOfTransport, Nationality}
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.*
import sttp.model.HeaderNames
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClientV2) extends Logging {

  private def get[T](url: URL)(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[Responses[T]]): Future[Responses[T]] =
    http
      .get(url)
      .setHeader(version1Header*)
      .execute[Responses[T]]

  def getCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[Country]] = {
    val url = url"${config.referenceDataUrl}/lists/CountryCodesFullList"
    get[Country](url)
  }

  def getNationalities()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[Nationality]] = {
    val url = url"${config.referenceDataUrl}/lists/Nationality"
    get[Nationality](url)
  }

  def getCountryCodesCommonTransitCountry(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[Country]] = {
    val queryParameters = Seq("data.code" -> code)
    val url             = url"${config.referenceDataUrl}/lists/CountryCodesCommonTransit?$queryParameters"
    get[Country](url)
      .map(_.map(_.head))

  }

  def getTransportModeCodes[T <: ModeOfTransport[T]]()(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier,
    reads: Reads[T],
    order: Order[T]
  ): Future[Responses[T]] = {
    val url = url"${config.referenceDataUrl}/lists/TransportModeCode"
    get[T](url)
  }

  def getMeansOfTransportIdentificationTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[departure.Identification]] = {
    val url = url"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport"
    get[departure.Identification](url)
  }

  def getMeansOfTransportIdentificationTypesActive()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[active.Identification]] = {
    val url = url"${config.referenceDataUrl}/lists/TypeOfIdentificationofMeansOfTransportActive"
    get[active.Identification](url)
  }

  def getSupplyChainActorTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[SupplyChainActorType]] = {
    val url = url"${config.referenceDataUrl}/lists/AdditionalSupplyChainActorRoleCode"
    get[SupplyChainActorType](url)
  }

  def getAuthorisationTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[AuthorisationType]] = {
    val url = url"${config.referenceDataUrl}/lists/AuthorisationTypeDeparture"
    get[AuthorisationType](url)
  }

  def getPaymentMethods()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[PaymentMethod]] = {
    val url = url"${config.referenceDataUrl}/lists/TransportChargesMethodOfPayment"
    get[PaymentMethod](url)
  }

  def getAdditionalReferences()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[AdditionalReferenceType]] = {
    val url = url"${config.referenceDataUrl}/lists/AdditionalReference"
    get[AdditionalReferenceType](url)
  }

  def getAdditionalInformationCodes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[AdditionalInformationCode]] = {
    val url = url"${config.referenceDataUrl}/lists/AdditionalInformation"
    get[AdditionalInformationCode](url)

  }

  private def version1Header: Seq[(String, String)] = Seq(
    HeaderNames.Accept -> "application/vnd.hmrc.1.0+json"
  )

  implicit def responseHandlerGeneric[A](implicit reads: Reads[A], order: Order[A]): HttpReads[Either[Exception, NonEmptySet[A]]] =
    (_: String, url: String, response: HttpResponse) =>
      response.status match {
        case OK =>
          (response.json \ "data").validate[List[A]] match {
            case JsSuccess(Nil, _) =>
              Left(NoReferenceDataFoundException(url))
            case JsSuccess(head :: tail, _) =>
              Right(NonEmptySet.of(head, tail*))
            case JsError(errors) =>
              Left(JsResultException(errors))
          }
        case e =>
          logger.warn(s"[ReferenceDataConnector][responseHandlerGeneric] Reference data call returned $e")
          Left(Exception(s"[ReferenceDataConnector][responseHandlerGeneric] $e - ${response.body}"))
      }
}

object ReferenceDataConnector {

  type Responses[T] = Either[Exception, NonEmptySet[T]]
  type Response[T]  = Either[Exception, T]

  class NoReferenceDataFoundException(url: String) extends Exception(s"The reference data call was successful but the response body is empty: $url")
}
