/*
 * This class was created by <Katrix>. It's distributed as
 * part of the DanmakuCore Mod. Get the Source Code in github:
 * https://github.com/Katrix-/DanmakuCore
 *
 * DanmakuCore is Open Source and distributed under the
 * the DanmakuCore license: https://github.com/Katrix-/DanmakuCore/blob/master/LICENSE.md
 */
package net.katsstuff.danmakucore.impl.form

import net.katsstuff.danmakucore.danmodel.{DanModel, FormDanModel}
import net.katsstuff.danmakucore.data.{ShotData, Vector3}
import net.katsstuff.danmakucore.scalastuff.DanmakuHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.SoundEvent
import net.minecraft.world.World

class FormNote(name: String, sound: SoundEvent, model: DanModel) extends FormDanModel(name, model) {

  override def playShotSound(user: EntityLivingBase, shotData: ShotData): Unit = {
    user.playSound(sound, shotData.damage / 3, 0.5F + (user.ticksExisted % 60) / 40F)
  }

  override def playShotSound(world: World, pos: Vector3, shotData: ShotData): Unit = {
    DanmakuHelper.playSoundAt(world, pos, sound, shotData.damage / 3, 0.5F + (world.getWorldTime % 60) / 40F)
  }
}