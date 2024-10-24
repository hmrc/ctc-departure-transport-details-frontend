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

package views.supplyChainActors

import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.supplyChainActors.SupplyChainActorYesNoView

class SupplyChainActorYesNoViewSpec extends YesNoViewBehaviours {

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[SupplyChainActorYesNoView].apply(form, lrn, NormalMode)(fakeRequest, messages)

  override val prefix: String = "supplyChainActors.supplyChainActorYesNo"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Supply chain actor")

  behave like pageWithContent(
    "p",
    "This is someone who helps move the goods to their destination. They can be a consolidator, freight forwarder, manufacturer or warehouse keeper."
  )

  behave like pageWithContent("p", "You can add supply chain actors for specific items in your Items section.")

  behave like pageWithContent("p", "Adding a supply chain actor is optional.")

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Save and continue")
}
