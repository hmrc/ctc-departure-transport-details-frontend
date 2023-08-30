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

package generators

import models._
import models.authorisations.AuthorisationType
import models.equipment.PaymentMethod
import models.reference._
import models.supplyChainActors.SupplyChainActorType
import models.transportMeans.active.{Identification => ActiveIdentification}
import models.transportMeans.departure.Identification
import models.transportMeans.{BorderModeOfTransport, InlandMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.authorisationsAndLimit.AuthorisationsInferredPage
import play.api.libs.json._
import queries.Gettable

import java.time.LocalDate

trait UserAnswersEntryGenerators {
  self: Generators =>

  def generateAnswer: PartialFunction[Gettable[_], Gen[JsValue]] =
    generateExternalAnswer orElse
      generateTransportAnswer

  private def generateExternalAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.external._
    {
      case ApprovedOperatorPage         => arbitrary[Boolean].map(JsBoolean)
      case DeclarationTypePage          => arbitrary[DeclarationType].map(Json.toJson(_))
      case OfficeOfDestinationPage      => arbitrary[CustomsOffice].map(Json.toJson(_))
      case OfficeOfDepartureInCL010Page => arbitrary[Boolean].map(JsBoolean)
      case ProcedureTypePage            => arbitrary[ProcedureType].map(Json.toJson(_))
      case SecurityDetailsTypePage      => arbitrary[SecurityDetailsType].map(Json.toJson(_))
    }
  }

  private def generateTransportAnswer: PartialFunction[Gettable[_], Gen[JsValue]] =
    generatePreRequisitesAnswer orElse
      generateTransportMeansAnswer orElse
      generateSupplyChainActorsAnswers orElse
      generateAuthorisationAnswers orElse
      generateLimitAnswers orElse
      generateCarrierDetailsAnswers orElse
      generateEquipmentsAndChargesAnswers

  private def generatePreRequisitesAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.preRequisites._
    {
      case SameUcrYesNoPage                  => arbitrary[Boolean].map(JsBoolean)
      case UniqueConsignmentReferencePage    => Gen.alphaNumStr.map(JsString)
      case SameCountryOfDispatchYesNoPage    => arbitrary[Boolean].map(JsBoolean)
      case CountryOfDispatchPage             => arbitrary[Country].map(Json.toJson(_))
      case TransportedToSameCountryYesNoPage => arbitrary[Boolean].map(JsBoolean)
      case ItemsDestinationCountryPage       => arbitrary[Country].map(Json.toJson(_))
      case ContainerIndicatorPage            => arbitrary[Option[Boolean]].map(Json.toJson(_))
    }
  }

  private def generateTransportMeansAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.transportMeans._
    generateTransportMeansDepartureAnswer orElse
      generateTransportMeansActiveAnswer orElse {
        case AddInlandModeYesNoPage                 => arbitrary[Boolean].map(JsBoolean)
        case InlandModePage                         => arbitrary[InlandMode].map(Json.toJson(_))
        case AddDepartureTransportMeansYesNoPage    => arbitrary[Boolean].map(JsBoolean)
        case AddBorderModeOfTransportYesNoPage      => arbitrary[Boolean].map(JsBoolean)
        case BorderModeOfTransportPage              => arbitrary[BorderModeOfTransport].map(Json.toJson(_))
        case AddActiveBorderTransportMeansYesNoPage => arbitrary[Boolean].map(JsBoolean)
      }
  }

  private def generateTransportMeansDepartureAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.transportMeans.departure._
    {
      case AddIdentificationTypeYesNoPage   => arbitrary[Boolean].map(JsBoolean)
      case IdentificationPage               => arbitrary[Identification].map(Json.toJson(_))
      case AddIdentificationNumberYesNoPage => arbitrary[Boolean].map(JsBoolean)
      case MeansIdentificationNumberPage    => Gen.alphaNumStr.map(JsString)
      case AddVehicleCountryYesNoPage       => arbitrary[Boolean].map(JsBoolean)
      case VehicleCountryPage               => arbitrary[Nationality].map(Json.toJson(_))
    }
  }

  private def generateTransportMeansActiveAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.transportMeans.active._
    {
      case IdentificationPage(_)                 => arbitrary[ActiveIdentification].map(Json.toJson(_))
      case IdentificationNumberPage(_)           => Gen.alphaNumStr.map(JsString)
      case AddNationalityYesNoPage(_)            => arbitrary[Boolean].map(JsBoolean)
      case NationalityPage(_)                    => arbitrary[Nationality].map(Json.toJson(_))
      case CustomsOfficeActiveBorderPage(_)      => arbitrary[CustomsOffice].map(Json.toJson(_))
      case ConveyanceReferenceNumberYesNoPage(_) => arbitrary[Boolean].map(JsBoolean)
      case ConveyanceReferenceNumberPage(_)      => Gen.alphaNumStr.map(JsString)
    }
  }

  private def generateSupplyChainActorsAnswers: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.supplyChainActors._

    val pf: PartialFunction[Gettable[_], Gen[JsValue]] = {
      case SupplyChainActorYesNoPage => arbitrary[Boolean].map(JsBoolean)
    }

    pf orElse
      generateSupplyChainActorAnswers
  }

  private def generateSupplyChainActorAnswers: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.supplyChainActors.index._
    {
      case SupplyChainActorTypePage(_) => arbitrary[SupplyChainActorType].map(Json.toJson(_))
      case IdentificationNumberPage(_) => Gen.alphaNumStr.map(JsString)
    }
  }

  private def generateAuthorisationAnswers: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.authorisationsAndLimit.authorisations._
    import pages.authorisationsAndLimit.authorisations.index._
    {
      case AuthorisationsInferredPage          => arbitrary[Boolean].map(JsBoolean)
      case AddAuthorisationsYesNoPage          => arbitrary[Boolean].map(JsBoolean)
      case AuthorisationTypePage(_)            => arbitrary[AuthorisationType].map(Json.toJson(_))
      case AuthorisationReferenceNumberPage(_) => Gen.alphaNumStr.map(JsString)
    }
  }

  private def generateLimitAnswers: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.authorisationsAndLimit.limit.LimitDatePage
    {
      case LimitDatePage => arbitrary[LocalDate].map(Json.toJson(_))
    }
  }

  private def generateCarrierDetailsAnswers: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.carrierDetails._

    val pf: PartialFunction[Gettable[_], Gen[JsValue]] = {
      case CarrierDetailYesNoPage => arbitrary[Boolean].map(JsBoolean)
    }

    pf orElse
      generateCarrierDetailAnswers
  }

  private def generateCarrierDetailAnswers: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.carrierDetails._
    import pages.carrierDetails.contact._
    {
      case IdentificationNumberPage => Gen.alphaNumStr.map(JsString)
      case AddContactYesNoPage      => arbitrary[Boolean].map(JsBoolean)
      case NamePage                 => Gen.alphaNumStr.map(JsString)
      case TelephoneNumberPage      => Gen.alphaNumStr.map(JsString)
    }
  }

  private def generateEquipmentsAndChargesAnswers: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.equipment._
    val pf: PartialFunction[Gettable[_], Gen[JsValue]] = {
      case AddTransportEquipmentYesNoPage => arbitrary[Boolean].map(JsBoolean)
      case AddPaymentMethodYesNoPage      => arbitrary[Boolean].map(JsBoolean)
      case PaymentMethodPage              => arbitrary[PaymentMethod].map(Json.toJson(_))
    }

    pf orElse
      generateEquipmentAnswers
  }

  private def generateEquipmentAnswers: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.equipment.index._

    val pf: PartialFunction[Gettable[_], Gen[JsValue]] = {
      case AddContainerIdentificationNumberYesNoPage(_) => arbitrary[Boolean].map(JsBoolean)
      case ContainerIdentificationNumberPage(_)         => Gen.alphaNumStr.map(JsString)
      case AddSealYesNoPage(_)                          => arbitrary[Boolean].map(JsBoolean)
    }

    pf orElse
      generateSealAnswers
  }

  private def generateSealAnswers: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.equipment.index.seals._
    {
      case IdentificationNumberPage(_, _) => Gen.alphaNumStr.map(JsString)
    }
  }
}
