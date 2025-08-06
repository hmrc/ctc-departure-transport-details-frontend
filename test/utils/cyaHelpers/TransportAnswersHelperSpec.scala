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

package utils.cyaHelpers

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.additionalInformation.routes as additionalInformationRoutes
import controllers.authorisationsAndLimit.authorisations.index.routes as authorisationRoutes
import controllers.authorisationsAndLimit.authorisations.routes as authorisationsRoutes
import controllers.authorisationsAndLimit.limit.routes as limitRoutes
import controllers.authorisationsAndLimit.routes as authorisationsAndLimitRoutes
import controllers.carrierDetails.contact.routes as carrierDetailsContactRoutes
import controllers.carrierDetails.routes as carrierDetailsRoutes
import controllers.equipment.index.routes as equipmentRoutes
import controllers.equipment.routes as equipmentsRoutes
import controllers.preRequisites.routes as preRequisitesRoutes
import controllers.supplyChainActors.index.routes as supplyChainActorRoutes
import controllers.supplyChainActors.routes as supplyChainActorsRoutes
import generators.Generators
import models.journeyDomain.authorisationsAndLimit.authorisations.AuthorisationDomain
import models.journeyDomain.equipment.EquipmentDomain
import models.journeyDomain.supplyChainActors.SupplyChainActorDomain
import models.reference.Country
import models.reference.additionalReference.AdditionalReferenceType
import models.reference.additionalInformation.AdditionalInformationCode
import models.reference.equipment.PaymentMethod
import models.{Index, Mode, OptionalBoolean}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalInformation.AddAdditionalInformationYesNoPage
import pages.additionalReference.AddAdditionalReferenceYesNoPage
import pages.additionalReference.index.AdditionalReferenceTypePage
import pages.additionalInformation.index.AdditionalInformationTypePage
import pages.authorisationsAndLimit.AddAuthorisationsYesNoPage
import pages.authorisationsAndLimit.limit.{AddLimitDateYesNoPage, LimitDatePage}
import pages.carrierDetails.contact.{NamePage, TelephoneNumberPage}
import pages.carrierDetails.{AddContactYesNoPage, CarrierDetailYesNoPage, IdentificationNumberPage}
import pages.equipment.{AddPaymentMethodYesNoPage, AddTransportEquipmentYesNoPage, PaymentMethodPage}
import pages.preRequisites.*
import pages.sections.additionalInformation.AdditionalInformationSection
import pages.sections.authorisationsAndLimit.AuthorisationSection
import pages.sections.equipment.EquipmentSection
import pages.sections.supplyChainActors.SupplyChainActorSection
import pages.supplyChainActors.SupplyChainActorYesNoPage
import play.api.libs.json.Json

import java.time.LocalDate

class TransportAnswersHelperSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "TransportAnswersHelper" - {

