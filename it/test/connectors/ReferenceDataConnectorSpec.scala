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

import cats.data.NonEmptySet
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, okJson, urlEqualTo}
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import itbase.{ItSpecBase, WireMockServerHandler}
import models.reference.*
import models.reference.additionalInformation.AdditionalInformationCode
import models.reference.additionalReference.AdditionalReferenceType
import models.reference.authorisations.AuthorisationType
import models.reference.equipment.PaymentMethod
import models.reference.supplyChainActors.SupplyChainActorType
import models.reference.transportMeans.*
import org.scalacheck.Gen
import org.scalatest.{Assertion, EitherValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends ItSpecBase with WireMockServerHandler with ScalaCheckPropertyChecks with EitherValues {

  private val baseUrl = "customs-reference-data/test-only"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder = super
    .guiceApplicationBuilder()
    .configure(
      conf = "microservice.services.customs-reference-data.port" -> server.port()
    )

  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]

  private val emptyResponseJson: String =
    """
      |{
      |  "data": []
      |}
      |""".stripMargin

  "Reference Data" - {

    "getCountries" - {
      def url: String = s"/$baseUrl/lists/CountryCodesFullList"

      "must return Seq of Country when successful" in {
        val countriesResponseJson: String =
          s"""
             |{
             |  "_links": {
             |    "self": {
             |      "href": "/customs-reference-data/lists/CountryCodesFullList"
             |    }
             |  },
             |  "meta": {
             |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
             |    "snapshotDate": "2023-01-01"
             |  },
             |  "id": "CountryCodesFullList",
             |  "data": [
             |    {
             |      "activeFrom": "2023-01-23",
             |      "code": "GB",
             |      "state": "valid",
             |      "description": "United Kingdom"
             |    },
             |    {
             |      "activeFrom": "2023-01-23",
             |      "code": "AD",
             |      "state": "valid",
             |      "description": "Andorra"
             |    }
             |  ]
             |}
             |""".stripMargin

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getCountries().futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCountries())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getCountries())
      }
    }

    "getNationalities" - {
      val url: String = s"/$baseUrl/lists/Nationality"

      "must return Seq of Country when successful" in {
        val nationalitiesResponseJson: String =
          """
            |{
            |  "_links": {
            |    "self": {
            |      "href": "/customs-reference-data/lists/Nationality"
            |    }
            |  },
            |  "meta": {
            |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
            |    "snapshotDate": "2023-01-01"
            |  },
            |  "id": "Nationality",
            |  "data": [
            |    {
            |      "code":"AR",
            |      "description":"Argentina"
            |    },
            |    {
            |      "code":"AU",
            |      "description":"Australia"
            |    }
            |  ]
            |}
            |""".stripMargin

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(nationalitiesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          Nationality("AR", "Argentina"),
          Nationality("AU", "Australia")
        )

        connector.getNationalities().futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getNationalities())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getNationalities())
      }
    }

    "getCountryCodesCommonTransitCountry" - {
      def url(countryId: String): String = s"/$baseUrl/lists/CountryCodesCommonTransit?data.code=$countryId"

      "must return Seq of Country when successful" in {
        val countryResponseJson: String =
          s"""
             |{
             |  "_links": {
             |    "self": {
             |      "href": "/customs-reference-data/lists/CountryCodesCommonTransit"
             |    }
             |  },
             |  "meta": {
             |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
             |    "snapshotDate": "2023-01-01"
             |  },
             |  "id": "CountryCodesCommonTransit",
             |  "data": [
             |    {
             |      "activeFrom": "2023-01-23",
             |      "code": "GB",
             |      "state": "valid",
             |      "description": "United Kingdom"
             |    }
             |  ]
             |}
             |""".stripMargin

        val countryId = "GB"

        val str = url(countryId)

        server.stubFor(
          get(urlEqualTo(str))
            .willReturn(okJson(countryResponseJson))
        )

        val expectedResult = Country(CountryCode(countryId), "United Kingdom")
        connector.getCountryCodesCommonTransitCountry(countryId).futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        val countryId = "AD"
        checkNoReferenceDataFoundResponse(url(countryId), connector.getCountryCodesCommonTransitCountry(countryId))
      }

      "must return an exception when an error response is returned" in {
        val countryId = "AD"
        checkErrorResponse(url(countryId), connector.getCountryCodesCommonTransitCountry(countryId))
      }
    }

    "getTransportModeCodes" - {

      val url: String = s"/$baseUrl/lists/TransportModeCode"

      val transportModeCodesResponseJson: String =
        """
          |{
          |  "_links": {
          |    "self": {
          |      "href": "/customs-reference-data/lists/TransportModeCode"
          |    }
          |  },
          |  "meta": {
          |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
          |    "snapshotDate": "2023-01-01"
          |  },
          |  "id": "TransportModeCode",
          |  "data": [
          |    {
          |      "code":"1",
          |      "description":"Maritime"
          |    },
          |    {
          |      "code":"2",
          |      "description":"Rail"
          |    }
          |  ]
          |}
          |""".stripMargin

      "when Inland Mode" - {

        "must return Seq of InlandMode when successful" in {
          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(okJson(transportModeCodesResponseJson))
          )

          val expectedResult = NonEmptySet.of(
            InlandMode("1", "Maritime"),
            InlandMode("2", "Rail")
          )

          connector.getTransportModeCodes[InlandMode]().futureValue.value mustEqual expectedResult
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          checkNoReferenceDataFoundResponse(url, connector.getTransportModeCodes[InlandMode]())
        }

        "must return an exception when an error response is returned" in {
          checkErrorResponse(url, connector.getTransportModeCodes[InlandMode]())
        }
      }

      "when Border Mode" - {

        "must return Seq of BorderMode when successful" in {
          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(okJson(transportModeCodesResponseJson))
          )

          val expectedResult = NonEmptySet.of(
            BorderMode("1", "Maritime"),
            BorderMode("2", "Rail")
          )

          connector.getTransportModeCodes[BorderMode]().futureValue.value mustEqual expectedResult
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          checkNoReferenceDataFoundResponse(url, connector.getTransportModeCodes[BorderMode]())
        }

        "must return an exception when an error response is returned" in {
          checkErrorResponse(url, connector.getTransportModeCodes[BorderMode]())
        }
      }
    }

    "getMeansOfTransportIdentificationTypes" - {
      val url: String = s"/$baseUrl/lists/TypeOfIdentificationOfMeansOfTransport"

      "must return Seq of Identification when successful" in {
        val identificationCodesDepartureResponseJson: String =
          """
            |{
            |  "_links": {
            |    "self": {
            |      "href": "/customs-reference-data/lists/TypeOfIdentificationOfMeansOfTransport"
            |    }
            |  },
            |  "meta": {
            |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
            |    "snapshotDate": "2023-01-01"
            |  },
            |  "id": "TypeOfIdentificationOfMeansOfTransport",
            |  "data": [
            |    {
            |      "type":"10",
            |      "description":"IMO ship identification number"
            |    },
            |    {
            |      "type":"11",
            |      "description":"Name of a sea-going vessel"
            |    }
            |  ]
            |}
            |""".stripMargin

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(identificationCodesDepartureResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          departure.Identification("10", "IMO ship identification number"),
          departure.Identification("11", "Name of a sea-going vessel")
        )

        connector.getMeansOfTransportIdentificationTypes().futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getMeansOfTransportIdentificationTypes())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getMeansOfTransportIdentificationTypes())
      }
    }

    "getMeansOfTransportIdentificationTypesActive" - {
      val url: String = s"/$baseUrl/lists/TypeOfIdentificationofMeansOfTransportActive"

      "must return Seq of Identification when successful" in {
        val identificationCodesActiveResponseJson: String =
          """
            |{
            |  "_links": {
            |    "self": {
            |      "href": "/customs-reference-data/lists/TypeOfIdentificationofMeansOfTransportActive"
            |    }
            |  },
            |  "meta": {
            |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
            |    "snapshotDate": "2023-01-01"
            |  },
            |  "id": "TypeOfIdentificationofMeansOfTransportActive",
            |  "data": [
            |    {
            |      "code":"10",
            |      "description":"IMO ship identification number"
            |    },
            |    {
            |      "code":"11",
            |      "description":"Name of a sea-going vessel"
            |    }
            |  ]
            |}
            |""".stripMargin

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(identificationCodesActiveResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          active.Identification("10", "IMO ship identification number"),
          active.Identification("11", "Name of a sea-going vessel")
        )

        connector.getMeansOfTransportIdentificationTypesActive().futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getMeansOfTransportIdentificationTypesActive())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getMeansOfTransportIdentificationTypesActive())
      }
    }

    "getSupplyChainActorTypes" - {
      val url: String = s"/$baseUrl/lists/AdditionalSupplyChainActorRoleCode"

      "must return Seq of SupplyChainActorType when successful" in {
        val supplyChainActorTypesResponseJson: String =
          """
            |{
            |  "_links": {
            |    "self": {
            |      "href": "/customs-reference-data/lists/AdditionalSupplyChainActorRoleCode"
            |    }
            |  },
            |  "meta": {
            |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
            |    "snapshotDate": "2023-01-01"
            |  },
            |  "id": "AdditionalSupplyChainActorRoleCode",
            |  "data": [
            |    {
            |      "role":"CS",
            |      "description":"Consolidator"
            |    },
            |    {
            |      "role":"MF",
            |      "description":"Manufacturer"
            |    }
            |  ]
            |}
            |""".stripMargin

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(supplyChainActorTypesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          SupplyChainActorType("CS", "Consolidator"),
          SupplyChainActorType("MF", "Manufacturer")
        )

        connector.getSupplyChainActorTypes().futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getSupplyChainActorTypes())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getSupplyChainActorTypes())
      }
    }

    "getAuthorisationTypes" - {
      val url: String = s"/$baseUrl/lists/AuthorisationTypeDeparture"

      "must return Seq of AuthorisationType when successful" in {
        val authorisationTypesResponseJson: String =
          """
            |{
            |  "_links": {
            |    "self": {
            |      "href": "/customs-reference-data/lists/AuthorisationTypeDeparture"
            |    }
            |  },
            |  "meta": {
            |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
            |    "snapshotDate": "2023-01-01"
            |  },
            |  "id": "AuthorisationTypeDeparture",
            |  "data": [
            |    {
            |      "code":"C521",
            |      "description":"ACR - authorisation for the status of authorised consignor for Union transit"
            |    },
            |    {
            |      "code":"C523",
            |      "description":"SSE - authorisation for the use of seals of a special type"
            |    }
            |  ]
            |}
            |""".stripMargin

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(authorisationTypesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          AuthorisationType(
            "C521",
            "ACR - authorisation for the status of authorised consignor for Union transit"
          ),
          AuthorisationType("C523", "SSE - authorisation for the use of seals of a special type")
        )

        connector.getAuthorisationTypes().futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getAuthorisationTypes())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getAuthorisationTypes())
      }
    }

    "getPaymentMethods" - {
      val url: String = s"/$baseUrl/lists/TransportChargesMethodOfPayment"

      "must return Seq of PaymentMethod when successful" in {
        val paymentMethodsResponseJson: String =
          """
             |{
             |  "_links": {
             |    "self": {
             |      "href": "/customs-reference-data/lists/TransportChargesMethodOfPayment"
             |    }
             |  },
             |  "meta": {
             |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
             |    "snapshotDate": "2023-01-01"
             |  },
             |  "id": "TransportChargesMethodOfPayment",
             |  "data": [
             |    {
             |      "method":"A",
             |      "description":"Cash"
             |    },
             |    {
             |      "method":"B",
             |      "description":"Credit card"
             |    }
             |  ]
             |}
             |""".stripMargin

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(paymentMethodsResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          PaymentMethod("A", "Cash"),
          PaymentMethod("B", "Credit card")
        )

        connector.getPaymentMethods().futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getPaymentMethods())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getPaymentMethods())
      }
    }

    "getAdditionalReferences" - {
      val url = s"/$baseUrl/lists/AdditionalReference"

      "must return Seq of AdditionalReference when successful" in {

        val additionalReferenceJson: String =
          """
            |{
            |  "_links": {
            |    "self": {
            |      "href": "/customs-reference-data/lists/AdditionalReference"
            |    }
            |  },
            |  "meta": {
            |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
            |    "snapshotDate": "2023-01-01"
            |  },
            |  "id": "AdditionalReference",
            |  "data": [
            | {
            |    "documentType": "documentType1",
            |    "description": "desc1"
            |  },
            |  {
            |    "documentType": "documentType2",
            |    "description": "desc2"
            |  }
            |]
            |}
            |""".stripMargin

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(additionalReferenceJson))
        )

        val expectedResult: NonEmptySet[AdditionalReferenceType] = NonEmptySet.of(
          AdditionalReferenceType("documentType1", "desc1"),
          AdditionalReferenceType("documentType2", "desc2")
        )

        connector.getAdditionalReferences().futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getAdditionalReferences())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getAdditionalReferences())
      }
    }

    "getAdditionalInformationCodes" - {
      val url = s"/$baseUrl/lists/AdditionalInformation"

      "must return Seq of AdditionalInformation when successful" in {

        val additionalInformationJson: String =
          """
            |{
            |  "_links": {
            |    "self": {
            |      "href": "/customs-reference-data/lists/AdditionalInformation"
            |    }
            |  },
            |  "meta": {
            |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
            |    "snapshotDate": "2023-01-01"
            |  },
            |  "id": "AdditionalInformation",
            |  "data": [
            | {
            |    "code": "20100",
            |    "description": "Export from one EFTA country subject to restriction or export from the Union subject to restriction"
            |  },
            |  {
            |    "code": "20300",
            |    "description": "Export"
            |  }
            |]
            |}
            |""".stripMargin

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(additionalInformationJson))
        )

        val expectedResult: NonEmptySet[AdditionalInformationCode] = NonEmptySet.of(
          AdditionalInformationCode("20100", "Export from one EFTA country subject to restriction or export from the Union subject to restriction"),
          AdditionalInformationCode("20300", "Export")
        )

        connector.getAdditionalInformationCodes().futureValue.value mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getAdditionalInformationCodes())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getAdditionalInformationCodes())
      }
    }

  }

  private def checkNoReferenceDataFoundResponse(url: String, result: => Future[Either[Exception, ?]]): Assertion = {
    server.stubFor(
      get(urlEqualTo(url))
        .willReturn(okJson(emptyResponseJson))
    )

    result.futureValue.left.value mustBe a[NoReferenceDataFoundException]
  }

  private def checkErrorResponse(url: String, result: => Future[Either[Exception, ?]]): Assertion = {
    val errorResponses: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        result.futureValue.left.value mustBe an[Exception]
    }
  }

}
