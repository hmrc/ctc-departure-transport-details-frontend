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

package views.preRequisites

import forms.SelectableFormProvider.CountryFormProvider
import generators.Generators
import models.reference.Country
import models.{NormalMode, SelectableList}
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.InputSelectViewBehaviours
import views.html.preRequisites.ItemsDestinationCountryView

class ItemsDestinationCountryViewSpec extends InputSelectViewBehaviours[Country] with Generators {

  val formProvider = new CountryFormProvider()

  override val field: String       = formProvider.field
  override def form: Form[Country] = formProvider(prefix, SelectableList(values))

  override def applyView(form: Form[Country]): HtmlFormat.Appendable =
    injector.instanceOf[ItemsDestinationCountryView].apply(form, lrn, values, NormalMode)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[Country] = arbitraryCountry

  override val prefix: String = "preRequisites.itemsDestinationCountry"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details")

  behave like pageWithHeading()

  behave like pageWithHint("Enter the country, like France or Portugal.")

  behave like pageWithSelect()

  behave like pageWithSubmitButton("Save and continue")
}
