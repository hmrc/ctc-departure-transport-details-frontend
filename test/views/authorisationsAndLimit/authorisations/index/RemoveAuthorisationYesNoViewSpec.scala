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

import generators.Generators
import models.NormalMode
import models.reference.authorisations.AuthorisationType
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.authorisationsAndLimit.authorisations.index.RemoveAuthorisationYesNoView

class RemoveAuthorisationYesNoViewSpec extends YesNoViewBehaviours with Generators {

  private val authorisationType = arbitrary[AuthorisationType].sample.value.forDisplay

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[RemoveAuthorisationYesNoView]
      .apply(form, lrn, NormalMode, authorisationIndex, authorisationType)(fakeRequest, messages)

  override val prefix: String = "authorisations.index.removeAuthorisationYesNo"

  behave like pageWithTitle(authorisationType)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Authorisations")

  behave like pageWithHeading(authorisationType)

  behave like pageWithRadioItems(args = Seq(authorisationType))

  behave like pageWithSubmitButton("Save and continue")
}
