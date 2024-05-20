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

import config.PhaseConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.YesNoFormProvider
import models.{Index, LocalReferenceNumber, Mode, UserAnswers}
import pages.additionalInformation.index.AdditionalInformationTypePage
import pages.sections.additionalInformation.AdditionalInformationSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.additionalInformation.RemoveAdditionalInformationYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveAdditionalInformationYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveAdditionalInformationYesNoView
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(): Form[Boolean] =
    formProvider("additionalInformation.index.removeAdditionalInformationYesNo")

  private def addAnother(lrn: LocalReferenceNumber, additionalInformationIndex: Index, mode: Mode): Call =
    controllers.additionalInformation.routes.AddAdditionalInformationYesNoController
      .onPageLoad(lrn, mode) //todo this will be add another page once implemented

  def additionalInformation(userAnswers: UserAnswers, additionalInformationIndex: Index): Option[String] =
    userAnswers.get(AdditionalInformationTypePage(additionalInformationIndex)).map(_.code)

  def onPageLoad(lrn: LocalReferenceNumber, additionalInformationIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(lrn) {
      implicit request =>
        Ok(
          view(form(), lrn, additionalInformationIndex, additionalInformation(request.userAnswers, additionalInformationIndex), mode)
        )
    }

  def onSubmit(lrn: LocalReferenceNumber, additionalInformationIndex: Index, mode: Mode): Action[AnyContent] = actions
    .requireIndex(lrn, AdditionalInformationSection(additionalInformationIndex), addAnother(lrn, additionalInformationIndex, mode))
    .async {
      implicit request =>
        form()
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future
                .successful(
                  BadRequest(
                    view(formWithErrors, lrn, additionalInformationIndex, additionalInformation(request.userAnswers, additionalInformationIndex), mode)
                  )
                ),
            {
              case true =>
                AdditionalInformationSection(additionalInformationIndex)
                  .removeFromUserAnswers()
                  .updateTask()
                  .writeToSession()
                  .navigateTo(addAnother(lrn, additionalInformationIndex, mode))
              case false =>
                Future.successful(Redirect(addAnother(lrn, additionalInformationIndex, mode)))
            }
          )

    }
}
