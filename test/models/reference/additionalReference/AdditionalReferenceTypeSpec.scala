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

package models.reference.additionalReference

import base.SpecBase
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

class AdditionalReferenceTypeSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "AdditionalReferenceType" - {

    "must serialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (docType, description) =>
          val additionalReferenceType = AdditionalReferenceType(docType, description)
          Json.toJson(additionalReferenceType) mustBe Json.parse(s"""
              |{
              |  "documentType": "$docType",
              |  "description": "$description"
              |}
              |""".stripMargin)
      }
    }

    "must deserialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (docType, description) =>
          val additionalReferenceType = AdditionalReferenceType(docType, description)
          Json
            .parse(s"""
              |{
              |  "documentType": "$docType",
              |  "description": "$description"
              |}
              |""".stripMargin)
            .as[AdditionalReferenceType] mustBe additionalReferenceType
      }
    }

    "must convert to select item" in {
      forAll(arbitrary[AdditionalReferenceType], arbitrary[Boolean]) {
        (additionalReferenceType, selected) =>
          additionalReferenceType.toSelectItem(selected) mustBe SelectItem(Some(additionalReferenceType.documentType),
                                                                           s"${additionalReferenceType.documentType} - ${additionalReferenceType.description}",
                                                                           selected
          )
      }
    }

    "must format as string" in {
      forAll(arbitrary[AdditionalReferenceType]) {
        additionalReferenceType =>
          additionalReferenceType.toString mustBe s"${additionalReferenceType.documentType} - ${additionalReferenceType.description}"
      }
    }
  }

}
