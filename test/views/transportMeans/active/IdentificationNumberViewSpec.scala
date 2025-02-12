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

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.IdentificationNumberFormProvider
import generators.Generators
import models.NormalMode
import models.reference.transportMeans.active.Identification
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.Application
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import views.behaviours.InputTextViewBehaviours
import views.html.transportMeans.active.IdentificationNumberView

class IdentificationNumberViewSpec extends InputTextViewBehaviours[String] with Generators with SpecBase with AppWithDefaultMockFixtures {

  override val prefix: String = "transportMeans.active.identificationNumber"

  private val identificationType   = arbitrary[Identification].sample.value
  private val identificationNumber = "idNumber"

  override def form: Form[String] = app.injector.instanceOf[IdentificationNumberFormProvider].apply(prefix)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    applyView(app, form)

  private def applyView(app: Application, form: Form[String]): HtmlFormat.Appendable =
    app.injector.instanceOf[IdentificationNumberView].apply(form, lrn, NormalMode, activeIndex, identificationType.asString)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Border means of transport")

  behave like pageWithHeading()

  behave like pageWithHint("This can be up to 35 characters long and include both letters and numbers.")

  behave like pageWithInputText(Some(InputSize.Width20))

  behave like pageWithSubmitButton("Save and continue")

  "when no identification type is present in user answers" - {

    val prefix: String = "transportMeans.active.identificationNumber"
    val form           = app.injector.instanceOf[IdentificationNumberFormProvider].apply(prefix)
    val view           = injector.instanceOf[IdentificationNumberView].apply(form, lrn, NormalMode, activeIndex, identificationNumber)(fakeRequest, messages)
    val doc            = parseView(view)

    behave like pageWithTitle(doc, prefix)

    behave like pageWithHeading(doc, prefix)
  }
}
