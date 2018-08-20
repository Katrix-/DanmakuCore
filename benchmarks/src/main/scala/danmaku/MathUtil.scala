/*
 * Copyright (C) 2018  Katrix
 * This file is part of DanmakuCore.
 *
 * DanmakuCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DanmakuCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with DanmakuCore.  If not, see <https://www.gnu.org/licenses/>.
 */
package danmaku

import java.lang.{Double => JDouble, Float => JFloat}

//We use Util instead of helper to avoid collision with minecraft's MathHelper
object MathUtil {
  final val Epsilon = 1E-5

  final def fuzzyEqual(a: Float, b: Float): Boolean = Math.abs(a - b) <= Epsilon

  final def fuzzyEqual(a: Double, b: Double): Boolean = Math.abs(a - b) <= Epsilon

  final def fuzzyCompare(a: Float, b: Float): Int   = if (Math.abs(a - b) <= Epsilon) 0 else JFloat.compare(a, b)
  final def fuzzyCompare(a: Double, b: Double): Int = if (Math.abs(a - b) <= Epsilon) 0 else JDouble.compare(a, b)

  implicit class RichFloat(val self: Float) extends AnyVal {
    @inline def ==~(that: Float): Boolean = fuzzyEqual(self, that)
    @inline def !=~(that: Float): Boolean = !fuzzyEqual(self, that)
    @inline def >~(that: Float): Boolean  = fuzzyCompare(self, that) > 0
    @inline def <~(that: Float): Boolean  = fuzzyCompare(self, that) < 0
    @inline def >=~(that: Float): Boolean = fuzzyCompare(self, that) >= 0
    @inline def <=~(that: Float): Boolean = fuzzyCompare(self, that) <= 0
  }

  implicit class RichDouble(val self: Double) extends AnyVal {
    @inline def =~(that: Double): Boolean  = fuzzyEqual(self, that)
    @inline def !=~(that: Double): Boolean = !fuzzyEqual(self, that)
    @inline def >~(that: Double): Boolean  = fuzzyCompare(self, that) > 0
    @inline def <~(that: Double): Boolean  = fuzzyCompare(self, that) < 0
    @inline def >=~(that: Double): Boolean = fuzzyCompare(self, that) >= 0
    @inline def <=~(that: Double): Boolean = fuzzyCompare(self, that) <= 0
  }
}