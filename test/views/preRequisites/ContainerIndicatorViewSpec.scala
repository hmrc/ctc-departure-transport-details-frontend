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

import models.{NormalMode, OptionalBoolean}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import play.twirl.api.TwirlHelperImports._
import views.behaviours.OptionalYesNoViewBehaviours
import views.html.preRequisites.ContainerIndicatorView

class ContainerIndicatorViewSpec extends OptionalYesNoViewBehaviours {

  override def applyView(form: Form[OptionalBoolean]): HtmlFormat.Appendable =
    applyView(form, "D")

  private def applyView(form: Form[OptionalBoolean], additionalDeclarationType: String): HtmlFormat.Appendable =
    injector.instanceOf[ContainerIndicatorView].apply(form, lrn, NormalMode, additionalDeclarationType)(fakeRequest, messages)

  override val prefix: String = "preRequisites.containerIndicator"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details")

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Save and continue")

  "when pre-lodge declaration" - {
    "must render 3 radio buttons not inlined" in {
      val view = applyView(form, "D")
      val doc  = parseView(view)
      doc.getElementsByClass("govuk-radios__item").toList.size mustBe 3
      assertNotRenderedByClass(doc, "govuk-radios--inline")
    }
  }

  "when standard declaration" - {
    "must render 2 radio buttons inlined" in {
      val view = applyView(form, "A")
      val doc  = parseView(view)
      doc.getElementsByClass("govuk-radios__item").toList.size mustBe 2
      assertRenderedByClass(doc, "govuk-radios--inline")
    }
  }
}
