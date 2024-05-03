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

package controllers.additionalReference.index

import config.PhaseConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.AdditionalReferenceNumberFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{TransportNavigatorProvider, UserAnswersNavigator}
import pages.additionalReference.index.AdditionalReferenceNumberPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.additionalReference.index.AdditionalReferenceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalReferenceNumberController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: TransportNavigatorProvider,
  formProvider: AdditionalReferenceNumberFormProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: AdditionalReferenceNumberView
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private val form: Form[String] =
    formProvider("additionalReference.index.additionalReferenceNumber")

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, additionalReferenceIndex: Index): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val preparedForm = request.userAnswers.get(AdditionalReferenceNumberPage(additionalReferenceIndex)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, lrn, mode, additionalReferenceIndex))
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, additionalReferenceIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, additionalReferenceIndex))),
          value => {
            implicit val navigator: UserAnswersNavigator = navigatorProvider(mode)
            AdditionalReferenceNumberPage(additionalReferenceIndex)
              .writeToUserAnswers(value)
              .updateTask()
              .writeToSession()
              .navigate()
          }
        )
  }
}
