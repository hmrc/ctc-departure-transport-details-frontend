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

package controllers.additionalReference

import config.{FrontendAppConfig, PhaseConfig}
import controllers.actions.*
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.AddAnotherFormProvider
import models.{LocalReferenceNumber, Mode}
import navigation.{TransportNavigatorProvider, UserAnswersNavigator}
import pages.additionalReference.AddAnotherAdditionalReferencePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.additionalReference.AddAnotherAdditionalReferenceViewModel
import viewModels.additionalReference.AddAnotherAdditionalReferenceViewModel.AddAnotherAdditionalReferenceViewModelProvider
import views.html.additionalReference.AddAnotherAdditionalReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAnotherAdditionalReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  navigatorProvider: TransportNavigatorProvider,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherAdditionalReferenceView,
  viewModelProvider: AddAnotherAdditionalReferenceViewModelProvider
)(implicit ec: ExecutionContext, config: FrontendAppConfig, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherAdditionalReferenceViewModel): Form[Boolean] = formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, mode)
      viewModel.count match {
        case 0 =>
          Redirect(controllers.additionalReference.routes.AddAdditionalReferenceYesNoController.onPageLoad(lrn, mode))
        case _ =>
          val preparedForm = request.userAnswers.get(AddAnotherAdditionalReferencePage) match {
            case None        => form(viewModel)
            case Some(value) => form(viewModel).fill(value)
          }
          Ok(view(preparedForm, lrn, viewModel))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, mode)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, viewModel))),
          value =>
            AddAnotherAdditionalReferencePage
              .writeToUserAnswers(value)
              .updateTask()
              .writeToSession(sessionRepository)
              .and {
                if (value) {
                  _.navigateTo(controllers.additionalReference.index.routes.AdditionalReferenceTypeController.onPageLoad(lrn, mode, viewModel.nextIndex))
                } else {
                  val navigator: UserAnswersNavigator = navigatorProvider(mode)
                  _.navigateWith(navigator)
                }
              }
        )
  }
}
