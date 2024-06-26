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

package forms

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.Constants.maxAdditionalInfoTextLength
import forms.behaviours.StringFieldBehaviours
import models.domain.StringFieldRegex.stringFieldRegexComma
import org.scalacheck.Gen
import play.api.data.FormError

class AdditionalInformationCodeFormProviderSpec extends StringFieldBehaviours with SpecBase with AppWithDefaultMockFixtures {

  private val prefix      = Gen.alphaNumStr.sample.value
  private val requiredKey = s"$prefix.error.required"
  private val invalidKey  = s"$prefix.error.invalidCharacters"
  private val lengthKey   = s"$prefix.error.length"

  val form = new AdditionalInformationFormProvider()(prefix)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form = form,
      fieldName = fieldName,
      validDataGenerator = stringsWithMaxLength(maxAdditionalInfoTextLength)
    )

    behave like mandatoryField(
      form = form,
      fieldName = fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithMaxLength(
      form = form,
      fieldName = fieldName,
      maxLength = maxAdditionalInfoTextLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxAdditionalInfoTextLength))
    )

    behave like fieldWithInvalidCharacters(
      form = form,
      fieldName = fieldName,
      error = FormError(fieldName, invalidKey, Seq(stringFieldRegexComma.regex)),
      length = maxAdditionalInfoTextLength
    )
  }
}
