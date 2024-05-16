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

import models.LockCheck.{LockCheckFailure, Locked, Unlocked}
import models._
import models.reference._
import models.reference.additionalReference.AdditionalReferenceType
import models.reference.authorisations.AuthorisationType
import models.reference.equipment.PaymentMethod
import models.reference.supplyChainActors.SupplyChainActorType
import models.reference.transportMeans._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.mvc.Call
import uk.gov.hmrc.http.HttpVerbs._

trait ModelGenerators {
  self: Generators =>

  lazy val arbitraryDeclarationType: Arbitrary[String] =
    Arbitrary {
      Gen.oneOf("T", "T1", "T2", "T2F", "TIR")
    }

  lazy val arbitraryNonTIRDeclarationType: Arbitrary[String] =
    Arbitrary {
      Gen.oneOf("T", "T1", "T2", "T2F")
    }

  lazy val arbitraryAdditionalDeclarationType: Arbitrary[String] =
    Arbitrary {
      Gen.oneOf("A", "D")
    }

  implicit lazy val arbitraryLocalReferenceNumber: Arbitrary[LocalReferenceNumber] =
    Arbitrary {
      for {
        lrn <- stringsWithMaxLength(22: Int, Gen.alphaNumChar)
      } yield new LocalReferenceNumber(lrn)
    }

  implicit lazy val arbitraryEoriNumber: Arbitrary[EoriNumber] =
    Arbitrary {
      for {
        number <- stringsWithMaxLength(17: Int)
      } yield EoriNumber(number)
    }

  lazy val arbitraryAmendedmentSubmissionState: Arbitrary[SubmissionState] = Arbitrary {
    val values = Seq(
      SubmissionState.GuaranteeAmendment,
      SubmissionState.RejectedPendingChanges,
      SubmissionState.Amendment
    )
    Gen.oneOf(values)
  }

  implicit lazy val arbitraryNonAmendmentSubmissionState: Arbitrary[SubmissionState] = Arbitrary {
    val values = Seq(
      SubmissionState.NotSubmitted,
      SubmissionState.Submitted
    )
    Gen.oneOf(values)
  }

  implicit lazy val arbitraryCustomsOffice: Arbitrary[CustomsOffice] =
    Arbitrary {
      for {
        id          <- nonEmptyString
        name        <- nonEmptyString
        phoneNumber <- Gen.option(Gen.alphaNumStr)
      } yield CustomsOffice(id, name, phoneNumber)
    }

  implicit lazy val arbitraryMode: Arbitrary[Mode] = Arbitrary {
    Gen.oneOf(NormalMode, CheckMode)
  }

  implicit lazy val arbitraryPhase: Arbitrary[Phase] = Arbitrary {
    Gen.oneOf(Phase.Transition, Phase.PostTransition)
  }

  implicit lazy val arbitraryIndex: Arbitrary[Index] = Arbitrary {
    for {
      position <- Gen.choose(0: Int, 10: Int)
    } yield Index(position)
  }

  implicit lazy val arbitraryCall: Arbitrary[Call] = Arbitrary {
    for {
      method <- Gen.oneOf(GET, POST)
      url    <- nonEmptyString
    } yield Call(method, url)
  }

  implicit lazy val arbitraryProcedureType: Arbitrary[ProcedureType.Value] =
    Arbitrary {
      Gen.oneOf(ProcedureType.values)
    }

  lazy val arbitrarySecurityDetailsType: Arbitrary[String] =
    Arbitrary {
      Gen.oneOf("0", "1", "2", "3")
    }

