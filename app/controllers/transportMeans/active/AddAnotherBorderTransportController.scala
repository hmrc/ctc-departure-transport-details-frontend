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

import config.{FrontendAppConfig, PhaseConfig}
import controllers.actions.*
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.AddAnotherFormProvider
import models.{LocalReferenceNumber, Mode}
import pages.transportMeans.AddAnotherBorderTransportPage
import play.api.data.Form
import repositories.SessionRepository
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transportMeans.active.AddAnotherBorderTransportViewModel
import viewModels.transportMeans.active.AddAnotherBorderTransportViewModel.AddAnotherBorderTransportViewModelProvider
import views.html.transportMeans.active.AddAnotherBorderTransportView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAnotherBorderTransportController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  viewModelProvider: AddAnotherBorderTransportViewModelProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherBorderTransportView
)(implicit ec: ExecutionContext, config: FrontendAppConfig, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherBorderTransportViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, mode)
      viewModel.count match {
        case 0 => Redirect(controllers.transportMeans.routes.BorderModeOfTransportController.onPageLoad(lrn, mode))
        case _ =>
          val preparedForm = request.userAnswers.get(AddAnotherBorderTransportPage) match {
            case None        => form(viewModel)
            case Some(value) => form(viewModel).fill(value)
          }
          Ok(view(preparedForm, lrn, viewModel))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      lazy val viewModel = viewModelProvider(request.userAnswers, mode)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, viewModel))),
          value =>
            AddAnotherBorderTransportPage
              .writeToUserAnswers(value)
              .updateTask()
              .writeToSession(sessionRepository)
              .navigateTo {
                if value then routes.IdentificationController.onPageLoad(lrn, mode, viewModel.nextIndex)
                else controllers.transportMeans.routes.TransportMeansCheckYourAnswersController.onPageLoad(lrn, mode)
              }
        )
  }
}
