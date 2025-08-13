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

package base

import config.FrontendAppConfig
import controllers.actions.*
import models.{Index, LockCheck, Mode, UserAnswers}
import navigation.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneAppPerSuite}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.{bind, Injector}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.test.Helpers.fakeRequest
import repositories.SessionRepository
import services.{CountriesService, LockService}

import scala.concurrent.Future

trait AppWithDefaultMockFixtures extends BeforeAndAfterEach with GuiceOneAppPerSuite with GuiceFakeApplicationFactory with MockitoSugar {
  self: TestSuite & SpecBase =>

  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
    reset(mockDataRetrievalActionProvider)

    when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))
    when(mockLockActionProvider.apply()).thenReturn(new FakeLockAction())

  }

  final val mockSessionRepository: SessionRepository                             = mock[SessionRepository]
  final private val mockDataRetrievalActionProvider: DataRetrievalActionProvider = mock[DataRetrievalActionProvider]
  final private val mockLockActionProvider: LockActionProvider                   = mock[LockActionProvider]
  final val mockCountriesService: CountriesService                               = mock[CountriesService]

  def injector: Injector = app.injector

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  implicit def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  final override def fakeApplication(): Application =
    guiceApplicationBuilder()
      .build()

  protected def setExistingUserAnswers(userAnswers: UserAnswers): Unit = setUserAnswers(Some(userAnswers))

  protected def setNoExistingUserAnswers(): Unit = setUserAnswers(None)

  private def setUserAnswers(userAnswers: Option[UserAnswers]): Unit = {
    when(mockLockActionProvider.apply()).thenReturn(new FakeLockAction())
    when(mockDataRetrievalActionProvider.apply(any())).thenReturn(new FakeDataRetrievalAction(userAnswers))
  }

  protected val onwardRoute: Call = Call("GET", "/foo")

  protected val fakeNavigator: Navigator = new FakeNavigator(onwardRoute)

  protected val fakeTransportNavigatorProvider: TransportNavigatorProvider =
    (mode: Mode) => new FakeTransportNavigator(onwardRoute, mode)

  protected val fakeTransportMeansNavigatorProvider: TransportMeansNavigatorProvider =
    (mode: Mode) => new FakeTransportMeansNavigator(onwardRoute, mode)

  protected val fakeTransportMeansActiveNavigatorProvider: TransportMeansActiveNavigatorProvider =
    (mode: Mode, index: Index) => new FakeTransportMeansActiveNavigator(onwardRoute, mode, index)

  protected val fakeTransportMeansDepartureNavigatorProvider: TransportMeansDepartureNavigatorProvider =
    (mode: Mode, index: Index) => new FakeTransportMeansDepartureNavigator(onwardRoute, mode, index)

  protected val fakeTransportMeansDepartureListNavigatorProvider: TransportMeansDepartureListNavigatorProvider =
    (mode: Mode) => new FakeTransportMeansDepartureListNavigator(onwardRoute, mode)

  protected val fakeTransportMeansActiveListNavigatorProvider: TransportMeansActiveListNavigatorProvider =
    (mode: Mode) => new FakeTransportMeansActiveListNavigator(onwardRoute, mode)

  protected val fakeSupplyChainActorNavigatorProvider: SupplyChainActorNavigatorProvider =
    (mode: Mode, index: Index) => new FakeSupplyChainActorNavigator(onwardRoute, mode, index)

  protected val fakeAuthorisationNavigatorProvider: AuthorisationNavigatorProvider =
    (mode: Mode, index: Index) => new FakeAuthorisationNavigator(onwardRoute, mode, index)

  protected val fakeEquipmentsNavigatorProvider: EquipmentsNavigatorProvider =
    (mode: Mode) => new FakeEquipmentsNavigator(onwardRoute, mode)

  protected val fakeEquipmentNavigatorProvider: EquipmentNavigatorProvider =
    (mode: Mode, index: Index) => new FakeEquipmentNavigator(onwardRoute, index, mode)

  protected val fakeSealNavigatorProvider: SealNavigatorProvider =
    (mode: Mode, equipmentIndex: Index, sealIndex: Index) => new FakeSealNavigator(onwardRoute, equipmentIndex, sealIndex, mode)

  protected val fakeAdditionalReferenceNavigatorProvider: AdditionalReferenceNavigatorProvider =
    (mode: Mode, additionalReferenceIndex: Index) => new FakeAdditionalReferenceNavigator(onwardRoute, additionalReferenceIndex, mode)

  protected val fakeAdditionalInformationNavigatorProvider: AdditionalInformationNavigatorProvider =
    (mode: Mode, additionalInformationIndex: Index) => new FakeAdditionalInformationNavigator(onwardRoute, additionalInformationIndex, mode)

  private def defaultApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[LockActionProvider].toInstance(mockLockActionProvider),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[DataRetrievalActionProvider].toInstance(mockDataRetrievalActionProvider),
        bind[DependentTasksAction].to[FakeDependentTasksAction],
        bind[CountriesService].toInstance(mockCountriesService)
      )

  protected def guiceApplicationBuilder(): GuiceApplicationBuilder =
    defaultApplicationBuilder()

}
