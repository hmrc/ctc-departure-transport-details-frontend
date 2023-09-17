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
import models.reference.authorisations.AuthorisationType
import models.domain.{EitherType, UserAnswersReader}
import models.journeyDomain.equipment.seal.{SealDomain, SealsDomain}
import models.{Index, OptionalBoolean, ProcedureType}
import pages.authorisationsAndLimit.authorisations.index.AuthorisationTypePage
import pages.equipment.AddTransportEquipmentYesNoPage
import pages.equipment.index._
import pages.equipment.index.seals.IdentificationNumberPage
import pages.external.ProcedureTypePage
import pages.preRequisites.ContainerIndicatorPage
import pages.sections.equipment.EquipmentSection
import play.api.libs.json.Json

class EquipmentDomainSpec extends SpecBase with Generators {

  private val containerId = nonEmptyString.sample.value
  private val sealId      = nonEmptyString.sample.value
  private val authTypeACR = AuthorisationType("C521", "ACR")
  private val authTypeSSE = AuthorisationType("C523", "SSE")
  private val authTypeTRD = AuthorisationType("C524", "TRD")

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
              )
            )
          )(equipmentIndex)

          val result: EitherType[EquipmentDomain] = UserAnswersReader[EquipmentDomain](
            EquipmentDomain.userAnswersReader(index)
          ).run(userAnswers)

          result.value mustBe expectedResult
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

          val result: EitherType[EquipmentDomain] = UserAnswersReader[EquipmentDomain](
            EquipmentDomain.userAnswersReader(index)
          ).run(userAnswers)

          result.value mustBe expectedResult
        }

      }

      "cannot be parsed from user answers" - {
        "when adding transport equipment" - {
          "and normal procedure type" in {
            val userAnswers = emptyUserAnswers
              .setValue(ProcedureTypePage, ProcedureType.Normal)
              .setValue(ContainerIndicatorPage, OptionalBoolean.no)
              .setValue(AddTransportEquipmentYesNoPage, true)

            val result: EitherType[EquipmentDomain] = UserAnswersReader[EquipmentDomain](
              EquipmentDomain.userAnswersReader(index)
            ).run(userAnswers)

            result.left.value.page mustBe AddSealYesNoPage(equipmentIndex)
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

            val result: EitherType[Option[String]] = UserAnswersReader[Option[String]](
              EquipmentDomain.containerIdReads(index)
            ).run(userAnswers)

            result.value mustBe expectedResult
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

              val result: EitherType[Option[String]] = UserAnswersReader[Option[String]](
                EquipmentDomain.containerIdReads(index)
              ).run(userAnswers)

              result.value mustBe expectedResult
            }

            "and add container id yes/no is false" in {
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
                .setValue(EquipmentSection(Index(0)), Json.obj("foo" -> "bar"))
                .setValue(AddContainerIdentificationNumberYesNoPage(index), false)

              val expectedResult = None

              val result: EitherType[Option[String]] = UserAnswersReader[Option[String]](
                EquipmentDomain.containerIdReads(index)
              ).run(userAnswers)

              result.value mustBe expectedResult
            }
          }
        }

        "when container indicator is false" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIndicatorPage, OptionalBoolean.no)

          val expectedResult = None

          val result: EitherType[Option[String]] = UserAnswersReader[Option[String]](
            EquipmentDomain.containerIdReads(index)
          ).run(userAnswers)

          result.value mustBe expectedResult
        }
      }

      "cannot be read from user answers" - {
        "when at index 0" - {
          val index = Index(0)

          "and container id is unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

            val result: EitherType[Option[String]] = UserAnswersReader[Option[String]](
              EquipmentDomain.containerIdReads(index)
            ).run(userAnswers)

            result.left.value.page mustBe ContainerIdentificationNumberPage(index)
          }
        }

        "when not at index 0" - {
          val index = Index(1)

          "and container indicator is true" - {
            "and add container id yes/no is unanswered" in {
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

              val result: EitherType[Option[String]] = UserAnswersReader[Option[String]](
                EquipmentDomain.containerIdReads(index)
              ).run(userAnswers)

              result.left.value.page mustBe AddContainerIdentificationNumberYesNoPage(index)
            }

            "and add container id yes/no is true" - {
              "and container id is unanswered" in {
                val userAnswers = emptyUserAnswers
                  .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
                  .setValue(EquipmentSection(Index(0)), Json.obj("foo" -> "bar"))
                  .setValue(AddContainerIdentificationNumberYesNoPage(index), true)

                val result: EitherType[Option[String]] = UserAnswersReader[Option[String]](
                  EquipmentDomain.containerIdReads(index)
                ).run(userAnswers)

                result.left.value.page mustBe ContainerIdentificationNumberPage(index)
              }
            }
          }
        }
      }
    }

    "sealsReads" - {
      "can be read from user answers" - {
        "when add seals yes/no is no" in {
          val userAnswers = emptyUserAnswers
            .setValue(ProcedureTypePage, ProcedureType.Normal)
            .setValue(AddSealYesNoPage(equipmentIndex), false)

          val expectedResult = None

          val result: EitherType[Option[SealsDomain]] = UserAnswersReader[Option[SealsDomain]](
            EquipmentDomain.sealsReads(equipmentIndex)
          ).run(userAnswers)

          result.value mustBe expectedResult
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
            )
          )

          val result: EitherType[Option[SealsDomain]] = UserAnswersReader[Option[SealsDomain]](
            EquipmentDomain.sealsReads(equipmentIndex)
          ).run(userAnswers)

          result.value mustBe expectedResult
        }
      }

      "cannot be read from user answers" - {
        "when simplified procedure type and any authorisation has SSE type" - {
          "and seal identification number unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ProcedureTypePage, ProcedureType.Simplified)
              .setValue(AuthorisationTypePage(Index(0)), authTypeACR)
              .setValue(AuthorisationTypePage(Index(1)), authTypeSSE)

            val result: EitherType[Option[SealsDomain]] = UserAnswersReader[Option[SealsDomain]](
              EquipmentDomain.sealsReads(equipmentIndex)
            ).run(userAnswers)

            result.left.value.page mustBe IdentificationNumberPage(equipmentIndex, Index(0))
          }
        }

        "when simplified procedure type and no authorisation has SSE type" - {
          "and add seals yes/no is unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ProcedureTypePage, ProcedureType.Simplified)
              .setValue(AuthorisationTypePage(Index(0)), authTypeACR)
              .setValue(AuthorisationTypePage(Index(1)), authTypeTRD)

            val result: EitherType[Option[SealsDomain]] = UserAnswersReader[Option[SealsDomain]](
              EquipmentDomain.sealsReads(equipmentIndex)
            ).run(userAnswers)

            result.left.value.page mustBe AddSealYesNoPage(equipmentIndex)
          }
        }

        "when not a simplified procedure type and any authorisation has SSE type" - {
          "and add seals yes/no is unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ProcedureTypePage, ProcedureType.Normal)
              .setValue(AuthorisationTypePage(Index(0)), authTypeSSE)
              .setValue(AuthorisationTypePage(Index(1)), authTypeACR)

            val result: EitherType[Option[SealsDomain]] = UserAnswersReader[Option[SealsDomain]](
              EquipmentDomain.sealsReads(equipmentIndex)
            ).run(userAnswers)

            result.left.value.page mustBe AddSealYesNoPage(equipmentIndex)
          }
        }

        "when simplified procedure type and no authorisation has SSE type" - {
          "and seal identification number unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ProcedureTypePage, ProcedureType.Normal)
              .setValue(AuthorisationTypePage(Index(0)), authTypeACR)
              .setValue(AuthorisationTypePage(Index(1)), authTypeTRD)

            val result: EitherType[Option[SealsDomain]] = UserAnswersReader[Option[SealsDomain]](
              EquipmentDomain.sealsReads(equipmentIndex)
            ).run(userAnswers)

            result.left.value.page mustBe AddSealYesNoPage(equipmentIndex)
          }
        }
      }
    }
  }
}
