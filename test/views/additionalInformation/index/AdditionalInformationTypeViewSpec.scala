/*
 * Copyright 2024 HM Revenue & Customs
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

package views.additionalInformation.index

import forms.SelectableFormProvider.AdditionalInformationTypeFormProvider
import models.reference.additionalInformation.AdditionalInformationCode
import models.{NormalMode, SelectableList}
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.InputSelectViewBehaviours
import views.html.additionalInformation.index.AdditionalInformationTypeView

class AdditionalInformationTypeViewSpec extends InputSelectViewBehaviours[AdditionalInformationCode] {

  val formProvider                                   = new AdditionalInformationTypeFormProvider()
  override val field: String                         = formProvider.field
  override def form: Form[AdditionalInformationCode] = new AdditionalInformationTypeFormProvider().apply(prefix, SelectableList(values))

  override def applyView(form: Form[AdditionalInformationCode]): HtmlFormat.Appendable =
    injector
      .instanceOf[AdditionalInformationTypeView]
      .apply(form, lrn, values, NormalMode, additionalReferenceIndex)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[AdditionalInformationCode] = arbitraryAdditionalInformationCode

  override val prefix: String =
    "additionalInformation.index.additionalInformationType"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Additional information")

  behave like pageWithHeading()

  behave like pageWithSelect()

  behave like pageWithHint("Enter the information name or code, like Export or 20300.")

  behave like pageWithSubmitButton("Save and continue")
}
