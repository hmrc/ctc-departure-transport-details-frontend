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

package views.equipment

import forms.EnumerableFormProvider
import models.NormalMode
import models.reference.equipment.PaymentMethod
import play.api.data.Form
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.behaviours.EnumerableViewBehaviours
import views.html.equipment.PaymentMethodView

class PaymentMethodViewSpec extends EnumerableViewBehaviours[PaymentMethod] {

  override def form: Form[PaymentMethod] = new EnumerableFormProvider()(prefix, values)

  override def applyView(form: Form[PaymentMethod]): HtmlFormat.Appendable =
    injector.instanceOf[PaymentMethodView].apply(form, lrn, values, NormalMode)(fakeRequest, messages)

  override val prefix: String = "equipment.paymentMethod"

  override def radioItems(fieldId: String, checkedValue: Option[PaymentMethod] = None): Seq[RadioItem] =
    values.toRadioItems(fieldId, checkedValue)

  override def values: Seq[PaymentMethod] = Seq(
    PaymentMethod("A", "Cash"),
    PaymentMethod("B", "Credit card")
  )

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Transport charges")

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Save and continue")
}
