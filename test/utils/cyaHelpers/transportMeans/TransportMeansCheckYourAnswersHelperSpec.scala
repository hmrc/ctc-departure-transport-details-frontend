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
import controllers.transportMeans.active.routes
import generators.Generators
import models.journeyDomain.transportMeans.{TransportMeansActiveDomain, TransportMeansDepartureDomain}
import models.reference.{BorderMode, InlandMode, Nationality}
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.external.OfficesOfTransitSection
import pages.sections.transportMeans.{ActiveSection, DepartureSection}
import pages.transportMeans._
import pages.transportMeans.active.NationalityPage
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Value}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class TransportMeansCheckYourAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "TransportMeansCheckYourAnswersHelper" - {

    "addDepartureTransportMeans" - {

      def prefix(addInlandModeYesNo: Boolean): String =
        if (addInlandModeYesNo) "transportMeans.addDepartureTransportMeansYesNo.inlandModeYes"
        else "transportMeans.addDepartureTransportMeansYesNo.inlandModeNo"

      "must return None" - {
        "when AddDepartureTransportMeansYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportMeansCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addDepartureTransportMeans(prefix(false))
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {

        "when AddInlandModeYesNo is true" - {
          "and AddDepartureTransportMeansYesNoPage defined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val answers = emptyUserAnswers
                  .setValue(AddInlandModeYesNoPage, true)
                  .setValue(AddDepartureTransportMeansYesNoPage, true)

                val helper = new TransportMeansCheckYourAnswersHelper(answers, mode)
                val result = helper.addDepartureTransportMeans(prefix(true))

                result.value mustEqual
                  SummaryListRow(
                    key = Key("Do you want to add identification for this vehicle?".toText),
                    value = Value("Yes".toText),
                    actions = Some(
                      Actions(
                        items = List(
                          ActionItem(
                            content = "Change".toText,
                            href = controllers.transportMeans.routes.AddDepartureTransportMeansYesNoController.onPageLoad(answers.lrn, mode).url,
                            visuallyHiddenText = Some("if you want to add identification for this vehicle"),
                            attributes = Map("id" -> "change-add-departure-transport-means")
                          )
                        )
                      )
                    )
                  )
            }
          }
        }
        "when AddInlandModeYesNo is false" - {
          "and AddDepartureTransportMeansYesNoPage defined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val answers = emptyUserAnswers
                  .setValue(AddInlandModeYesNoPage, false)
                  .setValue(AddDepartureTransportMeansYesNoPage, true)

                val helper = new TransportMeansCheckYourAnswersHelper(answers, mode)
                val result = helper.addDepartureTransportMeans(prefix(false))

                result.value mustEqual
                  SummaryListRow(
                    key = Key("Do you want to add identification for the departure means of transport?".toText),
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
            }
          }

        }
      }
    }

    "activeBorderTransportMeans" - {

      "must return None" - {
        "when active border transport means is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportMeansCheckYourAnswersHelper(emptyUserAnswers, mode)(messages, frontendAppConfig)
              val result = helper.activeBorderTransportMeans(index)
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when incident is defined" in {
          forAll(arbitrary[Nationality]) {
            nationality =>
              val initialAnswers = emptyUserAnswers
                .setValue(OfficesOfTransitSection, JsArray(Seq(Json.obj("foo" -> "bar"))))
                .setValue(NationalityPage(index), nationality)

              forAll(arbitraryTransportMeansActiveAnswers(initialAnswers, index), arbitrary[Mode]) {
                (userAnswers, mode) =>
                  val abtm = TransportMeansActiveDomain.userAnswersReader(index).apply(Nil).run(userAnswers).value.value

                  val helper = new TransportMeansCheckYourAnswersHelper(userAnswers, mode)(messages, frontendAppConfig)
                  val result = helper.activeBorderTransportMeans(index).get

                  result.key.value mustEqual "Active border transport means 1"
                  result.value.value mustEqual abtm.asString
                  val actions = result.actions.get.items
                  actions.size mustEqual 1
                  val action = actions.head
                  action.content.value mustEqual "Change"
                  action.href mustEqual routes.CheckYourAnswersController.onPageLoad(userAnswers.lrn, mode, activeIndex).url
                  action.visuallyHiddenText.get mustEqual "active border transport means 1"
                  action.id mustEqual "change-active-border-transport-means-1"
              }
          }
        }
      }
    }

    "departureTransportMeans" - {

      "must return None" - {
        "when departure transport means is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportMeansCheckYourAnswersHelper(emptyUserAnswers, mode)(messages, frontendAppConfig)
              val result = helper.departureTransportMeans(index)
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when departure transport means is defined" in {

          forAll(arbitraryTransportMeansDepartureAnswers(emptyUserAnswers, index), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val dtm = TransportMeansDepartureDomain.userAnswersReader(index).apply(Nil).run(userAnswers).value.value

              val helper = new TransportMeansCheckYourAnswersHelper(userAnswers, mode)(messages, frontendAppConfig)
              val result = helper.departureTransportMeans(index).get

              result.key.value mustEqual "Departure means of transport 1"
              result.value.value mustEqual dtm.asString
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transportMeans.departure.routes.DepartureTransportAnswersController
                .onPageLoad(userAnswers.lrn, mode, departureIndex)
                .url
              action.visuallyHiddenText.get mustEqual "departure means of transport 1"
              action.id mustEqual "change-departure-means-of-transport-1"
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
              result must not be defined
          }
        }
      }

      "must return Some(Link)" - {
        "when active border transports means array is non-empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(ActiveSection(Index(0)), Json.obj("foo" -> "bar"))
              val helper  = new TransportMeansCheckYourAnswersHelper(answers, mode)
              val result  = helper.addOrRemoveActiveBorderTransportsMeans().get

              result.id mustEqual "add-or-remove-border-means-of-transport"
              result.text mustEqual "Add or remove border means of transport"
              result.href mustEqual routes.AddAnotherBorderTransportController.onPageLoad(answers.lrn, mode).url
          }
        }
      }
    }

    "addOrRemoveDepartureTransportsMeans" - {
      "must return None" - {
        "when departure transports means array is empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportMeansCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addOrRemoveDepartureTransportsMeans()
              result must not be defined
          }
        }
      }

      "must return Some(Link)" - {
        "when departure transports means array is non-empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(DepartureSection(Index(0)), Json.obj("foo" -> "bar"))
              val helper  = new TransportMeansCheckYourAnswersHelper(answers, mode)
              val result  = helper.addOrRemoveDepartureTransportsMeans().get

              result.id mustEqual "add-or-remove-departure-means-of-transport"
              result.text mustEqual "Add or remove departure means of transport"
              result.href mustEqual controllers.transportMeans.departure.routes.AddAnotherDepartureTransportMeansController.onPageLoad(answers.lrn, mode).url
          }
        }
      }
    }

    "addiInlandModeYesNo" - {
      "must return None" - {
        "when addInlandModeYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportMeansCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addInlandModeYesNo
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when addInlandModeYesNoPage is yes" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddInlandModeYesNoPage, true)
              val helper  = new TransportMeansCheckYourAnswersHelper(answers, mode)
              val result  = helper.addInlandModeYesNo

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add an inland mode of transport?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transportMeans.routes.AddInlandModeYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want to add an inland mode of transport"),
                          attributes = Map("id" -> "change-add-transport-means-inland-mode")
                        )
                      )
                    )
                  )
                )
          }
        }

        "when addInlandModeYesNoPage is no" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddInlandModeYesNoPage, false)
              val helper  = new TransportMeansCheckYourAnswersHelper(answers, mode)
              val result  = helper.addInlandModeYesNo

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add an inland mode of transport?".toText),
                  value = Value("No - the goods are already at the port or airport".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transportMeans.routes.AddInlandModeYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want to add an inland mode of transport"),
                          attributes = Map("id" -> "change-add-transport-means-inland-mode")
                        )
                      )
                    )
                  )
                )
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
              result must not be defined
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

              result.value mustEqual
                SummaryListRow(
                  key = Key("Mode".toText),
                  value = Value(inlandMode.asString.toText),
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
              result must not be defined
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

              result.value mustEqual
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
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when ModeCrossingBorderPage defined" in {
          forAll(arbitrary[Mode], arbitrary[BorderMode]) {
            (mode, borderModeOfTransport) =>
              val answers = emptyUserAnswers.setValue(BorderModeOfTransportPage, borderModeOfTransport)
              val helper  = new TransportMeansCheckYourAnswersHelper(answers, mode)
              val result  = helper.modeCrossingBorder

              result.value mustEqual
                SummaryListRow(
                  key = Key("Border mode of transport".toText),
                  value = Value(borderModeOfTransport.asString.toText),
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
              result must not be defined
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

              result.value mustEqual
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
          }
        }
      }
    }
  }
}