    "usingSameUcr" - {
      "must return None" - {
        s"when $SameUcrYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.usingSameUcr
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $SameUcrYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(SameUcrYesNoPage, true)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.usingSameUcr.get

              result.key.value mustEqual "Do you want to use the same Unique Consignment Reference (UCR) for all items?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual preRequisitesRoutes.SameUcrYesNoController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if you want to use the same Unique Consignment Reference (UCR) for all items"
              action.id mustEqual "change-using-same-ucr"
          }
        }
      }
    }

    "ucr" - {
      "must return None" - {
        s"when $UniqueConsignmentReferencePage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.ucr
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $UniqueConsignmentReferencePage defined" in {
          forAll(arbitrary[Mode], nonEmptyString) {
            (mode, ucr) =>
              val answers = emptyUserAnswers.setValue(UniqueConsignmentReferencePage, ucr)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.ucr.get

              result.key.value mustEqual "Unique Consignment Reference (UCR)"
              result.value.value mustEqual ucr
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual preRequisitesRoutes.UniqueConsignmentReferenceController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "Unique Consignment Reference (UCR)"
              action.id mustEqual "change-ucr"
          }
        }
      }
    }

    "usingSameCountryOfDispatch" - {
      "must return None" - {
        s"when $SameCountryOfDispatchYesNoPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.usingSameCountryOfDispatch
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $SameCountryOfDispatchYesNoPage is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(SameCountryOfDispatchYesNoPage, true)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.usingSameCountryOfDispatch.get

              result.key.value mustEqual "Are all the items being dispatched from the same country?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual preRequisitesRoutes.SameCountryOfDispatchYesNoController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if all the items are being dispatched from the same country"
              action.id mustEqual "change-using-same-country-of-dispatch"
          }
        }
      }
    }

    "countryOfDispatch" - {
      "must return None" - {
        s"when $CountryOfDispatchPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.countryOfDispatch
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $CountryOfDispatchPage defined" in {
          forAll(arbitrary[Mode], arbitrary[Country]) {
            (mode, country) =>
              val answers = emptyUserAnswers.setValue(CountryOfDispatchPage, country)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.countryOfDispatch.get

              result.key.value mustEqual "Country of dispatch"
              result.value.value mustEqual country.toString
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual preRequisitesRoutes.CountryOfDispatchController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "country of dispatch"
              action.id mustEqual "change-country-of-dispatch"
          }
        }
      }
    }

    "addCountryOfDestination" - {
      "must return None" - {
        s"when $AddCountryOfDestinationPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addCountryOfDestination
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $AddCountryOfDestinationPage true" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddCountryOfDestinationPage, OptionalBoolean.yes)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addCountryOfDestination.get

              result.key.value mustEqual "Are the goods being transported to another country after the end of this transit movement?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual preRequisitesRoutes.AddCountryOfDestinationController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if the goods are being transported to another country after the end of this transit movement"
              action.id mustEqual "change-add-country-of-destination"
          }
        }

        s"when $AddCountryOfDestinationPage false" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddCountryOfDestinationPage, OptionalBoolean.no)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addCountryOfDestination.get

              result.key.value mustEqual "Are the goods being transported to another country after the end of this transit movement?"
              result.value.value mustEqual "No"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual preRequisitesRoutes.AddCountryOfDestinationController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if the goods are being transported to another country after the end of this transit movement"
              action.id mustEqual "change-add-country-of-destination"
          }
        }

        s"when $AddCountryOfDestinationPage not sure" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddCountryOfDestinationPage, OptionalBoolean.maybe)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addCountryOfDestination.get

              result.key.value mustEqual "Are the goods being transported to another country after the end of this transit movement?"
              result.value.value mustEqual "Not sure"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual preRequisitesRoutes.AddCountryOfDestinationController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if the goods are being transported to another country after the end of this transit movement"
              action.id mustEqual "change-add-country-of-destination"
          }
        }
      }
    }

    "transportedToSameCountry" - {
      "must return None" - {
        s"when $TransportedToSameCountryYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.transportedToSameCountry
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $TransportedToSameCountryYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(TransportedToSameCountryYesNoPage, true)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.transportedToSameCountry.get

              result.key.value mustEqual "Are all the items being transported to the same country?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual preRequisitesRoutes.TransportedToSameCountryYesNoController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if all the items are being transported to the same country"
              action.id mustEqual "change-transported-to-same-country"
          }
        }
      }
    }

    "countryOfDestination" - {
      "must return None" - {
        s"when $ItemsDestinationCountryPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.countryOfDestination
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $ItemsDestinationCountryPage defined" in {
          forAll(arbitrary[Mode], arbitrary[Country]) {
            (mode, country) =>
              val answers = emptyUserAnswers.setValue(ItemsDestinationCountryPage, country)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.countryOfDestination.get

              result.key.value mustEqual "Country the items are being transported to"
              result.value.value mustEqual country.toString
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual preRequisitesRoutes.ItemsDestinationCountryController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "country the items are being transported to"
              action.id mustEqual "change-country-of-destination"
          }
        }
      }
    }

    "usingContainersYesNo" - {
      "must return None" - {
        s"when $ContainerIndicatorPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.usingContainersYesNo
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $ContainerIndicatorPage true" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(ContainerIndicatorPage, OptionalBoolean.yes)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.usingContainersYesNo.get

              result.key.value mustEqual "Are you using any containers?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual preRequisitesRoutes.ContainerIndicatorController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if you are using any containers"
              action.id mustEqual "change-using-containers"
          }
        }

        s"when $ContainerIndicatorPage false" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(ContainerIndicatorPage, OptionalBoolean.no)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.usingContainersYesNo.get

              result.key.value mustEqual "Are you using any containers?"
              result.value.value mustEqual "No"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual preRequisitesRoutes.ContainerIndicatorController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if you are using any containers"
              action.id mustEqual "change-using-containers"
          }
        }

        s"when $ContainerIndicatorPage maybe" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(ContainerIndicatorPage, OptionalBoolean.maybe)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.usingContainersYesNo.get

              result.key.value mustEqual "Are you using any containers?"
              result.value.value mustEqual "Not sure"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual preRequisitesRoutes.ContainerIndicatorController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if you are using any containers"
              action.id mustEqual "change-using-containers"
          }
        }
      }
    }

    "addSupplyChainActor" - {
      "must return None" - {
        s"when $SupplyChainActorYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addSupplyChainActor
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $SupplyChainActorYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(SupplyChainActorYesNoPage, true)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addSupplyChainActor.get

              result.key.value mustEqual "Do you want to add a supply chain actor for all items?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual supplyChainActorsRoutes.SupplyChainActorYesNoController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if you want to add a supply chain actor for all items"
              action.id mustEqual "change-add-supply-chain-actor"
          }
        }
      }
    }

    "supplyChainActor" - {
      "must return None" - {
        "when supply chain actor is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.supplyChainActor(index)
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when supply chain actor is defined" in {
          forAll(arbitrarySupplyChainActorAnswers(emptyUserAnswers, index), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val supplyChainActor = SupplyChainActorDomain.userAnswersReader(index).apply(Nil).run(userAnswers).value.value
              val helper           = new TransportAnswersHelper(userAnswers, mode)
              val result           = helper.supplyChainActor(index).get

              result.key.value mustEqual "Supply chain actor 1"
              result.value.value mustEqual supplyChainActor.asString
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual supplyChainActorRoutes.SupplyChainActorTypeController.onPageLoad(userAnswers.lrn, mode, index).url
              action.visuallyHiddenText.get mustEqual "supply chain actor 1"
              action.id mustEqual "change-supply-chain-actor-1"
          }
        }
      }
    }

    "addOrRemoveSupplyChainActors" - {
      "must return None" - {
        "when supply chain actors array is empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addOrRemoveSupplyChainActors
              result must not be defined
          }
        }
      }

      "must return Some(Link)" - {
        "when supply chain actors array is non-empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(SupplyChainActorSection(Index(0)), Json.obj("foo" -> "bar"))
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addOrRemoveSupplyChainActors.get

              result.id mustEqual "add-or-remove-supply-chain-actors"
              result.text mustEqual "Add or remove supply chain actors"
              result.href mustEqual supplyChainActorsRoutes.AddAnotherSupplyChainActorController.onPageLoad(answers.lrn, mode).url
          }
        }
      }
    }

    "addAuthorisation" - {
      "must return None" - {
        s"when $AddAuthorisationsYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addAuthorisation
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $AddAuthorisationsYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddAuthorisationsYesNoPage, true)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addAuthorisation.get

              result.key.value mustEqual "Do you want to add an authorisation?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual authorisationsAndLimitRoutes.AddAuthorisationsYesNoController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if you want to add an authorisation"
              action.id mustEqual "change-add-authorisation"
          }
        }
      }
    }

    "authorisation" - {
      "must return None" - {
        "when authorisation is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.authorisation(index)
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when authorisation is defined" in {
          forAll(arbitraryAuthorisationAnswers(emptyUserAnswers, index), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val authorisation = AuthorisationDomain.userAnswersReader(index).apply(Nil).run(userAnswers).value.value
              val helper        = new TransportAnswersHelper(userAnswers, mode)
              val result        = helper.authorisation(index).get

              result.key.value mustEqual "Authorisation 1"
              result.value.value mustEqual authorisation.asString
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual authorisationRoutes.AuthorisationReferenceNumberController.onPageLoad(userAnswers.lrn, mode, index).url
              action.visuallyHiddenText.get mustEqual "authorisation 1"
              action.id mustEqual "change-authorisation-1"
          }
        }
      }
    }

    "addOrRemoveAuthorisations" - {
      "must return None" - {
        "when authorisations array is empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addOrRemoveAuthorisations
              result must not be defined
          }
        }
      }

      "must return Some(Link)" - {
        "when authorisations array is non-empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AuthorisationSection(Index(0)), Json.obj("foo" -> "bar"))
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addOrRemoveAuthorisations.get

              result.id mustEqual "add-or-remove-an-authorisation"
              result.text mustEqual "Add or remove an authorisation"
              result.href mustEqual authorisationsRoutes.AddAnotherAuthorisationController.onPageLoad(answers.lrn, mode).url
          }
        }
      }
    }

    "addLimitDateYesNo" - {
      "must return None" - {
        s"when $AddLimitDateYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addLimitDateYesNo
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $AddLimitDateYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddLimitDateYesNoPage, true)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addLimitDateYesNo.get

              result.key.value mustEqual "Do you want to add the arrival date at the office of destination?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual limitRoutes.AddLimitDateYesNoController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if you want to add the arrival date at the office of destination"
              action.id mustEqual "change-add-limit-date"
          }
        }
      }
    }

    "limitDate" - {
      "must return None" - {
        s"when $LimitDatePage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.limitDate
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $LimitDatePage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val limitDate = LocalDate.of(2000: Int, 1: Int, 8: Int)
              val answers   = emptyUserAnswers.setValue(LimitDatePage, limitDate)
              val helper    = new TransportAnswersHelper(answers, mode)
              val result    = helper.limitDate.get

              result.key.value mustEqual "Estimated arrival date at the office of destination"
              result.value.value mustEqual "8 January 2000"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual limitRoutes.LimitDateController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "estimated arrival date at the office of destination"
              action.id mustEqual "change-limit-date"
          }
        }
      }
    }

    "addCarrierDetail" - {
      "must return None" - {
        s"when $CarrierDetailYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addCarrierDetail
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $CarrierDetailYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(CarrierDetailYesNoPage, true)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addCarrierDetail.get

              result.key.value mustEqual "Do you want to add a carrier?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual carrierDetailsRoutes.CarrierDetailYesNoController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if you want to add a carrier"
              action.id mustEqual "change-add-carrier-detail"
          }
        }
      }
    }

    "eoriNumber" - {
      "must return None" - {
        s"when $IdentificationNumberPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.eoriNumber
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $IdentificationNumberPage defined" in {
          forAll(arbitrary[Mode], nonEmptyString) {
            (mode, eoriNumber) =>
              val answers = emptyUserAnswers.setValue(IdentificationNumberPage, eoriNumber)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.eoriNumber.get

              result.key.value mustEqual "EORI number or Third Country Unique Identification Number (TCUIN)"
              result.value.value mustEqual eoriNumber
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual carrierDetailsRoutes.IdentificationNumberController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "carrier EORI number or Third Country Unique Identification Number (TCUIN)"
              action.id mustEqual "change-eori-number"
          }
        }
      }
    }

    "addContact" - {
      "must return None" - {
        s"when $AddContactYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addContactPerson
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $AddContactYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddContactYesNoPage, true)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addContactPerson.get

              result.key.value mustEqual "Do you want to add a contact for the carrier?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual carrierDetailsRoutes.AddContactYesNoController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if you want to add a contact for the carrier"
              action.id mustEqual "change-add-contact"
          }
        }
      }
    }

    "contactName" - {
      "must return None" - {
        s"when $NamePage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.contactName
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $NamePage defined" in {
          forAll(arbitrary[Mode], nonEmptyString) {
            (mode, contactName) =>
              val answers = emptyUserAnswers.setValue(NamePage, contactName)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.contactName.get

              result.key.value mustEqual "Contact name"
              result.value.value mustEqual contactName
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual carrierDetailsContactRoutes.NameController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "carrier contact name"
              action.id mustEqual "change-contact-name"
          }
        }
      }
    }

    "contactTelephoneNumber" - {
      "must return None" - {
        s"when $TelephoneNumberPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.contactTelephoneNumber
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $TelephoneNumberPage defined" in {
          forAll(arbitrary[Mode], nonEmptyString) {
            (mode, contactNumber) =>
              val answers = emptyUserAnswers.setValue(TelephoneNumberPage, contactNumber)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.contactTelephoneNumber.get

              result.key.value mustEqual "Phone number"
              result.value.value mustEqual contactNumber
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual carrierDetailsContactRoutes.TelephoneNumberController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "carrier phone number"
              action.id mustEqual "change-contact-telephone-number"
          }
        }
      }
    }

    "addEquipment" - {
      "must return None" - {
        s"when $AddTransportEquipmentYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addEquipment
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $AddTransportEquipmentYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddTransportEquipmentYesNoPage, true)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addEquipment.get

              result.key.value mustEqual "Do you want to add any transport equipment?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual equipmentsRoutes.AddTransportEquipmentYesNoController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if you want to add any transport equipment"
              action.id mustEqual "change-add-equipment"
          }
        }
      }
    }

    "equipment" - {
      "must return None" - {
        "when equipment is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.equipment(index)
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when equipment is defined and container id is undefined" in {
          val initialUserAnswers = emptyUserAnswers
            .setValue(ContainerIndicatorPage, OptionalBoolean.no)

          forAll(arbitraryEquipmentAnswers(initialUserAnswers, index), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val helper = new TransportAnswersHelper(userAnswers, mode)
              val result = helper.equipment(index).get

              result.key.value mustEqual "Transport equipment 1"
              result.value.value mustEqual "No container identification number"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual equipmentRoutes.EquipmentAnswersController.onPageLoad(userAnswers.lrn, mode, index).url
              action.visuallyHiddenText.get mustEqual "transport equipment 1"
              action.id mustEqual "change-transport-equipment-1"
          }
        }

        "when equipment is defined and container id is unknown" in {
          val initialUserAnswers = emptyUserAnswers
            .setValue(ContainerIndicatorPage, OptionalBoolean.maybe)

          forAll(arbitraryEquipmentAnswers(initialUserAnswers, index), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val helper = new TransportAnswersHelper(userAnswers, mode)
              val result = helper.equipment(index).get

              result.key.value mustEqual "Transport equipment 1"
              result.value.value mustEqual "No container identification number"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual equipmentRoutes.EquipmentAnswersController.onPageLoad(userAnswers.lrn, mode, index).url
              action.visuallyHiddenText.get mustEqual "transport equipment 1"
              action.id mustEqual "change-transport-equipment-1"
          }
        }

        "when equipment is  defined and container id is defined" in {
          val initialUserAnswers = emptyUserAnswers
            .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

          forAll(arbitraryEquipmentAnswers(initialUserAnswers, index), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val equipment = EquipmentDomain.userAnswersReader(index).apply(Nil).run(userAnswers).value.value

              val helper = new TransportAnswersHelper(userAnswers, mode)
              val result = helper.equipment(index).get

              result.key.value mustEqual "Transport equipment 1"
              result.value.value mustEqual s"Container ${equipment.containerId.get}"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual equipmentRoutes.EquipmentAnswersController.onPageLoad(userAnswers.lrn, mode, index).url
              action.visuallyHiddenText.get mustEqual "transport equipment 1"
              action.id mustEqual "change-transport-equipment-1"
          }
        }
      }
    }

    "addOrRemoveEquipments" - {
      "must return None" - {
        "when equipments array is empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addOrRemoveEquipments
              result must not be defined
          }
        }
      }

      "must return Some(Link)" - {
        "when equipments array is non-empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(EquipmentSection(Index(0)), Json.obj("foo" -> "bar"))
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addOrRemoveEquipments.get

              result.id mustEqual "add-or-remove-transport-equipment"
              result.text mustEqual "Add or remove transport equipment"
              result.href mustEqual equipmentsRoutes.AddAnotherEquipmentController.onPageLoad(answers.lrn, mode).url
          }
        }
      }
    }

    "addPaymentMethod" - {
      "must return None" - {
        s"when $AddPaymentMethodYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addPaymentMethod
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $AddPaymentMethodYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddPaymentMethodYesNoPage, true)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addPaymentMethod.get

              result.key.value mustEqual "Do you want to add a method of payment for transport charges?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual equipmentsRoutes.AddPaymentMethodYesNoController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if you want to add a method of payment for transport charges"
              action.id mustEqual "change-add-payment-method"
          }
        }
      }
    }

    "paymentMethod" - {
      "must return None" - {
        s"when $PaymentMethodPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.paymentMethod
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $PaymentMethodPage defined" in {
          forAll(arbitrary[Mode], arbitrary[PaymentMethod]) {
            (mode, paymentMethod) =>
              val answers = emptyUserAnswers.setValue(PaymentMethodPage, paymentMethod)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.paymentMethod.get

              result.key.value mustEqual "Payment method"
              result.value.value mustEqual paymentMethod.asString
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual equipmentsRoutes.PaymentMethodController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "payment method for transport charges"
              action.id mustEqual "change-payment-method"
          }
        }
      }
    }

    "addAdditionalReference" - {
      "must return None" - {
        s"when $AddAdditionalReferenceYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addAdditionalReferenceYesNo
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $AddAdditionalReferenceYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddAdditionalReferenceYesNoPage, true)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addAdditionalReferenceYesNo.get

              result.key.value mustEqual "Do you want to add an additional reference for all items?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.additionalReference.routes.AddAdditionalReferenceYesNoController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if you want to add an additional reference  for all items"
              action.id mustEqual "change-add-additional-reference"
          }
        }
      }
    }

    "additionalReference" - {
      "must return None" - {
        s"when $AdditionalReferenceTypePage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.additionalReferences
              result mustEqual Seq.empty
          }
        }
      }

      "must return Some(Row)" - {
        s"when $AdditionalReferenceTypePage defined" in {
          forAll(arbitrary[Mode], arbitrary[AdditionalReferenceType]) {
            (mode, additionalReferenceType) =>
              val answers = emptyUserAnswers.setValue(AdditionalReferenceTypePage(additionalReferenceIndex), additionalReferenceType)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.additionalReferences

              result.head.key.value mustEqual "Additional reference 1"
              result.head.value.value mustEqual additionalReferenceType.toString
              val actions = result.head.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.additionalReference.index.routes.AdditionalReferenceTypeController
                .onPageLoad(answers.lrn, mode, additionalReferenceIndex)
                .url
              action.visuallyHiddenText.get mustEqual "if you want to add any additional information for all items"
              action.id mustEqual s"change-add-additional-reference-${additionalReferenceIndex.display}"
          }
        }
      }
    }

    "addAdditionalInformation" - {
      "must return None" - {
        s"when $AddAdditionalInformationYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addAdditionalInformationYesNo
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $AddAdditionalInformationYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddAdditionalInformationYesNoPage, true)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addAdditionalInformationYesNo.get

              result.key.value mustEqual "Do you want to add any additional information for all items?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.additionalInformation.routes.AddAdditionalInformationYesNoController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if you want to add any additional information for all items"
              action.id mustEqual "change-add-additional-information"
          }
        }
      }
    }

    "additionalInformation" - {
      "must return None" - {
        s"when $AdditionalInformationTypePage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.additionalInformationList
              result mustEqual Seq.empty
          }
        }
      }
      "must return Some(Row)" - {
        s"when $AdditionalInformationTypePage defined" in {
          forAll(arbitrary[Mode], arbitrary[AdditionalInformationCode]) {
            (mode, additionalInformationCode) =>
              val answers = emptyUserAnswers.setValue(AdditionalInformationTypePage(additionalInformationIndex), additionalInformationCode)
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.additionalInformationList

              result.head.key.value mustEqual "Additional information 1"
              result.head.value.value mustEqual additionalInformationCode.toString
              val actions = result.head.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.additionalInformation.index.routes.AdditionalInformationTypeController
                .onPageLoad(additionalInformationIndex, answers.lrn, mode)
                .url
              action.id mustEqual s"change-add-additional-information-${additionalInformationIndex.display}"
          }
        }
      }

    }

    "addOrRemoveAdditionalInformation" - {
      "must return None" - {
        "when additionalInformation array is empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addOrRemoveAdditionalInformation(mode)
              result must not be defined
          }
        }
      }

      "must return Some(Link)" - {
        "when additionalInformation array is non-empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AdditionalInformationSection(Index(0)), Json.obj("foo" -> "bar"))
              val helper  = new TransportAnswersHelper(answers, mode)
              val result  = helper.addOrRemoveAdditionalInformation(mode).get

              result.id mustEqual "add-or-remove-additional-information"
              result.text mustEqual "Add or remove additional information for all items"
              result.href mustEqual additionalInformationRoutes.AddAnotherAdditionalInformationController.onPageLoad(answers.lrn, mode).url
          }
        }
      }
    }
  }
}
