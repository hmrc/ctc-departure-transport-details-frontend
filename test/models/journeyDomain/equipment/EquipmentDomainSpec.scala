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

import base.SpecBase
import generators.Generators
import models.journeyDomain.equipment.seal.{SealDomain, SealsDomain}
import models.reference.authorisations.AuthorisationType
import models.{Index, OptionalBoolean, ProcedureType}
import pages.authorisationsAndLimit.authorisations.index.AuthorisationTypePage
import pages.equipment.AddTransportEquipmentYesNoPage
import pages.equipment.index._
import pages.equipment.index.seals.IdentificationNumberPage
import pages.external.ProcedureTypePage
import pages.preRequisites.ContainerIndicatorPage
import pages.sections.equipment.{EquipmentSection, SealsSection}
import play.api.libs.json.Json

class EquipmentDomainSpec extends SpecBase with Generators {

  private val containerId = nonEmptyString.sample.value
  private val sealId      = nonEmptyString.sample.value
  private val authTypeACR = AuthorisationType("C521", "ACR - authorisation for the status of authorised consignor for Union transit")
  private val authTypeSSE = AuthorisationType("C523", "SSE - authorisation for the use of seals of a special type")
  private val authTypeTRD = AuthorisationType("C524", "TRD - authorisation to use transit declaration with a reduced dataset")

