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

package utils.cyaHelpers

import config.{FrontendAppConfig, PhaseConfig}
import controllers.authorisationsAndLimit.authorisations.{routes => authorisationsRoutes}
import controllers.equipment.{routes => equipmentsRoutes}
import controllers.supplyChainActors.{routes => supplyChainActorsRoutes}
import models.journeyDomain.authorisationsAndLimit.authorisations.AuthorisationDomain
import models.journeyDomain.equipment.EquipmentDomain
import models.journeyDomain.supplyChainActors.SupplyChainActorDomain
import models.reference.Country
import models.reference.equipment.PaymentMethod
import models.{Index, Mode, OptionalBoolean, UserAnswers}
import pages.authorisationsAndLimit.AddAuthorisationsYesNoPage
import pages.authorisationsAndLimit.limit.{AddLimitDateYesNoPage, LimitDatePage}
import pages.carrierDetails.contact.{NamePage, TelephoneNumberPage}
import pages.carrierDetails.{AddContactYesNoPage, CarrierDetailYesNoPage, IdentificationNumberPage}
import pages.equipment.{AddPaymentMethodYesNoPage, AddTransportEquipmentYesNoPage, PaymentMethodPage}
import pages.preRequisites._
import pages.sections.authorisationsAndLimit.AuthorisationsSection
import pages.sections.equipment.EquipmentsSection
import pages.sections.supplyChainActors.SupplyChainActorsSection
import pages.supplyChainActors.SupplyChainActorYesNoPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import viewModels.Link

import java.time.LocalDate

