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

package controllers.supplyChainActors.index

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.EoriTcuinFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{SupplyChainActorNavigatorProvider, UserAnswersNavigator}
import pages.supplyChainActors.index.{IdentificationNumberPage, SupplyChainActorTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.supplyChainActors.index.IdentificationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: SupplyChainActorNavigatorProvider,
  formProvider: EoriTcuinFormProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  view: IdentificationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("supplyChainActors.index.identificationNumber")

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, actorIndex: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(SupplyChainActorTypePage(actorIndex))) {
      implicit request =>
        val supplyChainActor = request.arg.asString.toLowerCase

        val preparedForm = request.userAnswers.get(IdentificationNumberPage(actorIndex)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(view(preparedForm, lrn, mode, actorIndex, supplyChainActor))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, actorIndex: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(SupplyChainActorTypePage(actorIndex)))
    .async {
      implicit request =>
        val supplyChainActor = request.arg.asString.toLowerCase
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, actorIndex, supplyChainActor))),
            value => {
              val navigator: UserAnswersNavigator = navigatorProvider(mode, actorIndex)
              IdentificationNumberPage(actorIndex).writeToUserAnswers(value).updateTask().writeToSession(sessionRepository).navigateWith(navigator)
            }
          )
    }
}
