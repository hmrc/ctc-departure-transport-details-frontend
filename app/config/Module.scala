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

package config

import com.google.inject.AbstractModule
import controllers.actions._
import navigation._

import java.time.{Clock, ZoneOffset}

class Module extends AbstractModule {

  override def configure(): Unit = {

    bind(classOf[IdentifierAction]).to(classOf[IdentifierActionImpl]).asEagerSingleton()
    bind(classOf[DataRetrievalActionProvider]).to(classOf[DataRetrievalActionProviderImpl]).asEagerSingleton()
    bind(classOf[DataRequiredActionProvider]).to(classOf[DataRequiredActionImpl]).asEagerSingleton()
    bind(classOf[LockActionProvider]).to(classOf[LockActionProviderImpl]).asEagerSingleton()
    bind(classOf[DependentTasksAction]).to(classOf[DependentTasksActionImpl]).asEagerSingleton()
    bind(classOf[SpecificDataRequiredActionProvider]).to(classOf[SpecificDataRequiredActionImpl]).asEagerSingleton()
    bind(classOf[IndexRequiredActionProvider]).to(classOf[IndexRequiredActionProviderImpl]).asEagerSingleton()

    bind(classOf[TransportNavigatorProvider]).to(classOf[TransportNavigatorProviderImpl])
    bind(classOf[TransportMeansNavigatorProvider]).to(classOf[TransportMeansNavigatorProviderImpl])
    bind(classOf[TransportMeansActiveNavigatorProvider]).to(classOf[TransportMeansActiveNavigatorProviderImpl])
    bind(classOf[TransportMeansActiveListNavigatorProvider]).to(classOf[TransportMeansActiveListNavigatorProviderImpl])
    bind(classOf[SupplyChainActorNavigatorProvider]).to(classOf[SupplyChainActorNavigatorProviderImpl])
    bind(classOf[AuthorisationNavigatorProvider]).to(classOf[AuthorisationNavigatorProviderImpl])
    bind(classOf[EquipmentsNavigatorProvider]).to(classOf[EquipmentsNavigatorProviderImpl])
    bind(classOf[EquipmentNavigatorProvider]).to(classOf[EquipmentNavigatorProviderImpl])
    bind(classOf[SealNavigatorProvider]).to(classOf[SealNavigatorProviderImpl])

    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))
  }
}
