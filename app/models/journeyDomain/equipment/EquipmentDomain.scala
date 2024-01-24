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

import models.domain._
import models.journeyDomain.JourneyDomainModel
import models.journeyDomain.equipment.seal.SealsDomain
import models.{Index, OptionalBoolean, ProcedureType, UserAnswers}
import pages.authorisationsAndLimit.authorisations.index.AuthorisationTypePage
import pages.equipment.index._
import pages.external.ProcedureTypePage
import pages.preRequisites.ContainerIndicatorPage
import pages.sections.Section
import pages.sections.authorisationsAndLimit.AuthorisationsSection
import pages.sections.equipment.EquipmentSection
import play.api.i18n.Messages

case class EquipmentDomain(
  containerId: Option[String],
  seals: Option[SealsDomain]
)(index: Index)
    extends JourneyDomainModel {

  def asString(implicit messages: Messages): String =
    EquipmentDomain.asString(containerId, index: Index)

  def asCyaString(implicit messages: Messages): String =
    containerId.fold(
      messages("equipment.value.withoutIndex.withoutContainer")
    )(
      messages("equipment.value.withoutIndex.withContainer", _)
    )

  override def page(userAnswers: UserAnswers): Option[Section[_]] = Some(EquipmentSection(index))
}

object EquipmentDomain {

  def asString(containerId: Option[String], index: Index)(implicit messages: Messages): String =
    containerId.fold(
      messages("equipment.value.withIndex.withoutContainer", index.display)
    )(
      messages("equipment.value.withIndex.withContainer", index.display, _)
    )

  implicit def userAnswersReader(equipmentIndex: Index): Read[EquipmentDomain] =
    (
      containerIdReads(equipmentIndex),
      sealsReads(equipmentIndex)
    ).map(EquipmentDomain.apply(_, _)(equipmentIndex))

  def containerIdReads(equipmentIndex: Index): Read[Option[String]] =
    ContainerIndicatorPage.optionalReader.to {
      case Some(OptionalBoolean.yes) if equipmentIndex.isFirst =>
        ContainerIdentificationNumberPage(equipmentIndex).reader.toOption
      case Some(OptionalBoolean.yes) =>
        AddContainerIdentificationNumberYesNoPage(equipmentIndex)
          .filterOptionalDependent(identity) {
            ContainerIdentificationNumberPage(equipmentIndex).reader
          }
      case _ =>
        UserAnswersReader.none
    }

  def sealsReads(equipmentIndex: Index): Read[Option[SealsDomain]] =
    (
      ProcedureTypePage.reader,
      AuthorisationsSection.fieldReader(AuthorisationTypePage)
    ).to {
      case (ProcedureType.Simplified, authorisationTypes) if authorisationTypes.exists(_.isSSE) =>
        SealsDomain.userAnswersReader(equipmentIndex).toOption
      case _ =>
        AddSealYesNoPage(equipmentIndex).filterOptionalDependent(identity) {
          SealsDomain.userAnswersReader(equipmentIndex)
        }
    }

}
