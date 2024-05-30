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

package views.transportMeans.departure

import models.NormalMode
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.transportMeans.departure.AddIdentificationTypeViewModel
import views.behaviours.YesNoViewBehaviours
import views.html.transportMeans.departure.AddIdentificationTypeYesNoView

class AddIdentificationTypeYesNoViewSpec extends YesNoViewBehaviours {

  private val addInlandModeYesNo = false

  private def viewModel(addInlandModeYesNo: Boolean) = AddIdentificationTypeViewModel(addInlandModeYesNo)

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[AddIdentificationTypeYesNoView].apply(form, lrn, NormalMode, departureIndex, viewModel(addInlandModeYesNo))(fakeRequest, messages)

  override val prefix: String = s"transportMeans.departure.addIdentificationTypeYesNo.${if (addInlandModeYesNo) "inlandModeYes" else "inlandModeNo"}"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Departure means of transport")

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Save and continue")

  "when addInlandModeYesNo is false must display paragraph" - {

    val doc =
      parseView(injector.instanceOf[AddIdentificationTypeYesNoView].apply(form, lrn, NormalMode, departureIndex, viewModel(false))(fakeRequest, messages))

    behave like pageWithContent(doc, "p", "This is the means of transport used from the UK office of departure to a UK port or airport.")
  }

  "when addInlandModeYesNo is true must not display paragraph" - {

    val doc =
      parseView(injector.instanceOf[AddIdentificationTypeYesNoView].apply(form, lrn, NormalMode, departureIndex, viewModel(true))(fakeRequest, messages))

    behave like pageWithoutContent(doc, "p", "This is the means of transport used from the UK office of departure to a UK port or airport.")
  }
}
