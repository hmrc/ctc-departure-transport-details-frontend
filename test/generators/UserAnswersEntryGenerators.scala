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
import models.reference.authorisations.AuthorisationType
import models.reference.equipment.PaymentMethod
import models.reference._
import models.reference.supplyChainActors.SupplyChainActorType
import models.reference.transportMeans._
import models.reference.BorderMode
import models.reference.additionalInformation.AdditionalInformationCode
import models.reference.additionalReference.AdditionalReferenceType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.additionalInformation.index.AddCommentsYesNoPage
import play.api.libs.json._
import queries.Gettable

import java.time.LocalDate

trait UserAnswersEntryGenerators {
  self: Generators =>

  def generateAnswer: PartialFunction[Gettable[?], Gen[JsValue]] =
    generateExternalAnswer orElse
      generateTransportAnswer

  private def generateExternalAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.external._
    {
      case ApprovedOperatorPage          => arbitrary[Boolean].map(JsBoolean)
      case DeclarationTypePage           => arbitrary[String](arbitraryDeclarationType).map(Json.toJson(_))
      case OfficeOfDestinationPage       => arbitrary[CustomsOffice].map(Json.toJson(_))
      case OfficeOfDepartureInCL010Page  => arbitrary[Boolean].map(JsBoolean)
      case ProcedureTypePage             => arbitrary[ProcedureType.Value].map(Json.toJson(_))
      case SecurityDetailsTypePage       => arbitrary[String](arbitrarySecurityDetailsType).map(Json.toJson(_))
      case AdditionalDeclarationTypePage => arbitrary[String](arbitraryAdditionalDeclarationType).map(Json.toJson(_))
    }
  }

  private def generateTransportAnswer: PartialFunction[Gettable[?], Gen[JsValue]] =
    generatePreRequisitesAnswer orElse
      generateTransportMeansAnswer orElse
      generateSupplyChainActorsAnswers orElse
      generateAuthorisationsAndLimitAnswers orElse
      generateCarrierDetailsAnswers orElse
      generateEquipmentsAndChargesAnswers orElse
      generateAdditionalReferencesAnswers orElse
      generateAdditionalInformationAnswers

  private def generatePreRequisitesAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.preRequisites._
    {
      case SameUcrYesNoPage                  => arbitrary[Boolean].map(JsBoolean)
      case UniqueConsignmentReferencePage    => Gen.alphaNumStr.map(JsString.apply)
      case SameCountryOfDispatchYesNoPage    => arbitrary[Boolean].map(JsBoolean)
      case AddCountryOfDestinationPage       => arbitrary[Option[Boolean]].map(Json.toJson(_))
      case CountryOfDispatchPage             => arbitrary[Country].map(Json.toJson(_))
      case TransportedToSameCountryYesNoPage => arbitrary[Boolean].map(JsBoolean)
      case ItemsDestinationCountryPage       => arbitrary[Country].map(Json.toJson(_))
      case ContainerIndicatorPage            => arbitrary[Option[Boolean]].map(Json.toJson(_))
    }
  }

  private def generateTransportMeansAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.transportMeans._
    generateTransportMeansDepartureAnswer orElse
      generateTransportMeansActiveAnswer orElse {
        case AddInlandModeYesNoPage                 => arbitrary[Boolean].map(JsBoolean)
        case InlandModePage                         => arbitrary[InlandMode].map(Json.toJson(_))
        case AddDepartureTransportMeansYesNoPage    => arbitrary[Boolean].map(JsBoolean)
        case AddBorderModeOfTransportYesNoPage      => arbitrary[Boolean].map(JsBoolean)
        case BorderModeOfTransportPage              => arbitrary[BorderMode].map(Json.toJson(_))
        case AddActiveBorderTransportMeansYesNoPage => arbitrary[Boolean].map(JsBoolean)
      }
  }

  private def generateTransportMeansDepartureAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.transportMeans.departure._
    {
      case AddIdentificationTypeYesNoPage(_)   => arbitrary[Boolean].map(JsBoolean)
      case IdentificationPage(_)               => arbitrary[departure.Identification].map(Json.toJson(_))
      case AddIdentificationNumberYesNoPage(_) => arbitrary[Boolean].map(JsBoolean)
      case MeansIdentificationNumberPage(_)    => Gen.alphaNumStr.map(JsString.apply)
      case AddVehicleCountryYesNoPage(_)       => arbitrary[Boolean].map(JsBoolean)
      case VehicleCountryPage(_)               => arbitrary[Nationality].map(Json.toJson(_))
    }
  }

  private def generateTransportMeansActiveAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.transportMeans.active._
    {
      case IdentificationPage(_)                 => arbitrary[active.Identification].map(Json.toJson(_))
      case IdentificationNumberPage(_)           => Gen.alphaNumStr.map(JsString.apply)
      case AddNationalityYesNoPage(_)            => arbitrary[Boolean].map(JsBoolean)
      case NationalityPage(_)                    => arbitrary[Nationality].map(Json.toJson(_))
      case CustomsOfficeActiveBorderPage(_)      => arbitrary[CustomsOffice].map(Json.toJson(_))
      case ConveyanceReferenceNumberYesNoPage(_) => arbitrary[Boolean].map(JsBoolean)
      case ConveyanceReferenceNumberPage(_)      => Gen.alphaNumStr.map(JsString.apply)
    }
  }

  private def generateSupplyChainActorsAnswers: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.supplyChainActors._

    val pf: PartialFunction[Gettable[?], Gen[JsValue]] = {
      case SupplyChainActorYesNoPage => arbitrary[Boolean].map(JsBoolean)
    }

    pf orElse
      generateSupplyChainActorAnswers
  }

  private def generateSupplyChainActorAnswers: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.supplyChainActors.index._
    {
      case SupplyChainActorTypePage(_) => arbitrary[SupplyChainActorType].map(Json.toJson(_))
      case IdentificationNumberPage(_) => Gen.alphaNumStr.map(JsString.apply)
    }
  }

  private def generateAuthorisationsAndLimitAnswers: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.authorisationsAndLimit._

    val pf: PartialFunction[Gettable[?], Gen[JsValue]] = {
      case AuthorisationsInferredPage => arbitrary[Boolean].map(JsBoolean)
      case AddAuthorisationsYesNoPage => arbitrary[Boolean].map(JsBoolean)
    }

    pf orElse generateAuthorisationAnswers orElse generateLimitAnswers
  }

  private def generateAuthorisationAnswers: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.authorisationsAndLimit.authorisations.index._
    {
      case AuthorisationTypePage(_)            => arbitrary[AuthorisationType].map(Json.toJson(_))
      case AuthorisationReferenceNumberPage(_) => Gen.alphaNumStr.map(JsString.apply)
    }
  }

  private def generateLimitAnswers: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.authorisationsAndLimit.limit._
    {
      case AddLimitDateYesNoPage => arbitrary[Boolean].map(JsBoolean)
      case LimitDatePage         => arbitrary[LocalDate].map(Json.toJson(_))
    }
  }

  private def generateCarrierDetailsAnswers: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.carrierDetails._

    val pf: PartialFunction[Gettable[?], Gen[JsValue]] = {
      case CarrierDetailYesNoPage => arbitrary[Boolean].map(JsBoolean)
    }

    pf orElse
      generateCarrierDetailAnswers
  }

  private def generateCarrierDetailAnswers: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.carrierDetails._
    import pages.carrierDetails.contact._
    {
      case IdentificationNumberPage => Gen.alphaNumStr.map(JsString.apply)
      case AddContactYesNoPage      => arbitrary[Boolean].map(JsBoolean)
      case NamePage                 => Gen.alphaNumStr.map(JsString.apply)
      case TelephoneNumberPage      => Gen.alphaNumStr.map(JsString.apply)
    }
  }

  private def generateEquipmentsAndChargesAnswers: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.equipment._
    val pf: PartialFunction[Gettable[?], Gen[JsValue]] = {
      case AddTransportEquipmentYesNoPage => arbitrary[Boolean].map(JsBoolean)
      case AddPaymentMethodYesNoPage      => arbitrary[Boolean].map(JsBoolean)
      case PaymentMethodPage              => arbitrary[PaymentMethod].map(Json.toJson(_))
    }

    pf orElse
      generateEquipmentAnswers
  }

  private def generateEquipmentAnswers: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.equipment.index._

    val pf: PartialFunction[Gettable[?], Gen[JsValue]] = {
      case AddContainerIdentificationNumberYesNoPage(_) => arbitrary[Boolean].map(JsBoolean)
      case ContainerIdentificationNumberPage(_)         => Gen.alphaNumStr.map(JsString.apply)
      case AddSealYesNoPage(_)                          => arbitrary[Boolean].map(JsBoolean)
    }

    pf orElse
      generateSealAnswers
  }

  private def generateSealAnswers: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.equipment.index.seals._
    {
      case IdentificationNumberPage(_, _) => Gen.alphaNumStr.map(JsString.apply)
    }
  }

  private def generateAdditionalReferencesAnswers: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.additionalReference._

    val pf: PartialFunction[Gettable[?], Gen[JsValue]] = {
      case AddAdditionalReferenceYesNoPage => arbitrary[Boolean].map(JsBoolean)
    }

    pf orElse
      generateAdditionalReferenceAnswers
  }

  private def generateAdditionalReferenceAnswers: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.additionalReference.index._
    {
      case AdditionalReferenceTypePage(_)           => arbitrary[AdditionalReferenceType].map(Json.toJson(_))
      case AddAdditionalReferenceNumberYesNoPage(_) => arbitrary[Boolean].map(JsBoolean)
      case AdditionalReferenceNumberPage(_)         => Gen.alphaNumStr.map(JsString.apply)
    }
  }

  private def generateAdditionalInformationAnswers: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.additionalInformation._

    val pf: PartialFunction[Gettable[?], Gen[JsValue]] = {
      case AddAdditionalInformationYesNoPage => arbitrary[Boolean].map(JsBoolean)
      case AddCommentsYesNoPage(_)           => arbitrary[Boolean].map(JsBoolean)
    }

    pf orElse
      generateAdditionalInformationListAnswers
  }

  private def generateAdditionalInformationListAnswers: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.additionalInformation.index._
    {
      case AdditionalInformationTypePage(_) => arbitrary[AdditionalInformationCode].map(Json.toJson(_))
      case AdditionalInformationTextPage(_) => Gen.alphaNumStr.map(JsString.apply)
    }
  }
}
