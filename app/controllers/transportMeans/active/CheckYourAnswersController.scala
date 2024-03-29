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

package controllers.transportMeans.active

import controllers.actions._
import models.{Index, LocalReferenceNumber, Mode}
import navigation.TransportMeansActiveListNavigatorProvider
import pages.sections.transportMeans.ActiveSection
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transportMeans.active.ActiveBorderAnswersViewModel.ActiveBorderAnswersViewModelProvider
import views.html.transportMeans.active.CheckYourAnswersView

import javax.inject.Inject

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  navigatorProvider: TransportMeansActiveListNavigatorProvider,
  view: CheckYourAnswersView,
  viewModelProvider: ActiveBorderAnswersViewModelProvider
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, activeIndex: Index): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val section = viewModelProvider(request.userAnswers, mode, activeIndex).sections
      Ok(view(lrn, mode, activeIndex, section))
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, activeIndex: Index): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      Redirect(navigatorProvider(mode).nextPage(request.userAnswers, Some(ActiveSection(activeIndex))))
  }
}
