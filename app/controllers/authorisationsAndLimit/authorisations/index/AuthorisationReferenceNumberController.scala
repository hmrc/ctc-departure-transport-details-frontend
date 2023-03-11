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

import config.FrontendAppConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.AuthorisationReferenceNumberFormProvider
import models.requests.DataRequest
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{AuthorisationNavigatorProvider, UserAnswersNavigator}
import pages.authorisationsAndLimit.authorisations.index.{AuthorisationReferenceNumberPage, AuthorisationTypePage}
import pages.external.ApprovedOperatorPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.authorisationsAndLimit.authorisations.index.AuthorisationReferenceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisationReferenceNumberController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: AuthorisationNavigatorProvider,
  formProvider: AuthorisationReferenceNumberFormProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: AuthorisationReferenceNumberView,
  config: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix = "authorisations.authorisationReferenceNumber"

  private def authorisationType(authorisationIndex: Index)(implicit request: DataRequest[_]): Option[String] =
    AuthorisationTypePage(authorisationIndex).inferredReader.run(request.userAnswers).toOption.map(_.forDisplay)

  private def approvedOperator(implicit request: DataRequest[_]): Option[Boolean] =
    ApprovedOperatorPage.inferredReader.run(request.userAnswers).toOption

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, authorisationIndex: Index): Action[AnyContent] = actions
    .requireData(lrn) {
      implicit request =>
        (approvedOperator, authorisationType(authorisationIndex)) match {
          case (Some(approvedOperator), Some(authorisationType)) =>
            val form = formProvider(prefix, authorisationType)

            val preparedForm = request.userAnswers.get(AuthorisationReferenceNumberPage(authorisationIndex)) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, lrn, authorisationType, mode, authorisationIndex, approvedOperator))
          case _ => Redirect(config.sessionExpiredUrl)
        }
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, authorisationIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      (approvedOperator, authorisationType(authorisationIndex)) match {
        case (Some(approvedOperator), Some(authorisationType)) =>
          val form = formProvider(prefix, authorisationType)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, authorisationType, mode, authorisationIndex, approvedOperator))),
              value => {
                implicit val navigator: UserAnswersNavigator = navigatorProvider(mode, authorisationIndex)
                AuthorisationReferenceNumberPage(authorisationIndex).writeToUserAnswers(value).updateTask().writeToSession().navigate()
              }
            )
        case _ => Future.successful(Redirect(config.sessionExpiredUrl))
      }
  }
}
