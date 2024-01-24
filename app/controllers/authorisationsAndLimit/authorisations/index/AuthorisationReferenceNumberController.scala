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

import config.{FrontendAppConfig, PhaseConfig}
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.AuthorisationReferenceNumberFormProvider
import models.reference.authorisations.AuthorisationType
import models.requests.SpecificDataRequestProvider1
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{AuthorisationNavigatorProvider, UserAnswersNavigator}
import pages.authorisationsAndLimit.authorisations.index.{AuthorisationReferenceNumberPage, AuthorisationTypePage, InferredAuthorisationTypePage}
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
  getMandatoryPage: SpecificDataRequiredActionProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AuthorisationReferenceNumberView,
  config: FrontendAppConfig
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private type Request = SpecificDataRequestProvider1[AuthorisationType]#SpecificDataRequest[_]

  private val prefix = "authorisations.authorisationReferenceNumber"

  private def approvedOperator(implicit request: Request): Option[Boolean] =
    ApprovedOperatorPage.inferredReader.apply(Nil).map(_.value).run(request.userAnswers).toOption

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, authorisationIndex: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(AuthorisationTypePage(authorisationIndex), InferredAuthorisationTypePage(authorisationIndex))) {
      implicit request =>
        approvedOperator match {
          case Some(approvedOperator) =>
            val form = formProvider(prefix, request.arg.forDisplay)

            val preparedForm = request.userAnswers.get(AuthorisationReferenceNumberPage(authorisationIndex)) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, lrn, request.arg.forDisplay, mode, authorisationIndex, approvedOperator))
          case _ => Redirect(config.sessionExpiredUrl)
        }
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, authorisationIndex: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(AuthorisationTypePage(authorisationIndex), InferredAuthorisationTypePage(authorisationIndex)))
    .async {
      implicit request =>
        approvedOperator match {
          case Some(approvedOperator) =>
            val form = formProvider(prefix, request.arg.forDisplay)
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, request.arg.forDisplay, mode, authorisationIndex, approvedOperator))),
                value => {
                  implicit val navigator: UserAnswersNavigator = navigatorProvider(mode, authorisationIndex)
                  AuthorisationReferenceNumberPage(authorisationIndex).writeToUserAnswers(value).updateTask().writeToSession().navigate()
                }
              )
          case _ => Future.successful(Redirect(config.sessionExpiredUrl))
        }
    }
}
