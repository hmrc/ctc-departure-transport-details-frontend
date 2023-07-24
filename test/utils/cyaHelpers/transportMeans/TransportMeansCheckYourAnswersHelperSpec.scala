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

package utils.cyaHelpers.transportMeans

import base.SpecBase
import config.PhaseConfig
import controllers.transportMeans.active.routes
import generators.Generators
import models.domain.UserAnswersReader
import models.journeyDomain.transportMeans.TransportMeansActiveDomain
import models.reference.Nationality
import models.transportMeans.{BorderModeOfTransport, InlandMode}
import models.transportMeans.departure.{Identification => DepartureIdentification}
import models.{Index, Mode, Phase}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.external.OfficesOfTransitSection
import pages.sections.transportMeans.TransportMeansActiveSection
import pages.transportMeans.departure._
import pages.transportMeans._
import pages.transportMeans.active.NationalityPage
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Value}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class TransportMeansCheckYourAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "TransportMeansCheckYourAnswersHelper" - {

    val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
    when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

    "activeBorderTransportMeans" - {

      "during post transition" - {
        "must return None" - {
          "when active border transport means is undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val helper = new TransportMeansCheckYourAnswersHelper(emptyUserAnswers, mode)(messages, frontendAppConfig, mockPhaseConfig)
                val result = helper.activeBorderTransportMeans(index)
                result mustBe None
            }
          }
        }

        "must return Some(Row)" - {
          "when incident is defined" in {
            val prefix = "transportMeans.active.identification"
            forAll(arbitrary[Nationality]) {
              nationality =>
                val initialAnswers = emptyUserAnswers.setValue(OfficesOfTransitSection, JsArray(Seq(Json.obj("foo" -> "bar"))))
                  .setValue(NationalityPage(index), nationality)

                forAll(arbitraryTransportMeansActiveAnswers(initialAnswers, index)(mockPhaseConfig), arbitrary[Mode]) {
                  (userAnswers, mode) =>
                    val abtm = UserAnswersReader[TransportMeansActiveDomain](
                      TransportMeansActiveDomain.userAnswersReader(index)(phaseConfig)
                    ).run(userAnswers).value

                    val helper = new TransportMeansCheckYourAnswersHelper(userAnswers, mode)(messages, frontendAppConfig, mockPhaseConfig)
                    val result = helper.activeBorderTransportMeans(index).get

                    result.key.value mustBe "Active border transport means 1"
                    result.value.value mustBe s"${messages(s"$prefix.${abtm.identification}")} - ${abtm.identificationNumber}"
                    val actions = result.actions.get.items
                    actions.size mustBe 1
                    val action = actions.head
                    action.content.value mustBe "Change"
                    action.href mustBe routes.CheckYourAnswersController.onPageLoad(userAnswers.lrn, mode, activeIndex).url
                    action.visuallyHiddenText.get mustBe "active border transport means 1"
                    action.id mustBe "change-active-border-transport-means-1"
                }
            }
          }
        }
      }
    }

    "addOrRemoveActiveBorderTransportsMeans" - {
      "must return None" - {
        "when active border transports means array is empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportMeansCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addOrRemoveActiveBorderTransportsMeans()
              result mustBe None
          }
        }
      }

      "must return Some(Link)" - {
        "when active border transports means array is non-empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(TransportMeansActiveSection(Index(0)), Json.obj("foo" -> "bar"))
              val helper  = new TransportMeansCheckYourAnswersHelper(answers, mode)
              val result  = helper.addOrRemoveActiveBorderTransportsMeans().get

              result.id mustBe "add-or-remove-border-means-of-transport"
              result.text mustBe "Add or remove border means of transport"
              result.href mustBe routes.AddAnotherBorderTransportController.onPageLoad(answers.lrn, mode).url
          }
        }
      }
    }

    "inlandMode" - {
      "must return None" - {
        "when inlandModePage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportMeansCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.inlandMode
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when inlandModePage defined" in {
          forAll(arbitrary[Mode], arbitrary[InlandMode]) {
            (mode, inlandMode) =>
              val answers = emptyUserAnswers.setValue(InlandModePage, inlandMode)
              val helper  = new TransportMeansCheckYourAnswersHelper(answers, mode)
              val result  = helper.inlandMode

              result mustBe Some(
                SummaryListRow(
                  key = Key("Mode".toText),
                  value = Value(messages(s"${"transportMeans.inlandMode"}.$inlandMode").toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transportMeans.routes.InlandModeController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("inland mode of transport"),
                          attributes = Map("id" -> "change-transport-means-inland-mode")
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }
    }

    "addDepartureTransportMeans" - {
      "must return None" - {
        "when AddDepartureTransportMeansYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportMeansCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addDepartureTransportMeans
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when AddDepartureTransportMeansYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(AddDepartureTransportMeansYesNoPage, true)

              val helper = new TransportMeansCheckYourAnswersHelper(answers, mode)
              val result = helper.addDepartureTransportMeans

              result mustBe Some(
                SummaryListRow(
                  key = Key("Do you want to add identification for this vehicle?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transportMeans.routes.AddDepartureTransportMeansYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want to add identification for the departure means of transport"),
                          attributes = Map("id" -> "change-add-departure-transport-means")
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }
    }

    "departureIdentificationType" - {
      "must return None" - {
        "when departureIdentificationPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportMeansCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.departureIdentificationType
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when departureIdentificationPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[DepartureIdentification]) {
            (mode, departureIdentification) =>
              val answers = emptyUserAnswers.setValue(IdentificationPage, departureIdentification)
              val helper  = new TransportMeansCheckYourAnswersHelper(answers, mode)
              val result  = helper.departureIdentificationType

              result mustBe Some(
                SummaryListRow(
                  key = Key("Identification type".toText),
                  value = Value(messages(s"${"transportMeans.departure.identification"}.$departureIdentification").toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transportMeans.departure.routes.IdentificationController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("identification type for the departure means of transport"),
                          attributes = Map("id" -> "change-transport-means-departure-identification")
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }
    }

    "departureIdentificationNumber" - {
      "must return None" - {
        "when departureIdentificationNumberPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportMeansCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.departureIdentificationNumber
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when departureIdentificationNumberPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, identificationNumber) =>
              val answers = emptyUserAnswers.setValue(MeansIdentificationNumberPage, identificationNumber)
              val helper  = new TransportMeansCheckYourAnswersHelper(answers, mode)
              val result  = helper.departureIdentificationNumber

              result mustBe Some(
                SummaryListRow(
                  key = Key("Identification number".toText),
                  value = Value(s"$identificationNumber".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transportMeans.departure.routes.MeansIdentificationNumberController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("identification number for the departure means of transport"),
                          attributes = Map("id" -> "change-transport-means-departure-identification-number")
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }
    }

    "departureNationality" - {
      "must return None" - {
        "when VehicleCountryPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportMeansCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.departureNationality
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when VehicleCountryPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[Nationality]) {
            (mode, nationality) =>
              val answers = emptyUserAnswers.setValue(VehicleCountryPage, nationality)
              val helper  = new TransportMeansCheckYourAnswersHelper(answers, mode)
              val result  = helper.departureNationality

              result mustBe Some(
                SummaryListRow(
                  key = Key("Registered country".toText),
                  value = Value(s"$nationality".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transportMeans.departure.routes.VehicleCountryController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("registered country for the departure means of transport"),
                          attributes = Map("id" -> "change-transport-means-departure-vehicle-nationality")
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }
    }

    "addModeCrossingBorderYesNo" - {
      "must return None" - {
        "when AddBorderModeOfTransportYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportMeansCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addModeCrossingBorder()
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when AddBorderModeOfTransportYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(AddBorderModeOfTransportYesNoPage, true)

              val helper = new TransportMeansCheckYourAnswersHelper(answers, mode)
              val result = helper.addModeCrossingBorder()

              result mustBe Some(
                SummaryListRow(
                  key = Key("Do you want to add a border mode of transport?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transportMeans.routes.AddBorderModeOfTransportYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want to add a border mode of transport"),
                          attributes = Map("id" -> "change-add-border-mode-of-transport")
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }
    }

    "modeCrossingBorder" - {
      "must return None" - {
        "when ModeCrossingBorderPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportMeansCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.modeCrossingBorder
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when ModeCrossingBorderPage defined" in {
          forAll(arbitrary[Mode], arbitrary[BorderModeOfTransport]) {
            (mode, borderModeOfTransport) =>
              val answers = emptyUserAnswers.setValue(BorderModeOfTransportPage, borderModeOfTransport)
              val helper  = new TransportMeansCheckYourAnswersHelper(answers, mode)
              val result  = helper.modeCrossingBorder

              result mustBe Some(
                SummaryListRow(
                  key = Key("Border mode of transport".toText),
                  value = Value(messages(s"${"transportMeans.borderModeOfTransport"}.$borderModeOfTransport").toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transportMeans.routes.BorderModeOfTransportController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("border mode of transport"),
                          attributes = Map("id" -> "change-border-mode-of-transport")
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }
    }

    "addActiveBorderTransportMeans" - {
      "must return None" - {
        "when AddActiveBorderTransportMeansYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportMeansCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addActiveBorderTransportMeans
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when AddActiveBorderTransportMeansYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(AddActiveBorderTransportMeansYesNoPage, true)

              val helper = new TransportMeansCheckYourAnswersHelper(answers, mode)
              val result = helper.addActiveBorderTransportMeans

              result mustBe Some(
                SummaryListRow(
                  key = Key("Do you want to add identification for this vehicle?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transportMeans.routes.AddActiveBorderTransportMeansYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want to add identification for the border means of transport"),
                          attributes = Map("id" -> "change-add-active-border-transport-means")
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }
    }
  }
}
