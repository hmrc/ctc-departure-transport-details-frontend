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

import config.PhaseConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.IdentificationNumberFormProvider
import models.requests.DataRequest
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{TransportMeansActiveNavigatorProvider, UserAnswersNavigator}
import pages.transportMeans.active.{IdentificationNumberPage, IdentificationPage, InferredIdentificationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transportMeans.active.IdentificationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: TransportMeansActiveNavigatorProvider,
  formProvider: IdentificationNumberFormProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: IdentificationNumberView
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private case class DynamicHeading(prefix: String, args: String*)

  private def dynamicHeading(activeIndex: Index)(implicit request: DataRequest[_]): DynamicHeading = {
    val prefix = "transportMeans.active.identificationNumber"

    val identificationType = request.userAnswers
      .get(IdentificationPage(activeIndex))
      .orElse(request.userAnswers.get(InferredIdentificationPage(activeIndex)))

    identificationType match {
      case Some(identificationType) => DynamicHeading(s"$prefix.withIDType", identificationType.forDisplay)
      case None                     => DynamicHeading(s"$prefix.withNoIDType")
    }
  }

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, activeIndex: Index): Action[AnyContent] = actions
    .requireData(lrn) {
      implicit request =>
        val dynamicHeadingValue = dynamicHeading(activeIndex)(request)
        val form                = formProvider(dynamicHeadingValue.prefix, dynamicHeadingValue.args: _*)
        val preparedForm = request.userAnswers.get(IdentificationNumberPage(activeIndex)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(view(preparedForm, lrn, mode, activeIndex, dynamicHeadingValue.prefix, dynamicHeadingValue.args: _*))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, activeIndex: Index): Action[AnyContent] =
    actions
      .requireData(lrn)
      .async {
        implicit request =>
          val dynamicHeadingValue = dynamicHeading(activeIndex)(request)
          val form                = formProvider(dynamicHeadingValue.prefix, dynamicHeadingValue.args: _*)
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, lrn, mode, activeIndex, dynamicHeadingValue.prefix, dynamicHeadingValue.args: _*))),
              value => {
                implicit val navigator: UserAnswersNavigator = navigatorProvider(mode, activeIndex)
                IdentificationNumberPage(activeIndex).writeToUserAnswers(value).updateTask().writeToSession().navigate()
              }
            )
      }
}
