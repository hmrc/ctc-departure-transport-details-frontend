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
import models.reference._
import models.transportMeans.departure.InlandMode
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.mvc.Call
import uk.gov.hmrc.http.HttpVerbs._

trait ModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryDeclarationType: Arbitrary[DeclarationType] =
    Arbitrary {
      Gen.oneOf(DeclarationType.values)
    }

  lazy val arbitraryNonOption4DeclarationType: Arbitrary[DeclarationType] =
    Arbitrary {
      Gen.oneOf(DeclarationType.values.filterNot(_ == DeclarationType.Option4))
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

  implicit lazy val arbitraryProcedureType: Arbitrary[ProcedureType] =
    Arbitrary {
      Gen.oneOf(ProcedureType.values)
    }

  implicit lazy val arbitrarySecurityDetailsType: Arbitrary[SecurityDetailsType] =
    Arbitrary {
      Gen.oneOf(SecurityDetailsType.values)
    }

  implicit lazy val arbitraryBorderModeOfTransport: Arbitrary[models.transportMeans.BorderModeOfTransport] =
    Arbitrary {
      Gen.oneOf(models.transportMeans.BorderModeOfTransport.values)
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

  implicit lazy val arbitraryCountryList: Arbitrary[CountryList] = Arbitrary {
    for {
      countries <- listWithMaxLength[Country]()
    } yield CountryList(countries)
  }

  implicit lazy val arbitraryInlandMode: Arbitrary[InlandMode] =
    Arbitrary {
      Gen.oneOf(InlandMode.values)
    }

  implicit lazy val arbitraryPaymentMethod: Arbitrary[models.equipment.PaymentMethod] =
    Arbitrary {
      Gen.oneOf(models.equipment.PaymentMethod.values)
    }

  implicit lazy val arbitraryAuthorisationType: Arbitrary[models.authorisations.AuthorisationType] =
    Arbitrary {
      Gen.oneOf(models.authorisations.AuthorisationType.values)
    }

  implicit lazy val arbitraryIdentificationDeparture: Arbitrary[models.transportMeans.departure.Identification] =
    Arbitrary {
      Gen.oneOf(models.transportMeans.departure.Identification.values)
    }

  implicit lazy val arbitraryActiveIdentificationDeparture: Arbitrary[models.transportMeans.active.Identification] =
    Arbitrary {
      Gen.oneOf(models.transportMeans.active.Identification.values)
    }

  implicit lazy val arbitrarySupplyChainActorType: Arbitrary[models.supplyChainActors.SupplyChainActorType] =
    Arbitrary {
      Gen.oneOf(models.supplyChainActors.SupplyChainActorType.values)
    }

  lazy val arbitraryIncompleteTaskStatus: Arbitrary[TaskStatus] = Arbitrary {
    Gen.oneOf(TaskStatus.InProgress, TaskStatus.NotStarted, TaskStatus.CannotStartYet)
  }

  lazy val arbitrarySomeSecurityDetailsType: Arbitrary[SecurityDetailsType] =
    Arbitrary {
      Gen.oneOf(SecurityDetailsType.values.filterNot(_ == SecurityDetailsType.NoSecurityDetails))
    }

  lazy val arbitraryNonMailOrUnknownInlandMode: Arbitrary[InlandMode] =
    Arbitrary {
      Gen.oneOf(InlandMode.values.filterNot(_ == InlandMode.Mail).filterNot(_ == InlandMode.Unknown))
    }

}
