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

package controllers.supplyChainActors.index

import controllers.actions._
import controllers.supplyChainActors.routes
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.YesNoFormProvider
import models.{Index, LocalReferenceNumber, Mode, UserAnswers}
import pages.sections.supplyChainActors.SupplyChainActorSection
import pages.supplyChainActors.index.SupplyChainActorTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.supplyChainActors.index.RemoveSupplyChainActorView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveSupplyChainActorController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveSupplyChainActorView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("supplyChainActors.index.removeSupplyChainActor")

  def insetText(userAnswers: UserAnswers, actorIndex: Index): Option[String] =
    userAnswers.get(SupplyChainActorTypePage(actorIndex)).map(_.toString)

  private def addAnother(lrn: LocalReferenceNumber, mode: Mode): Call =
    routes.AddAnotherSupplyChainActorController.onPageLoad(lrn, mode)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions
    .requireIndex(lrn, SupplyChainActorSection(index), addAnother(lrn, mode)) {
      implicit request =>
        Ok(view(form, lrn, mode, index, insetText(request.userAnswers, index)))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions
    .requireIndex(lrn, SupplyChainActorSection(index), addAnother(lrn, mode))
    .async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, index, insetText(request.userAnswers, index)))),
            {
              case true =>
                SupplyChainActorSection(index)
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
