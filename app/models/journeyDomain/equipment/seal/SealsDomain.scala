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

package models.journeyDomain.equipment.seal

import models.domain._
import models.journeyDomain.{JourneyDomainModel, ReaderSuccess}
import models.{Index, RichJsArray}
import pages.sections.Section
import pages.sections.equipment.SealsSection

case class SealsDomain(value: Seq[SealDomain])(equipmentIndex: Index) extends JourneyDomainModel {

  override def page: Option[Section[_]] = Some(SealsSection(equipmentIndex))
}

object SealsDomain {

  implicit def userAnswersReader(equipmentIndex: Index): Read[SealsDomain] = {

    val sealsReader: Read[Seq[SealDomain]] =
      SealsSection(equipmentIndex).arrayReader.apply(_).flatMap {
        case ReaderSuccess(x, pages) if x.isEmpty =>
          SealDomain.userAnswersReader(equipmentIndex, Index(0)).toSeq.apply(pages)
        case ReaderSuccess(x, pages) =>
          x.traverse[SealDomain](SealDomain.userAnswersReader(equipmentIndex, _).apply(_)).apply(pages)
      }

    sealsReader.map(SealsDomain.apply(_)(equipmentIndex))
  }
}
