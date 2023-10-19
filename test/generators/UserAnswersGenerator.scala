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

import config.PhaseConfig
import models.domain.UserAnswersReader
import models.journeyDomain.OpsError.ReaderError
import models.journeyDomain.authorisationsAndLimit.authorisations.AuthorisationDomain
import models.journeyDomain.authorisationsAndLimit.limit.LimitDomain
import models.journeyDomain.equipment.seal.SealDomain
import models.journeyDomain.equipment.{EquipmentDomain, EquipmentsDomain}
import models.journeyDomain.supplyChainActors.SupplyChainActorDomain
import models.journeyDomain.transportMeans.{TransportMeansActiveDomain, TransportMeansDepartureDomain, TransportMeansDomain}
import models.journeyDomain.{PreRequisitesDomain, TransportDomain}
import models.{EoriNumber, Index, LocalReferenceNumber, RichJsObject, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import models.SubmissionState

trait UserAnswersGenerator extends UserAnswersEntryGenerators {
  self: Generators =>

  implicit def arbitraryUserAnswers(implicit phaseConfig: PhaseConfig): Arbitrary[UserAnswers] =
    Arbitrary {
      for {
        lrn             <- arbitrary[LocalReferenceNumber]
        eoriNumber      <- arbitrary[EoriNumber]
        submissionState <- arbitrary[SubmissionState]
        answers         <- buildUserAnswers[TransportDomain](UserAnswers(lrn, eoriNumber, submissionState))
      } yield answers
    }

  protected def buildUserAnswers[T](
    initialUserAnswers: UserAnswers
  )(implicit userAnswersReader: UserAnswersReader[T]): Gen[UserAnswers] = {

    def rec(userAnswers: UserAnswers): Gen[UserAnswers] =
      userAnswersReader.run(userAnswers) match {
        case Left(ReaderError(page, _)) =>
          generateAnswer
            .apply(page)
            .map {
              value =>
                userAnswers.copy(
                  data = userAnswers.data.setObject(page.path, value).getOrElse(userAnswers.data)
                )
            }
            .flatMap(rec)
        case Right(_) => Gen.const(userAnswers)
      }

    rec(initialUserAnswers)
  }

  def arbitraryTransportAnswers(userAnswers: UserAnswers)(implicit phaseConfig: PhaseConfig): Gen[UserAnswers] =
    buildUserAnswers[TransportDomain](userAnswers)

  def arbitraryPreRequisitesAnswers(userAnswers: UserAnswers): Gen[UserAnswers] =
    buildUserAnswers[PreRequisitesDomain](userAnswers)

  def arbitraryTransportMeansAnswers(userAnswers: UserAnswers)(implicit phaseConfig: PhaseConfig): Gen[UserAnswers] =
    buildUserAnswers[TransportMeansDomain](userAnswers)

  def arbitraryTransportMeansDepartureAnswers(userAnswers: UserAnswers)(implicit phaseConfig: PhaseConfig): Gen[UserAnswers] =
    buildUserAnswers[TransportMeansDepartureDomain](userAnswers)

  def arbitraryTransportMeansActiveAnswers(userAnswers: UserAnswers, index: Index)(implicit phaseConfig: PhaseConfig): Gen[UserAnswers] =
    buildUserAnswers[TransportMeansActiveDomain](userAnswers)(TransportMeansActiveDomain.userAnswersReader(index))

  def arbitrarySupplyChainActorAnswers(userAnswers: UserAnswers, index: Index): Gen[UserAnswers] =
    buildUserAnswers[SupplyChainActorDomain](userAnswers)(SupplyChainActorDomain.userAnswersReader(index))

  def arbitraryAuthorisationAnswers(userAnswers: UserAnswers, index: Index): Gen[UserAnswers] =
    buildUserAnswers[AuthorisationDomain](userAnswers)(AuthorisationDomain.userAnswersReader(index))

  def arbitraryLimitAnswers(userAnswers: UserAnswers): Gen[UserAnswers] =
    buildUserAnswers[LimitDomain](userAnswers)

  def arbitraryEquipmentsAnswers(userAnswers: UserAnswers): Gen[UserAnswers] =
    buildUserAnswers[EquipmentsDomain](userAnswers)

  def arbitraryEquipmentAnswers(userAnswers: UserAnswers, equipmentIndex: Index): Gen[UserAnswers] =
    buildUserAnswers[EquipmentDomain](userAnswers)(EquipmentDomain.userAnswersReader(equipmentIndex))

  def arbitrarySealAnswers(userAnswers: UserAnswers, equipmentIndex: Index, sealIndex: Index): Gen[UserAnswers] =
    buildUserAnswers[SealDomain](userAnswers)(SealDomain.userAnswersReader(equipmentIndex, sealIndex))

}
