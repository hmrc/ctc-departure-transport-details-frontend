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

package navigation

import config.{FrontendAppConfig, PhaseConfig}
import models.journeyDomain.{TransportDomain, UserAnswersReader}
import models.{Index, Mode, UserAnswers}
import pages.Page
import pages.additionalInformation.index.{AdditionalInformationTextPage, AdditionalInformationTypePage}
import pages.additionalInformation.{AddAdditionalInformationYesNoPage, AddCommentsYesNoPage}
import pages.additionalReference.AddAdditionalReferenceYesNoPage
import pages.sections.additionalInformation.AdditionalInformationListSection
import pages.sections.additionalReference.AdditionalReferencesSection
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class TransportNavigatorProviderImpl @Inject() (implicit appConfig: FrontendAppConfig, phaseConfig: PhaseConfig) extends TransportNavigatorProvider {

  override def apply(mode: Mode): UserAnswersNavigator =
    new TransportNavigator(mode)
}

trait TransportNavigatorProvider {
  def apply(mode: Mode): UserAnswersNavigator
}

class TransportNavigator(override val mode: Mode)(implicit override val appConfig: FrontendAppConfig, override val phaseConfig: PhaseConfig)
    extends UserAnswersNavigator
    with CustomNavigator {

  override type T = TransportDomain

  implicit override val reader: UserAnswersReader[TransportDomain] =
    TransportDomain.userAnswersReader

  override def nextPage(userAnswers: UserAnswers, currentPage: Option[Page]) = currentPage match {
    case Some(page) => navigateToNextPage(page, userAnswers, mode)
    case None       => super.nextPage(userAnswers, currentPage)
  }

  override protected def normalRoutes(mode: Mode) = {
    case AddAdditionalReferenceYesNoPage      => ua => additionalRefYesNoRoute(ua, mode)
    case AdditionalReferencesSection          => ua => AddAdditionalInformationYesNoPage.route(ua, mode)
    case AddAdditionalInformationYesNoPage    => ua => additionalInfoYesNoRoute(ua, mode)
    case AdditionalInformationTypePage(index) => ua => AddCommentsYesNoPage(index).route(ua, mode)
    case AddCommentsYesNoPage(index)          => ua => addCommentYesNoRoute(ua, index, mode)
    case AdditionalInformationTextPage(_) =>
      ua => Some(controllers.additionalInformation.routes.AddAnotherAdditionalInformationController.onPageLoad(ua.lrn, mode))
    case AdditionalInformationListSection => ua => Some(controllers.routes.TransportAnswersController.onPageLoad(ua.lrn))
  }

  private def additionalRefYesNoRoute(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    userAnswers.get(AddAdditionalReferenceYesNoPage) flatMap {
      case true  => ???
      case false => AddAdditionalInformationYesNoPage.route(userAnswers, mode)
    }

  private def additionalInfoYesNoRoute(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    userAnswers.get(AddAdditionalInformationYesNoPage) flatMap {
      case true  => AdditionalInformationTypePage(Index(0)).route(userAnswers, mode)
      case false => Some(controllers.routes.TransportAnswersController.onPageLoad(userAnswers.lrn))
    }

  private def addCommentYesNoRoute(userAnswers: UserAnswers, index: Index, mode: Mode): Option[Call] =
    userAnswers.get(AddCommentsYesNoPage(index)) flatMap {
      case true  => AdditionalInformationTextPage(index).route(userAnswers, mode)
      case false => Some(controllers.additionalInformation.routes.AddAnotherAdditionalInformationController.onPageLoad(userAnswers.lrn, mode))
    }
}
