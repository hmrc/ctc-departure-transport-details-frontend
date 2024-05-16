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

import forms.YesNoFormProvider
import generators.Generators
import models.NormalMode
import models.reference.additionalReference.AdditionalReferenceType
import models.removable.AdditionalReference
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.additionalReference.index.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.additionalReference.index.RemoveAdditionalReferenceYesNoView

class RemoveAdditionalReferenceYesNoViewSpec extends YesNoViewBehaviours with Generators {

  private val additionalReferenceType   = arbitrary[AdditionalReferenceType].sample.value
  private val additionalReferenceNumber = Gen.alphaStr.sample.value

  private val userAnswersFullAdditionalReference = emptyUserAnswers
    .setValue(AdditionalReferenceTypePage(index), additionalReferenceType)
    .setValue(AdditionalReferenceNumberPage(index), additionalReferenceNumber)

  private val userAnswersPartialAdditionalReference = emptyUserAnswers
    .setValue(AdditionalReferenceTypePage(index), additionalReferenceType)
    .setValue(AdditionalReferenceNumberPage(index), None)

  private val additionalReference: Option[AdditionalReference]              = AdditionalReference(userAnswersFullAdditionalReference, index)
  private val additionalReferenceWithoutNumber: Option[AdditionalReference] = AdditionalReference(userAnswersPartialAdditionalReference, index)

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[RemoveAdditionalReferenceYesNoView]
      .apply(form, lrn, NormalMode, additionalReferenceIndex, additionalReference.map(_.forRemoveDisplay))(fakeRequest, messages)

  override val prefix: String = "additionalReference.index.removeAdditionalReference"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Additional reference")

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithInsetText(s"$additionalReferenceType - $additionalReferenceNumber")

  behave like pageWithSubmitButton("Save and continue")

  "without a additional reference number" - {
    val form: Form[Boolean] = new YesNoFormProvider()(prefix)
    val view = injector
      .instanceOf[RemoveAdditionalReferenceYesNoView]
      .apply(form, lrn, NormalMode, additionalReferenceIndex, additionalReferenceWithoutNumber.map(_.forRemoveDisplay))(fakeRequest, messages)
    val doc = parseView(view)

    behave like pageWithInsetText(doc, s"$additionalReferenceType")
  }
}
