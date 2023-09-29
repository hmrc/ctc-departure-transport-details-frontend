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

import base._
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, okJson, urlEqualTo}
import models.reference._
import models.reference.authorisations.AuthorisationType
import models.reference.equipment.PaymentMethod
import models.reference.supplyChainActors.SupplyChainActorType
import models.reference.transportMeans._
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockServerHandler with ScalaCheckPropertyChecks {

  private val baseUrl = "customs-reference-data/test-only"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder = super
    .guiceApplicationBuilder()
    .configure(
      conf = "microservice.services.customsReferenceData.port" -> server.port()
    )

  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]

  "Reference Data" - {

    def countriesResponseJson(listName: String): String =
      s"""
         |{
         |  "_links": {
         |    "self": {
         |      "href": "/customs-reference-data/lists/$listName"
         |    }
         |  },
         |  "meta": {
         |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
         |    "snapshotDate": "2023-01-01"
         |  },
         |  "id": "$listName",
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

    "getCountries" - {
      def url: String = s"/$baseUrl/lists/CountryCodesFullList"

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson("CountryCodesFullList")))
        )

        val expectedResult: Seq[Country] = Seq(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getCountries().futureValue mustEqual expectedResult
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

        val expectedResult: Seq[Nationality] = Seq(
          Nationality("AR", "Argentina"),
          Nationality("AU", "Australia")
        )

        connector.getNationalities().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getNationalities())
      }
    }

    "getCountryCodesCTC" - {
      val url: String = s"/$baseUrl/lists/CountryCodesCommonTransit"

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson("CountryCodesCommonTransit")))
        )

        val expectedResult: Seq[Country] = Seq(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getCountryCodesCommonTransit().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getCountryCodesCommonTransit())
      }
    }

    "getTransportModeCodes" - {

      val url: String = s"/$baseUrl/lists/TransportModeCode"

      "must return Seq of InlandMode when successful" in {
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

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(transportModeCodesResponseJson))
        )

        val expectedResult: Seq[InlandMode] = Seq(
          InlandMode("1", "Maritime"),
          InlandMode("2", "Rail")
        )

        connector.getTransportModeCodes[InlandMode]().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned for InlandMode" in {
        checkErrorResponse(url, connector.getTransportModeCodes[InlandMode]())
      }

      "must return Seq of BorderMode when successful" in {
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

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(transportModeCodesResponseJson))
        )

        val expectedResult: Seq[BorderMode] = Seq(
          BorderMode("1", "Maritime"),
          BorderMode("2", "Rail")
        )

        connector.getTransportModeCodes[BorderMode]().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned for BorderMode" in {
        checkErrorResponse(url, connector.getTransportModeCodes[BorderMode]())
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

        val expectedResult: Seq[departure.Identification] = Seq(
          departure.Identification("10", "IMO ship identification number"),
          departure.Identification("11", "Name of a sea-going vessel")
        )

        connector.getMeansOfTransportIdentificationTypes().futureValue mustEqual expectedResult
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

        val expectedResult: Seq[active.Identification] = Seq(
          active.Identification("10", "IMO ship identification number"),
          active.Identification("11", "Name of a sea-going vessel")
        )

        connector.getMeansOfTransportIdentificationTypesActive().futureValue mustEqual expectedResult
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

        val expectedResult: Seq[SupplyChainActorType] = Seq(
          SupplyChainActorType("CS", "Consolidator"),
          SupplyChainActorType("MF", "Manufacturer")
        )

        connector.getSupplyChainActorTypes().futureValue mustEqual expectedResult
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

        val expectedResult: Seq[AuthorisationType] = Seq(
          AuthorisationType(
            "C521",
            "ACR - authorisation for the status of authorised consignor for Union transit"
          ),
          AuthorisationType("C523", "SSE - authorisation for the use of seals of a special type")
        )

        connector.getAuthorisationTypes().futureValue mustEqual expectedResult
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

        val expectedResult: Seq[PaymentMethod] = Seq(
          PaymentMethod("A", "Cash"),
          PaymentMethod("B", "Credit card")
        )

        connector.getPaymentMethods().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getPaymentMethods())
      }
    }
  }

  private def checkErrorResponse(url: String, result: => Future[_]): Assertion = {
    val errorResponses: Gen[Int] = Gen
      .chooseNum(400: Int, 599: Int)
      .suchThat(_ != 404)

    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        whenReady(result.failed) {
          _ mustBe an[Exception]
        }
    }
  }

}
