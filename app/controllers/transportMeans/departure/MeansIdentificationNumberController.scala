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
import forms.IdentificationNumberFormProvider
import models.{LocalReferenceNumber, Mode}
import navigation.{TransportMeansNavigatorProvider, UserAnswersNavigator}
import pages.transportMeans.departure.{IdentificationPage, MeansIdentificationNumberPage}
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
  formProvider: IdentificationNumberFormProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  view: MeansIdentificationNumberView
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix = "transportMeans.departure.meansIdentificationNumber"

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(IdentificationPage)) {
      implicit request =>
        val identificationType = request.arg
        val form               = formProvider(prefix)
        val preparedForm = request.userAnswers.get(MeansIdentificationNumberPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(view(preparedForm, lrn, mode, identificationType.arg))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] =
    actions
      .requireData(lrn)
      .andThen(getMandatoryPage(IdentificationPage))
      .async {
        implicit request =>
          val identificationType = request.arg
          val form               = formProvider(prefix)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, identificationType.arg))),
              value => {
                implicit val navigator: UserAnswersNavigator = navigatorProvider(mode)
                MeansIdentificationNumberPage.writeToUserAnswers(value).updateTask().writeToSession().navigate()
              }
            )

      }

}
