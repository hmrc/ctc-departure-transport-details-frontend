/*
 * Copyright 2024 HM Revenue & Customs
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

import config.Constants.AdditionalInformation._
import connectors.ReferenceDataConnector
import models.reference.additionalInformation.AdditionalInformationCode
import models.{Index, RichOptionalJsArray, SelectableList, UserAnswers}
import pages.external.ItemCountryOfDestinationInCL009Page
import pages.preRequisites.ItemsDestinationCountryInCL009Page
import pages.sections.external.ItemsSection
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationService @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def getAdditionalInformationCodes(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[SelectableList[AdditionalInformationCode]] =
    referenceDataConnector
      .getAdditionalInformationCodes()
      .map {
        additionalInformationList =>
          val isConsignmentCountryOfDestinationInCL009 = userAnswers.get(ItemsDestinationCountryInCL009Page).contains(true)

          val isAtLeastOneItemCountryOfDestinationInCL009 = {
            val numberOfItems = userAnswers.get(ItemsSection).length
            (0 until numberOfItems).map(Index(_)).exists {
              index => userAnswers.get(ItemCountryOfDestinationInCL009Page(index)).contains(true)
            }
          }

          if (isConsignmentCountryOfDestinationInCL009 || isAtLeastOneItemCountryOfDestinationInCL009) {
            SelectableList(additionalInformationList.filterNot(_.code == `30600`).toSeq)
          } else {
            SelectableList(additionalInformationList)
          }
      }
}
