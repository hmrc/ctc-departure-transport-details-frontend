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

package views.authorisationsAndLimit.authorisations.index

import forms.EnumerableFormProvider
import models.NormalMode
import models.reference.authorisations.AuthorisationType
import play.api.data.Form
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.behaviours.EnumerableViewBehaviours
import views.html.authorisationsAndLimit.authorisations.index.AuthorisationTypeView

class AuthorisationTypeViewSpec extends EnumerableViewBehaviours[AuthorisationType] {

  override def form: Form[AuthorisationType] = new EnumerableFormProvider()(prefix, values)

  override def applyView(form: Form[AuthorisationType]): HtmlFormat.Appendable =
    injector.instanceOf[AuthorisationTypeView].apply(form, lrn, values, NormalMode, authorisationIndex)(fakeRequest, messages)

  override val prefix: String = "authorisations.authorisationType"

  override def radioItems(fieldId: String, checkedValue: Option[AuthorisationType] = None): Seq[RadioItem] =
    values.toRadioItems(fieldId, checkedValue)

  override def values: Seq[AuthorisationType] = Seq(
    AuthorisationType("C523", "SSE - authorisation for the use of seals of a special type"),
    AuthorisationType("C524", "TRD - authorisation to use transit declaration with a reduced dataset")
  )

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Authorisations")

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Save and continue")
}
