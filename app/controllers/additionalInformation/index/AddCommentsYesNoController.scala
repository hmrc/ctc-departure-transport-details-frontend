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

package controllers.additionalInformation.index

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.YesNoFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{AdditionalInformationNavigatorProvider, UserAnswersNavigator}
import pages.additionalInformation.index.AddCommentsYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.additionalInformation.index.AddCommentsYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddCommentsYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: AdditionalInformationNavigatorProvider,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddCommentsYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("additionalInformation.addCommentsYesNo")

  def onPageLoad(lrn: LocalReferenceNumber, additionalInformationIndex: Index, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val preparedForm = request.userAnswers.get(AddCommentsYesNoPage(additionalInformationIndex)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, lrn, additionalInformationIndex, mode))
  }

  def onSubmit(lrn: LocalReferenceNumber, additionalInformationIndex: Index, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, additionalInformationIndex, mode))),
          value => {
            val navigator: UserAnswersNavigator = navigatorProvider(mode, additionalInformationIndex)
            AddCommentsYesNoPage(additionalInformationIndex).writeToUserAnswers(value).updateTask().writeToSession(sessionRepository).navigateWith(navigator)
          }
        )
  }
}
