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

import config.FrontendAppConfig
import controllers.actions.*
import forms.AddAnotherFormProvider
import models.{LocalReferenceNumber, Mode}
import navigation.TransportNavigatorProvider
import pages.sections.additionalReference.AdditionalReferencesSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.additionalReference.AddAnotherAdditionalReferenceViewModel
import viewModels.additionalReference.AddAnotherAdditionalReferenceViewModel.AddAnotherAdditionalReferenceViewModelProvider
import views.html.additionalReference.AddAnotherAdditionalReferenceView

import javax.inject.Inject

class AddAnotherAdditionalReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  navigatorProvider: TransportNavigatorProvider,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherAdditionalReferenceView,
  viewModelProvider: AddAnotherAdditionalReferenceViewModelProvider
)(implicit config: FrontendAppConfig)
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
          Ok(view(form(viewModel), lrn, viewModel))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, mode)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(view(formWithErrors, lrn, viewModel)),
          {
            case true =>
              Redirect(controllers.additionalReference.index.routes.AdditionalReferenceTypeController.onPageLoad(lrn, mode, viewModel.nextIndex))
            case false =>
              Redirect(navigatorProvider(mode).nextPage(request.userAnswers, Some(AdditionalReferencesSection)))
          }
        )
  }
}
