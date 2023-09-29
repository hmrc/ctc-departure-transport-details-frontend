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

package views.behaviours

import forms.OptionalYesNoFormProvider
import models.OptionalBoolean
import play.api.data.Form
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

trait OptionalYesNoViewBehaviours extends RadioViewBehaviours[OptionalBoolean] {

  override val getValue: OptionalBoolean => String = _.toString

  override def form: Form[OptionalBoolean] = new OptionalYesNoFormProvider()(prefix)

  override def radioItems(fieldId: String, checkedValue: Option[OptionalBoolean]): Seq[RadioItem] =
    Seq(
      RadioItem(
        content = Text("Yes"),
        id = Some(fieldId),
        value = Some("true")
      ),
      RadioItem(
        content = Text("No"),
        id = Some(s"$fieldId-no"),
        value = Some("false")
      ),
      RadioItem(
        content = Text("Not sure"),
        id = Some(s"$fieldId-maybe"),
        value = Some("maybe")
      )
    )

  override def values: Seq[OptionalBoolean] = Seq(OptionalBoolean.yes, OptionalBoolean.no, OptionalBoolean.maybe)
}
