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

package controllers.equipment.index

import config.{FrontendAppConfig, PhaseConfig}
import controllers.actions._
import controllers.equipment.routes
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner, UpdateOps}
import forms.YesNoFormProvider
import models.removable.TransportEquipment
import models.{Index, LocalReferenceNumber, Mode, UserAnswers}
import pages.equipment.index.UuidPage
import pages.sections.equipment.EquipmentSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.equipment.index.RemoveTransportEquipmentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveTransportEquipmentController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveTransportEquipmentView
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def addAnother(lrn: LocalReferenceNumber, mode: Mode): Call =
    routes.AddAnotherEquipmentController.onPageLoad(lrn, mode)

  private def form(equipmentIndex: Index): Form[Boolean] =
    formProvider("equipment.index.removeTransportEquipment", equipmentIndex.display)

  private def formatInsetText(userAnswers: UserAnswers, transportEquipmentIndex: Index)(implicit messages: Messages): Option[String] =
    TransportEquipment(userAnswers, transportEquipmentIndex).flatMap(_.forRemoveDisplay)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions
    .requireIndex(lrn, EquipmentSection(equipmentIndex), addAnother(lrn, mode)) {
      implicit request =>
        Ok(view(form(equipmentIndex), lrn, mode, equipmentIndex, formatInsetText(request.userAnswers, equipmentIndex)))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions
    .requireIndex(lrn, EquipmentSection(equipmentIndex), addAnother(lrn, mode))
    .async {
      implicit request =>
        form(equipmentIndex)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, lrn, mode, equipmentIndex, formatInsetText(request.userAnswers, equipmentIndex)))),
            {
              case true =>
                EquipmentSection(equipmentIndex)
                  .removeFromUserAnswers()
                  .removeTransportEquipmentFromItems(request.userAnswers.get(UuidPage(equipmentIndex)))
                  .updateTask()
                  .writeToSession(sessionRepository)
                  .getNextPage(addAnother(lrn, mode))
                  .updateItems(lrn)
                  .navigate()
              case false =>
                Future.successful(Redirect(addAnother(lrn, mode)))
            }
          )
    }
}
