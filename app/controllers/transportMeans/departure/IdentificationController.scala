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

import config.PhaseConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.EnumerableFormProvider
import models.reference.transportMeans.departure.Identification
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{TransportMeansNavigatorProvider, UserAnswersNavigator}
import pages.transportMeans.InlandModePage
import pages.transportMeans.departure.IdentificationPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.MeansOfTransportIdentificationTypesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transportMeans.departure.IdentificationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentificationController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: TransportMeansNavigatorProvider,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IdentificationView,
  service: MeansOfTransportIdentificationTypesService
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(identificationTypes: Seq[Identification]): Form[Identification] =
    formProvider[Identification]("transportMeans.departure.identification", identificationTypes)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, departureIndex: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .async {
      implicit request =>
        service.getMeansOfTransportIdentificationTypes(request.userAnswers.get(InlandModePage)).map {
          identificationTypes =>
            val preparedForm = request.userAnswers.get(IdentificationPage(departureIndex)) match {
              case None        => form(identificationTypes)
              case Some(value) => form(identificationTypes).fill(value)
            }

            Ok(view(preparedForm, lrn, identificationTypes, mode, departureIndex))
        }
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, departureIndex: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .async {
      implicit request =>
        service.getMeansOfTransportIdentificationTypes(request.userAnswers.get(InlandModePage)).flatMap {
          identificationTypes =>
            form(identificationTypes)
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, identificationTypes, mode, departureIndex))),
                value => {
                  implicit val navigator: UserAnswersNavigator = navigatorProvider(mode)
                  IdentificationPage(departureIndex).writeToUserAnswers(value).updateTask().writeToSession().navigate()
                }
              )
        }
    }
}
