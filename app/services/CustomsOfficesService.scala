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

import cats.data.NonEmptySet
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.SelectableList.{officesOfExitReads, officesOfTransitReads}
import models.reference.CustomsOffice
import models.{RichOptionalJsArray, SelectableList, UserAnswers}
import pages.external.OfficeOfDestinationPage
import pages.sections.external.{OfficesOfExitSection, OfficesOfTransitSection}

import javax.inject.Inject

class CustomsOfficesService @Inject() () {

  def getCustomsOffices(userAnswers: UserAnswers): SelectableList[CustomsOffice] = {
    val officesOfExit       = userAnswers.get(OfficesOfExitSection).validate(officesOfExitReads).map(_.values).getOrElse(Nil)
    val officesOfTransit    = userAnswers.get(OfficesOfTransitSection).validate(officesOfTransitReads).map(_.values).getOrElse(Nil)
    val officeOfDestination = userAnswers.get(OfficeOfDestinationPage).toSeq

    val offices = officesOfExit ++ officesOfTransit ++ officeOfDestination
    offices.toList match {
      case Nil =>
        throw new NoReferenceDataFoundException("")
      case head :: tail =>
        SelectableList(NonEmptySet.of(head, tail *).toSeq)
    }
  }

}
