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
package net.katsstuff.teamnightclipse.danmakucore.impl.subentity

import net.katsstuff.teamnightclipse.danmakucore.danmaku.{DanmakuState, DanmakuUpdate, DanmakuUpdateSignal}
import net.katsstuff.teamnightclipse.danmakucore.danmaku.subentity.{SubEntity, SubEntityType}
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.fml.common.FMLCommonHandler

private[danmakucore] class SubEntityTypeTeleport(name: String) extends SubEntityType(name) {
  override def instantiate: SubEntity =
    new SubEntityTeleport
}

private[subentity] class SubEntityTeleport extends SubEntityDefault {
  override protected def impact(danmaku: DanmakuState, rayTrace: RayTraceResult): DanmakuUpdate = {
    super.impact(danmaku, rayTrace).addCallbackIf(danmaku.user.isDefined && !danmaku.world.isRemote) {
      val usr = danmaku.user.get
      usr.rotationYaw = danmaku.orientation.yaw.toFloat
      usr.rotationPitch = danmaku.orientation.pitch.toFloat
      usr.setPositionAndUpdate(danmaku.pos.x, danmaku.pos.y, danmaku.pos.z)
    }
  }
}