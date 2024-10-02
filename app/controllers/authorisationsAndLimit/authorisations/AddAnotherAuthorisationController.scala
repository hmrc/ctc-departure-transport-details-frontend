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

package controllers.authorisationsAndLimit.authorisations

import config.FrontendAppConfig
import controllers.actions._
import controllers.authorisationsAndLimit.authorisations.index.{routes => authorisationRoutes}
import controllers.authorisationsAndLimit.{routes => authorisationsRoutes}
import forms.AddAnotherFormProvider
import models.{LocalReferenceNumber, Mode}
import navigation.TransportNavigatorProvider
import pages.sections.authorisationsAndLimit.AuthorisationsSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AuthorisationTypesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.authorisations.AddAnotherAuthorisationViewModel
import viewModels.authorisations.AddAnotherAuthorisationViewModel.AddAnotherAuthorisationViewModelProvider
import views.html.authorisationsAndLimit.authorisations.AddAnotherAuthorisationView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AddAnotherAuthorisationController @Inject() (
  override val messagesApi: MessagesApi,
  navigatorProvider: TransportNavigatorProvider,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherAuthorisationView,
  viewModelProvider: AddAnotherAuthorisationViewModelProvider,
  authorisationTypesService: AuthorisationTypesService
)(implicit config: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherAuthorisationViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      authorisationTypesService.getAuthorisationTypes(request.userAnswers, None).map {
        availableAuthorisationsYetToAdd =>
          val viewModel = viewModelProvider(request.userAnswers, mode, availableAuthorisationsYetToAdd)
          viewModel.count match {
            case 0 => Redirect(authorisationsRoutes.AddAuthorisationsYesNoController.onPageLoad(lrn, mode))
            case _ => Ok(view(form(viewModel), lrn, viewModel))
          }
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      authorisationTypesService.getAuthorisationTypes(request.userAnswers, None).map {
        availableAuthorisationsYetToAdd =>
          val viewModel = viewModelProvider(request.userAnswers, mode, availableAuthorisationsYetToAdd)
          form(viewModel)
            .bindFromRequest()
            .fold(
              formWithErrors => BadRequest(view(formWithErrors, lrn, viewModel)),
              {
                case true =>
                  Redirect(authorisationRoutes.AuthorisationTypeController.onPageLoad(lrn, mode, viewModel.nextIndex))
                case false =>
                  Redirect(navigatorProvider(mode).nextPage(request.userAnswers, Some(AuthorisationsSection)))
              }
            )
      }
  }
}
