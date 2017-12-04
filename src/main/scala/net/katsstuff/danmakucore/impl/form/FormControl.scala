/*
 * This class was created by <Katrix>. It's distributed as
 * part of the DanmakuCore Mod. Get the Source Code in github:
 * https://github.com/Katrix-/DanmakuCore
 *
 * DanmakuCore is Open Source and distributed under the
 * the DanmakuCore license: https://github.com/Katrix-/DanmakuCore/blob/master/LICENSE.md
 */
package net.katsstuff.danmakucore.impl.form

import net.katsstuff.danmakucore.data.ShotData
import net.katsstuff.danmakucore.entity.danmaku.EntityDanmaku
import net.katsstuff.danmakucore.entity.danmaku.form.IRenderForm
import net.katsstuff.danmakucore.lib.LibFormName
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

private[danmakucore] class FormControl extends FormGeneric(LibFormName.CONTROL) {

  @SideOnly(Side.CLIENT)
  override protected def createRenderer: IRenderForm =
    (_: EntityDanmaku, _: Double, _: Double, _: Double, _: Float, _: Float, _: RenderManager) => {} //NO-OP

  override def onShotDataChange(oldShot: ShotData, newShot: ShotData): ShotData =
    newShot.setSize(oldShot.sizeX, oldShot.sizeY, oldShot.sizeZ)
}
