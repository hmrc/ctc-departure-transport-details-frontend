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
import generators.Generators
import models.SecurityDetailsType._
import models.domain.{EitherType, UserAnswersReader}
import models.reference.{CustomsOffice, Nationality}
import models.transportMeans.BorderModeOfTransport
import models.transportMeans.BorderModeOfTransport._
import models.transportMeans.active.Identification
import models.transportMeans.departure.{InlandMode, Identification => DepartureIdentification}
import models.{Index, SecurityDetailsType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.SecurityDetailsTypePage
import pages.transportMeans.active._
import pages.transportMeans.departure.{InlandModePage, MeansIdentificationNumberPage, VehicleCountryPage}
import pages.transportMeans.{active, departure, AddBorderModeOfTransportYesNoPage, BorderModeOfTransportPage}

class TransportMeansDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val borderModeOfTransport     = arbitrary[BorderModeOfTransport].sample.value
  private val departureIdentification   = arbitrary[DepartureIdentification].sample.value
  private val meansIdentificationNumber = Gen.alphaNumStr.sample.value
  private val vehicleCountry            = arbitrary[Nationality].sample.value
  private val activeIdentification      = arbitrary[Identification].sample.value
  private val identificationNumber      = Gen.alphaNumStr.sample.value
  private val nationality               = arbitrary[Nationality].sample.value
  private val customsOffice             = arbitrary[CustomsOffice].sample.value
  private val conveyanceReferenceNumber = Gen.alphaNumStr.sample.value

  "TransportMeansDomain" - {
    "can be parsed from user answers" - {
      "when inland mode is 5 (mail)" in {
        val inlandMode = InlandMode.Mail

        val answers = emptyUserAnswers.setValue(InlandModePage, inlandMode)

        val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
          TransportMeansDomain.userAnswersReader
        ).run(answers)

        result.value mustBe TransportMeansDomainWithMailInlandMode
      }

      "when inland mode is not 5 (mail)" - {
        "and security type is not 4 (EntryAndExitSummaryDeclarationSecurityDetails)" in {
          forAll(
            arbitrary[InlandMode](arbitraryNonMailInlandMode),
            arbitrary[SecurityDetailsType](arbitraryNonEntryAndExitSummaryDeclarationSecurityDetailsType)
          ) {
            (inlandMode, securityDetailsType) =>
              val initialAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, securityDetailsType)
                .setValue(InlandModePage, inlandMode)
                .setValue(departure.IdentificationPage, departureIdentification)
                .setValue(MeansIdentificationNumberPage, meansIdentificationNumber)
                .setValue(VehicleCountryPage, vehicleCountry)
                .setValue(BorderModeOfTransportPage, borderModeOfTransport)
                .setValue(active.IdentificationPage(index), activeIdentification)
                .setValue(IdentificationNumberPage(index), identificationNumber)
                .setValue(AddNationalityYesNoPage(index), true)
                .setValue(NationalityPage(index), nationality)
                .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)
                .setValue(ConveyanceReferenceNumberYesNoPage(index), true)
                .setValue(ConveyanceReferenceNumberPage(index), conveyanceReferenceNumber)

              val expectedResult = TransportMeansDomainWithOtherInlandMode(
                inlandMode = inlandMode,
                transportMeansDeparture = TransportMeansDepartureDomain(departureIdentification, meansIdentificationNumber, vehicleCountry),
                borderModeOfTransport = Some(borderModeOfTransport),
                transportMeansActiveList = TransportMeansActiveListDomain(
                  Seq(
                    TransportMeansActiveDomain(activeIdentification, identificationNumber, Some(nationality), customsOffice, Some(conveyanceReferenceNumber))(
                      index
                    )
                  )
                )
              )

              forAll(arbitraryTransportMeansAnswers(initialAnswers)) {
                answers =>
                  val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
                    TransportMeansDomain.userAnswersReader
                  ).run(answers)

                  result.value mustBe expectedResult
              }
          }
        }

        "and security type is 4 (EntryAndExitSummaryDeclarationSecurityDetails)" in {
          forAll(arbitrary[InlandMode](arbitraryNonMailInlandMode)) {
            inlandMode =>
              val initialAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, EntryAndExitSummaryDeclarationSecurityDetails)
                .setValue(InlandModePage, inlandMode)
                .setValue(departure.IdentificationPage, departureIdentification)
                .setValue(MeansIdentificationNumberPage, meansIdentificationNumber)
                .setValue(VehicleCountryPage, vehicleCountry)
                .setValue(AddBorderModeOfTransportYesNoPage, true)
                .setValue(BorderModeOfTransportPage, borderModeOfTransport)
                .setValue(active.IdentificationPage(index), activeIdentification)
                .setValue(IdentificationNumberPage(index), identificationNumber)
                .setValue(AddNationalityYesNoPage(index), true)
                .setValue(NationalityPage(index), nationality)
                .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)
                .setValue(ConveyanceReferenceNumberYesNoPage(index), true)
                .setValue(ConveyanceReferenceNumberPage(index), conveyanceReferenceNumber)

              val expectedResult = TransportMeansDomainWithOtherInlandMode(
                inlandMode = inlandMode,
                transportMeansDeparture = TransportMeansDepartureDomain(departureIdentification, meansIdentificationNumber, vehicleCountry),
                borderModeOfTransport = Some(borderModeOfTransport),
                transportMeansActiveList = TransportMeansActiveListDomain(
                  Seq(
                    TransportMeansActiveDomain(activeIdentification, identificationNumber, Some(nationality), customsOffice, Some(conveyanceReferenceNumber))(
                      index
                    )
                  )
                )
              )

              forAll(arbitraryTransportMeansAnswers(initialAnswers)) {
                answers =>
                  val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
                    TransportMeansDomain.userAnswersReader
                  ).run(answers)

                  result.value mustBe expectedResult
              }
          }
        }
      }
    }

    "cannot be parsed from user answers" - {
      "when inland mode is unanswered" in {
        val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
          TransportMeansDomain.userAnswersReader
        ).run(emptyUserAnswers)

        result.left.value.page mustBe InlandModePage
      }

      "when add border mode of transport yes no is unanswered" in {
        forAll(arbitrary[InlandMode](arbitraryNonMailInlandMode)) {
          inlandMode =>
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, EntryAndExitSummaryDeclarationSecurityDetails)
              .setValue(InlandModePage, inlandMode)
              .setValue(departure.IdentificationPage, arbitrary[DepartureIdentification].sample.value)
              .setValue(departure.MeansIdentificationNumberPage, nonEmptyString.sample.value)
              .setValue(departure.VehicleCountryPage, arbitrary[Nationality].sample.value)

            val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
              TransportMeansDomain.userAnswersReader
            ).run(userAnswers)

            result.left.value.page mustBe AddBorderModeOfTransportYesNoPage
        }
      }

      "when border mode of transport is unanswered" in {
        forAll(arbitrary[InlandMode](arbitraryNonMailInlandMode)) {
          inlandMode =>
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, EntryAndExitSummaryDeclarationSecurityDetails)
              .setValue(InlandModePage, inlandMode)
              .setValue(departure.IdentificationPage, arbitrary[DepartureIdentification].sample.value)
              .setValue(departure.MeansIdentificationNumberPage, nonEmptyString.sample.value)
              .setValue(departure.VehicleCountryPage, arbitrary[Nationality].sample.value)
              .setValue(AddBorderModeOfTransportYesNoPage, true)

            val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
              TransportMeansDomain.userAnswersReader
            ).run(userAnswers)

            result.left.value.page mustBe BorderModeOfTransportPage
        }
      }

      "when no active border means answered" in {
        forAll(
          arbitrary[InlandMode](arbitraryNonMailInlandMode),
          Gen.oneOf(Sea, Air),
          arbitrary[SecurityDetailsType](arbitraryNonEntryAndExitSummaryDeclarationSecurityDetailsType)
        ) {
          (inlandMode, borderMode, securityDetailsType) =>
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, securityDetailsType)
              .setValue(InlandModePage, inlandMode)
              .setValue(departure.IdentificationPage, arbitrary[DepartureIdentification].sample.value)
              .setValue(departure.MeansIdentificationNumberPage, nonEmptyString.sample.value)
              .setValue(departure.VehicleCountryPage, arbitrary[Nationality].sample.value)
              .setValue(BorderModeOfTransportPage, borderMode)

            val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
              TransportMeansDomain.userAnswersReader
            ).run(userAnswers)

            result.left.value.page mustBe active.IdentificationPage(Index(0))
        }
      }
    }
  }
}
