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

package controllers.additionalReference.index

import config.PhaseConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.SelectableFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{AdditionalReferenceNavigatorProvider, UserAnswersNavigator}
import pages.additionalReference.index.AdditionalReferenceTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AdditionalReferencesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.additionalReference.index.AdditionalReferenceTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalReferenceTypeController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: AdditionalReferenceNavigatorProvider,
  actions: Actions,
  formProvider: SelectableFormProvider,
  service: AdditionalReferencesService,
  val controllerComponents: MessagesControllerComponents,
  view: AdditionalReferenceTypeView
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, additionalReferenceIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getAdditionalReferences().map {
        additionalReferenceList =>
          val form = formProvider("additionalReference.index.additionalReferenceType", additionalReferenceList)
          val preparedForm = request.userAnswers.get(AdditionalReferenceTypePage(additionalReferenceIndex)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, lrn, additionalReferenceList.values, mode, additionalReferenceIndex))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, additionalReferenceIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getAdditionalReferences().flatMap {
        additionalReferenceList =>
          val form = formProvider("additionalReference.index.additionalReferenceType", additionalReferenceList)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, additionalReferenceList.values, mode, additionalReferenceIndex))),
              value => {
                implicit val navigator: UserAnswersNavigator = navigatorProvider(mode, additionalReferenceIndex)
                AdditionalReferenceTypePage(additionalReferenceIndex).writeToUserAnswers(value).updateTask().writeToSession().navigate()
              }
            )
      }
  }
}
