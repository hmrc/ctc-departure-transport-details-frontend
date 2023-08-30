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
import views.behaviours.OptionalYesNoViewBehaviours
import views.html.preRequisites.ContainerIndicatorView

class ContainerIndicatorViewSpec extends OptionalYesNoViewBehaviours {

  override def applyView(form: Form[OptionalBoolean]): HtmlFormat.Appendable =
    injector.instanceOf[ContainerIndicatorView].apply(form, lrn, NormalMode)(fakeRequest, messages)

  override val prefix: String = "preRequisites.containerIndicator"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details")

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Save and continue")

  /*"when two radio buttons" - {
    "must inline them" in {
      val view = applyView(form, Seq(True, False))
      val doc  = parseView(view)
      assertRenderedByClass(doc, "govuk-radios--inline")
    }
  }

  "when three radio buttons" - {
    "must not inline them" in {
      val view = applyView(form, Seq(True, False, Maybe))
      val doc  = parseView(view)
      assertNotRenderedByClass(doc, "govuk-radios--inline")
    }
  }*/
}
