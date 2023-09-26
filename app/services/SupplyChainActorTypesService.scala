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

package services

import connectors.ReferenceDataConnector
import models.reference.supplyChainActors.SupplyChainActorType
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SupplyChainActorTypesService @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def getSupplyChainActorTypes()(implicit hc: HeaderCarrier): Future[Seq[SupplyChainActorType]] =
    referenceDataConnector.getSupplyChainActorTypes().map(sort)

  private def sort(supplyChainActorTypes: Seq[SupplyChainActorType]): Seq[SupplyChainActorType] =
    supplyChainActorTypes.sortBy(_.code.toLowerCase)
}