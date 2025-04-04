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

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.DepartureTransportMeansIdentificationNumberFormProvider
import generators.Generators
import models.NormalMode
import models.reference.transportMeans.departure.Identification
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.Application
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import viewModels.transportMeans.departure.MeansIdentificationNumberViewModel
import views.behaviours.InputTextViewBehaviours
import views.html.transportMeans.departure.MeansIdentificationNumberView

class MeansIdentificationNumberViewSpec extends InputTextViewBehaviours[String] with Generators with SpecBase with AppWithDefaultMockFixtures {

  private val viewModel       = arbitrary[MeansIdentificationNumberViewModel].sample.value
  override val prefix: String = viewModel.prefix

  private def formProvider(prefix: String): Form[String] =
    app.injector.instanceOf[DepartureTransportMeansIdentificationNumberFormProvider].apply(prefix)

  override def form: Form[String] = formProvider(prefix)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    applyView(app, form, viewModel)

  private def applyView(app: Application, form: Form[String], viewModel: MeansIdentificationNumberViewModel): HtmlFormat.Appendable =
    app.injector.instanceOf[MeansIdentificationNumberView].apply(form, lrn, NormalMode, viewModel, departureIndex)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Departure means of transport")

  behave like pageWithHint("This can be up to 35 characters long and include both letters and numbers.")

  behave like pageWithInputText(Some(InputSize.Width20))

  behave like pageWithSubmitButton("Save and continue")

  "when no identification type is present in user answers" - {

    val viewModel      = MeansIdentificationNumberViewModel(None)
    val prefix: String = "transportMeans.departure.meansIdentificationNumber.withNoIDType"
    val view           = applyView(app, formProvider(prefix), viewModel)
    val doc            = parseView(view)

    behave like pageWithTitle(doc, prefix)

    behave like pageWithHeading(doc, prefix)

    behave like pageWithoutInsetText(doc)
  }

  "when identification type is present in user answers" - {

    val identification = arbitrary[Identification].sample.value
    val viewModel      = MeansIdentificationNumberViewModel(Some(identification))
    val prefix: String = "transportMeans.departure.meansIdentificationNumber.withIDType"
    val view           = applyView(app, formProvider(prefix), viewModel)
    val doc            = parseView(view)

    behave like pageWithTitle(doc, prefix)

    behave like pageWithHeading(doc, prefix)

    behave like pageWithInsetText(doc, identification.asString)
  }
}
