/*
 * Copyright 2024 HM Revenue & Customs
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

package views.templates

import base.SpecBase
import config.RenderConfig
import generators.Generators
import models.LocalReferenceNumber
import org.jsoup.Jsoup
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.base.ViewSpecAssertions
import views.html.templates.MainTemplate

class MainTemplateSpec extends SpecBase with ViewSpecAssertions with ScalaCheckPropertyChecks with Generators {

  private val path                                           = "foo"
  implicit private lazy val request: FakeRequest[AnyContent] = FakeRequest("GET", path)

  "when not in trader test" - {
    val app = new GuiceApplicationBuilder()
      .configure("trader-test.enabled" -> false)
      .build()

    "must point feedback at feedback form" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr, arbitrary[LocalReferenceNumber]) {
        (content, title, lrn) =>
          val view = app.injector
            .instanceOf[MainTemplate]
            .apply(title, timeoutEnabled = true, showBackLink = true, lrn) {
              Html.apply(content)
            }

          val doc = Jsoup.parse(view.toString())

          val link = getElementBySelector(doc, ".govuk-phase-banner__text > .govuk-link")
          getElementHref(link) mustEqual s"http://localhost:9250/contact/beta-feedback?service=CTCTraders&referrerUrl=$path"
      }
    }

    "must use HMRC 'report technical issue' helper" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr, arbitrary[LocalReferenceNumber]) {
        (content, title, lrn) =>
          val view = app.injector
            .instanceOf[MainTemplate]
            .apply(title, timeoutEnabled = true, showBackLink = true, lrn) {
              Html.apply(content)
            }

          val doc = Jsoup.parse(view.toString())

          val link = getElementBySelector(doc, ".hmrc-report-technical-issue")
          getElementHref(link) mustEqual s"http://localhost:9250/contact/report-technical-problem?service=CTCTraders&referrerUrl=$path"
          link.text() mustEqual "Is this page not working properly? (opens in new tab)"
      }
    }

    "when session has an auth token" - {
      "must render timeout dialog and sign out link" in {
        implicit lazy val request: FakeRequest[AnyContent] = FakeRequest("GET", path).withSession("authToken" -> "auth123")

        forAll(Gen.alphaNumStr, Gen.alphaNumStr, arbitrary[LocalReferenceNumber]) {
          (content, title, lrn) =>
            val view = app.injector
              .instanceOf[MainTemplate]
              .apply(title, timeoutEnabled = true, showBackLink = true, lrn) {
                Html.apply(content)
              }

            val doc = Jsoup.parse(view.toString())

            val metas = getElementsByTag(doc, "meta")
            assertElementExists(metas, _.attr("name") == "hmrc-timeout-dialog")
            assertElementExists(metas, _.attr("data-keep-alive-url").endsWith(s"/manage-transit-movements/departures/$lrn/keep-alive"))

            val link = getElementByClass(doc, "hmrc-sign-out-nav__link")
            assertElementContainsText(link, "Sign out")
            assertElementContainsHref(link, frontendAppConfig.signOutUrl(lrn))
        }
      }
    }

    "when session does not have an auth token" - {
      "must not render timeout dialog or sign out link" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr, arbitrary[LocalReferenceNumber]) {
          (content, title, lrn) =>
            val view = app.injector
              .instanceOf[MainTemplate]
              .apply(title, timeoutEnabled = true, showBackLink = true, lrn) {
                Html.apply(content)
              }

            val doc = Jsoup.parse(view.toString())

            val metas = getElementsByTag(doc, "meta")
            assertElementDoesNotExist(metas, _.attr("name") == "hmrc-timeout-dialog")
            assertElementDoesNotExist(doc, "hmrc-sign-out-nav__link")
        }
      }
    }
  }

  "when in trader test" - {
    val app = new GuiceApplicationBuilder()
      .configure("trader-test.enabled" -> true)
      .build()

    val config = app.injector.instanceOf[RenderConfig]

    "must point feedback at google form" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr, arbitrary[LocalReferenceNumber]) {
        (content, title, lrn) =>
          val view = app.injector
            .instanceOf[MainTemplate]
            .apply(title, timeoutEnabled = true, showBackLink = true, lrn) {
              Html.apply(content)
            }

          val doc = Jsoup.parse(view.toString())

          val link = getElementBySelector(doc, ".govuk-phase-banner__text > .govuk-link")
          getElementHref(link) mustEqual config.feedbackForm
      }
    }

    "must use custom link for reporting issues" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr, arbitrary[LocalReferenceNumber]) {
        (content, title, lrn) =>
          val view = app.injector
            .instanceOf[MainTemplate]
            .apply(title, timeoutEnabled = true, showBackLink = true, lrn) {
              Html.apply(content)
            }

          val doc = Jsoup.parse(view.toString())

          val link = getElementBySelector(doc, ".hmrc-report-technical-issue")
          getElementHref(link) must startWith(s"mailto:${config.feedbackEmail}")
          link.text() mustEqual s"If you have any questions or issues, email us at ${config.feedbackEmail}"
      }
    }

    "when session has an auth token" - {
      "must render timeout dialog and sign out link" in {
        implicit lazy val request: FakeRequest[AnyContent] = FakeRequest("GET", path).withSession("authToken" -> "auth123")

        forAll(Gen.alphaNumStr, Gen.alphaNumStr, arbitrary[LocalReferenceNumber]) {
          (content, title, lrn) =>
            val view = app.injector
              .instanceOf[MainTemplate]
              .apply(title, timeoutEnabled = true, showBackLink = true, lrn) {
                Html.apply(content)
              }

            val doc = Jsoup.parse(view.toString())

            val metas = getElementsByTag(doc, "meta")
            assertElementExists(metas, _.attr("name") == "hmrc-timeout-dialog")
            assertElementExists(metas, _.attr("data-keep-alive-url").endsWith(s"/manage-transit-movements/departures/$lrn/keep-alive"))

            val link = getElementByClass(doc, "hmrc-sign-out-nav__link")
            assertElementContainsText(link, "Sign out")
            assertElementContainsHref(link, frontendAppConfig.signOutUrl(lrn))
        }
      }
    }

    "when session does not have an auth token" - {
      "must not render timeout dialog or sign out link" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr, arbitrary[LocalReferenceNumber]) {
          (content, title, lrn) =>
            val view = app.injector
              .instanceOf[MainTemplate]
              .apply(title, timeoutEnabled = true, showBackLink = true, lrn) {
                Html.apply(content)
              }

            val doc = Jsoup.parse(view.toString())

            val metas = getElementsByTag(doc, "meta")
            assertElementDoesNotExist(metas, _.attr("name") == "hmrc-timeout-dialog")
            assertElementDoesNotExist(doc, "hmrc-sign-out-nav__link")
        }
      }
    }
  }
}
