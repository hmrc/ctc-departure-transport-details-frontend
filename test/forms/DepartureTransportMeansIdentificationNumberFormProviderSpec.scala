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
import forms.behaviours.StringFieldBehaviours
import models.domain.StringFieldRegex.alphaNumericRegex
import org.scalacheck.Gen
import play.api.data.FormError
import play.api.test.Helpers.running

class DepartureTransportMeansIdentificationNumberFormProviderSpec extends StringFieldBehaviours with SpecBase with AppWithDefaultMockFixtures {

  private val prefix = Gen.alphaNumStr.sample.value

  private val requiredKey = s"$prefix.error.required"
  private val invalidKey  = s"$prefix.error.invalid"

  "DepartureTransportMeansIdentificationNumberFormProvider" - {

    val lengthKey = s"$prefix.error.length.transition"
    val app       = transitionApplicationBuilder().build()
    val maxLength = 27

    ".value" - {

      running(app) {

        val form      = app.injector.instanceOf[DepartureTransportMeansIdentificationNumberFormProvider].apply(prefix)
        val fieldName = "value"

        behave like fieldThatBindsValidData(
          form,
          fieldName,
          stringsWithMaxLength(maxLength)
        )

        behave like fieldWithMaxLength(
          form,
          fieldName,
          maxLength = maxLength,
          lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, requiredKey)
        )

        behave like fieldWithInvalidCharacters(
          form,
          fieldName,
          error = FormError(fieldName, invalidKey, Seq(alphaNumericRegex.toString())),
          maxLength
        )

        "must convert input to upper case" in {
          val result = form.bind(Map(fieldName -> "abc123def456"))
          result.value.value mustBe "ABC123DEF456"
        }
      }
    }
  }

  "PostTransitionIdentificationNumberFormProvider" - {

    val lengthKey = s"$prefix.error.length.postTransition"
    val app       = postTransitionApplicationBuilder().build()
    val maxLength = 35

    ".value" - {

      running(app) {

        val form      = app.injector.instanceOf[DepartureTransportMeansIdentificationNumberFormProviderSpec].apply(prefix)
        val fieldName = "value"

        behave like fieldThatBindsValidData(
          form,
          fieldName,
          stringsWithMaxLength(maxLength)
        )

        behave like fieldWithMaxLength(
          form,
          fieldName,
          maxLength = maxLength,
          lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, requiredKey)
        )

        behave like fieldWithInvalidCharacters(
          form,
          fieldName,
          error = FormError(fieldName, invalidKey, Seq(alphaNumericRegex.toString())),
          maxLength
        )

        "must convert input to upper case" in {
          val result = form.bind(Map(fieldName -> "abc123def456"))
          result.value.value mustBe "ABC123DEF456"
        }
      }
    }
  }
}
