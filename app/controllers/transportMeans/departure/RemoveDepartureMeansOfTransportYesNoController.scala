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
import controllers.equipment.routes
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.YesNoFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{TransportMeansNavigatorProvider, UserAnswersNavigator}
import pages.sections.transportMeans.TransportMeansSection
import pages.transportMeans.departure.AddVehicleCountryYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transportMeans.departure.{AddVehicleCountryYesNoView, RemoveDepartureMeansOfTransportYesNoView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveDepartureMeansOfTransportYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: TransportMeansNavigatorProvider,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveDepartureMeansOfTransportYesNoView
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(transportMeansIndex: Index) = formProvider("transportMeans.departure.removeTransportMeansOfDepartureYesNo", transportMeansIndex.display)

  private def addAnother(lrn: LocalReferenceNumber, mode: Mode): Call =
    routes.AddAnotherEquipmentController.onPageLoad(lrn, mode) //todo change to AddAnotherDepartureMeansOfTransportController once built

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, transportMeansIndex: Index): Action[AnyContent] = actions
    .requireIndex(lrn, TransportMeansSection, addAnother(lrn, mode)) {
      implicit request =>
        val insetText = request.userAnswers.
        Ok(view(form(transportMeansIndex), lrn, mode, transportMeansIndex))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, transportMeansIndex: Index): Action[AnyContent] = actions
    .requireIndex(lrn, TransportMeansSection, addAnother(lrn, mode))
    .async {
      implicit request =>
        form(transportMeansIndex)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, transportMeansIndex))),
            {
              case true =>
                // TODO - update items task status
                TransportMeansSection
                  .removeFromUserAnswers()
                  .updateTask()
                  .writeToSession()
                  .navigateTo(addAnother(lrn, mode))
              case false =>
                Future.successful(Redirect(addAnother(lrn, mode)))
            }
          )
    }
}
