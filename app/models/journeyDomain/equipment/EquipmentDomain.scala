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

package models.journeyDomain.equipment

import cats.implicits._
import controllers.equipment.index.routes
import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, JsArrayGettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.equipment.seal.SealsDomain
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.{Index, Mode, Phase, ProcedureType, UserAnswers}
import pages.authorisationsAndLimit.authorisations.index.AuthorisationTypePage
import pages.equipment.index._
import pages.external.ProcedureTypePage
import pages.preRequisites.ContainerIndicatorPage
import pages.sections.authorisationsAndLimit.AuthorisationsSection
import play.api.i18n.Messages
import play.api.mvc.Call

case class EquipmentDomain(
  containerId: Option[String],
  seals: Option[SealsDomain]
)(index: Index)
    extends JourneyDomainModel {

  def asString(implicit messages: Messages): String =
    EquipmentDomain.asString(containerId)

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] =
    Some(routes.EquipmentAnswersController.onPageLoad(userAnswers.lrn, mode, index))
}

object EquipmentDomain {

  def asString(containerId: Option[String])(implicit messages: Messages): String =
    containerId.fold(
      messages("equipment.value.withoutContainer")
    )(
      messages("equipment.value.withContainer", _)
    )

  implicit def userAnswersReader(equipmentIndex: Index): UserAnswersReader[EquipmentDomain] =
    (
      containerIdReads(equipmentIndex),
      sealsReads(equipmentIndex)
    ).tupled.map((EquipmentDomain.apply _).tupled).map(_(equipmentIndex))

  def containerIdReads(equipmentIndex: Index): UserAnswersReader[Option[String]] =
    ContainerIndicatorPage.reader.flatMap {
      case true if equipmentIndex.isFirst =>
        ContainerIdentificationNumberPage(equipmentIndex).reader.map(Option(_))
      case true =>
        AddContainerIdentificationNumberYesNoPage(equipmentIndex).filterOptionalDependent(identity) {
          ContainerIdentificationNumberPage(equipmentIndex).reader
        }
      case false =>
        none[String].pure[UserAnswersReader]
    }

  def sealsReads(equipmentIndex: Index): UserAnswersReader[Option[SealsDomain]] = for {
    procedureType      <- ProcedureTypePage.reader
    authorisationTypes <- AuthorisationsSection.fieldReader(AuthorisationTypePage)
    hasSSEAuthorisation = authorisationTypes.exists(_.isSSE)
    reader <- (procedureType, hasSSEAuthorisation) match {
      case (ProcedureType.Simplified, true) =>
        UserAnswersReader[SealsDomain](SealsDomain.userAnswersReader(equipmentIndex)).map(Option(_))
      case _ =>
        AddSealYesNoPage(equipmentIndex).filterOptionalDependent(identity) {
          UserAnswersReader[SealsDomain](SealsDomain.userAnswersReader(equipmentIndex))
        }
    }
  } yield reader

}
