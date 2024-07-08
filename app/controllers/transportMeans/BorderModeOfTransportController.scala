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

package controllers.transportMeans

import config.PhaseConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.EnumerableFormProvider
import models.reference.BorderMode
import models.{LocalReferenceNumber, Mode}
import navigation.{TransportMeansNavigatorProvider, UserAnswersNavigator}
import pages.transportMeans.BorderModeOfTransportPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TransportModeCodesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transportMeans.BorderModeOfTransportView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BorderModeOfTransportController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: TransportMeansNavigatorProvider,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: BorderModeOfTransportView,
  service: TransportModeCodesService
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(borderModeCodes: Seq[BorderMode]): Form[BorderMode] = formProvider[BorderMode]("transportMeans.borderModeOfTransport", borderModeCodes)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getBorderModes().map {
        borderModeCodes =>
          val preparedForm = request.userAnswers.get(BorderModeOfTransportPage) match {
            case None        => form(borderModeCodes)
            case Some(value) => form(borderModeCodes).fill(value)
          }

          Ok(view(preparedForm, lrn, borderModeCodes, mode))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getBorderModes().flatMap {
        borderModeCodes =>
          form(borderModeCodes)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, borderModeCodes, mode))),
              value => {
                val navigator: UserAnswersNavigator = navigatorProvider(mode)
                BorderModeOfTransportPage.writeToUserAnswers(value).updateTask().writeToSession(sessionRepository).navigateWith(navigator)
              }
            )
      }
  }
}
