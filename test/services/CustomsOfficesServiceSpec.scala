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

import base.SpecBase
import models.reference.CustomsOffice
import models.{Index, SelectableList}
import org.scalatest.BeforeAndAfterEach
import pages.external.{OfficeOfDestinationPage, OfficeOfExitPage, OfficeOfTransitPage}

class CustomsOfficesServiceSpec extends SpecBase with BeforeAndAfterEach {

  val service = new CustomsOfficesService()

  "CustomsOfficesService" - {

    "getCustomsOffices" - {

      val destinationOffice = CustomsOffice("GB1", "Bristol", None)
      val transitOffice1    = CustomsOffice("GB2", "Brighton", None)
      val transitOffice2    = CustomsOffice("GB3", "Birmingham", None)
      val exitOffice1       = CustomsOffice("GB4", "Boston", None)
      val exitOffice2       = CustomsOffice("GB5", "Barnsley", None)

      "must return the office of destination" in {

        val userAnswers = emptyUserAnswers
          .setValue(OfficeOfDestinationPage, destinationOffice)

        val result = service.getCustomsOffices(userAnswers)

        result mustBe SelectableList(Seq(destinationOffice))
      }

      "must return a list of sorted customs offices of transit" in {

        val userAnswers = emptyUserAnswers
          .setValue(OfficeOfTransitPage(Index(0)), transitOffice1)
          .setValue(OfficeOfTransitPage(Index(1)), transitOffice2)

        val result = service.getCustomsOffices(userAnswers)

        result mustBe SelectableList(Seq(transitOffice2, transitOffice1))
      }

      "must return a list of sorted customs offices of exit" in {

        val userAnswers = emptyUserAnswers
          .setValue(OfficeOfExitPage(Index(0)), exitOffice1)
          .setValue(OfficeOfExitPage(Index(1)), exitOffice2)

        val result = service.getCustomsOffices(userAnswers)

        result mustBe SelectableList(Seq(exitOffice2, exitOffice1))
      }

      "must return a list of sorted customs offices of exit, transit and destination with no duplicates" in {

        val userAnswers = emptyUserAnswers
          .setValue(OfficeOfDestinationPage, destinationOffice)
          .setValue(OfficeOfExitPage(Index(0)), exitOffice1)
          .setValue(OfficeOfExitPage(Index(1)), exitOffice2)
          .setValue(OfficeOfExitPage(Index(2)), exitOffice2)
          .setValue(OfficeOfTransitPage(Index(0)), transitOffice1)
          .setValue(OfficeOfTransitPage(Index(1)), transitOffice2)
          .setValue(OfficeOfTransitPage(Index(2)), transitOffice2)

        val result = service.getCustomsOffices(userAnswers)

        result mustBe SelectableList(Seq(exitOffice2, transitOffice2, exitOffice1, transitOffice1, destinationOffice))
      }

    }

  }
}
