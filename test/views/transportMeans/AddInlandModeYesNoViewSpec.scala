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

import forms.EnumerableFormProvider
import models.NormalMode
import models.transportMeans.InlandModeYesNo
import play.api.data.Form
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.behaviours.RadioViewBehaviours
import views.html.transportMeans.AddInlandModeYesNoView

class AddInlandModeYesNoViewSpec extends RadioViewBehaviours[InlandModeYesNo] {

  override def form: Form[InlandModeYesNo] = new EnumerableFormProvider()(prefix)

  override def applyView(form: Form[InlandModeYesNo]): HtmlFormat.Appendable =
    injector.instanceOf[AddInlandModeYesNoView].apply(form, lrn, values, NormalMode)(fakeRequest, messages)

  override val prefix: String = "transportMeans.addInlandModeYesNo"

  override def radioItems(fieldId: String, checkedValue: Option[InlandModeYesNo] = None): Seq[RadioItem] =
    values.toRadioItems(fieldId, checkedValue)

  override def values: Seq[InlandModeYesNo] = InlandModeYesNo.values

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Inland mode of transport")

  behave like pageWithHeading()

  behave like pageWithContent("p", "This is the mode of transport used from the UK office of departure to a UK port or airport.")

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Save and continue")
}
