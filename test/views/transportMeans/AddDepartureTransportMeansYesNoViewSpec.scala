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

package views.transportMeans

import models.NormalMode
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.transportMeans.departure.{AddDepartureTransportMeansYesNoViewModel, AddIdentificationTypeViewModel}
import views.behaviours.YesNoViewBehaviours
import views.html.transportMeans.AddDepartureTransportMeansYesNoView

class AddDepartureTransportMeansYesNoViewSpec extends YesNoViewBehaviours {

  private val addInlandModeYesNo = arbitrary[Boolean].sample.value

  private def viewModel(addInlandModeYesNo: Boolean) = AddDepartureTransportMeansYesNoViewModel(addInlandModeYesNo)

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[AddDepartureTransportMeansYesNoView].apply(form, lrn, NormalMode, viewModel(addInlandModeYesNo))(fakeRequest, messages)

  override val prefix: String = s"transportMeans.addDepartureTransportMeansYesNo.${if (addInlandModeYesNo) "inlandModeYes" else "inlandModeNo"}"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Departure means of transport")

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Save and continue")

  "when addInlandModeYesNo is false must display paragraph" - {

    val doc =
      parseView(injector.instanceOf[AddDepartureTransportMeansYesNoView].apply(form, lrn, NormalMode, viewModel(false))(fakeRequest, messages))

    behave like pageWithContent(doc, "p", "This is the means of transport used from the UK office of departure to a UK port or airport.")
  }

  "when addInlandModeYesNo is true must not display paragraph" - {

    val doc =
      parseView(injector.instanceOf[AddDepartureTransportMeansYesNoView].apply(form, lrn, NormalMode, viewModel(true))(fakeRequest, messages))

    behave like pageWithoutContent(doc, "p", "This is the means of transport used from the UK office of departure to a UK port or airport.")
  }
}
