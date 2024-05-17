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

package views.additionalInformation.index

import generators.Generators
import models.NormalMode
import org.scalacheck.Gen
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.additionalInformation.RemoveAdditionalInformationYesNoView
import views.html.additionalReference.index.RemoveAdditionalReferenceYesNoView

class RemoveAdditionalInformationYesNoViewSpec extends YesNoViewBehaviours with Generators {

  private val insetText = "Additional information" //todo update once additional information page is built

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[RemoveAdditionalInformationYesNoView]
      .apply(form, lrn, index, insetText, NormalMode)(fakeRequest, messages)

  override val prefix: String = "additionalInformation.index.removeAdditionalInformationYesNo"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Additional information")

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithInsetText("Additional information") //todo update once additional information page is built

  behave like pageWithSubmitButton("Save and continue")

}
