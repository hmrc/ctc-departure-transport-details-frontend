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

package services

import base.SpecBase
import connectors.ReferenceDataConnector
import models.reference.equipment.PaymentMethod
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PaymentMethodsServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new PaymentMethodsService(mockRefDataConnector)

  private val paymentMethod1 = PaymentMethod("A", "Cash")
  private val paymentMethod2 = PaymentMethod("B", "Credit card")
  private val paymentMethod3 = PaymentMethod("C", "Cheque")
  private val paymentMethod4 = PaymentMethod("H", "Electronic credit transfer")
  private val paymentMethod5 = PaymentMethod("Y", "Account holder with carrier")
  private val paymentMethod6 = PaymentMethod("Z", "Not pre-paid")
  private val paymentMethod7 = PaymentMethod("D", "Other")

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "PaymentMethodsService" - {

    "getPaymentMethods" - {
      "must return a list of payment methods" in {
        when(mockRefDataConnector.getPaymentMethods()(any(), any()))
          .thenReturn(Future.successful(Seq(paymentMethod1, paymentMethod2, paymentMethod3, paymentMethod4, paymentMethod5, paymentMethod6, paymentMethod7)))

        service.getPaymentMethods().futureValue mustBe
          Seq(paymentMethod1, paymentMethod2, paymentMethod3, paymentMethod4, paymentMethod5, paymentMethod6, paymentMethod7)

        verify(mockRefDataConnector).getPaymentMethods()(any(), any())
      }
    }
  }
}
