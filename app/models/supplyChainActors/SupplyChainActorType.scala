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

package models.supplyChainActors

import models.{RadioModel, WithName}
import play.api.i18n.Messages

sealed trait SupplyChainActorType {

  def asString(implicit messages: Messages): String =
    messages(s"${SupplyChainActorType.messageKeyPrefix}.$this")
}

object SupplyChainActorType extends RadioModel[SupplyChainActorType] {

  case object Consolidator extends WithName("consolidator") with SupplyChainActorType
  case object FreightForwarder extends WithName("freightForwarder") with SupplyChainActorType
  case object Manufacturer extends WithName("manufacturer") with SupplyChainActorType
  case object WarehouseKeeper extends WithName("warehouseKeeper") with SupplyChainActorType

  override val messageKeyPrefix: String = "supplyChainActors.index.supplyChainActorType"

  val values: Seq[SupplyChainActorType] = Seq(
    Consolidator,
    FreightForwarder,
    Manufacturer,
    WarehouseKeeper
  )
}
