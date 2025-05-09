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

import controllers.actions.Actions
import models.{Index, LocalReferenceNumber, Mode}
import navigation.TransportMeansDepartureListNavigatorProvider
import pages.sections.transportMeans.DepartureSection
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
  navigatorProvider: TransportMeansDepartureListNavigatorProvider,
  viewModelProvider: DepartureTransportAnswersViewModelProvider
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val sections = viewModelProvider(request.userAnswers, index).sections
      Ok(view(lrn, mode, index, sections))
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      Redirect(navigatorProvider(mode).nextPage(request.userAnswers, Some(DepartureSection(index))))
  }

}
