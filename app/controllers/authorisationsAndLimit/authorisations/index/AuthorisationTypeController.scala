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

package controllers.authorisationsAndLimit.authorisations.index

import config.PhaseConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.EnumerableFormProvider
import models.reference.authorisations.AuthorisationType
import models.requests.DataRequest
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{AuthorisationNavigatorProvider, UserAnswersNavigator}
import pages.QuestionPage
import pages.authorisationsAndLimit.authorisations.index.{AuthorisationTypePage, InferredAuthorisationTypePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.AuthorisationTypesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.authorisationsAndLimit.authorisations.index.AuthorisationTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisationTypeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: AuthorisationNavigatorProvider,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AuthorisationTypeView,
  service: AuthorisationTypesService
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(authorisationTypes: Seq[AuthorisationType]): Form[AuthorisationType] =
    formProvider[AuthorisationType]("authorisations.authorisationType", authorisationTypes)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, authorisationIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getAuthorisationTypes(request.userAnswers, Some(authorisationIndex)).flatMap {
        case authorisationType :: Nil =>
          redirect(mode, authorisationIndex, InferredAuthorisationTypePage.apply, authorisationType)
        case authorisationTypes =>
          val preparedForm = request.userAnswers.get(AuthorisationTypePage(authorisationIndex)) match {
            case None        => form(authorisationTypes)
            case Some(value) => form(authorisationTypes).fill(value)
          }

          Future.successful(Ok(view(preparedForm, lrn, authorisationTypes, mode, authorisationIndex)))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, authorisationIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getAuthorisationTypes(request.userAnswers, Some(authorisationIndex)).flatMap {
        authorisationTypesList =>
          form(authorisationTypesList)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, authorisationTypesList, mode, authorisationIndex))),
              value => redirect(mode, authorisationIndex, AuthorisationTypePage.apply, value)
            )
      }
  }

  private def redirect(
    mode: Mode,
    index: Index,
    page: Index => QuestionPage[AuthorisationType],
    value: AuthorisationType
  )(implicit request: DataRequest[?]): Future[Result] = {
    val navigator: UserAnswersNavigator = navigatorProvider(mode, index)
    page(index).writeToUserAnswers(value).updateTask().writeToSession(sessionRepository).navigateWith(navigator)
  }
}
