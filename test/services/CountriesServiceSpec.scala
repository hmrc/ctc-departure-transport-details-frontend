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

import base.SpecBase
import cats.data.NonEmptySet
import connectors.ReferenceDataConnector
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import generators.Generators
import models.SelectableList
import models.reference.{Country, CountryCode}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CountriesServiceSpec extends SpecBase with BeforeAndAfterEach with Generators with ScalaCheckPropertyChecks {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new CountriesService(mockRefDataConnector)

  private val country1: Country = Country(CountryCode("GB"), "United Kingdom")
  private val country2: Country = Country(CountryCode("FR"), "France")
  private val country3: Country = Country(CountryCode("ES"), "Spain")
  private val countries         = NonEmptySet.of(country1, country2, country3)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "CountriesService" - {

    "getCountries" - {
      "must return a list of sorted countries" in {

        when(mockRefDataConnector.getCountries()(any(), any()))
          .thenReturn(Future.successful(Right(countries)))

        service.getCountries().futureValue mustEqual
          SelectableList(Seq(country2, country3, country1))

        verify(mockRefDataConnector).getCountries()(any(), any())
      }
    }

    "isInCL009" - {
      "must return true" - {
        "when connector call returns the country" in {
          forAll(arbitrary[Country]) {
            country =>
              beforeEach()

              when(mockRefDataConnector.getCountryCodesCommonTransitCountry(any())(any(), any()))
                .thenReturn(Future.successful(Right(country)))

              val result = service.isInCL009(country).futureValue

              result mustEqual true

              verify(mockRefDataConnector).getCountryCodesCommonTransitCountry(eqTo(country.code.code))(any(), any())
          }
        }
      }

      "must return false" - {
        "when connector call returns NoReferenceDataFoundException" in {
          forAll(arbitrary[Country]) {
            country =>
              when(mockRefDataConnector.getCountryCodesCommonTransitCountry(any())(any(), any()))
                .thenReturn(Future.successful(Left(new NoReferenceDataFoundException(""))))

              val result = service.isInCL009(country).futureValue

              result mustEqual false
          }
        }
      }

      "must fail" - {
        "when connector call otherwise fails" in {
          forAll(arbitrary[Country]) {
            country =>
              when(mockRefDataConnector.getCountryCodesCommonTransitCountry(any())(any(), any()))
                .thenReturn(Future.successful(Left(new Throwable(""))))

              val result = service.isInCL009(country)

              result.failed.futureValue mustBe a[Throwable]
          }
        }
      }
    }
  }
}
