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

package controllers.transportMeans.departure

import controllers.actions._
import config.FrontendAppConfig
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.MeansIdentificationNumberFormProvider
import models.requests.SpecificDataRequestProvider1
import models.transportMeans.departure.{Identification, InlandMode}
import models.{LocalReferenceNumber, Mode}
import navigation.UserAnswersNavigator
import navigation.TransportMeansNavigatorProvider
import pages.transportMeans.departure.{IdentificationPage, InlandModePage, MeansIdentificationNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transportMeans.departure.MeansIdentificationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MeansIdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: TransportMeansNavigatorProvider,
  formProvider: MeansIdentificationNumberFormProvider,
  actions: Actions,
  config: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  view: MeansIdentificationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private type Request = SpecificDataRequestProvider1[InlandMode]#SpecificDataRequest[_]

  private def identificationType(implicit request: Request): Option[Identification] = request.arg match {
    case InlandMode.Unknown => Some(Identification.Unknown)
    case _                  => request.userAnswers.get(IdentificationPage)
  }

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(InlandModePage)) {
      implicit request =>
        identificationType match {
          case Some(value) =>
            val form = formProvider("transportMeans.departure.meansIdentificationNumber", value.arg)
            val preparedForm = request.userAnswers.get(MeansIdentificationNumberPage) match {
              case None        => form
              case Some(value) => form.fill(value)
            }
            Ok(view(preparedForm, lrn, mode, value))
          case _ => Redirect(config.sessionExpiredUrl)
        }
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(InlandModePage))
    .async {
      implicit request =>
        identificationType match {
          case Some(value) =>
            val form = formProvider("transportMeans.departure.meansIdentificationNumber", value.arg)
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, value))),
                value => {
                  implicit val navigator: UserAnswersNavigator = navigatorProvider(mode)
                  MeansIdentificationNumberPage.writeToUserAnswers(value).updateTask().writeToSession().navigate()
                }
              )
          case _ => Future.successful(Redirect(config.sessionExpiredUrl))
        }
    }
}
