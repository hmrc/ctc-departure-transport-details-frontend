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

package navigation

import config.FrontendAppConfig
import models.{Index, Mode, UserAnswers}
import play.api.mvc.Call

class FakeNavigator(desiredRoute: Call) extends Navigator {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeTransportNavigator(desiredRoute: Call, mode: Mode)(implicit config: FrontendAppConfig) extends TransportNavigator(mode) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeTransportMeansNavigator(desiredRoute: Call, mode: Mode)(implicit config: FrontendAppConfig) extends TransportMeansNavigator(mode) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeTransportMeansActiveNavigator(desiredRoute: Call, mode: Mode, index: Index)(implicit config: FrontendAppConfig)
    extends TransportMeansActiveNavigator(mode, index) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeTransportMeansActiveListNavigator(desiredRoute: Call, mode: Mode)(implicit config: FrontendAppConfig)
    extends TransportMeansActiveListNavigator(mode) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeSupplyChainActorNavigator(desiredRoute: Call, mode: Mode, index: Index)(implicit config: FrontendAppConfig)
    extends SupplyChainActorNavigator(mode, index) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeAuthorisationNavigator(desiredRoute: Call, mode: Mode, index: Index)(implicit config: FrontendAppConfig) extends AuthorisationNavigator(mode, index) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeEquipmentsNavigator(desiredRoute: Call, mode: Mode)(implicit config: FrontendAppConfig) extends EquipmentsNavigator(mode) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeEquipmentNavigator(desiredRoute: Call, index: Index, mode: Mode)(implicit config: FrontendAppConfig) extends EquipmentNavigator(mode, index) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeSealNavigator(desiredRoute: Call, equipmentIndex: Index, sealIndex: Index, mode: Mode)(implicit config: FrontendAppConfig)
    extends SealNavigator(mode, equipmentIndex, sealIndex) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeItemNumberNavigator(desiredRoute: Call, equipmentIndex: Index, itemNumberIndex: Index, mode: Mode)(implicit config: FrontendAppConfig)
    extends ItemNumberNavigator(mode, equipmentIndex, itemNumberIndex) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}
