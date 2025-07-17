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

package utils.cyaHelpers.transportMeans.active

import base.SpecBase
import config.Constants.SecurityType._
import controllers.transportMeans.active.routes
import generators.Generators
import models.journeyDomain.transportMeans.TransportMeansActiveDomain
import models.reference.transportMeans.active.Identification
import models.reference.{BorderMode, Nationality}
import models.{Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.SecurityDetailsTypePage
import pages.sections.external.OfficesOfTransitSection
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.active._
import play.api.libs.json.{JsArray, Json}
import viewModels.ListItem

class ActiveBorderTransportsAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val officesOfTransit: JsArray = JsArray(Seq(Json.obj("foo" -> "bar")))

  "ActiveBorderTransportsAnswersHelperSpec" - {

    "when empty user answers" - {
      "must return empty list of list items" in {
        val userAnswers = emptyUserAnswers

        val helper = new ActiveBorderTransportsAnswersHelper(userAnswers, NormalMode)
        helper.listItems mustEqual Nil
      }
    }

    "when user answers populated with a complete active border transport" - {
      "and AnotherVehicleCrossingBorder has been answered" in {
        forAll(arbitrary[Nationality]) {
          nationality =>
            val initialAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(OfficesOfTransitSection, officesOfTransit)
              .setValue(BorderModeOfTransportPage, BorderMode("1", "Maritime"))
              .setValue(NationalityPage(index), nationality)

            forAll(arbitraryTransportMeansActiveAnswers(initialAnswers, index), arbitrary[Mode]) {
              (userAnswers, mode) =>
                val helper = new ActiveBorderTransportsAnswersHelper(userAnswers, mode)(messages, frontendAppConfig)
                val active = TransportMeansActiveDomain.userAnswersReader(index).apply(Nil).run(userAnswers).value.value
                helper.listItems mustEqual Seq(
                  Right(
                    ListItem(
                      name = active.asString,
                      changeUrl = routes.CheckYourAnswersController.onPageLoad(userAnswers.lrn, mode, activeIndex).url,
                      removeUrl = Some(routes.ConfirmRemoveBorderTransportController.onPageLoad(userAnswers.lrn, mode, index).url)
                    )
                  )
                )
            }
        }
      }
    }

    "when user answers populated with an in progress active border transport" - {
      "and identification type is defined" in {
        forAll(arbitrary[Nationality]) {
          nationality =>
            val identificationType = Identification("11", "Name of a sea-going vessel")
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(BorderModeOfTransportPage, BorderMode("3", "Road"))
              .setValue(IdentificationPage(index), identificationType)
              .setValue(NationalityPage(index), nationality)

            forAll(arbitrary[Mode]) {
              mode =>
                val helper = new ActiveBorderTransportsAnswersHelper(userAnswers, mode)
                helper.listItems mustEqual Seq(
                  Left(
                    ListItem(
                      name = identificationType.asString,
                      changeUrl = routes.IdentificationNumberController.onPageLoad(userAnswers.lrn, mode, index).url,
                      removeUrl = Some(routes.ConfirmRemoveBorderTransportController.onPageLoad(userAnswers.lrn, mode, index).url)
                    )
                  )
                )
            }
        }
      }

      "and identification is defined" in {
        val identificationNumber = nonEmptyString.sample.value
        val userAnswers = emptyUserAnswers
          .setValue(SecurityDetailsTypePage, NoSecurityDetails)
          .setValue(BorderModeOfTransportPage, BorderMode("2", "Rail"))
          .setValue(InferredIdentificationPage(index), Identification("21", "Train number"))
          .setValue(IdentificationNumberPage(index), identificationNumber)

        forAll(arbitrary[Mode]) {
          mode =>
            val helper = new ActiveBorderTransportsAnswersHelper(userAnswers, mode)
            helper.listItems mustEqual Seq(
              Left(
                ListItem(
                  name = identificationNumber,
                  changeUrl = routes.NationalityController.onPageLoad(userAnswers.lrn, mode, index).url,
                  removeUrl = Some(routes.ConfirmRemoveBorderTransportController.onPageLoad(userAnswers.lrn, mode, index).url)
                )
              )
            )
        }
      }

      "and identification type and identification is defined" in {
        val identificationType   = Identification("11", "Name of a sea-going vessel")
        val identificationNumber = nonEmptyString.sample.value
        val userAnswers = emptyUserAnswers
          .setValue(SecurityDetailsTypePage, NoSecurityDetails)
          .setValue(BorderModeOfTransportPage, BorderMode("2", "Rail"))
          .setValue(IdentificationPage(index), identificationType)
          .setValue(IdentificationNumberPage(index), identificationNumber)

        forAll(arbitrary[Mode]) {
          mode =>
            val helper = new ActiveBorderTransportsAnswersHelper(userAnswers, mode)
            helper.listItems mustEqual Seq(
              Left(
                ListItem(
                  name = s"${identificationType.asString} - $identificationNumber",
                  changeUrl = routes.NationalityController.onPageLoad(userAnswers.lrn, mode, index).url,
                  removeUrl = Some(routes.ConfirmRemoveBorderTransportController.onPageLoad(userAnswers.lrn, mode, index).url)
                )
              )
            )
        }
      }
    }
  }
}
