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
package net.katsstuff.teamnightclipse.danmakucore.data

import scala.beans.BeanProperty

import net.katsstuff.teamnightclipse.danmakucore.helper.NBTHelper
import net.katsstuff.teamnightclipse.mirror.data.Vector3
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants

/**
	* Defines how a [[net.katsstuff.teamnightclipse.danmakucore.danmaku.DanmakuState]] will move.
	*/
sealed abstract class AbstractMovementData {

  /**
		* The speed that the [[net.katsstuff.teamnightclipse.danmakucore.danmaku.DanmakuState]] starts with.
		*/
  def speedOriginal: Double

  /**
		* The lower limit of the speed of the [[net.katsstuff.teamnightclipse.danmakucore.danmaku.DanmakuState]].
		*/
  def lowerSpeedLimit: Double

  /**
		* The upper limit of the speed of the [[net.katsstuff.teamnightclipse.danmakucore.danmaku.DanmakuState]].
		*/
  def upperSpeedLimit: Double

  /**
		* The change in speed each tick.
		*/
  def speedAcceleration: Double

  /**
		* The gravity that is applied each tick to the entity's speed.
		* Think of this as an additional, directional [[speedAcceleration]].
		*/
  def gravity: Vector3

  def serializeNBT: NBTTagCompound = {
    val tag = new NBTTagCompound
    tag.setDouble(MovementData.NbtOriginal, speedOriginal)
    tag.setDouble(MovementData.NbtLowerLimit, lowerSpeedLimit)
    tag.setDouble(MovementData.NbtUpperLimit, upperSpeedLimit)
    tag.setDouble(MovementData.NbtAcceleration, speedAcceleration)

    NBTHelper.setVector(tag, MovementData.NbtGravity, gravity)
    tag
  }
}

final case class MutableMovementData(
    @BeanProperty var speedOriginal: Double,
    @BeanProperty var lowerSpeedLimit: Double,
    @BeanProperty var upperSpeedLimit: Double,
    @BeanProperty var speedAcceleration: Double,
    @BeanProperty var gravity: Vector3
) extends AbstractMovementData {

  def copyObj: MutableMovementData = copy()
}

final case class MovementData(
    @BeanProperty speedOriginal: Double,
    @BeanProperty lowerSpeedLimit: Double,
    @BeanProperty upperSpeedLimit: Double,
    @BeanProperty speedAcceleration: Double,
    @BeanProperty gravity: Vector3
) extends AbstractMovementData {

  def setSpeedOriginal(speedOriginal: Double): MovementData         = copy(speedOriginal = speedOriginal)
  def setSpeedLimit(speedLimit: Double): MovementData               = copy(upperSpeedLimit = speedLimit)
  def setSpeedAcceleration(speedAcceleration: Double): MovementData = copy(speedAcceleration = speedAcceleration)
  def setGravity(gravity: Vector3): MovementData                    = copy(gravity = gravity)

  def setConstant(speed: Double): MovementData =
    copy(speedOriginal = speed, upperSpeedLimit = speed, speedAcceleration = 0D)
}

object MovementData {

  final val NbtOriginal     = "original"
  final val OldNbtLimit     = "limit"
  final val NbtLowerLimit   = "lowerLimit"
  final val NbtUpperLimit   = "upperLimit"
  final val NbtAcceleration = "acceleration"
  final val NbtGravity      = "gravity"

  /**
		* Creates a [[MovementData]] with constant speed and no gravity.
		*/
  def constant(speed: Double): MovementData = MovementData(speed, speed, speed, 0D, Vector3.Zero)

  /**
		* Creates a [[MovementData]] with no gravity.
		*/
  def noGravity(start: Double, lowerLimit: Double, upperLimit: Double, acceleration: Double): MovementData =
    MovementData(start, lowerLimit, upperLimit, acceleration, Vector3.Zero)

  def fromNBT(tag: NBTTagCompound): MovementData = {
    val speedOriginal     = tag.getDouble(NbtOriginal)
    val speedAcceleration = tag.getDouble(NbtAcceleration)

    val gravity = NBTHelper.getVector(tag, MovementData.NbtGravity)
    if (tag.hasKey(OldNbtLimit, Constants.NBT.TAG_DOUBLE)) {
      val limit = tag.getDouble(OldNbtLimit)
      if (speedAcceleration < 0D && limit < speedOriginal) {
        MovementData(
          speedOriginal = speedOriginal,
          lowerSpeedLimit = limit,
          upperSpeedLimit = speedOriginal,
          speedAcceleration = speedAcceleration,
          gravity = gravity
        )
      } else {
        MovementData(
          speedOriginal = speedOriginal,
          lowerSpeedLimit = 0D,
          upperSpeedLimit = limit,
          speedAcceleration = speedAcceleration,
          gravity = gravity
        )
      }
    } else {
      val lowerSpeedLimit = tag.getDouble(NbtLowerLimit)
      val upperSpeedLimit = tag.getDouble(NbtUpperLimit)
      MovementData(
        speedOriginal = speedOriginal,
        lowerSpeedLimit = upperSpeedLimit,
        upperSpeedLimit = lowerSpeedLimit,
        speedAcceleration = speedAcceleration,
        gravity = gravity
      )
    }
  }
}
