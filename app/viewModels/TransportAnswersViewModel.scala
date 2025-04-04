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

package viewModels

import config.FrontendAppConfig
import models.{CheckMode, UserAnswers}
import play.api.i18n.Messages
import utils.cyaHelpers.TransportAnswersHelper
import viewModels.transportMeans.TransportMeansAnswersViewModel.TransportMeansAnswersViewModelProvider

import javax.inject.Inject

case class TransportAnswersViewModel(sections: Seq[Section])

object TransportAnswersViewModel {

  class TransportAnswersViewModelProvider @Inject() (
    transportMeansAnswersViewModelProvider: TransportMeansAnswersViewModelProvider,
    implicit val config: FrontendAppConfig
  ) {

    // scalastyle:off method.length
    def apply(userAnswers: UserAnswers)(implicit messages: Messages): TransportAnswersViewModel = {
      val mode = CheckMode

      val helper = new TransportAnswersHelper(userAnswers, mode)

      val preRequisitesSection = Section(
        rows = Seq(
          helper.usingSameUcr,
          helper.ucr,
          helper.usingSameCountryOfDispatch,
          helper.countryOfDispatch,
          helper.addCountryOfDestination,
          helper.transportedToSameCountry,
          helper.countryOfDestination,
          helper.usingContainersYesNo
        ).flatten
      )

      val transportMeansSections = transportMeansAnswersViewModelProvider.apply(userAnswers, mode).sections

      val supplyChainActorsSection = Section(
        sectionTitle = messages("checkYourAnswers.supplyChainActors"),
        rows = helper.addSupplyChainActor.toList ++ helper.supplyChainActors,
        addAnotherLink = helper.addOrRemoveSupplyChainActors
      )

      val authorisationsSection = {
        val authorisationRows = helper.addAuthorisation.toList ++ helper.authorisations
        val limitDateRows     = Seq(helper.addLimitDateYesNo, helper.limitDate).flatten
        Section(
          sectionTitle = messages("checkYourAnswers.authorisations"),
          rows = authorisationRows ++ limitDateRows,
          addAnotherLink = helper.addOrRemoveAuthorisations
        )
      }

      val carrierDetailsSection = Section(
        sectionTitle = messages("checkYourAnswers.carrierDetails"),
        rows = Seq(
          helper.addCarrierDetail,
          helper.eoriNumber,
          helper.addContactPerson,
          helper.contactName,
          helper.contactTelephoneNumber
        ).flatten
      )

      val transportEquipmentSection = Section(
        sectionTitle = messages("checkYourAnswers.transportEquipment"),
        rows = helper.addEquipment.toList ++ helper.equipments,
        addAnotherLink = helper.addOrRemoveEquipments
      )

      val transportChargesSection = Section(
        sectionTitle = messages("checkYourAnswers.transportCharges"),
        rows = Seq(
          helper.addPaymentMethod,
          helper.paymentMethod
        ).flatten
      )

      val additionalReferenceSection =
        Section(
          sectionTitle = messages("checkYourAnswers.additionalReference"),
          rows = Seq(
            helper.addAdditionalReferenceYesNo,
            helper.additionalReferences
          ).flatten,
          addAnotherLink = helper.addOrRemoveAdditionalReferences(mode)
        )

      val additionalInformationSection =
        Section(
          sectionTitle = messages("checkYourAnswers.additionalInformation"),
          rows = Seq(
            helper.addAdditionalInformationYesNo,
            helper.additionalInformationList
          ).flatten,
          addAnotherLink = helper.addOrRemoveAdditionalInformation(mode)
        )

      val sections = preRequisitesSection.toSeq ++
        transportMeansSections ++
        supplyChainActorsSection.toSeq ++
        authorisationsSection.toSeq ++
        carrierDetailsSection.toSeq ++
        transportEquipmentSection.toSeq ++
        transportChargesSection.toSeq ++
        additionalReferenceSection.toSeq ++
        additionalInformationSection.toSeq

      new TransportAnswersViewModel(sections)
    }
    // scalastyle:on method.length
  }
}
