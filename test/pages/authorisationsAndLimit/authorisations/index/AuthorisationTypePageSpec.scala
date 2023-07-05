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

package pages.authorisationsAndLimit.authorisations.index

import models.authorisations.AuthorisationType
import pages.behaviours.PageBehaviours
import org.scalacheck.Arbitrary.arbitrary

class AuthorisationTypePageSpec extends PageBehaviours {

  "AuthorisationTypePage" - {

    beRetrievable[AuthorisationType](AuthorisationTypePage(index))

    beSettable[AuthorisationType](AuthorisationTypePage(index))

    beRemovable[AuthorisationType](AuthorisationTypePage(index))

    "cleanup" - {
      "must remove authorisation number and inferred value" in {
        forAll(arbitrary[AuthorisationType]) {
          authorisationType =>
            val userAnswers = emptyUserAnswers
              .setValue(InferredAuthorisationTypePage(authorisationIndex), authorisationType)
              .setValue(AuthorisationReferenceNumberPage(authorisationIndex), arbitrary[String].sample.value)

            val result = userAnswers.setValue(AuthorisationTypePage(authorisationIndex), authorisationType)

            result.get(InferredAuthorisationTypePage(authorisationIndex)) must not be defined
            result.get(AuthorisationReferenceNumberPage(authorisationIndex)) must not be defined
        }
      }
    }
  }
}

class InferredAuthorisationTypePageSpec extends PageBehaviours {

  "InferredAuthorisationTypePage" - {

    beRetrievable[AuthorisationType](InferredAuthorisationTypePage(index))

    beSettable[AuthorisationType](InferredAuthorisationTypePage(index))

    beRemovable[AuthorisationType](InferredAuthorisationTypePage(index))

    "cleanup" - {
      "must remove authorisation number and non-inferred value" in {
        forAll(arbitrary[AuthorisationType]) {
          authorisationType =>
            val userAnswers = emptyUserAnswers
              .setValue(AuthorisationTypePage(authorisationIndex), authorisationType)
              .setValue(AuthorisationReferenceNumberPage(authorisationIndex), arbitrary[String].sample.value)

            val result = userAnswers.setValue(InferredAuthorisationTypePage(authorisationIndex), authorisationType)

            result.get(AuthorisationTypePage(authorisationIndex)) must not be defined
            result.get(AuthorisationReferenceNumberPage(authorisationIndex)) must not be defined
        }
      }
    }
  }
}
