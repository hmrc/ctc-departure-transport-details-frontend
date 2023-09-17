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

package forms.mappings

import models.reference.CountryCode
import models.{Enumerable, OptionalBoolean, Radioable, RichString, Selectable, SelectableList}
import play.api.data.FormError
import play.api.data.format.Formatter

import scala.util.control.Exception.nonFatalCatch

trait Formatters {

  private[mappings] def stringFormatter(errorKey: String, args: Seq[Any] = Seq.empty): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None                      => Left(Seq(FormError(key, errorKey, args)))
        case Some(s) if s.trim.isEmpty => Left(Seq(FormError(key, errorKey, args)))
        case Some(s)                   => Right(s)
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def eoriFormatter(errorKey: String): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      val n = CountryCode.Constants.countryCodeLength
      spacelessStringFormatter(errorKey)
        .bind(key, data)
        .flatMap {
          eori =>
            Right(eori.take(n).toUpperCase + eori.drop(n))
        }
    }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def spacelessStringFormatter(errorKey: String): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      lazy val error = Left(Seq(FormError(key, errorKey)))
      data.get(key) match {
        case None                                => error
        case Some(s) if s.removeSpaces().isEmpty => error
        case Some(s)                             => Right(s.removeSpaces())
      }
    }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def booleanFormatter(requiredKey: String, invalidKey: String, args: Seq[Any] = Seq.empty): Formatter[Boolean] =
    new Formatter[Boolean] {

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Boolean] =
        baseFormatter
          .bind(key, data)
          .flatMap {
            case "true"  => Right(true)
            case "false" => Right(false)
            case _       => Left(Seq(FormError(key, invalidKey, args)))
          }

      def unbind(key: String, value: Boolean): Map[String, String] = Map(key -> value.toString)
    }

  private[mappings] def optionalBooleanFormatter(requiredKey: String, invalidKey: String, args: Seq[Any] = Seq.empty): Formatter[OptionalBoolean] =
    new Formatter[OptionalBoolean] {

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], OptionalBoolean] =
        baseFormatter
          .bind(key, data)
          .flatMap {
            case "true"  => Right(OptionalBoolean.yes)
            case "false" => Right(OptionalBoolean.no)
            case "maybe" => Right(OptionalBoolean.maybe)
            case _       => Left(Seq(FormError(key, invalidKey, args)))
          }

      def unbind(key: String, value: OptionalBoolean): Map[String, String] = {
        val str = value.value match {
          case Some(bool) => bool.toString
          case None       => "maybe"
        }
        Map(key -> str)
      }
    }

  private[mappings] def intFormatter(requiredKey: String, wholeNumberKey: String, nonNumericKey: String, args: Seq[String] = Seq.empty): Formatter[Int] =
    new Formatter[Int] {

      val decimalRegexp = """^-?(\d*\.\d*)$"""

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Int] =
        baseFormatter
          .bind(key, data)
          .map(_.replace(",", ""))
          .flatMap {
            case s if s.matches(decimalRegexp) =>
              Left(Seq(FormError(key, wholeNumberKey, args)))
            case s =>
              nonFatalCatch
                .either(s.toInt)
                .left
                .map(
                  _ => Seq(FormError(key, nonNumericKey, args))
                )
          }

      override def unbind(key: String, value: Int): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] def enumerableFormatter[A <: Radioable[A]](requiredKey: String, invalidKey: String)(implicit ev: Enumerable[A]): Formatter[A] =
    new Formatter[A] {

      private val baseFormatter = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], A] =
        baseFormatter.bind(key, data).flatMap {
          str =>
            ev.withName(str).map(Right.apply).getOrElse(Left(Seq(FormError(key, invalidKey))))
        }

      override def unbind(key: String, value: A): Map[String, String] =
        baseFormatter.unbind(key, value.code)
    }

  private[mappings] def currencyFormatter(
    requiredKey: String = "error.required",
    invalidCharactersKey: String = "error.invalidCharacters",
    invalidFormatKey: String = "error.invalidFormat",
    invalidValueKey: String = "error.invalidValue"
  ): Formatter[BigDecimal] =
    new Formatter[BigDecimal] {

      private val invalidCharactersRegex = """^[0-9.]*$"""
      private val invalidFormatRegex     = """^[0-9]*(\.[0-9]{1,2})?$"""
      private val invalidValueRegex      = """^[0-9]{0,16}(\.[0-9]{1,2})?$"""

      private val baseFormatter = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] =
        baseFormatter
          .bind(key, data)
          .map(_.replace(",", ""))
          .map(_.replace(" ", ""))
          .flatMap {
            case s if !s.matches(invalidCharactersRegex) => Left(Seq(FormError(key, invalidCharactersKey)))
            case s if !s.matches(invalidFormatRegex)     => Left(Seq(FormError(key, invalidFormatKey)))
            case s if !s.matches(invalidValueRegex)      => Left(Seq(FormError(key, invalidValueKey)))
            case s                                       => Right(BigDecimal(s))
          }

      override def unbind(key: String, value: BigDecimal): Map[String, String] =
        baseFormatter.unbind(key, value.toString())
    }

  private[mappings] def selectableFormatter[T <: Selectable](
    selectableList: SelectableList[T],
    errorKey: String,
    args: Seq[Any] = Seq.empty
  ): Formatter[T] = new Formatter[T] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], T] = {
      lazy val error = Left(Seq(FormError(key, errorKey, args)))
      data.get(key) match {
        case None =>
          error
        case Some(value) =>
          selectableList.values.find(_.value == value) match {
            case Some(selectable) => Right(selectable)
            case None             => error
          }
      }
    }

    override def unbind(key: String, selectable: T): Map[String, String] =
      Map(key -> selectable.value)
  }
}
