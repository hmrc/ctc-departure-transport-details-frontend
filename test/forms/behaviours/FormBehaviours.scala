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

package forms.behaviours

import models._
import forms.FormSpec
import play.api.data.Form

trait FormBehaviours extends FormSpec {

  val validData: Map[String, String]

  val form: Form[?]

  def questionForm[A](expectedResult: A) =
    "must  bind valid values correctly" in {
      val boundForm = form.bind(validData)
      boundForm.get mustEqual expectedResult
    }

  def formWithOptionalTextFields(fields: String*) =
    for (field <- fields)
      s"must bind when $field is omitted" in {
        val data      = validData - field
        val boundForm = form.bind(data)
        boundForm.errors.isEmpty mustEqual true
      }

  def formWithMandatoryTextFields(fields: Field*) =
    for (field <- fields) {
      s"must fail to bind when ${field.name} is omitted" in {
        val data          = validData - field.name
        val expectedError = error(field.name, field.errorKeys(Required))
        checkForError(form, data, expectedError)
      }

      s"must fail to bind when ${field.name} is blank" in {
        val data          = validData + (field.name -> "")
        val expectedError = error(field.name, field.errorKeys(Required))
        checkForError(form, data, expectedError)
      }
    }

  def formWithConditionallyMandatoryField(booleanField: String, field: String) = {
    s"must bind when $booleanField is false and $field is omitted" in {
      val data      = validData + (booleanField -> "false") - field
      val boundForm = form.bind(data)
      boundForm.errors.isEmpty mustEqual true
    }

    s"must fail to bind when $booleanField is true and $field is omitted" in {
      val data          = validData + (booleanField -> "true") - field
      val expectedError = error(field, "error.required")
      checkForError(form, data, expectedError)
    }
  }

  def formWithBooleans(fields: String*) =
    for (field <- fields) {
      s"must fail to bind when $field is omitted" in {
        val data          = validData - field
        val expectedError = error(field, "error.boolean")
        checkForError(form, data, expectedError)
      }

      s"must fail to bind when $field is invalid" in {
        val data          = validData + (field -> "invalid value")
        val expectedError = error(field, "error.boolean")
        checkForError(form, data, expectedError)
      }
    }

  def formWithOptionField(field: Field, validValues: String*) = {
    for (validValue <- validValues)
      s"must bind when ${field.name} is set to $validValue" in {
        val data      = validData + (field.name -> validValue)
        val boundForm = form.bind(data)
        boundForm.errors.isEmpty mustEqual true
      }

    s"must fail to bind when ${field.name} is omitted" in {
      val data          = validData - field.name
      val expectedError = error(field.name, field.errorKeys(Required))
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when ${field.name} is invalid" in {
      val data          = validData + (field.name -> "invalid value")
      val expectedError = error(field.name, field.errorKeys(Invalid))
      checkForError(form, data, expectedError)
    }
  }

  def formWithDateField(field: String) = {
    s"must fail to bind when $field day is omitted" in {
      val data          = validData - s"$field.day"
      val expectedError = error(s"$field.day", "error.date.day_blank")
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when $field day is 0" in {
      val data          = validData + (s"$field.day" -> "0")
      val expectedError = error(s"$field.day", "error.date.day_invalid")
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when $field day is greater than 31" in {
      val data          = validData + (s"$field.day" -> "32")
      val expectedError = error(s"$field.day", "error.date.day_invalid")
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when $field day is negative" in {
      val data          = validData + (s"$field.day" -> "-1")
      val expectedError = error(s"$field.day", "error.date.day_invalid")
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when $field day is non-numeric" in {
      val data          = validData + (s"$field.day" -> "invalid")
      val expectedError = error(s"$field.day", "error.date.day_invalid")
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when $field month is omitted" in {
      val data          = validData - s"$field.month"
      val expectedError = error(s"$field.month", "error.date.month_blank")
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when $field month is 0" in {
      val data          = validData + (s"$field.month" -> "0")
      val expectedError = error(s"$field.month", "error.date.month_invalid")
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when $field month is greater than 12" in {
      val data          = validData + (s"$field.month" -> "13")
      val expectedError = error(s"$field.month", "error.date.month_invalid")
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when $field month is negative" in {
      val data          = validData + (s"$field.month" -> "-1")
      val expectedError = error(s"$field.month", "error.date.month_invalid")
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when $field month is non-numeric" in {
      val data          = validData + (s"$field.month" -> "invalid")
      val expectedError = error(s"$field.month", "error.date.month_invalid")
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when $field year is omitted" in {
      val data          = validData - s"$field.year"
      val expectedError = error(s"$field.year", "error.date.year_blank")
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when $field year is 0" in {
      val data          = validData + (s"$field.year" -> "0")
      val expectedError = error(s"$field.year", "error.date.year_invalid")
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when $field year is greater than 2050" in {
      val data          = validData + (s"$field.year" -> "2051")
      val expectedError = error(s"$field.year", "error.date.year_invalid")
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when $field year is negative" in {
      val data          = validData + (s"$field.year" -> "-1")
      val expectedError = error(s"$field.year", "error.date.year_invalid")
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when $field year is non-numeric" in {
      val data          = validData + (s"$field.year" -> "invalid")
      val expectedError = error(s"$field.year", "error.date.year_invalid")
      checkForError(form, data, expectedError)
    }

    s"must fail to bind when the $field is invalid" in {
      val data          = validData + (s"$field.day" -> "30") + (s"$field.month" -> "2")
      val expectedError = error("dateOfBirth", "error.invalid_date")
      checkForError(form, data, expectedError)
    }

  }
}
