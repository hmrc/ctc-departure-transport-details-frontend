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

package models.journeyDomain.transportMeans

import base.SpecBase
import config.Constants.SecurityType.*
import generators.Generators
import models.reference.transportMeans.active.Identification
import models.reference.{BorderMode, CustomsOffice, Nationality}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.SecurityDetailsTypePage
import pages.sections.external.OfficesOfTransitSection
import pages.sections.transportMeans.ActiveSection
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.active.*
import play.api.libs.json.{JsArray, Json}

class TransportMeansActiveDomainSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "TransportMeansActiveDomain" - {

    val identification: Identification = arbitrary[Identification].sample.value
    val identificationNumber: String   = Gen.alphaNumStr.sample.value
    val nationality: Nationality       = arbitrary[Nationality].sample.value
    val customsOffice: CustomsOffice   = arbitrary[CustomsOffice].sample.value
    val conveyanceNumber: String       = Gen.alphaNumStr.sample.value

    "hasMultiplicity" - {
      "when at least one office of transit added" - {
        "must be true" in {
          val userAnswers = emptyUserAnswers.setValue(OfficesOfTransitSection, JsArray(Seq(Json.obj("foo" -> "bar"))))
          val result      = TransportMeansActiveDomain.hasMultiplicity(userAnswers)
          result mustBe true
        }
      }

      "when no offices of transit added" - {
        "must be false" in {
          val result = TransportMeansActiveDomain.hasMultiplicity(emptyUserAnswers)
          result mustBe false
        }
      }
    }

    "can be parsed from user answers" - {
      "when the add nationality is answered yes" - {
        "and security detail type is 0 and inland mode is Sea and add conveyance number is yes" in {
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(OfficesOfTransitSection, JsArray(Seq(Json.obj("foo" -> "bar"))))
            .setValue(BorderModeOfTransportPage, BorderMode("1", "Maritime"))
            .setValue(IdentificationPage(index), identification)
            .setValue(IdentificationNumberPage(index), identificationNumber)
            .setValue(NationalityPage(index), nationality)
            .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)
            .setValue(ConveyanceReferenceNumberYesNoPage(index), true)
            .setValue(ConveyanceReferenceNumberPage(index), conveyanceNumber)

          val expectedResult = TransportMeansActiveDomain(
            identification = identification,
            identificationNumber = identificationNumber,
            nationality = nationality,
            customsOffice = customsOffice,
            conveyanceReferenceNumber = Some(conveyanceNumber)
          )(index)

          val result = TransportMeansActiveDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            IdentificationPage(index),
            IdentificationNumberPage(index),
            NationalityPage(index),
            CustomsOfficeActiveBorderPage(index),
            ConveyanceReferenceNumberYesNoPage(index),
            ConveyanceReferenceNumberPage(index),
            ActiveSection(index)
          )
        }

        "and security detail type is 1 and inland mode is Air" in {
          val securityGen = arbitrary[String](arbitrarySomeSecurityDetailsType)
          forAll(securityGen) {
            securityType =>
              val userAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, securityType)
                .setValue(BorderModeOfTransportPage, BorderMode("4", "Air"))
                .setValue(IdentificationPage(index), identification)
                .setValue(IdentificationNumberPage(index), identificationNumber)
                .setValue(NationalityPage(index), nationality)
                .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)
                .setValue(ConveyanceReferenceNumberPage(index), conveyanceNumber)

              val expectedResult = TransportMeansActiveDomain(
                identification = identification,
                identificationNumber = identificationNumber,
                nationality = nationality,
                customsOffice = customsOffice,
                conveyanceReferenceNumber = Some(conveyanceNumber)
              )(index)

              val result = TransportMeansActiveDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

