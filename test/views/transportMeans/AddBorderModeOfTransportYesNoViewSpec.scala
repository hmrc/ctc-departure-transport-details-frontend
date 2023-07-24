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
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.transportMeans.AddBorderModeOfTransportYesNoView

class AddBorderModeOfTransportYesNoViewSpec extends YesNoViewBehaviours {

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[AddBorderModeOfTransportYesNoView].apply(form, lrn, NormalMode)(fakeRequest, messages)

  override val prefix: String = "transportMeans.addBorderModeOfTransportYesNo"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Border mode of transport")

  behave like pageWithHeading()

  behave like pageWithContent("p", "This is the mode of transport used from a UK port or airport to another customs territory. For example, road, sea or air.")

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Save and continue")
}
