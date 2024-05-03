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

package controllers.transportMeans.departure

import config.{FrontendAppConfig, PhaseConfig}
import controllers.actions.Actions
import models.{Index, LocalReferenceNumber}
import navigation.TransportNavigatorProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.DepartureTransportAnswersViewModel.DepartureTransportAnswersViewModelProvider
import views.html.transportMeans.DepartureTransportAnswersView

import javax.inject.Inject

class DepartureTransportAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: DepartureTransportAnswersView,
  navigatorProvider: TransportNavigatorProvider,
  viewModelProvider: DepartureTransportAnswersViewModelProvider,
  config: FrontendAppConfig
)(implicit phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(lrn: LocalReferenceNumber, index: Index): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val sections = viewModelProvider(request.userAnswers, index).sections
      Ok(view(lrn, index, sections))
  }

  def onSubmit(lrn: LocalReferenceNumber, index: Index): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      Redirect(config.sessionExpiredUrl) //TODO change to redirect correctly
  }

}