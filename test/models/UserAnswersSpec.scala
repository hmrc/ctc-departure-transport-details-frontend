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

package models

import base.SpecBase
import pages.QuestionPage
import play.api.libs.json.{JsObject, JsPath, Json}

import java.util.UUID
import scala.util.Try

class UserAnswersSpec extends SpecBase {

  private val testPageAnswer  = "foo"
  private val testPageAnswer2 = "bar"
  private val testPagePath    = "testPath"

  private val testCleanupPagePath   = "testCleanupPagePath"
  private val testCleanupPageAnswer = "testCleanupPageAnswer"

  case object TestPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ testPagePath

    override def cleanup(value: Option[String], userAnswers: UserAnswers): Try[UserAnswers] =
      value match {
        case Some(_) => userAnswers.remove(TestCleanupPage)
        case _       => super.cleanup(value, userAnswers)
      }
  }

  case object TestCleanupPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ testCleanupPagePath
  }

  "UserAnswers" - {

    "set" - {
      "must run cleanup when given a new answer" in {

        val userAnswers = emptyUserAnswers.setValue(TestCleanupPage, testCleanupPageAnswer)
        val result      = userAnswers.setValue(TestPage, testPageAnswer)

        val expectedData = Json.obj(
          testPagePath -> testPageAnswer
        )

        result.data mustEqual expectedData
      }

      "must run cleanup when given a different answer" in {

        val result = emptyUserAnswers
          .setValue(TestPage, testPageAnswer)
          .setValue(TestCleanupPage, testCleanupPageAnswer)
          .setValue(TestPage, testPageAnswer2)

        val expectedData = Json.obj(
          testPagePath -> testPageAnswer2
        )

        result.data mustEqual expectedData
      }

      "must not run cleanup when given the same answer" in {

        val result = emptyUserAnswers
          .setValue(TestPage, testPageAnswer)
          .setValue(TestCleanupPage, testCleanupPageAnswer)
          .setValue(TestPage, testPageAnswer)

        val expectedData = Json.obj(
          testCleanupPagePath -> testCleanupPageAnswer,
          testPagePath        -> testPageAnswer
        )

        result.data mustEqual expectedData
      }
    }

    "updateTask" - {
      val section = ".transportDetails"

      "must set task status" - {
        "when task has not previously been set" in {
          val result = emptyUserAnswers.updateTask(section, TaskStatus.InProgress)
          result.tasks mustEqual Map(section -> TaskStatus.InProgress)
        }

        "when task has previously been set" in {
          val tasks  = Map(section -> TaskStatus.InProgress)
          val result = emptyUserAnswers.copy(tasks = tasks).updateTask(section, TaskStatus.Completed)
          result.tasks mustEqual Map(section -> TaskStatus.Completed)
        }

        "when there are other tasks" in {
          val tasks = Map(
            section          -> TaskStatus.InProgress,
            ".routeDetails"  -> TaskStatus.NotStarted,
            ".traderDetails" -> TaskStatus.CannotStartYet
          )
          val result = emptyUserAnswers.copy(tasks = tasks).updateTask(section, TaskStatus.Completed)
          result.tasks mustEqual Map(
            section          -> TaskStatus.Completed,
            ".routeDetails"  -> TaskStatus.NotStarted,
            ".traderDetails" -> TaskStatus.CannotStartYet
          )
        }
      }
    }

    "removeTransportEquipmentFromItems" - {

      val uuid = "1794d93b-17d5-44fe-a18d-aaa2059d06fe"

      "must remove any transport equipment from items with given UUID" in {
        val data = Json
          .parse(s"""
            |{
            |  "items" : {
            |    "items" : [
            |      {
            |        "description" : "item1",
            |        "transportEquipment" : "$uuid"
            |      },
            |      {
            |        "description" : "item2",
            |        "transportEquipment" : "a573bfd3-6470-40c4-a290-ea2d8d43c02a"
            |      },
            |      {
            |        "description" : "item3",
            |        "transportEquipment" : "ac50154c-cad1-4320-8def-d282eea63b2e"
            |      }
            |    ]
            |  }
            |}
            |""".stripMargin)
          .as[JsObject]

        val userAnswers = emptyUserAnswers.copy(data = data)

        val result = userAnswers.removeTransportEquipmentFromItems(Some(UUID.fromString(uuid))).data

        val expectedResult = Json
          .parse(s"""
            |{
            |  "items" : {
            |    "items" : [
            |      {
            |        "description" : "item1"
            |      },
            |      {
            |        "description" : "item2",
            |        "transportEquipment" : "a573bfd3-6470-40c4-a290-ea2d8d43c02a"
            |      },
            |      {
            |        "description" : "item3",
            |        "transportEquipment" : "ac50154c-cad1-4320-8def-d282eea63b2e"
            |      }
            |    ]
            |  }
            |}
            |""".stripMargin)
          .as[JsObject]

        result mustEqual expectedResult
      }
    }
  }
}
