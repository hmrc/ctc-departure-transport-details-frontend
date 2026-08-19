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
import com.github.tomakehurst.wiremock.client.WireMock.*
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
      |[]
      |""".stripMargin

  "Reference Data" - {

    "getCountries" - {
      val url: String = s"/$baseUrl/lists/CountryCodesFullList"
      "must return Seq of Country when successful" in {
        val countriesResponseJson: String = s"""
               |[
               |    {
               |      "key": "GB",
               |      "value": "United Kingdom"
               |    },
               |    {
               |      "key": "AD",
               |      "value": "Andorra"
               |    }
               |]
               |""".stripMargin

        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val nationalitiesResponseJson: String =
        """
            |[
            |    {
            |      "key":"AR",
            |      "value":"Argentina"
            |    },
            |    {
            |      "key":"AU",
            |      "value":"Australia"
            |    }
            |  ]
            |""".stripMargin

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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
      def url(countryId: String): String = s"/$baseUrl/lists/CountryCodesCommonTransit?keys=$countryId"
      val countryResponseJson: String =
        s"""
             |[
             |    {
             |      "activeFrom": "2023-01-23",
             |      "key": "GB",
             |      "state": "valid",
             |      "value": "United Kingdom"
             |    }
             |]
             |""".stripMargin

      "must return Seq of Country when successful" in {
        val countryId = "GB"
        val str       = url(countryId)
        server.stubFor(
          get(urlEqualTo(str))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      "when Inland Mode" - {
        val transportModeCodesResponseJson: String =
          """
              |[
              |    {
              |      "key":"1",
              |      "value":"Maritime"
              |    },
              |    {
              |      "key":"2",
              |      "value":"Rail"
              |    }
              |  ]
              |""".stripMargin

        "must return Seq of InlandMode when successful" in {
          server.stubFor(
            get(urlEqualTo(url))
              .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
              .willReturn(okJson(transportModeCodesResponseJson))
          )

          val expectedResult = NonEmptySet.of(
            InlandMode("1", "Maritime"),
            InlandMode("2", "Rail")
          )

          connector.getInlandModes().futureValue.value mustEqual expectedResult
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          checkNoReferenceDataFoundResponse(url, connector.getInlandModes())
        }

        "must return an exception when an error response is returned" in {
          checkErrorResponse(url, connector.getInlandModes())
        }

      }

      "when Border Mode" - {
        val transportModeCodesResponseJson: String =
          """
              |[
              |    {
              |      "key":"1",
              |      "value":"Maritime"
              |    },
              |    {
              |      "key":"2",
              |      "value":"Rail"
              |    }
              |  ]
              |""".stripMargin

        "must return Seq of BorderMode when successful" in {
          server.stubFor(
            get(urlEqualTo(url))
              .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
              .willReturn(okJson(transportModeCodesResponseJson))
          )

          val expectedResult = NonEmptySet.of(
            BorderMode("1", "Maritime"),
            BorderMode("2", "Rail")
          )

          connector.getBorderModes().futureValue.value mustEqual expectedResult

        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          checkNoReferenceDataFoundResponse(url, connector.getBorderModes())

        }

        "must return an exception when an error response is returned" in {
          checkErrorResponse(url, connector.getBorderModes())
        }
      }
    }

    "getMeansOfTransportIdentificationTypes" - {
      val url: String = s"/$baseUrl/lists/TypeOfIdentificationOfMeansOfTransport"

      val identificationCodesDepartureResponseJson: String =
        """
            |[
            |    {
            |      "key":"10",
            |      "value":"IMO ship identification number"
            |    },
            |    {
            |      "key":"11",
            |      "value":"Name of a sea-going vessel"
            |    }
            |  ]
            |""".stripMargin

      "must return Seq of Identification when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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
        val identificationCodesActiveResponseJson: String = """
            |[
            |    {
            |      "key":"10",
            |      "value":"IMO ship identification number"
            |    },
            |    {
            |      "key":"11",
            |      "value":"Name of a sea-going vessel"
            |    }
            |  ]
            |""".stripMargin

        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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

      val supplyChainActorTypesResponseJson: String =
        """
            | [
            |    {
            |      "key":"CS",
            |      "value":"Consolidator"
            |    },
            |    {
            |      "key":"MF",
            |      "value":"Manufacturer"
            |    }
            | ]
            |""".stripMargin

      "must return Seq of SupplyChainActorType when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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
      val authorisationTypesResponseJson: String =
        """
            |[
            |    {
            |      "key":"C521",
            |      "value":"ACR - authorisation for the status of authorised consignor for Union transit"
            |    },
            |    {
            |      "key":"C523",
            |      "value":"SSE - authorisation for the use of seals of a special type"
            |    }
            |  ]
            |""".stripMargin

      "must return Seq of AuthorisationType when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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
      val paymentMethodsResponseJson: String =
        """
            |[
            |    {
            |      "key":"A",
            |      "value":"Cash"
            |    },
            |    {
            |      "key":"B",
            |      "value":"Credit card"
            |    }
            |  ]
            |""".stripMargin

      "must return Seq of PaymentMethod when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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
      val additionalReferenceJson: String =
        """
            | [
            | {
            |    "key": "documentType1",
            |    "value": "desc1"
            |  },
            |  {
            |    "key": "documentType2",
            |    "value": "desc2"
            |  }
            |]
            |""".stripMargin
      "must return Seq of AdditionalReference when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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
      val additionalInformationJson: String =
        """
            | [
            | {
            |    "key": "20100",
            |    "value": "Export from one EFTA country subject to restriction or export from the Union subject to restriction"
            |  },
            |  {
            |    "key": "20300",
            |    "value": "Export"
            |  }
            |]
            |""".stripMargin
      "must return Seq of AdditionalInformation when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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
        .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
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
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        result.futureValue.left.value mustBe an[Exception]
    }
  }

}
