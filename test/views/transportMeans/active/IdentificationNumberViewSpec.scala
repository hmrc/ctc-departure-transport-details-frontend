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

package views.transportMeans.active

import base.SpecBase
import forms.IdentificationNumberFormProvider
import generators.Generators
import models.NormalMode
import models.transportMeans.active.Identification
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import views.behaviours.InputTextViewBehaviours
import views.html.transportMeans.active.IdentificationNumberView

class IdentificationNumberViewSpec extends InputTextViewBehaviours[String] with Generators with SpecBase {

  override val prefix: String = "transportMeans.active.identificationNumber.withIDType"

  private val identificationType = arbitrary[Identification].sample.value

  private val formProvider = new IdentificationNumberFormProvider()

  override def form: Form[String] = formProvider(prefix, identificationType.forDisplay)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[IdentificationNumberView].apply(form, lrn, NormalMode, activeIndex, prefix, identificationType.forDisplay)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle(identificationType.forDisplay)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Border means of transport")

  behave like pageWithHeading(identificationType.forDisplay)

  behave like pageWithHint(
    "This can be up to 27 characters long and include both letters and numbers."
  )

  behave like pageWithInputText(Some(InputSize.Width20))

  behave like pageWithSubmitButton("Save and continue")

  "when no identification type is present in user answers" - {

    val withNoIDTypePrefix: String = "transportMeans.active.identificationNumber.withNoIDType"
    val form                       = formProvider(withNoIDTypePrefix)
    val view                       = injector.instanceOf[IdentificationNumberView].apply(form, lrn, NormalMode, activeIndex, withNoIDTypePrefix)(fakeRequest, messages)
    val doc                        = parseView(view)

    behave like pageWithTitle(doc, withNoIDTypePrefix)

    behave like pageWithHeading(doc, withNoIDTypePrefix)
  }
}
