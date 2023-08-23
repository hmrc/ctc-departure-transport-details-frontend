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

package controllers.authorisationsAndLimit

import controllers.actions._
import models.{LocalReferenceNumber, Mode}
import navigation.TransportNavigatorProvider
import pages.authorisationsAndLimit.AuthorisationsInferredPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AuthorisationInferenceService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisationInferenceController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: TransportNavigatorProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  authorisationInferenceService: AuthorisationInferenceService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def infer(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      val userAnswers = authorisationInferenceService.inferAuthorisations(request.userAnswers)
      for {
        updatedUserAnswers <- Future.fromTry(userAnswers.set(AuthorisationsInferredPage, true))
        _                  <- sessionRepository.set(updatedUserAnswers)
      } yield {
        val navigator = navigatorProvider(mode)
        Redirect(navigator.nextPage(userAnswers))
      }
  }
}
