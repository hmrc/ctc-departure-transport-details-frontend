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
import forms.EnumerableFormProvider
import models.reference.supplyChainActors.SupplyChainActorType
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{SupplyChainActorNavigatorProvider, UserAnswersNavigator}
import pages.supplyChainActors.index.SupplyChainActorTypePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SupplyChainActorTypesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.supplyChainActors.index.SupplyChainActorTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SupplyChainActorTypeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: SupplyChainActorNavigatorProvider,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SupplyChainActorTypeView,
  service: SupplyChainActorTypesService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(supplyChainActorTypes: Seq[SupplyChainActorType]): Form[SupplyChainActorType] =
    formProvider[SupplyChainActorType]("supplyChainActors.index.supplyChainActorType", supplyChainActorTypes)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, actorIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getSupplyChainActorTypes().map {
        supplyChainActorTypes =>
          val preparedForm = request.userAnswers.get(SupplyChainActorTypePage(actorIndex)) match {
            case None        => form(supplyChainActorTypes)
            case Some(value) => form(supplyChainActorTypes).fill(value)
          }

          Ok(view(preparedForm, lrn, supplyChainActorTypes, mode, actorIndex))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, actorIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getSupplyChainActorTypes().flatMap {
        supplyChainActorTypes =>
          form(supplyChainActorTypes)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, supplyChainActorTypes, mode, actorIndex))),
              value => {
                val navigator: UserAnswersNavigator = navigatorProvider(mode, actorIndex)
                SupplyChainActorTypePage(actorIndex).writeToUserAnswers(value).updateTask().writeToSession(sessionRepository).navigateWith(navigator)
              }
            )
      }
  }
}