              result.value.value mustBe expectedResult
              result.value.pages mustBe Seq(
                IdentificationPage(index),
                IdentificationNumberPage(index),
                NationalityPage(index),
                CustomsOfficeActiveBorderPage(index),
                ConveyanceReferenceNumberPage(index)
              )
          }
        }
      }
    }

    "can not be parsed from user answers" - {
      "when border mode is answered" - {
        "must go to identification type" in {
          forAll(arbitrary[BorderMode]) {
            borderModeOfTransport =>
              val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, borderModeOfTransport)

              val result = TransportMeansActiveDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

              result.left.value.page mustBe IdentificationPage(index)
              result.left.value.pages mustBe Seq(
                IdentificationPage(index)
              )
          }
        }
      }

      "when nationality is unanswered" in {
        val userAnswers = emptyUserAnswers
          .setValue(BorderModeOfTransportPage, BorderMode("4", "Air"))
          .setValue(IdentificationPage(index), Identification("40", "IATA flight number"))
          .setValue(IdentificationNumberPage(index), identificationNumber)

        val result = TransportMeansActiveDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

        result.left.value.page mustBe NationalityPage(index)
        result.left.value.pages mustBe Seq(
          IdentificationPage(index),
          IdentificationNumberPage(index),
          NationalityPage(index)
        )
      }

      "when customs office ref. number is unanswered" in {
        val userAnswers = emptyUserAnswers
          .setValue(BorderModeOfTransportPage, BorderMode("3", "Road"))
          .setValue(InferredIdentificationPage(index), Identification("30", "Registration number of a road vehicle"))
          .setValue(IdentificationNumberPage(index), identificationNumber)
          .setValue(NationalityPage(index), nationality)

        val result = TransportMeansActiveDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

        result.left.value.page mustBe CustomsOfficeActiveBorderPage(index)
        result.left.value.pages mustBe Seq(
          IdentificationNumberPage(index),
          NationalityPage(index),
          CustomsOfficeActiveBorderPage(index)
        )
      }

      "when security is in set {1,2,3}" - {
        "and border mode of transport is 4 (Air)" in {
          val securityGen       = arbitrary[String](arbitrarySomeSecurityDetailsType)
          val identificationGen = Gen.oneOf(Identification("40", "IATA flight number"), Identification("41", "Registration number of an aircraft"))
          forAll(securityGen, identificationGen) {
            (securityType, identification) =>
              val userAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, securityType)
                .setValue(BorderModeOfTransportPage, BorderMode("4", "Air"))
                .setValue(IdentificationPage(index), identification)
                .setValue(IdentificationNumberPage(index), identificationNumber)
                .setValue(NationalityPage(index), nationality)
                .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)

              val result = TransportMeansActiveDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

              result.left.value.page mustBe ConveyanceReferenceNumberPage(index)
              result.left.value.pages mustBe Seq(
                IdentificationPage(index),
                IdentificationNumberPage(index),
                NationalityPage(index),
                CustomsOfficeActiveBorderPage(index),
                ConveyanceReferenceNumberPage(index)
              )
          }
        }

        "and border mode of transport is not 4 (Air)" in {
          val securityGen       = arbitrary[String](arbitrarySomeSecurityDetailsType)
          val borderModeGen     = arbitrary[Option[BorderMode]](arbitraryOptionalNonAirBorderModeOfTransport).sample.value
          val identificationGen = Gen.oneOf(Identification("40", "IATA flight number"), Identification("41", "Registration number of an aircraft"))
          forAll(securityGen, borderModeGen, identificationGen) {
            (securityType, borderMode, identification) =>
              val userAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, securityType)
                .setValue(BorderModeOfTransportPage, borderMode)
                .setValue(IdentificationPage(index), identification)
                .setValue(IdentificationNumberPage(index), identificationNumber)
                .setValue(NationalityPage(index), nationality)
                .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)

              val result = TransportMeansActiveDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

              result.left.value.page mustBe ConveyanceReferenceNumberYesNoPage(index)
              result.left.value.pages mustBe Seq(
                IdentificationPage(index),
                IdentificationNumberPage(index),
                NationalityPage(index),
                CustomsOfficeActiveBorderPage(index),
                ConveyanceReferenceNumberYesNoPage(index)
              )
          }
        }
      }

      "when security is 0 (No security)" in {
        val borderModeGen     = arbitrary[BorderMode]
        val identificationGen = arbitrary[Identification]
        forAll(borderModeGen, identificationGen) {
          (borderMode, identification) =>
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(BorderModeOfTransportPage, borderMode)
              .setValue(IdentificationPage(index), identification)
              .setValue(IdentificationNumberPage(index), identificationNumber)
              .setValue(NationalityPage(index), nationality)
              .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)

            val result = TransportMeansActiveDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

            result.left.value.page mustBe ConveyanceReferenceNumberYesNoPage(index)
            result.left.value.pages mustBe Seq(
              IdentificationPage(index),
              IdentificationNumberPage(index),
              NationalityPage(index),
              CustomsOfficeActiveBorderPage(index),
              ConveyanceReferenceNumberYesNoPage(index)
            )
        }
      }

      "when conveyance reference number needs to be answered" in {
        val borderModeGen     = arbitrary[BorderMode]
        val identificationGen = arbitrary[Identification]
        forAll(borderModeGen, identificationGen) {
          (borderMode, identification) =>
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(BorderModeOfTransportPage, borderMode)
              .setValue(IdentificationPage(index), identification)
              .setValue(IdentificationNumberPage(index), identificationNumber)
              .setValue(NationalityPage(index), nationality)
              .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)
              .setValue(ConveyanceReferenceNumberYesNoPage(index), true)

            val result = TransportMeansActiveDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

            result.left.value.page mustBe ConveyanceReferenceNumberPage(index)
            result.left.value.pages mustBe Seq(
              IdentificationPage(index),
              IdentificationNumberPage(index),
              NationalityPage(index),
              CustomsOfficeActiveBorderPage(index),
              ConveyanceReferenceNumberYesNoPage(index),
              ConveyanceReferenceNumberPage(index)
            )
        }
      }
    }

  }
}
