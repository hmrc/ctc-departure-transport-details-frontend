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

import config.{FrontendAppConfig, PhaseConfig}
import models.journeyDomain.UserAnswersReader
import models.journeyDomain.transportMeans.TransportMeansDomain
import models.{CheckMode, Mode, NormalMode}

import javax.inject.{Inject, Singleton}

@Singleton
class TransportMeansNavigatorProviderImpl @Inject() (implicit appConfig: FrontendAppConfig, phaseConfig: PhaseConfig) extends TransportMeansNavigatorProvider {

  override def apply(mode: Mode): UserAnswersNavigator =
    mode match {
      case NormalMode => new TransportMeansNavigator(mode)
      case CheckMode  => new TransportNavigator(mode)
    }
}

trait TransportMeansNavigatorProvider {
  def apply(mode: Mode): UserAnswersNavigator
}

class TransportMeansNavigator(override val mode: Mode)(implicit override val appConfig: FrontendAppConfig, override val phaseConfig: PhaseConfig)
    extends UserAnswersNavigator {

  override type T = TransportMeansDomain

  implicit override val reader: UserAnswersReader[TransportMeansDomain] =
    TransportMeansDomain.userAnswersReader.apply(Nil)
}