  implicit lazy val arbitraryBorderModeOfTransport: Arbitrary[BorderMode] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("1", "2", "3", "4")
        description <- nonEmptyString
      } yield BorderMode(code, description)
    }

  implicit lazy val arbitraryOptionalNonAirBorderModeOfTransport: Arbitrary[Option[BorderMode]] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("1", "2", "3")
        description <- nonEmptyString
      } yield Some(BorderMode(code, description))
    }

  lazy val arbitraryOptionalNonRailBorderModeOfTransport: Arbitrary[Option[BorderMode]] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("1", "3", "4")
        description <- nonEmptyString
      } yield Some(BorderMode(code, description))
    }

  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      for {
        code <- arbitrary[CountryCode]
        name <- nonEmptyString
      } yield Country(code, name)
    }

  implicit lazy val arbitraryCountryCode: Arbitrary[CountryCode] =
    Arbitrary {
      Gen
        .pick(CountryCode.Constants.countryCodeLength, 'A' to 'Z')
        .map(
          code => CountryCode(code.mkString)
        )
    }

  implicit lazy val arbitraryNationality: Arbitrary[Nationality] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- nonEmptyString
      } yield Nationality(code, desc)
    }

  implicit lazy val arbitraryAdditionalReferenceType: Arbitrary[AdditionalReferenceType] =
    Arbitrary {
      for {
        docType <- nonEmptyString
        desc    <- nonEmptyString
      } yield AdditionalReferenceType(docType, desc)
    }

  implicit def arbitrarySelectableList[T <: Selectable](implicit arbitrary: Arbitrary[T]): Arbitrary[SelectableList[T]] = Arbitrary {
    for {
      values <- listWithMaxLength[T]()
    } yield SelectableList(values.distinctBy(_.value))
  }

  implicit lazy val arbitraryInlandMode: Arbitrary[InlandMode] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("1", "2", "3", "4", "5", "7", "8")
        description <- nonEmptyString
      } yield InlandMode(code, description)
    }

  val arbitraryNonMailInlandMode: Arbitrary[InlandMode] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("1", "2", "3", "4", "7", "8")
        description <- nonEmptyString
      } yield InlandMode(code, description)
    }

  val arbitraryNonRailInlandMode: Arbitrary[InlandMode] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("1", "3", "4", "5", "7", "8")
        description <- nonEmptyString
      } yield InlandMode(code, description)
    }

  val arbitraryMaritimeRailAirInlandMode: Arbitrary[InlandMode] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("1", "2", "4")
        description <- nonEmptyString
      } yield InlandMode(code, description)
    }

  val arbitraryNonMaritimeRailAirInlandMode: Arbitrary[InlandMode] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("3", "5", "7", "8")
        description <- nonEmptyString
      } yield InlandMode(code, description)
    }

  val arbitraryNonRoadInlandMode: Arbitrary[InlandMode] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("1", "2", "4", "5", "7", "8")
        description <- nonEmptyString
      } yield InlandMode(code, description)
    }

  val arbitraryOptionalNonMaritimeRailAirInlandMode: Arbitrary[Option[InlandMode]] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("3", "5", "7", "8")
        description <- nonEmptyString
        gen         <- Gen.oneOf(Some(InlandMode(code, description)), None)
      } yield gen
    }

  implicit lazy val arbitraryPaymentMethod: Arbitrary[PaymentMethod] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("A", "B", "C", "H", "Y", "Z", "D")
        description <- nonEmptyString
      } yield PaymentMethod(code, description)
    }

  implicit lazy val arbitraryAuthorisationType: Arbitrary[AuthorisationType] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("C521", "C523", "C524")
        description <- nonEmptyString
      } yield AuthorisationType(code, description)
    }

  implicit lazy val arbitraryIdentificationActive: Arbitrary[active.Identification] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("10", "11", "21", "30", "40", "41", "80", "81")
        description <- nonEmptyString
      } yield active.Identification(code, description)
    }

  implicit lazy val arbitraryIdentificationDeparture: Arbitrary[departure.Identification] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("10", "11", "20", "21", "30", "31", "40", "41", "80", "81")
        description <- nonEmptyString
      } yield departure.Identification(code, description)
    }

  implicit lazy val arbitrarySupplyChainActorType: Arbitrary[SupplyChainActorType] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("CS", "FW", "MF", "WH")
        description <- nonEmptyString
      } yield SupplyChainActorType(code, description)
    }

  lazy val arbitraryIncompleteTaskStatus: Arbitrary[TaskStatus] = Arbitrary {
    Gen.oneOf(TaskStatus.InProgress, TaskStatus.NotStarted, TaskStatus.CannotStartYet)
  }

  lazy val arbitrarySomeSecurityDetailsType: Arbitrary[String] =
    Arbitrary {
      Gen.oneOf("1", "2", "3")
    }

  implicit lazy val arbitraryOptionalBoolean: Arbitrary[OptionalBoolean] =
    Arbitrary {
      Gen.oneOf(OptionalBoolean.yes, OptionalBoolean.no, OptionalBoolean.maybe)
    }

  implicit lazy val arbitraryLockCheck: Arbitrary[LockCheck] =
    Arbitrary {
      Gen.oneOf(Locked, Unlocked, LockCheckFailure)
    }

  implicit def arbitraryRadioableList[T <: Radioable[T]](implicit arbitrary: Arbitrary[T]): Arbitrary[Seq[T]] = Arbitrary {
    for {
      values <- listWithMaxLength[T]()
    } yield values.distinctBy(_.code)
  }
}
