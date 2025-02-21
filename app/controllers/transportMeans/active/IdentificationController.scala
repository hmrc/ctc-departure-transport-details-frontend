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
import models.reference.transportMeans.active.Identification
import models.requests.DataRequest
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{TransportMeansActiveNavigatorProvider, UserAnswersNavigator}
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.active.{BaseIdentificationPage, IdentificationPage, InferredIdentificationPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.MeansOfTransportIdentificationTypesActiveService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transportMeans.active.IdentificationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentificationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: TransportMeansActiveNavigatorProvider,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IdentificationView,
  service: MeansOfTransportIdentificationTypesActiveService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(identificationTypes: Seq[Identification]): Form[Identification] =
    formProvider[Identification]("transportMeans.active.identification", identificationTypes)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, activeIndex: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .async {
      implicit request =>
        service.getMeansOfTransportIdentificationTypesActive(activeIndex, request.userAnswers.get(BorderModeOfTransportPage)).flatMap {
          case identifier :: Nil =>
            redirect(mode, activeIndex, InferredIdentificationPage.apply, identifier)
          case identifiers =>
            val preparedForm = request.userAnswers.get(IdentificationPage(activeIndex)) match {
              case None        => form(identifiers)
              case Some(value) => form(identifiers).fill(value)
            }

            Future.successful(Ok(view(preparedForm, lrn, identifiers, mode, activeIndex)))
        }
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, activeIndex: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .async {
      implicit request =>
        service.getMeansOfTransportIdentificationTypesActive(activeIndex, request.userAnswers.get(BorderModeOfTransportPage)).flatMap {
          identificationTypeList =>
            form(identificationTypeList)
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(view(formWithErrors, lrn, identificationTypeList, mode, activeIndex))
                  ),
                value => redirect(mode, activeIndex, IdentificationPage.apply, value)
              )
        }
    }

  private def redirect(
    mode: Mode,
    index: Index,
    page: Index => BaseIdentificationPage,
    value: Identification
  )(implicit request: DataRequest[?]): Future[Result] = {
    val navigator: UserAnswersNavigator = navigatorProvider(mode, index)
    page(index).writeToUserAnswers(value).updateTask().writeToSession(sessionRepository).navigateWith(navigator)
  }
}
