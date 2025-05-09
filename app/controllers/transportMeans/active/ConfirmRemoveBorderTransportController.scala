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

package controllers.transportMeans.active

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.YesNoFormProvider
import models.removable.ActiveBorderTransport
import models.{Index, LocalReferenceNumber, Mode, UserAnswers}
import pages.sections.transportMeans.ActiveSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transportMeans.active.ConfirmRemoveBorderTransportView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmRemoveBorderTransportController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ConfirmRemoveBorderTransportView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def addAnother(lrn: LocalReferenceNumber, mode: Mode): Call =
    controllers.transportMeans.active.routes.AddAnotherBorderTransportController.onPageLoad(lrn, mode)

  def insetText(userAnswers: UserAnswers, activeIndex: Index): Option[String] =
    ActiveBorderTransport(userAnswers, activeIndex).map(_.forRemoveDisplay)

  private def form(activeIndex: Index): Form[Boolean] =
    formProvider("transportMeans.active.confirmRemoveBorderTransport", activeIndex.display)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, activeIndex: Index): Action[AnyContent] = actions
    .requireIndex(lrn, ActiveSection(activeIndex), addAnother(lrn, mode)) {
      implicit request =>
        Ok(view(form(activeIndex), lrn, mode, activeIndex, insetText(request.userAnswers, activeIndex)))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, activeIndex: Index): Action[AnyContent] = actions
    .requireIndex(lrn, ActiveSection(activeIndex), addAnother(lrn, mode))
    .async {
      implicit request =>
        form(activeIndex)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, activeIndex, insetText(request.userAnswers, activeIndex)))),
            {
              case true =>
                ActiveSection(activeIndex)
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
