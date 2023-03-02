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

package pages.equipment.index.seals

import pages.behaviours.PageBehaviours

class IdentificationNumberPageSpec extends PageBehaviours {

  "IdentificationNumberPage" - {

    beRetrievable[String](IdentificationNumberPage(equipmentIndex, sealIndex))

    beSettable[String](IdentificationNumberPage(equipmentIndex, sealIndex))

    beRemovable[String](IdentificationNumberPage(equipmentIndex, sealIndex))
  }
}