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

import controllers.actions._
import controllers.authorisationsAndLimit.authorisations.{routes => authRoutes}
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.YesNoFormProvider
import models.reference.authorisations.AuthorisationType
import models.requests.SpecificDataRequestProvider1
import models.{Index, LocalReferenceNumber, Mode}
import pages.authorisationsAndLimit.authorisations.index.{AuthorisationTypePage, InferredAuthorisationTypePage}
import pages.sections.authorisationsAndLimit.AuthorisationSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.authorisationsAndLimit.authorisations.index.RemoveAuthorisationYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveAuthorisationYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveAuthorisationYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private type Request = SpecificDataRequestProvider1[AuthorisationType]#SpecificDataRequest[?]

  private def authType(implicit request: Request): AuthorisationType = request.arg

  private def form(implicit request: Request): Form[Boolean] =
    formProvider("authorisations.index.removeAuthorisationYesNo", authType.forDisplay)

  private def addAnother(lrn: LocalReferenceNumber, mode: Mode): Call =
    authRoutes.AddAnotherAuthorisationController.onPageLoad(lrn, mode)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, authorisationIndex: Index): Action[AnyContent] = actions
    .requireIndex(lrn, AuthorisationSection(authorisationIndex), addAnother(lrn, mode))
    .andThen(getMandatoryPage.getFirst(AuthorisationTypePage(authorisationIndex), InferredAuthorisationTypePage(authorisationIndex))) {
      implicit request =>
        Ok(view(form, lrn, mode, authorisationIndex, authType.forDisplay))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, authorisationIndex: Index): Action[AnyContent] = actions
    .requireIndex(lrn, AuthorisationSection(authorisationIndex), addAnother(lrn, mode))
    .andThen(getMandatoryPage.getFirst(AuthorisationTypePage(authorisationIndex), InferredAuthorisationTypePage(authorisationIndex)))
    .async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, authorisationIndex, authType.forDisplay))),
            {
              case true =>
                AuthorisationSection(authorisationIndex)
                  .removeFromUserAnswers()
                  .updateTask()
                  .writeToSession(sessionRepository)
                  .navigateTo(addAnother(lrn, mode))
              case false =>
                Future.successful(Redirect(addAnother(lrn, mode)))
            }
          )
    }
}
