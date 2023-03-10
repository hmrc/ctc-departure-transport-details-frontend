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
import connectors.ReferenceDataConnector
import models.reference.CustomsOffice
import models.{CustomsOfficeList, Index}
import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import pages.external.{OfficeOfDestinationPage, OfficeOfExitPage, OfficeOfTransitPage}

class CustomsOfficesServiceSpec extends SpecBase with BeforeAndAfterEach {

  val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  val service                                      = new CustomsOfficesService(mockRefDataConnector)

  val gbCustomsOffice1: CustomsOffice     = CustomsOffice("GB1", "BOSTON", None)
  val gbCustomsOffice2: CustomsOffice     = CustomsOffice("GB2", "Appledore", None)
  val xiCustomsOffice1: CustomsOffice     = CustomsOffice("XI1", "Belfast", None)
  val gbCustomsOffices: CustomsOfficeList = CustomsOfficeList(Seq(gbCustomsOffice1, gbCustomsOffice2))
  val xiCustomsOffices: CustomsOfficeList = CustomsOfficeList(Seq(xiCustomsOffice1))
  val customsOffices: CustomsOfficeList   = CustomsOfficeList(gbCustomsOffices.getAll ++ xiCustomsOffices.getAll)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

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

        result mustBe CustomsOfficeList(Seq(destinationOffice))
      }

      "must return a list of sorted customs offices of transit" in {

        val userAnswers = emptyUserAnswers
          .setValue(OfficeOfTransitPage(Index(0)), transitOffice1)
          .setValue(OfficeOfTransitPage(Index(1)), transitOffice2)

        val result = service.getCustomsOffices(userAnswers)

        result mustBe CustomsOfficeList(Seq(transitOffice2, transitOffice1))
      }

      "must return a list of sorted customs offices of exit" in {

        val userAnswers = emptyUserAnswers
          .setValue(OfficeOfExitPage(Index(0)), exitOffice1)
          .setValue(OfficeOfExitPage(Index(1)), exitOffice2)

        val result = service.getCustomsOffices(userAnswers)

        result mustBe CustomsOfficeList(Seq(exitOffice2, exitOffice1))
      }

      "must return a list of sorted customs offices of exit, transit and destination" in {

        val userAnswers = emptyUserAnswers
          .setValue(OfficeOfDestinationPage, destinationOffice)
          .setValue(OfficeOfExitPage(Index(0)), exitOffice1)
          .setValue(OfficeOfExitPage(Index(1)), exitOffice2)
          .setValue(OfficeOfTransitPage(Index(0)), transitOffice1)
          .setValue(OfficeOfTransitPage(Index(1)), transitOffice2)

        val result = service.getCustomsOffices(userAnswers)

        result mustBe CustomsOfficeList(Seq(exitOffice2, transitOffice2, exitOffice1, transitOffice1, destinationOffice))
      }

    }

  }
}
