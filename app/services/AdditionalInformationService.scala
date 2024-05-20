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

import connectors.ReferenceDataConnector
import models.{SelectableList, UserAnswers}
import models.reference.additionalInformation.AdditionalInformationCode
import pages.preRequisites.ItemsDestinationCountryInCL009Page
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationService @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def getAdditionalInformationCodes(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[SelectableList[AdditionalInformationCode]] =
    referenceDataConnector
      .getAdditionalInformationCodes()
      .map(
        nonEmptySet => SelectableList(nonEmptySet)
      )
      .map {
        additionalInformationList =>
          userAnswers.get(ItemsDestinationCountryInCL009Page) match {
            case Some(true) =>
              SelectableList(additionalInformationList.values.filterNot(_.value.equals("30600")))
            case _ =>
              additionalInformationList
          }
      }
}
