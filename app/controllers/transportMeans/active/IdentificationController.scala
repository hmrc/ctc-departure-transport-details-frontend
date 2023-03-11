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

package controllers.transportMeans.active

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.EnumerableFormProvider
import models.requests.DataRequest
import models.transportMeans.active.Identification
import models.{Index, LocalReferenceNumber, Mode, UserAnswers}
import navigation.{TransportMeansActiveNavigatorProvider, UserAnswersNavigator}
import pages.transportMeans.active.{BaseIdentificationPage, IdentificationPage, InferredIdentificationPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.InferenceService
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transportMeans.active.IdentificationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentificationController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: TransportMeansActiveNavigatorProvider,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IdentificationView,
  inferenceService: InferenceService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider[Identification]("transportMeans.active.identification")

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, activeIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      inferenceService.inferActiveIdentifier(request.userAnswers, activeIndex) match {
        case Some(value) =>
          redirect(mode, activeIndex, InferredIdentificationPage, value)
        case None =>
          val preparedForm = request.userAnswers.get(IdentificationPage(activeIndex)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Future.successful(Ok(view(preparedForm, lrn, radioOptions(request.userAnswers, activeIndex), mode, activeIndex)))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, activeIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, radioOptions(request.userAnswers, activeIndex), mode, activeIndex))),
          value => redirect(mode, activeIndex, IdentificationPage, value)
        )
  }

  private def radioOptions(
    userAnswers: UserAnswers,
    index: Index
  )(implicit messages: Messages): (String, Option[Identification]) => Seq[RadioItem] =
    if (index.isFirst) Identification.radioItemsU(userAnswers) else Identification.radioItems

  private def redirect(
    mode: Mode,
    index: Index,
    page: Index => BaseIdentificationPage,
    value: Identification
  )(implicit request: DataRequest[_]): Future[Result] = {
    implicit val navigator: UserAnswersNavigator = navigatorProvider(mode, index)
    page(index).writeToUserAnswers(value).updateTask().writeToSession().navigate()
  }
}
