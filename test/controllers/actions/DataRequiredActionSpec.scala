package controllers.actions

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.requests.{DataRequest, OptionalDataRequest}
import models.{LocalReferenceNumber, SubmissionState, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRequiredActionSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private class Harness(lrn: LocalReferenceNumber) extends DataRequiredAction(lrn, frontendAppConfig) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "Data Required Action" - {

    "when there are no UserAnswers" - {

      "must return Left and redirect to session expired" in {

        val harness = new Harness(lrn)

        val result = harness.callRefine(OptionalDataRequest(fakeRequest, eoriNumber, None)).map(_.left.value)

        status(result) mustBe 303
        redirectLocation(result).value mustBe frontendAppConfig.sessionExpiredUrl(lrn)
      }
    }

    "when there are UserAnswers" - {

      "and answers have previously been submitted" - {
        "must return Left and redirect to session expired" in {
          val userAnswers = UserAnswers(lrn, eoriNumber, status = SubmissionState.Submitted)

          val harness = new Harness(lrn)

          val result = harness.callRefine(OptionalDataRequest(fakeRequest, eoriNumber, Some(userAnswers))).map(_.left.value)

          status(result) mustBe 303
          redirectLocation(result).value mustBe frontendAppConfig.sessionExpiredUrl(lrn)
        }
      }

      "and answers have not previously been submitted" - {
        "must return Right with DataRequest" in {
          forAll(arbitrary[SubmissionState].retryUntil(_ != SubmissionState.Submitted)) {
            submissionStatus =>
              val userAnswers = UserAnswers(lrn = lrn, eoriNumber = eoriNumber, status = submissionStatus)

              val harness = new Harness(lrn)

              val result = harness.callRefine(OptionalDataRequest(fakeRequest, eoriNumber, Some(userAnswers)))

              whenReady[Either[Result, DataRequest[_]], Assertion](result) {
                result =>
                  result.value.userAnswers mustBe userAnswers
                  result.value.eoriNumber mustBe eoriNumber
              }
          }
        }
      }
    }
  }
}
