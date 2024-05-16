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

package controllers.additionalInformation

import controllers.actions._
import forms.YesNoFormProvider
import models.{Index, LocalReferenceNumber, Mode, UserAnswers}
import navigation.TransportNavigatorProvider
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
  navigatorProvider: TransportNavigatorProvider,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveAdditionalInformationYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(additionalInformationIndex: Index): Form[Boolean] =
    formProvider("additionalInformation.removeAdditionalInformationYesNo", additionalInformationIndex.display)

  private def addAnother(lrn: LocalReferenceNumber, additionalInformationIndex: Index, mode: Mode): Call =
    controllers.additionalInformation.routes.AddAdditionalInformationYesNoController
      .onPageLoad(lrn, mode) //todo this will be add another page once implemented

  def additionalInformation(userAnswers: UserAnswers, additionalInformationIndex: Index): String =
    "Additional Information" //todo replace with answer to additional information once implemented

  def onPageLoad(lrn: LocalReferenceNumber, additionalInformationIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(lrn) {
      implicit request =>
        Ok(
          view(form(additionalInformationIndex), lrn, additionalInformationIndex, additionalInformation(request.userAnswers, additionalInformationIndex), mode)
        )
    }

  def onSubmit(lrn: LocalReferenceNumber, additionalInformationIndex: Index, mode: Mode): Action[AnyContent] = actions
    .requireIndex(lrn, AdditionalInformationSection(additionalInformationIndex), addAnother(lrn, additionalInformationIndex, mode))
    .async {
      implicit request =>
        form(additionalInformationIndex)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, lrn, additionalInformationIndex, additionalInformation(request.userAnswers, additionalInformationIndex), mode))
              ),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.remove(AdditionalInformationSection(additionalInformationIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(request.userAnswers.lrn, additionalInformationIndex, mode))
          )

    }
}
