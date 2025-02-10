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

package controllers.preRequisites

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.SelectableFormProvider
import models.{LocalReferenceNumber, Mode}
import navigation.{TransportNavigatorProvider, UserAnswersNavigator}
import pages.preRequisites.{ItemsDestinationCountryInCL009Page, ItemsDestinationCountryPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.CountriesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.preRequisites.ItemsDestinationCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ItemsDestinationCountryController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: TransportNavigatorProvider,
  actions: Actions,
  formProvider: SelectableFormProvider,
  service: CountriesService,
  val controllerComponents: MessagesControllerComponents,
  view: ItemsDestinationCountryView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getCountries().map {
        countryList =>
          val form = formProvider("preRequisites.itemsDestinationCountry", countryList)
          val preparedForm = request.userAnswers.get(ItemsDestinationCountryPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, lrn, countryList.values, mode))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getCountries().flatMap {
        countryList =>
          val form = formProvider("preRequisites.itemsDestinationCountry", countryList)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, countryList.values, mode))),
              value => {
                val navigator: UserAnswersNavigator = navigatorProvider(mode)
                for {
                  isInCL009 <- service.isInCL009(value)
                  result <- ItemsDestinationCountryPage
                    .writeToUserAnswers(value)
                    .appendValue(ItemsDestinationCountryInCL009Page, isInCL009)
                    .updateTask()
                    .writeToSession(sessionRepository)
                    .navigateWith(navigator)
                } yield result
              }
            )
      }
  }
}
