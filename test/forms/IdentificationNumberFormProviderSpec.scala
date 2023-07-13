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
import models.transportMeans.active.Identification
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.FormError
import play.api.test.Helpers.running

class IdentificationNumberFormProviderSpec extends StringFieldBehaviours with SpecBase with AppWithDefaultMockFixtures {

  private val identificationType = arbitrary[Identification].sample.value
  private val prefix             = Gen.alphaNumStr.sample.value

  private val dynamicTitle = s"$prefix.${identificationType.toString}"
  private val requiredKey  = s"$prefix.error.required"
  private val invalidKey   = s"$prefix.error.invalid"

  private val maxIdentificationNumberTransitionLength: Int     = 27
  private val maxIdentificationNumberPostTransitionLength: Int = 35

  "TransitionIdentificationNumberFormProvider" - {

    val lengthKey = s"$prefix.error.length.transition"
    val app       = transitionApplicationBuilder().build()

    ".value" - {

      running(app) {

        val form      = app.injector.instanceOf[IdentificationNumberFormProvider].apply(prefix, dynamicTitle)
        val fieldName = "value"

        behave like fieldThatBindsValidData(
          form,
          fieldName,
          stringsWithMaxLength(maxIdentificationNumberTransitionLength)
        )

        behave like fieldWithMaxLength(
          form,
          fieldName,
          maxLength = maxIdentificationNumberTransitionLength,
          lengthError = FormError(fieldName, lengthKey, Seq(dynamicTitle))
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, requiredKey, Seq(dynamicTitle))
        )

        behave like fieldWithInvalidCharacters(
          form,
          fieldName,
          error = FormError(fieldName, invalidKey, Seq(dynamicTitle)),
          maxIdentificationNumberTransitionLength
        )
      }
    }

  }

  "PostTransitionIdentificationNumberFormProvider" - {

    val lengthKey = s"$prefix.error.length.postTransition"
    val app       = postTransitionApplicationBuilder().build()

    ".value" - {

      running(app) {

        val form      = app.injector.instanceOf[IdentificationNumberFormProvider].apply(prefix, dynamicTitle)
        val fieldName = "value"

        behave like fieldThatBindsValidData(
          form,
          fieldName,
          stringsWithMaxLength(maxIdentificationNumberPostTransitionLength)
        )

        behave like fieldWithMaxLength(
          form,
          fieldName,
          maxLength = maxIdentificationNumberPostTransitionLength,
          lengthError = FormError(fieldName, lengthKey, Seq(dynamicTitle))
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, requiredKey, Seq(dynamicTitle))
        )

        behave like fieldWithInvalidCharacters(
          form,
          fieldName,
          error = FormError(fieldName, invalidKey, Seq(dynamicTitle)),
          maxIdentificationNumberPostTransitionLength
        )
      }
    }

  }
}
