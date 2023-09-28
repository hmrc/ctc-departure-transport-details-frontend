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
      conf = "microservice.services.customs-reference-data.port" -> server.port()
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
      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(s"/$baseUrl/lists/CountryCodesFullList"))
            .willReturn(okJson(countriesResponseJson("CountryCodesFullList")))
        )

        val expectedResult: Seq[Country] = Seq(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getCountries().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(s"/$baseUrl/lists/CountryCodesFullList", connector.getCountries())
      }
    }

    "getNationalities" - {
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
          get(urlEqualTo(s"/$baseUrl/lists/Nationality"))
            .willReturn(okJson(nationalitiesResponseJson))
        )

        val expectedResult: Seq[Nationality] = Seq(
          Nationality("AR", "Argentina"),
          Nationality("AU", "Australia")
        )

        connector.getNationalities().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(s"/$baseUrl/lists/Nationality", connector.getNationalities())
      }
    }

    "getCountryCodesCTC" - {
      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(s"/$baseUrl/lists/CountryCodesCommonTransit"))
            .willReturn(okJson(countriesResponseJson("CountryCodesCommonTransit")))
        )

        val expectedResult: Seq[Country] = Seq(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getCountryCodesCommonTransit().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(s"/$baseUrl/lists/CountryCodesCommonTransit", connector.getCountryCodesCommonTransit())
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
