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

import models.NormalMode
import org.scalacheck.Gen
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.transportMeans.active.ConfirmRemoveBorderTransportView

class ConfirmRemoveBorderTransportViewSpec extends YesNoViewBehaviours {

  private val insetText = Gen.alphaNumStr.sample.value

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[ConfirmRemoveBorderTransportView].apply(form, lrn, NormalMode, activeIndex, Some(insetText))(fakeRequest, messages)

  override val prefix: String = "transportMeans.active.confirmRemoveBorderTransport"

  behave like pageWithTitle(activeIndex.display)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Border means of transport")

  behave like pageWithHeading(activeIndex.display)

  behave like pageWithRadioItems(args = Seq(activeIndex.display))

  behave like pageWithInsetText(insetText)

  behave like pageWithSubmitButton("Save and continue")

  "when inset text undefined" - {
    val view = injector
      .instanceOf[ConfirmRemoveBorderTransportView]
      .apply(form, lrn, NormalMode, activeIndex, None)(fakeRequest, messages)
    val doc = parseView(view)

    behave like pageWithoutInsetText(doc)
  }
}
