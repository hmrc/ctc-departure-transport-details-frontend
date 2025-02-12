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
import models.journeyDomain.UserAnswersReader
import models.journeyDomain.supplyChainActors.SupplyChainActorDomain
import models.{CheckMode, Index, Mode, NormalMode}

import javax.inject.{Inject, Singleton}

@Singleton
class SupplyChainActorNavigatorProviderImpl @Inject() (implicit appConfig: FrontendAppConfig) extends SupplyChainActorNavigatorProvider {

  override def apply(mode: Mode, index: Index): UserAnswersNavigator =
    mode match {
      case NormalMode => new SupplyChainActorNavigator(mode, index)
      case CheckMode  => new TransportNavigator(mode)
    }
}

trait SupplyChainActorNavigatorProvider {
  def apply(mode: Mode, index: Index): UserAnswersNavigator
}

class SupplyChainActorNavigator(override val mode: Mode, index: Index)(implicit
  override val appConfig: FrontendAppConfig
) extends UserAnswersNavigator {

  override type T = SupplyChainActorDomain

  implicit override val reader: UserAnswersReader[SupplyChainActorDomain] =
    SupplyChainActorDomain.userAnswersReader(index).apply(Nil)
}
