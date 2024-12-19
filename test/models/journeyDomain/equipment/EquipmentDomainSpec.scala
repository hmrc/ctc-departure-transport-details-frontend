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
import org.scalacheck.Gen
import pages.authorisationsAndLimit.authorisations.index.AuthorisationTypePage
import pages.equipment.AddTransportEquipmentYesNoPage
import pages.equipment.index.*
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

        "when container indicator is true" - {
          "and at index 0" - {
            "and simplified with SSE authorisation" in {
              val index = Index(0)

              val userAnswers = emptyUserAnswers
                .setValue(ProcedureTypePage, ProcedureType.Simplified)
                .setValue(AuthorisationTypePage(Index(0)), authTypeSSE)
                .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
                .setValue(ContainerIdentificationNumberPage(index), containerId)
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
                IdentificationNumberPage(equipmentIndex, sealIndex),
                SealsSection(equipmentIndex),
                EquipmentSection(equipmentIndex)
              )
            }

            "and simplified with no SSE authorisation" in {
              val index = Index(0)

              val userAnswers = emptyUserAnswers
                .setValue(ProcedureTypePage, ProcedureType.Simplified)
                .setValue(AuthorisationTypePage(Index(0)), authTypeACR)
                .setValue(AuthorisationTypePage(Index(1)), authTypeTRD)
                .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
                .setValue(ContainerIdentificationNumberPage(index), containerId)
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

            "and normal with SSE authorisation" in {
              val index = Index(0)

              val userAnswers = emptyUserAnswers
                .setValue(ProcedureTypePage, ProcedureType.Normal)
                .setValue(AuthorisationTypePage(Index(0)), authTypeSSE)
                .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
                .setValue(ContainerIdentificationNumberPage(index), containerId)
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
          }

          "and not at index 0" - {
            val index = Index(1)

            "and add container id yes/no is true" - {
              "and Simplified with SSE authorisation" in {
                val userAnswers = emptyUserAnswers
                  .setValue(ProcedureTypePage, ProcedureType.Simplified)
                  .setValue(AuthorisationTypePage(Index(0)), authTypeSSE)
                  .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
                  .setValue(EquipmentSection(Index(0)), Json.obj("foo" -> "bar"))
                  .setValue(AddContainerIdentificationNumberYesNoPage(index), true)
                  .setValue(ContainerIdentificationNumberPage(index), containerId)
                  .setValue(IdentificationNumberPage(index, sealIndex), sealId)

                val expectedResult = EquipmentDomain(
                  containerId = Some(containerId),
                  seals = Some(
                    SealsDomain(
                      Seq(
                        SealDomain(sealId)(index, sealIndex)
                      )
                    )(index)
                  )
                )(index)

                val result = EquipmentDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

                result.value.value mustBe expectedResult
                result.value.pages mustBe Seq(
                  AddContainerIdentificationNumberYesNoPage(index),
                  ContainerIdentificationNumberPage(index),
                  IdentificationNumberPage(index, sealIndex),
                  SealsSection(index),
                  EquipmentSection(index)
                )
              }

              "and Normal" in {
                val userAnswers = emptyUserAnswers
                  .setValue(ProcedureTypePage, ProcedureType.Normal)
                  .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
                  .setValue(EquipmentSection(Index(0)), Json.obj("foo" -> "bar"))
                  .setValue(AddContainerIdentificationNumberYesNoPage(index), true)
                  .setValue(ContainerIdentificationNumberPage(index), containerId)
                  .setValue(AddSealYesNoPage(index), false)

                val expectedResult = EquipmentDomain(
                  containerId = Some(containerId),
                  seals = None
                )(index)

                val result = EquipmentDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

                result.value.value mustBe expectedResult
                result.value.pages mustBe Seq(
                  AddContainerIdentificationNumberYesNoPage(index),
                  ContainerIdentificationNumberPage(index),
                  AddSealYesNoPage(index),
                  EquipmentSection(index)
                )
              }
            }

            "and add container id yes/no is false" in {
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
                .setValue(EquipmentSection(Index(0)), Json.obj("foo" -> "bar"))
                .setValue(AddContainerIdentificationNumberYesNoPage(index), false)
                .setValue(IdentificationNumberPage(index, sealIndex), sealId)

              val expectedResult = EquipmentDomain(
                containerId = None,
                seals = Some(
                  SealsDomain(
                    Seq(
                      SealDomain(sealId)(index, sealIndex)
                    )
                  )(index)
                )
              )(index)

              val result = EquipmentDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

              result.value.value mustBe expectedResult
              result.value.pages mustBe Seq(
                AddContainerIdentificationNumberYesNoPage(index),
                IdentificationNumberPage(index, sealIndex),
                SealsSection(index),
                EquipmentSection(index)
              )
            }
          }
        }

        "when container indicator is no/maybe" - {

          val containerIndicator = Gen.oneOf(OptionalBoolean.no, OptionalBoolean.maybe).sample.value

          "and at index 0" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, containerIndicator)
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

          "and not at index 0" in {
            val index = Index(1)

            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, containerIndicator)
              .setValue(EquipmentSection(Index(0)), Json.obj("foo" -> "bar"))
              .setValue(IdentificationNumberPage(index, sealIndex), sealId)

            val expectedResult = EquipmentDomain(
              containerId = None,
              seals = Some(
                SealsDomain(
                  Seq(
                    SealDomain(sealId)(index, sealIndex)
                  )
                )(index)
              )
            )(index)

            val result = EquipmentDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

            result.value.value mustBe expectedResult
            result.value.pages mustBe Seq(
              IdentificationNumberPage(index, sealIndex),
              SealsSection(index),
              EquipmentSection(index)
            )
          }
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
  }
}
