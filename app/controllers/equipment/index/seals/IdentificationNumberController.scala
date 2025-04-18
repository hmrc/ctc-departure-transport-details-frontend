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

package controllers.equipment.index.seals

import config.FrontendAppConfig
import controllers.actions.*
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner, UpdateOps}
import forms.SealIdentificationNumberFormProvider
import models.requests.DataRequest
import models.{Index, LocalReferenceNumber, Mode, RichOptionalJsArray}
import navigation.{SealNavigatorProvider, UserAnswersNavigator}
import pages.equipment.index.seals.IdentificationNumberPage
import pages.sections.equipment.SealsSection
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.equipment.index.seals.IdentificationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: SealNavigatorProvider,
  formProvider: SealIdentificationNumberFormProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: IdentificationNumberView
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def otherIdentificationNumbers(equipmentIndex: Index, sealIndex: Index)(implicit request: DataRequest[?]): Seq[String] = {
    val numberOfSeals = request.userAnswers.get(SealsSection(equipmentIndex)).length
    (0 until numberOfSeals)
      .map(Index(_))
      .filterNot(_ == sealIndex)
      .map(IdentificationNumberPage(equipmentIndex, _))
      .flatMap(request.userAnswers.get(_))
  }

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, equipmentIndex: Index, sealIndex: Index): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val form = formProvider("equipment.index.seals.identificationNumber", otherIdentificationNumbers(equipmentIndex, sealIndex))
      val preparedForm = request.userAnswers.get(IdentificationNumberPage(equipmentIndex: Index, sealIndex: Index)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, lrn, mode, equipmentIndex, sealIndex))
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, equipmentIndex: Index, sealIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      val form = formProvider("equipment.index.seals.identificationNumber", otherIdentificationNumbers(equipmentIndex, sealIndex))
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, equipmentIndex, sealIndex))),
          value => {
            val navigator: UserAnswersNavigator = navigatorProvider(mode, equipmentIndex, sealIndex)
            IdentificationNumberPage(equipmentIndex: Index, sealIndex: Index)
              .writeToUserAnswers(value)
              .appendTransportEquipmentUuidIfNotPresent(equipmentIndex)
              .updateTask()
              .writeToSession(sessionRepository)
              .getNextPage(navigator)
              .updateItems(lrn)
              .navigate()
          }
        )
  }
}
