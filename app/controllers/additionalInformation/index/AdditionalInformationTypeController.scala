/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.additionalInformation.index

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.SelectableFormProvider.AdditionalInformationTypeFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{AdditionalInformationNavigatorProvider, UserAnswersNavigator}
import pages.additionalInformation.index.AdditionalInformationTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AdditionalInformationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.additionalInformation.index.AdditionalInformationTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationTypeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: AdditionalInformationNavigatorProvider,
  actions: Actions,
  formProvider: AdditionalInformationTypeFormProvider,
  service: AdditionalInformationService,
  val controllerComponents: MessagesControllerComponents,
  view: AdditionalInformationTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(additionalInformationIndex: Index, lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getAdditionalInformationCodes(request.userAnswers).map {
        additionalInformationList =>
          val form = formProvider("additionalInformation.index.additionalInformationType", additionalInformationList)
          val preparedForm = request.userAnswers.get(AdditionalInformationTypePage(additionalInformationIndex)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, lrn, additionalInformationList.values, mode, additionalInformationIndex))
      }
  }

  def onSubmit(additionalInformationIndex: Index, lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getAdditionalInformationCodes(request.userAnswers).flatMap {
        additionalInformationList =>
          val form = formProvider("additionalInformation.index.additionalInformationType", additionalInformationList)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, additionalInformationList.values, mode, additionalInformationIndex))),
              value => {
                val navigator: UserAnswersNavigator = navigatorProvider(mode, additionalInformationIndex)
                AdditionalInformationTypePage(additionalInformationIndex)
                  .writeToUserAnswers(value)
                  .updateTask()
                  .writeToSession(sessionRepository)
                  .navigateWith(navigator)
              }
            )
      }
  }
}
