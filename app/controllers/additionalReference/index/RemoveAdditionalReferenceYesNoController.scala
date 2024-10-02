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
import forms.YesNoFormProvider
import models.removable.AdditionalReference
import models.{Index, LocalReferenceNumber, Mode, UserAnswers}
import pages.additionalReference.index.AdditionalReferenceTypePage
import pages.sections.additionalReference.AdditionalReferenceSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.additionalReference.index.RemoveAdditionalReferenceYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveAdditionalReferenceYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveAdditionalReferenceYesNoView
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  def insetText(userAnswers: UserAnswers, additionalReferenceIndex: Index): Option[String] =
    AdditionalReference(userAnswers, additionalReferenceIndex).map(_.forRemoveDisplay)

  private def form: Form[Boolean] =
    formProvider("additionalReference.index.removeAdditionalReference")

  private def addAnother(lrn: LocalReferenceNumber, mode: Mode): Call =
    controllers.additionalReference.routes.AddAnotherAdditionalReferenceController.onPageLoad(lrn, mode)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, additionalReferenceIndex: Index): Action[AnyContent] = actions
    .requireIndex(lrn, AdditionalReferenceSection(additionalReferenceIndex), addAnother(lrn, mode))
    .andThen(getMandatoryPage.getFirst(AdditionalReferenceTypePage(additionalReferenceIndex))) {
      implicit request =>
        Ok(view(form, lrn, mode, additionalReferenceIndex, insetText(request.userAnswers, additionalReferenceIndex)))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, additionalReferenceIndex: Index): Action[AnyContent] = actions
    .requireIndex(lrn, AdditionalReferenceSection(additionalReferenceIndex), addAnother(lrn, mode))
    .andThen(getMandatoryPage.getFirst(AdditionalReferenceTypePage(additionalReferenceIndex)))
    .async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future
                .successful(BadRequest(view(formWithErrors, lrn, mode, additionalReferenceIndex, insetText(request.userAnswers, additionalReferenceIndex)))),
            {
              case true =>
                AdditionalReferenceSection(additionalReferenceIndex)
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