class TransportAnswersHelper(
  userAnswers: UserAnswers,
  mode: Mode
)(implicit messages: Messages, appConfig: FrontendAppConfig, phaseConfig: PhaseConfig)
    extends AnswersHelper(userAnswers, mode) {

  def usingSameUcr: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = SameUcrYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "preRequisites.sameUcrYesNo",
    id = Some("change-using-same-ucr")
  )

  def ucr: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = UniqueConsignmentReferencePage,
    formatAnswer = formatAsText,
    prefix = "preRequisites.uniqueConsignmentReference",
    id = Some("change-ucr")
  )

  def usingSameCountryOfDispatch: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = SameCountryOfDispatchYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "preRequisites.sameCountryOfDispatchYesNo",
    id = Some("change-using-same-country-of-dispatch")
  )

  def countryOfDispatch: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = CountryOfDispatchPage,
    formatAnswer = formatAsCountry,
    prefix = "preRequisites.countryOfDispatch",
    id = Some("change-country-of-dispatch")
  )

  def transportedToSameCountry: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = TransportedToSameCountryYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "preRequisites.transportedToSameCountryYesNo",
    id = Some("change-transported-to-same-country")
  )

  def countryOfDestination: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = ItemsDestinationCountryPage,
    formatAnswer = formatAsCountry,
    prefix = "preRequisites.itemsDestinationCountry",
    id = Some("change-country-of-destination")
  )

  def usingContainersYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[OptionalBoolean](
    page = ContainerIndicatorPage,
    formatAnswer = formatAsOptionalYesOrNo,
    prefix = "preRequisites.containerIndicator",
    id = Some("change-using-containers")
  )

  def addAuthorisation: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddAuthorisationsYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "authorisations.addAuthorisationsYesNo",
    id = Some("change-add-authorisation")
  )

  def authorisations: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(AuthorisationsSection)(authorisation)

  def authorisation(index: Index): Option[SummaryListRow] = getAnswerAndBuildSectionRow[AuthorisationDomain](
    formatAnswer = _.asString.toText,
    prefix = "checkYourAnswers.authorisation",
    id = Some(s"change-authorisation-${index.display}"),
    args = index.display
  )(AuthorisationDomain.userAnswersReader(index).apply(Nil))

  def addOrRemoveAuthorisations: Option[Link] = buildLink(AuthorisationsSection) {
    Link(
      id = "add-or-remove-an-authorisation",
      text = messages("checkYourAnswers.authorisations.addOrRemove"),
      href = authorisationsRoutes.AddAnotherAuthorisationController.onPageLoad(userAnswers.lrn, mode).url
    )
  }

  def addSupplyChainActor: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = SupplyChainActorYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "supplyChainActors.supplyChainActorYesNo",
    id = Some("change-add-supply-chain-actor")
  )

  def supplyChainActors: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(SupplyChainActorsSection)(supplyChainActor)

  def supplyChainActor(index: Index): Option[SummaryListRow] = getAnswerAndBuildSectionRow[SupplyChainActorDomain](
    formatAnswer = _.asString.toText,
    prefix = "checkYourAnswers.supplyChainActor",
    id = Some(s"change-supply-chain-actor-${index.display}"),
    args = index.display
  )(SupplyChainActorDomain.userAnswersReader(index).apply(Nil))

  def addOrRemoveSupplyChainActors: Option[Link] = buildLink(SupplyChainActorsSection) {
    Link(
      id = "add-or-remove-supply-chain-actors",
      text = messages("checkYourAnswers.supplyChainActors.addOrRemove"),
      href = supplyChainActorsRoutes.AddAnotherSupplyChainActorController.onPageLoad(userAnswers.lrn, mode).url
    )
  }

  def addLimitDateYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddLimitDateYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "authorisationsAndLimit.limit.addLimitDateYesNo",
    id = Some("change-add-limit-date")
  )

  def limitDate: Option[SummaryListRow] = getAnswerAndBuildRow[LocalDate](
    page = LimitDatePage,
    formatAnswer = formatAsDate,
    prefix = "authorisationsAndLimit.limit.limitDate",
    id = Some("change-limit-date")
  )

  def addCarrierDetail: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = CarrierDetailYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "carrierDetails.carrierDetailYesNo",
    id = Some("change-add-carrier-detail")
  )

  def eoriNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = IdentificationNumberPage,
    formatAnswer = formatAsText,
    prefix = "carrierDetails.identificationNumber",
    id = Some("change-eori-number")
  )

  def addContactPerson: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddContactYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "carrierDetails.addContactYesNo",
    id = Some("change-add-contact")
  )

  def contactName: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = NamePage,
    formatAnswer = formatAsText,
    prefix = "carrierDetails.contact.name",
    id = Some("change-contact-name")
  )

  def contactTelephoneNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = TelephoneNumberPage,
    formatAnswer = formatAsText,
    prefix = "carrierDetails.contact.telephoneNumber",
    id = Some("change-contact-telephone-number")
  )

  def addEquipment: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddTransportEquipmentYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "equipment.addTransportEquipmentYesNo",
    id = Some("change-add-equipment")
  )

  def equipments: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(EquipmentsSection)(equipment)

  def equipment(index: Index): Option[SummaryListRow] = getAnswerAndBuildSectionRow[EquipmentDomain](
    formatAnswer = _.asCyaString.toText,
    prefix = "checkYourAnswers.equipment",
    id = Some(s"change-transport-equipment-${index.display}"),
    args = index.display
  )(EquipmentDomain.userAnswersReader(index).apply(Nil))

  def addOrRemoveEquipments: Option[Link] = buildLink(EquipmentsSection) {
    Link(
      id = "add-or-remove-transport-equipment",
      text = messages("checkYourAnswers.transportEquipment.addOrRemove"),
      href = equipmentsRoutes.AddAnotherEquipmentController.onPageLoad(userAnswers.lrn, mode).url
    )
  }

  def addPaymentMethod: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddPaymentMethodYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "equipment.addPaymentMethodYesNo",
    id = Some("change-add-payment-method")
  )

  def paymentMethod: Option[SummaryListRow] = getAnswerAndBuildRow[PaymentMethod](
    page = PaymentMethodPage,
    formatAnswer = formatDynamicEnumAsText(_),
    prefix = "equipment.paymentMethod",
    id = Some("change-payment-method")
  )

}
