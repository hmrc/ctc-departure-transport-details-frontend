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

package views.additionalReference.index

import forms.SelectableFormProvider.AdditionalReferenceTypeFormProvider
import models.reference.additionalReference.AdditionalReferenceType
import models.{NormalMode, SelectableList}
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.InputSelectViewBehaviours
import views.html.additionalReference.index.AdditionalReferenceTypeView

class AdditionalReferenceTypeViewSpec extends InputSelectViewBehaviours[AdditionalReferenceType] {

  val formProvider                                 = new AdditionalReferenceTypeFormProvider()
  override val field: String                       = formProvider.field
  override def form: Form[AdditionalReferenceType] = formProvider.apply(prefix, SelectableList(values))

  override def applyView(form: Form[AdditionalReferenceType]): HtmlFormat.Appendable =
    injector
      .instanceOf[AdditionalReferenceTypeView]
      .apply(form, lrn, values, NormalMode, additionalReferenceIndex)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[AdditionalReferenceType] = arbitraryAdditionalReferenceType

  override val prefix: String =
    "additionalReference.index.additionalReferenceType"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Additional reference")

  behave like pageWithHeading()

  behave like pageWithSelect()

  behave like pageWithHint("Enter the reference name or code, like Carrier (AEO certificate number) or Y028.")

  behave like pageWithSubmitButton("Save and continue")
}
