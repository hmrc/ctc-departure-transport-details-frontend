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

import controllers.actions._
import controllers.equipment.routes
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.YesNoFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import pages.sections.equipment.EquipmentSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.equipment.index.RemoveTransportEquipmentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveTransportEquipmentController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveTransportEquipmentView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(equipmentIndex: Index): Form[Boolean] = formProvider("equipment.index.removeTransportEquipment", equipmentIndex.display)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      Ok(view(form(equipmentIndex), lrn, mode, equipmentIndex))
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      lazy val redirect = routes.AddAnotherEquipmentController.onPageLoad(lrn, mode)
      form(equipmentIndex)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, equipmentIndex))),
          {
            case true =>
              EquipmentSection(equipmentIndex)
                .removeFromUserAnswers()
                .updateTask()
                .writeToSession()
                .navigateTo(redirect)
            case false =>
              Future.successful(Redirect(redirect))
          }
        )
  }
}