  "Equipment domain" - {

    "userAnswersReader" - {
      "can be parsed from user answers" - {
        "when there are seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ProcedureTypePage, ProcedureType.Normal)
            .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), containerId)
            .setValue(AddSealYesNoPage(equipmentIndex), true)
            .setValue(IdentificationNumberPage(equipmentIndex, sealIndex), sealId)

          val expectedResult = EquipmentDomain(
            containerId = Some(containerId),
            seals = Some(
              SealsDomain(
                Seq(
                  SealDomain(sealId)(equipmentIndex, sealIndex)
                )
              )(equipmentIndex)
            )
          )(equipmentIndex)

          val result = EquipmentDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            ContainerIdentificationNumberPage(equipmentIndex),
            AddSealYesNoPage(equipmentIndex),
            IdentificationNumberPage(equipmentIndex, sealIndex),
            SealsSection(equipmentIndex),
            EquipmentSection(equipmentIndex)
          )
        }

        "when there are no seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ProcedureTypePage, ProcedureType.Normal)
            .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), containerId)
            .setValue(AddSealYesNoPage(equipmentIndex), false)

          val expectedResult = EquipmentDomain(
            containerId = Some(containerId),
            seals = None
          )(equipmentIndex)

          val result = EquipmentDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            ContainerIdentificationNumberPage(equipmentIndex),
            AddSealYesNoPage(equipmentIndex),
            EquipmentSection(equipmentIndex)
          )
        }

        "when container indicator is set to no" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIndicatorPage, OptionalBoolean.no)
            .setValue(AddTransportEquipmentYesNoPage, true)
            .setValue(IdentificationNumberPage(equipmentIndex, sealIndex), sealId)

          val expectedResult = EquipmentDomain(
            containerId = None,
            seals = Some(
              SealsDomain(
                Seq(
                  SealDomain(sealId)(equipmentIndex, sealIndex)
                )
              )(equipmentIndex)
            )
          )(equipmentIndex)

          val result = EquipmentDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            IdentificationNumberPage(equipmentIndex, sealIndex),
            SealsSection(equipmentIndex),
            EquipmentSection(equipmentIndex)
          )
        }
      }

      "cannot be parsed from user answers" - {
        "when adding transport equipment" - {
          "and container indicator is set to no" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.no)
              .setValue(AddTransportEquipmentYesNoPage, true)

            val result = EquipmentDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

            result.left.value.page mustBe IdentificationNumberPage(equipmentIndex, sealIndex)
            result.left.value.pages mustBe Seq(
              IdentificationNumberPage(equipmentIndex, sealIndex)
            )
          }
        }
      }
    }

    "containerIdReads" - {
      "can be read from user answers" - {
        "when container indicator is true" - {
          "and at index 0" in {
            val index = Index(0)

            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
              .setValue(ContainerIdentificationNumberPage(index), containerId)

            val expectedResult = Some(containerId)

            val result = EquipmentDomain.containerIdReads(index).apply(Nil).run(userAnswers)

            result.value.value mustBe expectedResult
            result.value.pages mustBe Seq(
              ContainerIdentificationNumberPage(index)
            )
          }

          "and not at index 0" - {
            val index = Index(1)

            "and add container id yes/no is true" in {
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
                .setValue(EquipmentSection(Index(0)), Json.obj("foo" -> "bar"))
                .setValue(AddContainerIdentificationNumberYesNoPage(index), true)
                .setValue(ContainerIdentificationNumberPage(index), containerId)

              val expectedResult = Some(containerId)

              val result = EquipmentDomain.containerIdReads(index).apply(Nil).run(userAnswers)

              result.value.value mustBe expectedResult
              result.value.pages mustBe Seq(
                AddContainerIdentificationNumberYesNoPage(index),
                ContainerIdentificationNumberPage(index)
              )
            }

            "and add container id yes/no is false" in {
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
                .setValue(EquipmentSection(Index(0)), Json.obj("foo" -> "bar"))
                .setValue(AddContainerIdentificationNumberYesNoPage(index), false)

              val expectedResult = None

              val result = EquipmentDomain.containerIdReads(index).apply(Nil).run(userAnswers)

              result.value.value mustBe expectedResult
              result.value.pages mustBe Seq(
                AddContainerIdentificationNumberYesNoPage(index)
              )
            }
          }
        }

        "when container indicator is false" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIndicatorPage, OptionalBoolean.no)

          val expectedResult = None

          val result = EquipmentDomain.containerIdReads(index).apply(Nil).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Nil
        }
      }

      "cannot be read from user answers" - {
        "when at index 0" - {
          val index = Index(0)

          "and container id is unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

            val result = EquipmentDomain.containerIdReads(index).apply(Nil).run(userAnswers)

            result.left.value.page mustBe ContainerIdentificationNumberPage(index)
            result.left.value.pages mustBe Seq(
              ContainerIdentificationNumberPage(index)
            )
          }
        }

        "when not at index 0" - {
          val index = Index(1)

          "and container indicator is true" - {
            "and add container id yes/no is unanswered" in {
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

              val result = EquipmentDomain.containerIdReads(index).apply(Nil).run(userAnswers)

              result.left.value.page mustBe AddContainerIdentificationNumberYesNoPage(index)
              result.left.value.pages mustBe Seq(
                AddContainerIdentificationNumberYesNoPage(index)
              )
            }

            "and add container id yes/no is true" - {
              "and container id is unanswered" in {
                val userAnswers = emptyUserAnswers
                  .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
                  .setValue(EquipmentSection(Index(0)), Json.obj("foo" -> "bar"))
                  .setValue(AddContainerIdentificationNumberYesNoPage(index), true)

                val result = EquipmentDomain.containerIdReads(index).apply(Nil).run(userAnswers)

                result.left.value.page mustBe ContainerIdentificationNumberPage(index)
                result.left.value.pages mustBe Seq(
                  AddContainerIdentificationNumberYesNoPage(index),
                  ContainerIdentificationNumberPage(index)
                )
              }
            }
          }
        }
      }
    }

    "sealsReads" - {
      "can be read from user answers" - {
        "when container indicator is 0" - {
          "and add transport equipment is true" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.no)
              .setValue(AddTransportEquipmentYesNoPage, true)
              .setValue(IdentificationNumberPage(equipmentIndex, sealIndex), sealId)

            val expectedResult = Some(
              SealsDomain(
                Seq(
                  SealDomain(sealId)(equipmentIndex, sealIndex)
                )
              )(equipmentIndex)
            )

            val result = EquipmentDomain.sealsReads(equipmentIndex).apply(Nil).run(userAnswers)

            result.value.value mustBe expectedResult
            result.value.pages mustBe Seq(
              IdentificationNumberPage(equipmentIndex, sealIndex),
              SealsSection(equipmentIndex)
            )
          }
        }

        "when add seals yes/no is no" in {
          val userAnswers = emptyUserAnswers
            .setValue(ProcedureTypePage, ProcedureType.Normal)
            .setValue(AddSealYesNoPage(equipmentIndex), false)

          val expectedResult = None

          val result = EquipmentDomain.sealsReads(equipmentIndex).apply(Nil).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            AddSealYesNoPage(equipmentIndex)
          )
        }

        "when add seals yes/no is yes" in {
          val userAnswers = emptyUserAnswers
            .setValue(ProcedureTypePage, ProcedureType.Normal)
            .setValue(AddSealYesNoPage(equipmentIndex), true)
            .setValue(IdentificationNumberPage(equipmentIndex, sealIndex), sealId)

          val expectedResult = Some(
            SealsDomain(
              Seq(
                SealDomain(sealId)(equipmentIndex, sealIndex)
              )
            )(equipmentIndex)
          )

          val result = EquipmentDomain.sealsReads(equipmentIndex).apply(Nil).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            AddSealYesNoPage(equipmentIndex),
            IdentificationNumberPage(equipmentIndex, sealIndex),
            SealsSection(equipmentIndex)
          )
        }
      }

      "cannot be read from user answers" - {
        "when container indicator is 0 and add transport equipment is true" - {
          "and seal identification number unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.no)
              .setValue(AddTransportEquipmentYesNoPage, true)

            val result = EquipmentDomain.sealsReads(equipmentIndex).apply(Nil).run(userAnswers)

            result.left.value.page mustBe IdentificationNumberPage(equipmentIndex, Index(0))
            result.left.value.pages mustBe Seq(
              IdentificationNumberPage(equipmentIndex, sealIndex)
            )
          }
        }

        "when simplified procedure type and any authorisation has SSE type" - {
          "and seal identification number unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ProcedureTypePage, ProcedureType.Simplified)
              .setValue(AuthorisationTypePage(Index(0)), authTypeACR)
              .setValue(AuthorisationTypePage(Index(1)), authTypeSSE)

            val result = EquipmentDomain.sealsReads(equipmentIndex).apply(Nil).run(userAnswers)

            result.left.value.page mustBe IdentificationNumberPage(equipmentIndex, Index(0))
            result.left.value.pages mustBe Seq(
              IdentificationNumberPage(equipmentIndex, sealIndex)
            )
          }
        }

        "when simplified procedure type and no authorisation has SSE type" - {
          "and add seals yes/no is unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ProcedureTypePage, ProcedureType.Simplified)
              .setValue(AuthorisationTypePage(Index(0)), authTypeACR)
              .setValue(AuthorisationTypePage(Index(1)), authTypeTRD)

            val result = EquipmentDomain.sealsReads(equipmentIndex).apply(Nil).run(userAnswers)

            result.left.value.page mustBe AddSealYesNoPage(equipmentIndex)
            result.left.value.pages mustBe Seq(
              AddSealYesNoPage(equipmentIndex)
            )
          }
        }

        "when not a simplified procedure type and any authorisation has SSE type" - {
          "and add seals yes/no is unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ProcedureTypePage, ProcedureType.Normal)
              .setValue(AuthorisationTypePage(Index(0)), authTypeSSE)
              .setValue(AuthorisationTypePage(Index(1)), authTypeACR)

            val result = EquipmentDomain.sealsReads(equipmentIndex).apply(Nil).run(userAnswers)

            result.left.value.page mustBe AddSealYesNoPage(equipmentIndex)
            result.left.value.pages mustBe Seq(
              AddSealYesNoPage(equipmentIndex)
            )
          }
        }

        "when simplified procedure type and no authorisation has SSE type" - {
          "and seal identification number unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ProcedureTypePage, ProcedureType.Normal)
              .setValue(AuthorisationTypePage(Index(0)), authTypeACR)
              .setValue(AuthorisationTypePage(Index(1)), authTypeTRD)

            val result = EquipmentDomain.sealsReads(equipmentIndex).apply(Nil).run(userAnswers)

            result.left.value.page mustBe AddSealYesNoPage(equipmentIndex)
            result.left.value.pages mustBe Seq(
              AddSealYesNoPage(equipmentIndex)
            )
          }
        }
      }
    }
  }
}
