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

import forms.EnumerableFormProvider
import models.{Index, NormalMode}
import models.reference.InlandMode
import models.reference.transportMeans.departure.Identification
import play.api.data.Form
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import viewModels.transportMeans.departure.IdentificationViewModel
import views.behaviours.EnumerableViewBehaviours
import views.html.transportMeans.departure.IdentificationView

// when inland mode is 3(road) and there is one departure means of transport:
class IdentificationViewSingularSpec extends EnumerableViewBehaviours[Identification] {

  override def form: Form[Identification] = new EnumerableFormProvider()(prefix, values)
  private val identificationViewModel     = new IdentificationViewModel(Some(Identification("2", "test")))

  override def applyView(form: Form[Identification]): HtmlFormat.Appendable =
    injector
      .instanceOf[IdentificationView]
      .apply(form, lrn, values, NormalMode, departureIndex, identificationViewModel, InlandMode("3", ""))(fakeRequest, messages)

  override val prefix: String = "transportMeans.departure.identification"

  override def radioItems(fieldId: String, checkedValue: Option[Identification] = None): Seq[RadioItem] =
    values.toRadioItems(fieldId, checkedValue)

  override def values: Seq[Identification] = Seq(
    Identification("11", "Name of a sea-going vessel")
  )

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Departure means of transport")

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithContent("p", "You must add the registration number of the road vehicle for your inland mode.")

  behave like pageWithSubmitButton("Save and continue")
}

// when inland mode is 3(road) and there is more than one departure means of transport:
class IdentificationViewMultipleSpec extends EnumerableViewBehaviours[Identification] {

  override def form: Form[Identification] = new EnumerableFormProvider()(prefix, values)
  private val identificationViewModel     = new IdentificationViewModel(Some(Identification("2", "test")))

  override def applyView(form: Form[Identification]): HtmlFormat.Appendable =
    injector
      .instanceOf[IdentificationView]
      .apply(form, lrn, values, NormalMode, Index(1), identificationViewModel, InlandMode("3", ""))(fakeRequest, messages)

  override val prefix: String = "transportMeans.departure.identification"

  override def radioItems(fieldId: String, checkedValue: Option[Identification] = None): Seq[RadioItem] =
    values.toRadioItems(fieldId, checkedValue)

  override def values: Seq[Identification] = Seq(
    Identification("11", "Name of a sea-going vessel")
  )

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Departure means of transport")

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithContent("p", "You must add the registration number of the road trailer for your inland mode.")

  behave like pageWithSubmitButton("Save and continue")
}

// when inland mode is not 3(road)
class IdentificationViewNotRoadSpec extends EnumerableViewBehaviours[Identification] {

  override def form: Form[Identification] = new EnumerableFormProvider()(prefix, values)
  private val identificationViewModel     = new IdentificationViewModel(Some(Identification("2", "test")))

  override def applyView(form: Form[Identification]): HtmlFormat.Appendable =
    injector
      .instanceOf[IdentificationView]
      .apply(form, lrn, values, NormalMode, departureIndex, identificationViewModel, InlandMode("2", ""))(fakeRequest, messages)

  override val prefix: String = "transportMeans.departure.identification"

  override def radioItems(fieldId: String, checkedValue: Option[Identification] = None): Seq[RadioItem] =
    values.toRadioItems(fieldId, checkedValue)

  override def values: Seq[Identification] = Seq(
    Identification("11", "Name of a sea-going vessel")
  )

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Departure means of transport")

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Save and continue")

  behave like pageWithoutContent(doc, "p", "You must add the registration number of the road vehicle for your inland mode.")
  behave like pageWithoutContent(doc, "p", "You must add the registration number of the road trailer for your inland mode.")
}
