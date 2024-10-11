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

package controllers.preRequisites

import config.PhaseConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.OptionalYesNoFormProvider
import models.{LocalReferenceNumber, Mode}
import navigation.{TransportNavigatorProvider, UserAnswersNavigator}
import pages.preRequisites.AddCountryOfDestinationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.preRequisites.AddCountryOfDestinationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddCountryOfDestinationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: TransportNavigatorProvider,
  actions: Actions,
  formProvider: OptionalYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddCountryOfDestinationView
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("preRequisites.addCountryOfDestination")

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn) {
      implicit request =>
        val preparedForm = request.userAnswers.get(AddCountryOfDestinationPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, lrn, mode))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode))),
            value => {
              val navigator: UserAnswersNavigator = navigatorProvider(mode)
              AddCountryOfDestinationPage.writeToUserAnswers(value).updateTask().writeToSession(sessionRepository).navigateWith(navigator)
            }
          )
    }
}
