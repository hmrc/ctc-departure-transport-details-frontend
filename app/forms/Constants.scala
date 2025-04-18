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

object Constants {
  lazy val maxAuthorisationRefNumberLength: Int = 35
  lazy val maxTelephoneNumberLength: Int        = 35
  lazy val minTelephoneNumberLength: Int        = 6
  lazy val maxNameLength: Int                   = 70
  lazy val maxEoriNumberLength: Int             = 17
  lazy val minLengthCarrierEori                 = 3
  lazy val conveyanceRefNumberLength: Int       = 17
  lazy val maxUCRLength: Int                    = 35
  lazy val maxSealIdNumberLength: Int           = 20
  lazy val itemNumberLength: Int                = 4
  lazy val itemNumberMax: Int                   = 1999
  lazy val maxContainerIdNumberLength: Int      = 17
  lazy val maxAdditionalReferenceNumLength: Int = 70
  lazy val maxAdditionalInfoTextLength: Int     = 512
  lazy val maxIdentificationNumberLength: Int   = 35
}
