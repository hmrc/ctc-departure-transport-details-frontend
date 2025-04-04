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

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.YesNoFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{TransportMeansNavigatorProvider, UserAnswersNavigator}
import pages.transportMeans.departure.AddIdentificationTypeYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transportMeans.departure.AddIdentificationTypeViewModel.AddIdentificationTypeViewModelProvider
import views.html.transportMeans.departure.AddIdentificationTypeYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddIdentificationTypeYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: TransportMeansNavigatorProvider,
  actions: Actions,
  viewModel: AddIdentificationTypeViewModelProvider,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddIdentificationTypeYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form: Form[Boolean] = formProvider("transportMeans.departure.addIdentificationTypeYesNo")

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, departureIndex: Index): Action[AnyContent] = actions
    .requireData(lrn) {
      implicit request =>
        val preparedForm = request.userAnswers.get(AddIdentificationTypeYesNoPage(departureIndex)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, lrn, mode, departureIndex, viewModel(request.userAnswers)))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, departureIndex: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, departureIndex, viewModel(request.userAnswers)))),
            value => {
              val navigator: UserAnswersNavigator = navigatorProvider(mode)
              AddIdentificationTypeYesNoPage(departureIndex).writeToUserAnswers(value).updateTask().writeToSession(sessionRepository).navigateWith(navigator)
            }
          )
    }
}
