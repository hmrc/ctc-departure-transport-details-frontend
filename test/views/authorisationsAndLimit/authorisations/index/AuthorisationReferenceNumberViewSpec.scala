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

import base.SpecBase
import forms.AuthorisationReferenceNumberFormProvider
import generators.Generators
import models.NormalMode
import models.reference.authorisations.AuthorisationType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import views.behaviours.InputTextViewBehaviours
import views.html.authorisationsAndLimit.authorisations.index.AuthorisationReferenceNumberView

class AuthorisationReferenceNumberViewSpec extends InputTextViewBehaviours[String] with Generators with SpecBase {

  override val prefix: String = "authorisations.authorisationReferenceNumber"

  private val authorisationType = arbitrary[AuthorisationType].sample.value

  override def form: Form[String] = new AuthorisationReferenceNumberFormProvider()(prefix, authorisationType.forDisplay)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector
      .instanceOf[AuthorisationReferenceNumberView]
      .apply(form, lrn, authorisationType, NormalMode, authorisationIndex)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  private val paragraphACR: String = "This authorisation is for the status of authorised consignor for Union transit."

  private val paragraphTRD: String = "You need to enter this as you are using a reduced data set."

  private val paragraphSSE: String = "You need to enter this as you are using seals of a special type."

  behave like pageWithTitle(authorisationType.forDisplay)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Authorisations")

  behave like pageWithHeading(authorisationType.forDisplay)

  behave like pageWithHint("This can be up to 35 characters long and include both letters and numbers.")

  behave like pageWithInputText(Some(InputSize.Width20))

  behave like pageWithSubmitButton("Save and continue")

  "when auth type is ACR" - {
    val authorisationType = arbitrary[AuthorisationType](arbitraryACRAuthorisationType).sample.value
    val view              = injector.instanceOf[AuthorisationReferenceNumberView]
    val doc = parseView(
      view.apply(form, lrn, authorisationType, NormalMode, authorisationIndex)(fakeRequest, messages)
    )

    behave like pageWithContent(doc, "p", paragraphACR)
  }

  "when auth type is not ACR" - {
    val authorisationType = arbitrary[AuthorisationType](arbitraryNonACRAuthorisationType).sample.value
    val view              = injector.instanceOf[AuthorisationReferenceNumberView]
    val doc = parseView(
      view.apply(form, lrn, authorisationType, NormalMode, authorisationIndex)(fakeRequest, messages)
    )

    behave like pageWithoutContent(doc, "p", paragraphACR)
  }

  "when auth type is TRD" - {
    val authorisationType = arbitrary[AuthorisationType](arbitraryTRDAuthorisationType).sample.value
    val view              = injector.instanceOf[AuthorisationReferenceNumberView]
    val doc = parseView(
      view.apply(form, lrn, authorisationType, NormalMode, authorisationIndex)(fakeRequest, messages)
    )

    behave like pageWithContent(doc, "p", paragraphTRD)
  }

  "when auth type is not TRD" - {
    val authorisationType = arbitrary[AuthorisationType](arbitraryNonTRDAuthorisationType).sample.value
    val view              = injector.instanceOf[AuthorisationReferenceNumberView]
    val doc = parseView(
      view.apply(form, lrn, authorisationType, NormalMode, authorisationIndex)(fakeRequest, messages)
    )

    behave like pageWithoutContent(doc, "p", paragraphTRD)
  }

  "when auth type is SSE" - {
    val authorisationType = arbitrary[AuthorisationType](arbitrarySSEAuthorisationType).sample.value
    val view              = injector.instanceOf[AuthorisationReferenceNumberView]
    val doc = parseView(
      view.apply(form, lrn, authorisationType, NormalMode, authorisationIndex)(fakeRequest, messages)
    )

    behave like pageWithContent(doc, "p", paragraphSSE)
  }

  "when auth type is not SSE" - {
    val authorisationType = arbitrary[AuthorisationType](arbitraryNonSSEAuthorisationType).sample.value
    val view              = injector.instanceOf[AuthorisationReferenceNumberView]
    val doc = parseView(
      view.apply(form, lrn, authorisationType, NormalMode, authorisationIndex)(fakeRequest, messages)
    )

    behave like pageWithoutContent(doc, "p", paragraphSSE)
  }
}
