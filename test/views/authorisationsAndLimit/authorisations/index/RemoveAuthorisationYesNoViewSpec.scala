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

import models.NormalMode
import models.authorisations.AuthorisationType
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.authorisationsAndLimit.authorisations.index.RemoveAuthorisationYesNoView

class RemoveAuthorisationYesNoViewSpec extends YesNoViewBehaviours {

  private val authorisationType = Gen.oneOf(AuthorisationType.values).sample.value.forDisplay

  private val authorisationReferenceNumber = Arbitrary(Gen.alphaStr).arbitrary.sample.value

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[RemoveAuthorisationYesNoView]
      .apply(form, lrn, NormalMode, authorisationIndex, authorisationType, authorisationReferenceNumber)(fakeRequest, messages)

  override val prefix: String = "authorisations.index.removeAuthorisationYesNo"

  behave like pageWithTitle(authorisationType, authorisationReferenceNumber)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Authorisations")

  behave like pageWithHeading(authorisationType, authorisationReferenceNumber)

  behave like pageWithRadioItems(args = Seq(authorisationType, authorisationReferenceNumber))

  behave like pageWithSubmitButton("Save and continue")
}
